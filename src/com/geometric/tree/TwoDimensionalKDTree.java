package com.geometric.tree;

import java.util.*;

import com.geometric.util.Geometric.*;
import com.geometric.util.Utils;

public class TwoDimensionalKDTree {
    // The 2-dimensional KD Tree uses a different approach than the range tree.
    // Build
    // =====
    // It divides the coordinate space by 2 at every node and distributes
    // points to the subtrees accordingly.
    // It alternates between splitting the space by X and then by Y.
    //
    // Query
    // =====
    // Queries are satisfied by finding the subregions that within the query
    // window, and then accumulating the nodes within these subregions.
    class KDNode {
        Point point;
        KDNode left;
        KDNode right;
        KDNode(Point point) {
            this.point = point;
        }
        boolean isLeaf() {
            return left == null && right == null;
        }
    }

    // We add points corresponding to the leaf nodes in order.
    void addLeafsInorder(KDNode node, List<Point> points) {
        if (node == null) {
            return;
        }
        addLeafsInorder(node.left, points);
        if (node.isLeaf()) {
            points.add(node.point);
        }
        addLeafsInorder(node.right, points);
    }

    // Find the points in the subtree that lie within the window.
    // Region corresponds to the region for the node and it will be split
    // alternately by vertical and horizontal lines.
    void findSubtreePointsWithinWindow(KDNode node, Window window,
                                       List<Point> points, Window region,
                                       boolean checkXCoordinate) {
        if (node == null) {
            return;
        } else if (node.isLeaf()) {
            if (window.isPointInWindow(node.point)) {
                points.add(node.point);
            }
            return;
        }
        // Find the subtree regions after splitting by the given horizontal or
        // vertical line.
        Window childRegions[] = region.getRegionSplitByLine(node.point,
                checkXCoordinate);
        if (window.contains(childRegions[0])) {
            addLeafsInorder(node.left, points);
        } else if (window.intersects(childRegions[0])) {
            findSubtreePointsWithinWindow(node.left, window, points,
                    childRegions[0], !checkXCoordinate);
        }
        if (window.contains(childRegions[1])) {
            addLeafsInorder(node.right, points);
        } else if (window.intersects(childRegions[1])) {
            findSubtreePointsWithinWindow(node.right, window, points,
                    childRegions[1], !checkXCoordinate);
        }

        return;
    }

    // We alternate between splitting by x coordinate and y coordinate.
    // This could be extended to more dimensions.
    // We avoid resorting the points by requiring pre-sorting and then use
    // a list intersection to select the relevant points for the subtrees.
    // The intersection walk ensures the selected points are sorted as well.
    // Time Complexity = O(nlogn)
    KDNode build(List<Point> pointsSortedByX,
                 List<Point> pointsSortedByY,
                 boolean splitByXCoordinate) {
        if (pointsSortedByX.isEmpty()) {
            return null;
        } else if (pointsSortedByX.size() == 1) {
            return new KDNode(pointsSortedByX.getFirst());
        }
        int medianX = Utils.median(pointsSortedByX, /*forXCoordinate=*/true);
        int medianY = Utils.median(pointsSortedByY, /*forXCoordinate=*/false);
        KDNode node = new KDNode(new Point(medianX, medianY));
        if (splitByXCoordinate) {
            int i;
            // This is academic, we could choose the median such that the
            // list is split evenly.
            for (i = 0; i < pointsSortedByX.size(); i++) {
                if (pointsSortedByX.get(i).getX() > medianX) {
                    break;
                }
            }
            List<Point> leftPointsSortedByX = pointsSortedByX.subList(0, i);
            List<Point> rightPointsSortedByX = pointsSortedByX.subList(i,
                    pointsSortedByX.size());

            // To find the left and right points sorted by Y we use the
            // intersection walk with the original points sorted by Y.
            List<Point> leftPointsSortedByY =
                    Utils.getIntersectingPoints(pointsSortedByY,
                            leftPointsSortedByX);
            List<Point> rightPointsSortedByY =
                    Utils.getIntersectingPoints(pointsSortedByY,
                            rightPointsSortedByX);
            node.left = build(leftPointsSortedByX, leftPointsSortedByY,
                    /*splitByXCoordinate*/false);
            node.right = build(rightPointsSortedByX, rightPointsSortedByY,
                    /*splitByXCoordinate*/false);
        } else {
            int i;
            // This is academic, we could choose the median such that the list
            // is split evenly.
            for (i = 0; i < pointsSortedByY.size(); i++) {
                if (pointsSortedByY.get(i).getY() > medianY) {
                    break;
                }
            }
            List<Point> leftPointsSortedByY = pointsSortedByY.subList(0, i);
            List<Point> rightPointsSortedByY = pointsSortedByY.subList(i,
                    pointsSortedByY.size());
            // To find the left and right points sorted by Y we use the
            // intersection walk with the original points sorted by Y.
            List<Point> leftPointsSortedByX =
                    Utils.getIntersectingPoints(pointsSortedByX,
                            leftPointsSortedByY);
            List<Point> rightPointsSortedByX =
                    Utils.getIntersectingPoints(pointsSortedByX,
                            rightPointsSortedByY);
            node.left = build(leftPointsSortedByX, leftPointsSortedByY,
                    /*splitByXCoordinate*/true);
            node.right = build(rightPointsSortedByX, rightPointsSortedByY,
                    /*splitByXCoordinate*/true);
        }
        return node;
    }

    public static void main(String[] args) {
        TwoDimensionalKDTree twoDimensionalKDTree =
                new TwoDimensionalKDTree();
        TreeSet<Point> pointSetX = new TreeSet<>(Utils.getPointXComparator());
        TreeSet<Point> pointSetY = new TreeSet<>(Utils.getPointYComparator());
        do {
            Point point = new Point(Utils.getRandomPositiveInteger(40),
                    Utils.getRandomPositiveInteger(40));
            pointSetX.add(point);
            pointSetY.add(point);
        } while (pointSetX.size() < 20);
        List<Point> pointsX = new ArrayList(pointSetX);
        List<Point> pointsY = new ArrayList(pointSetY);
        System.out.println("Input points::");
        Utils.print(pointsX);
        Utils.print(pointsY);
        // sorted the points, build the KD tree now.
        TwoDimensionalKDTree.KDNode root =
                twoDimensionalKDTree.build(pointsX, pointsY,
                        /*splitByXCoordinate*/true);
        Window window = new Window(1, 1, 14, 24);
        System.out.println("Window " + window);

        System.out.println("Points returned");
        List<Point> pointsReturned = new ArrayList<>();
        twoDimensionalKDTree.findSubtreePointsWithinWindow(root, window,
                pointsReturned,
                new Window(pointSetX.getFirst().getX(),
                        pointSetY.getFirst().getY(),
                        pointSetX.getLast().getX(),
                        pointSetY.getLast().getY()),
                /*checkXCoordinate=*/true);
        Utils.print(pointsReturned);
    }
}

