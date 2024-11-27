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

    void findPointsInLeftSubtree(RangeNode node, Window window, List<Point> points) {
        if (node == null) {
            return;
        } else if (node.isLeaf()) {
            if (window.isPointInWindow(node.point)) {
                points.add(node.point);
            }
            return;
        }
        if (node.point.getX() >= window.getStartX()) {
            findPointsInLeftSubtree(node.left, window, points);
            // Filter points that are within the Y coordinates for the window
            singleDimensionalRangeTree.findPoints(node.right.canonicalSet,
                    window, points, /*orderByX=*/false);
        } else {
            findPointsInLeftSubtree(node.right, window, points);
        }
    }

    void findPointsInRightSubtree(RangeNode node, Window window, List<Point> points) {
        if (node == null) {
            return;
        } else if (node.isLeaf()) {
            if (window.isPointInWindow(node.point)) {
                points.add(node.point);
            }
            return;
        }

        if (node.point.getX() <= window.getEndX()) {
            // Filter points that are within the Y coordinates for the window.
            singleDimensionalRangeTree.findPoints(node.left.canonicalSet,
                    window, points, /*orderByX=*/false);
            findPointsInRightSubtree(node.right, window, points);
        } else {
            findPointsInRightSubtree(node.left, window, points);
        }
    }

    void findPoints(RangeNode node, Window window, List<Point> points) {
        RangeNode splitNode = findSplitNode(node, window);
        if (splitNode == null) {
            return;
        } else if (splitNode.isLeaf()) {
            /* Had missed this in the first impl. */
            if (window.isPointInWindow(splitNode.point)) {
                points.add(splitNode.point);
            }
        }
        findPointsInLeftSubtree(splitNode.left, window, points);
        findPointsInRightSubtree(splitNode.right, window, points);
    }

    // Recursively build out the 2D Range Tree.
    // We cannot have the same x coordinates for any 2 points due to issues with
    // splitting infinitely.
    RangeNode build(List<Point> pointsX, List<Point> pointsY) {
        if (pointsX.isEmpty()) {
            return null;
        } else if (pointsX.size() == 1) {
            RangeNode node = new RangeNode(pointsX.getFirst());
            /* Had missed this in the first impl. */
            node.buildCanonicalSet(List.of(pointsX.getFirst()));
            return node;
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
        } while (pointSetX.size() < 10);
        List<Point> pointsX = new ArrayList(pointSetX);
        List<Point> pointsY = new ArrayList(pointSetY);
        System.out.println("Input points::");
        Utils.print(pointsX);
        Utils.print(pointsY);

        // sorted the points, build the 2D range tree now.
        RangeNode root =
                twoDimensionalRangeTree.build(pointsX, pointsY);
        Window window = new Window(1, 1, 20, 40);
        List<Point> pointsReturned = new ArrayList<>();
        twoDimensionalRangeTree.findPoints(root, window, pointsReturned);
        System.out.println("Points returned");
        Utils.print(pointsReturned);
    }
}
