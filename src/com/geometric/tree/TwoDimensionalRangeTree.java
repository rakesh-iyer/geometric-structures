package com.geometric.tree;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import com.geometric.util.Geometric.*;
import com.geometric.util.Utils;

public class TwoDimensionalRangeTree {
    // The canonical set is actually a single dimensional range tree.
    // We use a singleton of the class for invocation of the relevant methods.
    static SingleDimensionalRangeTree singleDimensionalRangeTree =
            new SingleDimensionalRangeTree();
    static class RangeNode {
        Point point;
        SingleDimensionalRangeTree.RangeNode canonicalSet;
        RangeNode left;
        RangeNode right;
        RangeNode(Point point) {
            this.point = point;
        }

        void buildCanonicalSet(List<Point> points) {
            canonicalSet = singleDimensionalRangeTree.build(points, /*orderByX
                    =*/false);
        }

        boolean isLeaf() {
            return left == null && right == null;
        }
    }

    RangeNode findSplitNode(RangeNode node, Window window) {
        if (node == null) {
            return node;
        }
        if (node.point.getX() < window.getStartX()) {
            return findSplitNode(node.right, window);
        } else if (node.point.getX() > window.getEndX()) {
            return findSplitNode(node.left, window);
        }
        return node;
    }

    void findPointsLeft(RangeNode node, Window window, List<Point> points) {
        if (node == null) {
            return;
        } else if (node.isLeaf()) {
            if (window.isPointInWindow(node.point)) {
                points.add(node.point);
            }
            return;
         }
        if (node.point.getX() >= window.getStartX()) {
            findPointsLeft(node.left, window, points);
            singleDimensionalRangeTree.findPoints(node.right.canonicalSet,
                    window, points, /*orderByX=*/false);
        } else {
            findPointsLeft(node.right, window, points);
        }
    }

    void findPointsRight(RangeNode node, Window window, List<Point> points) {
        if (node == null) {
            return;
        } else if (node.isLeaf()) {
            if (window.isPointInWindow(node.point)) {
                points.add(node.point);
            }
            return;
        }

        if (node.point.getX() <= window.getEndX()) {
            singleDimensionalRangeTree.findPoints(node.left.canonicalSet,
                    window, points, /*orderByX=*/false);
            findPointsRight(node.right, window, points);
        } else {
            findPointsRight(node.left, window, points);
        }
    }

    void findPoints(RangeNode node, Window window, List<Point> points) {
        RangeNode splitNode = findSplitNode(node, window);
        if (splitNode == null) {
            return;
        }
        findPointsLeft(splitNode.left, window, points);
        findPointsRight(splitNode.right, window, points);
    }

    // Recursively build out the 2D Range Tree.
    // We cannot have the same x coordinates for any 2 points due to issues with
    // splitting infinitely.
    RangeNode build(List<Point> pointsX, List<Point> pointsY) {
        if (pointsX.isEmpty()) {
            return null;
        } else if (pointsX.size() == 1) {
            return new RangeNode(pointsX.getFirst());
        }

        int medianX = Utils.median(pointsX, true);
        int medianY = Utils.median(pointsY, false);
        RangeNode node = new RangeNode(new Point(medianX, medianY));
        int i;
        for (i = 0; i < pointsX.size(); i++) {
            if (pointsX.get(i).getX() > medianX) {
                break;
            }
        }

        node.buildCanonicalSet(pointsY);
        List<Point> leftSubtreePointsSortedByX = pointsX.subList(0, i);
        List<Point> rightSubtreePointsSortedByX = pointsX.subList(i,
                pointsX.size());
        List<Point> leftSubtreePointsSortedByY =
                Utils.getIntersectingPoints(pointsY,
                        leftSubtreePointsSortedByX);
        List<Point> rightSubtreePointsSortedByY =
                Utils.getIntersectingPoints(pointsY,
                        rightSubtreePointsSortedByX);

        node.left = build(leftSubtreePointsSortedByX,
                leftSubtreePointsSortedByY);
        node.right = build(rightSubtreePointsSortedByX,
                rightSubtreePointsSortedByY);
        return node;
    }

    public static void main(String[] args) {
        TwoDimensionalRangeTree twoDimensionalRangeTree =
                new TwoDimensionalRangeTree();
        Set<Point> pointSetX = new TreeSet<>(Utils.getPointXComparator());
        Set<Point> pointSetY = new TreeSet<>(Utils.getPointYComparator());
        Set<Integer> seenX = new HashSet<>();
        Set<Integer> seenY = new HashSet<>();
        do {
            Point point = new Point(Utils.getRandomPositiveInteger(40),
                    Utils.getRandomPositiveInteger(40));
            // The default implementation requires that the x and y coordinates
            // of any 2 points be unique.
            if (seenX.contains(point.getX()) || seenY.contains(point.getY())) {
                continue;
            }
            seenX.add(point.getX());
            seenY.add(point.getY());
            pointSetX.add(point);
            pointSetY.add(point);
        } while (pointSetX.size() < 20);
        List<Point> pointsX = new ArrayList(pointSetX);
        List<Point> pointsY = new ArrayList(pointSetY);
        System.out.println("Input points::");
        Utils.print(pointsX);
        Utils.print(pointsY);

        // sorted the points, build the 2D range tree now.
        RangeNode root =
                twoDimensionalRangeTree.build(pointsX, pointsY);
        Window window = new Window(3, 1, 22, 14);
        System.out.println("Points returned");
        List<Point> pointsReturned = new ArrayList<>();
        twoDimensionalRangeTree.findPoints(root, window, pointsReturned);
        Utils.print(pointsReturned);
    }
}
