package com.geometric.tree;

import java.util.*;
import com.geometric.util.Geometric.*;
import com.geometric.util.Utils;

public class SingleDimensionalRangeTree {
    // Node in the Range Tree.
    // The Point is to be interpreted as single dimensional with either valid
    // x or y coordinates.
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

    // We add points corresponding to the leaf nodes in order.
    void addLeafsInorder(RangeNode node, List<Point> points) {
        if (node == null) {
            return;
        }
        addLeafsInorder(node.left, points);
        if (node.isLeaf()) {
            points.add(node.point);
        }
        addLeafsInorder(node.right, points);
    }

    // Accumulate the subtree points within the Window as we traverse through
    // the range tree.
    // Time complexity = On
    private void getSubtreePointsInWindow(RangeNode node, Window window,
                                      List<Point> points, boolean checkForX,
                                      boolean isLeftSubtree) {
        if (node == null) {
            return;
        } else if (node.isLeaf()) {
            if (window.isPointInWindow(node.point)) {
                points.add(node.point);
            }
        }
        if (isLeftSubtree) {
            // In left subtree of split node the x coordinate will be less
            // than or equal to window's endX, so we check the start coordinate.
            if ((checkForX && node.point.getX() >= window.getStartX()) ||
                    (!checkForX && node.point.getY() >= window.getStartY())) {
                getSubtreePointsInWindow(node.left, window, points,
                        checkForX, isLeftSubtree);
                addLeafsInorder(node.right, points);
            } else {
                getSubtreePointsInWindow(node.right, window, points,
                        checkForX, isLeftSubtree);
            }
        } else {
            // In right subtree of split node the x coordinate will be greater
            // than or equal to window's startX, so we check the end coordinate.
            if ((checkForX && node.point.getX() <= window.getEndX()) ||
                    (!checkForX && node.point.getY() <= window.getEndY())) {
                addLeafsInorder(node.left, points);
                getSubtreePointsInWindow(node.right, window, points,
                        checkForX, isLeftSubtree);
            } else {
                getSubtreePointsInWindow(node.left, window, points,
                        checkForX, isLeftSubtree);
            }
        }
    }

    // Find the split node for the given range, i.e. the node where left subtree
    // has a key with a value less than the maxima of the range, and the right
    // subtree has a key with value greater than the minima of the range.
    RangeNode findSplitNode(RangeNode node, Window window, boolean orderByX) {
        // Either null or actual Split node found for the window in the
        // traversal of the path to the leaf.
        if (node == null) {
            return null;
        } else if ((orderByX && window.isPointInXWindow(node.point)) ||
                (!orderByX && window.isPointInYWindow(node.point))) {
            return node;
        } else if ((orderByX && node.point.getX() >= window.getEndX())
                || (!orderByX && node.point.getY() >= window.getEndY())) {
            return findSplitNode(node.left, window, orderByX);
        } else {
            return findSplitNode(node.right, window, orderByX);
        }
    }

    // To find points in the given window we do the following.
    // a. Find split node for the x interval or the y interval for the window.
    // b. Add the node if search ends in a leaf, and it is within the window.
    // c. Accumulate points from the left and right subtrees of the split
    // node that are within the window.
    void findPoints(RangeNode node, Window window, List<Point> points,
                    boolean orderByX) {
        RangeNode splitNode = findSplitNode(node, window, orderByX);
        if (splitNode == null) {
            return;
        } else if (splitNode.isLeaf()) {
            if (window.isPointInWindow(splitNode.point)) {
                points.add(splitNode.point);
            }
            return;
        }
        getSubtreePointsInWindow(splitNode.left, window, points, orderByX,
                /*isLeftSubtree*/true);
        getSubtreePointsInWindow(splitNode.right, window, points, orderByX,
                /*isLeftSubtree*/false);
    }

    // Build a range tree from the given list of points and the relevant
    // coordinate dimension.
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
        Window window = new Window(7, 7, 20, 20);
        List<Point> pointsReturned = new ArrayList<>();
        singleDimensionalRangeTree.findPoints(root, window, pointsReturned,
                /*orderByX=*/true);
        System.out.println("Points returned");
        Utils.print(pointsReturned);
    }
}
