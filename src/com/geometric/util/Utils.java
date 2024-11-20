package com.geometric.util;

import java.util.*;

public class Utils {
    static Comparator<Geometric.Point> pointXComparator =
            (Geometric.Point point1, Geometric.Point point2) -> {
                if (point1.x == point2.x) {return point1.y - point2.y;}
                else { return point1.x - point2.x;}};
    static Comparator<Geometric.Point> pointYComparator =
            (Geometric.Point point1, Geometric.Point point2) -> {
                if (point1.y == point2.y) {return point1.x - point2.x;}
                else { return point1.y - point2.y;}};
    static Random random = new Random(0);

    public static Comparator<Geometric.Point> getPointXComparator() {
        return pointXComparator;
    }

    public static Comparator<Geometric.Point> getPointYComparator() {
        return pointYComparator;
    }

    public static void sort(List<Geometric.Point> points) {
        Collections.sort(points,
                (Geometric.Point point1, Geometric.Point point2) -> (point1.x - point2.x));
    }

    public static void print(List<Geometric.Point> points) {
        System.out.println(points.size());
        for (Geometric.Point point: points) {
            System.out.println(point);
        }
    }

    /* To limit to positive randoms you need to provide a bound.
    // Default Method Definition
    public int nextInt();
    // Bounded MethodDefinition
    public int nextInt(int bound);
    */
    public static int getRandomPositiveInteger(int limit) {
        return random.nextInt(limit);
    }


    public static int median(List<Geometric.Point> points,
                          boolean forXCoordinate) {
        int size = points.size();
        if (size % 2 == 0) {
            if (forXCoordinate) {
                return points.get(size/2-1).getX()/2 + points.get(size/2).getX()/2;
            } else {
                return points.get(size/2-1).getY()/2 + points.get(size/2).getY()/2;
            }
        } else {
            if (forXCoordinate) {
                return points.get(size/2).getX();
            } else {
                return points.get(size/2).getY();
            }
        }
    }

    // Time Complexity = On
    public static List<Geometric.Point> getIntersectingPoints(List<Geometric.Point> points,
                                                List<Geometric.Point> allPoints){
        Set<Geometric.Point> allPointsSet = new HashSet<>(allPoints);
        List<Geometric.Point> intersectingPoints = new ArrayList<>();
        for (Geometric.Point point: points) {
            if (allPointsSet.contains(point)) {
                intersectingPoints.add(point);
            }
        }
        return intersectingPoints;
    }
}
