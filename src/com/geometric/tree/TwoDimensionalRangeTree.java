package com.geometric.tree;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import com.geometric.util.Geometric.*;
import com.geometric.util.Utils;

public class TwoDimensionalRangeTree {
    // The two-dimensional range tree is built as follows.
    // a. The Range tree is built using the X coordinates of the points.
    // b. The canonical set corresponding to the points in the subtree under the
    // range node is a single dimensional range tree on the y coordinate.
    // Since range trees build faster if the points are pre-sorted, the points
    // are provided sorted by X and Y coordinates.

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

    // Find the split node for the given range, i.e. the node where left subtree
    // has a key with a value less than the maxima of the range, and the right
    // subtree has a key with value greater than the minima of the range.
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

    // Accumulate the subtree points within the Window as we traverse through
    // the range tree.
    // Time complexity = On
    void findSubtreePointsInWindow(RangeNode node, Window window,
                                   List<Point> points, boolean isLeftSubtree) {
        if (node == null) {
            return;
        } else if (node.isLeaf()) {
            if (window.isPointInWindow(node.point)) {
                points.add(node.point);
            }
            return;
        }
        if (isLeftSubtree) {
            if (node.point.getX() >= window.getStartX()) {
                findSubtreePointsInWindow(node.left, window, points,
                        isLeftSubtree);
                // Filter points that are within the Y coordinates for the window
                singleDimensionalRangeTree.findPoints(node.right.canonicalSet,
                        window, points, /*orderByX=*/false);
            } else {
                findSubtreePointsInWindow(node.right, window, points,
                        isLeftSubtree);
            }
        } else {
            if (node.point.getX() <= window.getEndX()) {
                // Filter points that are within the Y coordinates for the window.
                singleDimensionalRangeTree.findPoints(node.left.canonicalSet,
                        window, points, /*orderByX=*/false);
                findSubtreePointsInWindow(node.right, window, points,
                        isLeftSubtree);
            } else {
                findSubtreePointsInWindow(node.left, window, points,
                        isLeftSubtree);
            }
        }
    }

    // To find points in the given window we do the following.
    // a. Find split node using both the X and Y coordinates of the window.
    // b. Add the node if search ends in a leaf, and it is within the window.
    // c. Accumulate points from the left and right subtrees of the split
    // node that are within the window.
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
        findSubtreePointsInWindow(splitNode.left, window, points,
                /*isLeftSubtree=*/true);
        findSubtreePointsInWindow(splitNode.right, window, points,
                /*isLeftSubtree=*/false);
    }

    // Recursively build out the 2D Range Tree.
    //
    // NOTE:
    // We cannot have the same x or y coordinates for any 2 points and there
    // are some workarounds to allow for this which are out of scope for this
    // implementation.
    RangeNode build(List<Point> pointsX, List<Point> pointsY) {
        if (pointsX.isEmpty()) {
            return null;
        } else if (pointsX.size() == 1) {
            RangeNode node = new RangeNode(pointsX.getFirst());
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
