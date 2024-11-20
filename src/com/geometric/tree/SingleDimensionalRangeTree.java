package com.geometric.tree;

import java.util.*;
import com.geometric.util.Geometric.*;
import com.geometric.util.Utils;

public class SingleDimensionalRangeTree {
    // We store points that only have x coordinates.
    // Limit coordinates to integers.
    static class RangeNode {
        Point point;
        RangeNode left;
        RangeNode right;
        RangeNode(Point point) {
            this.point = point;
        }

        boolean isLeaf() {
            return left == null && right == null;
        }
    }

    void addAllLeafs(RangeNode node, List<Point> points) {
        // do inorder until you get to the leaves.
        if (node == null) {
            return;
        }
        addAllLeafs(node.left, points);
        if (node.isLeaf()) {
            points.add(node.point);
        }
        addAllLeafs(node.right, points);
    }

    // All nodes we traverse will share the invariant that their x coordinate
    // will be less than or equal to window's endX, so we only check startX.
    private void getPointsFromLeftSubtreeOfSplitNode(RangeNode node,
                                                     Window window,
                                                     List<Point> points,
                                                     boolean checkForX) {
        if (node == null) {
            return;
        } else if (node.isLeaf()) {
            if (window.isPointInWindow(node.point)) {
                points.add(node.point);
            }
        }
        if ((checkForX && node.point.getX() >= window.getStartX()) ||
            (!checkForX && node.point.getY() >= window.getStartY())) {
            getPointsFromLeftSubtreeOfSplitNode(node.left, window, points,
                    checkForX);
            if (node.right != null) {
                addAllLeafs(node.right, points);
            }
        } else {
            getPointsFromLeftSubtreeOfSplitNode(node.right, window, points,
                    checkForX);
        }
    }

    // All nodes we traverse will share the invariant that their x coordinate
    // will be greater than or equal to window's startX, so we only check endX.
    private void getPointsFromRightSubtreeOfSplitNode(RangeNode node,
                                                      Window window,
                                                      List<Point> points,
                                                      boolean checkForX) {
        if (node == null) {
            return;
        } else if (node.isLeaf()) {
            if (window.isPointInWindow(node.point)) {
                points.add(node.point);
            }
        }
        if ((checkForX && node.point.getX() <= window.getEndX()) ||
            (!checkForX && node.point.getY() <= window.getEndY())) {
            if (node.left != null) {
                addAllLeafs(node.left, points);
            }
            getPointsFromRightSubtreeOfSplitNode(node.right, window, points,
                    checkForX);
        } else {
            getPointsFromRightSubtreeOfSplitNode(node.left, window, points,
                    checkForX);
        }
    }

    RangeNode findSplitNode(RangeNode node, Window window, boolean orderByX) {
        // Either null or actual Split node found for the window in the
        // traversal of the path to the leaf.
        if (node == null ||
                (orderByX && node.point.getX() >= window.getStartX() &&
                node.point.getX() <= window.getEndX()) ||
                (!orderByX && node.point.getY() >= window.getStartY() &&
                        node.point.getY() <= window.getEndY())) {
            return node;
        } else if ((orderByX && node.point.getX() >= window.getEndX())
                || (!orderByX && node.point.getY() >= window.getEndY())) {
            return findSplitNode(node.left, window, orderByX);
        } else {
            return findSplitNode(node.right, window, orderByX);
        }
    }

    void findPoints(RangeNode node, Window window, List<Point> points,
                    boolean orderByX) {
        RangeNode splitNode = findSplitNode(node, window, orderByX);
        if (splitNode == null) {
            return;
        }
        getPointsFromLeftSubtreeOfSplitNode(splitNode.left, window, points,
                orderByX);
        getPointsFromRightSubtreeOfSplitNode(splitNode.right, window, points,
                orderByX);
    }

    RangeNode build(List<Point> points, boolean orderByX) {
        if (points.isEmpty()) {
            return null;
        } else if (points.size() == 1) {
            return new RangeNode(points.getFirst());
        }
        int medianX = Utils.median(points, /*forXCoordinate=*/true);
        int medianY = Utils.median(points, /*forXCoordinate=*/false);
        RangeNode node = new RangeNode(new Point(medianX, medianY));
        int i;
        for (i = 0; i < points.size(); i++) {
            if ((orderByX && points.get(i).getX() > medianX) ||
                    (!orderByX && points.get(i).getY() > medianY)) {
                break;
            }
        }
        node.left = build(points.subList(0, i), orderByX);
        node.right = build(points.subList(i, points.size()), orderByX);
        return node;
    }

    public static void main(String[] args) {
        SingleDimensionalRangeTree singleDimensionalRangeTree =
                new SingleDimensionalRangeTree();

        Set<Point> pointSet = new TreeSet<>(Utils.getPointXComparator());
        do {
            int coordinate = Utils.getRandomPositiveInteger(20);
            pointSet.add(new Point(coordinate, coordinate));
        } while (pointSet.size() < 10);
        List<Point> points = new ArrayList(pointSet);
        System.out.println("Input points::");
        Utils.print(points);
        // sorted the points, build the range tree now.
        RangeNode root = singleDimensionalRangeTree.build(points,
                /*orderByX=*/true);
        Window window = new Window(3, 1, 13, 13);
        System.out.println("Points returned");
        List<Point> pointsReturned = new ArrayList<>();
        singleDimensionalRangeTree.findPoints(root, window, pointsReturned,
                /*orderByX=*/true);
        Utils.print(pointsReturned);
    }
}
