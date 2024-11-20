package com.geometric.tree;

import java.util.*;

import com.geometric.util.Geometric.*;
import com.geometric.util.Utils;

public class TwoDimensionalKDTree {
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

    // We alternate between splitting by x coordinate and y coordinate.
    // This could be extended to more dimensions.
    // Time Complexity = O nlogn
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
            for (i = 0; i < pointsSortedByX.size(); i++) {
                if (pointsSortedByX.get(i).getX() > medianX) {
                    break;
                }
            }
            List<Point> leftPointsSortedByX = pointsSortedByX.subList(0, i);
            List<Point> rightPointsSortedByX = pointsSortedByX.subList(i,
                    pointsSortedByX.size());
            List<Point> leftPointsSortedByY =
                    Utils.getIntersectingPoints(pointsSortedByY,
                            leftPointsSortedByX);
            List<Point> rightPointsSortedByY =
                    Utils.getIntersectingPoints(pointsSortedByY,
                            rightPointsSortedByX);

            node.left = build(leftPointsSortedByX, leftPointsSortedByY, false);
            node.right = build(rightPointsSortedByX, rightPointsSortedByY,
                    false);
        } else {
            int i;
            for (i = 0; i < pointsSortedByY.size(); i++) {
                if (pointsSortedByY.get(i).getY() > medianY) {
                    break;
                }
            }
            List<Point> leftPointsSortedByY = pointsSortedByY.subList(0, i);
            List<Point> rightPointsSortedByY = pointsSortedByY.subList(i,
                    pointsSortedByY.size());
            List<Point> leftPointsSortedByX =
                    Utils.getIntersectingPoints(pointsSortedByX,
                            leftPointsSortedByY);
            List<Point> rightPointsSortedByX =
                    Utils.getIntersectingPoints(pointsSortedByX,
                            rightPointsSortedByY);
            node.left = build(leftPointsSortedByX, leftPointsSortedByY, true);
            node.right = build(rightPointsSortedByX, rightPointsSortedByY, true);
        }
        return node;
    }

    // Time Complexity = 0klogn
    void findPoints(KDNode node, Window window, List<Point> points,
                     boolean checkXCoordinate) {
        if (node == null) {
            return;
        }
        if (node.isLeaf()) {
            if (window.isPointInWindow(node.point)) {
                points.add(node.point);
            }
            return;
        }
        if (checkXCoordinate) {
            if (window.getEndX() < node.point.getX()) {
                findPoints(node.left, window, points, false);
            } else if (window.getStartX() > node.point.getX()) {
                findPoints(node.right, window, points, false);
            } else {
                // node is in the window so check both left and right subtrees.
                findPoints(node.left, window, points, false);
                findPoints(node.right, window, points, false);
            }
        } else {
            if (window.getEndY() < node.point.getY()) {
                findPoints(node.left, window, points, true);
            } else if (window.getStartY() > node.point.getY()) {
                findPoints(node.right, window, points, true);
            } else {
                // node is in the window.
                findPoints(node.left, window, points, true);
                findPoints(node.right, window, points, true);
            }
        }
        return;
    }

    public static void main(String[] args) {
        TwoDimensionalKDTree twoDimensionalKDTree =
                new TwoDimensionalKDTree();
        Set<Point> pointSetX = new TreeSet<>(Utils.getPointXComparator());
        Set<Point> pointSetY = new TreeSet<>(Utils.getPointYComparator());
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
        Window window = new Window(3, 1, 14, 14);
        System.out.println("Points returned");
        List<Point> pointsReturned = new ArrayList<>();
        twoDimensionalKDTree.findPoints(root, window, pointsReturned,
                /*checkXCoordinate=*/true);
        Utils.print(pointsReturned);
    }
}

