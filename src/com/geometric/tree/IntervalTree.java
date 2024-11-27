package com.geometric.tree;

import java.util.*;

import com.geometric.util.Geometric.*;
import com.geometric.util.Utils;

public class IntervalTree {
    TwoDimensionalRangeTree twoDimensionalRangeTree =
            new TwoDimensionalRangeTree();
    static class IntervalNode {
        int mid;
        TwoDimensionalRangeTree.RangeNode intersectingSegmentsStartPoints;
        TwoDimensionalRangeTree.RangeNode intersectingSegmentsEndPoints;
        // use this data structure to overcome the current limitation of trying
        // to find the segment to which the points found in the 2d range match.
        Map<Point, Segment> pointToSegmentMap = new HashMap<>();
        IntervalNode left;
        IntervalNode right;
    }

    int medianX(List<Segment> segments) {
        int size = segments.size();
        if (size % 2 == 0) {
            return segments.get(size/2-1).getXInterval().getStart()/2 +
                    segments.get(size/2).getXInterval().getStart()/2;
        } else {
            return segments.get(size/2).getXInterval().getStart();
        }
    }

    List<Segment> findIntersectingSegments(List<Segment> allSegments,
                                             List<Segment> selectedSegments) {
        List<Segment> intersectingSegments = new ArrayList<>();
        for (Segment segment: allSegments) {
            if (selectedSegments.contains(segment)) {
                intersectingSegments.add(segment);
            }
        }
        return intersectingSegments;
    }

    void findSegments(IntervalNode node,
                       QueryLine queryLine, List<Segment> segments) {
        if (node == null) {
            return;
        }
        int queryX = queryLine.getX();
        if (node.mid > queryX) {
            // check those intervals in node that could possibly contain queryX.
            findSegments(node.left, queryLine, segments);
            Window window = new Window(Integer.MIN_VALUE,
                    queryLine.getStartY(), queryX, queryLine.getEndY());
            List<Point> pointsReturned = new ArrayList<>();
            twoDimensionalRangeTree.findPoints(node.intersectingSegmentsStartPoints,
                    window, pointsReturned);
            for (Point point: pointsReturned) {
                segments.add(node.pointToSegmentMap.get(point));
            }

        } else if (node.mid < queryX) {
            Window window = new Window(queryX, queryLine.getStartY(),
                    Integer.MAX_VALUE, queryLine.getEndY());
            List<Point> pointsReturned = new ArrayList<>();
            twoDimensionalRangeTree.findPoints(node.intersectingSegmentsEndPoints,
                    window, pointsReturned);
            for (Point point: pointsReturned) {
                segments.add(node.pointToSegmentMap.get(point));
            }
            findSegments(node.right, queryLine, segments);
        } else {
            // queryX = mid.
            // We may want to have this stored in a set independently.
            segments.addAll(new HashSet<>(node.pointToSegmentMap.values()));
        }
    }

    TwoDimensionalRangeTree.RangeNode buildTwoDimensionalRangeTreeForSegmentStart(
            IntervalNode intervalNode, List<Segment> segments) {
        Set<Point> pointSetX = new TreeSet<>(Utils.getPointXComparator());
        Set<Point> pointSetY = new TreeSet<>(Utils.getPointYComparator());
        for (Segment segment: segments) {
            pointSetX.add(segment.getStart());
            pointSetY.add(segment.getStart());
            intervalNode.pointToSegmentMap.put(segment.getStart(), segment);
        }
        List<Point> pointsX = new ArrayList(pointSetX);
        List<Point> pointsY = new ArrayList(pointSetY);
        // sorted the points, build the 2D range tree now.
        TwoDimensionalRangeTree.RangeNode root =
                twoDimensionalRangeTree.build(pointsX, pointsY);
        return root;
    }

    TwoDimensionalRangeTree.RangeNode buildTwoDimensionalRangeTreeForSegmentEnd(
            IntervalNode intervalNode, List<Segment> segments) {
        Set<Point> pointSetX = new TreeSet<>(Utils.getPointXComparator());
        Set<Point> pointSetY = new TreeSet<>(Utils.getPointYComparator());
        for (Segment segment: segments) {
            pointSetX.add(segment.getEnd());
            pointSetY.add(segment.getEnd());
            intervalNode.pointToSegmentMap.put(segment.getEnd(), segment);
        }
        List<Point> pointsX = new ArrayList(pointSetX);
        List<Point> pointsY = new ArrayList(pointSetY);
        // sorted the points, build the 2D range tree now.
        TwoDimensionalRangeTree.RangeNode root =
                twoDimensionalRangeTree.build(pointsX, pointsY);
        return root;
    }

    // This function recieves segments sorted by X coordinates.
    // find the segments that intersect a particular vertical line represented
    // by a single x coordinate.
    IntervalNode build(List<Segment> segmentsSortedByStart,
                       List<Segment> segmentsSortedByEnd) {
        List<Segment> intersectingSegmentsSortedByStart = new ArrayList<>();
        List<Segment> intersectingSegmentsSortedByEnd = new ArrayList<>();
        if (segmentsSortedByStart.isEmpty()) {
            return null;
        } else if (segmentsSortedByStart.size() == 1) {
            IntervalNode node = new IntervalNode();
            node.mid =
                    segmentsSortedByStart.getFirst().getXInterval().getStart();
            intersectingSegmentsSortedByStart.add(segmentsSortedByStart.getFirst());
            intersectingSegmentsSortedByEnd.add(segmentsSortedByEnd.getFirst());
            node.intersectingSegmentsStartPoints =
                    buildTwoDimensionalRangeTreeForSegmentStart(node,
                            intersectingSegmentsSortedByStart);
            node.intersectingSegmentsEndPoints =
                    buildTwoDimensionalRangeTreeForSegmentEnd(node,
                            intersectingSegmentsSortedByEnd);
            return node;
        }

        // intervals are sorted by start.
        List<Segment> leftSegmentsSortedByStart = new ArrayList<>();
        List<Segment> rightSegmentsSortedByStart = new ArrayList<>();
        int medianX = medianX(segmentsSortedByStart);
        IntervalNode node = new IntervalNode();
        node.mid = medianX;
        for (int i = 0; i < segmentsSortedByStart.size(); i++) {
            Segment currentSegment =
                    segmentsSortedByStart.get(i);
            Interval currentInterval = currentSegment.getXInterval();
            if (currentInterval.getEnd() < medianX) {
                leftSegmentsSortedByStart.add(currentSegment);
            } else if (currentInterval.getStart() > medianX) {
                rightSegmentsSortedByStart.add(currentSegment);
            } else {
                intersectingSegmentsSortedByStart.add(currentSegment);
            }
        }
        List<Segment> leftSegmentsSortedByEnd =
                findIntersectingSegments(segmentsSortedByEnd,
                        leftSegmentsSortedByStart);
        List<Segment> rightSegmentsSortedByEnd =
                findIntersectingSegments(segmentsSortedByEnd,
                        rightSegmentsSortedByStart);
        intersectingSegmentsSortedByEnd.addAll(
                findIntersectingSegments(segmentsSortedByEnd,
                        intersectingSegmentsSortedByStart));

        node.intersectingSegmentsStartPoints =
                buildTwoDimensionalRangeTreeForSegmentStart(node,
                        intersectingSegmentsSortedByStart);
        node.intersectingSegmentsEndPoints =
                buildTwoDimensionalRangeTreeForSegmentEnd(node,
                        intersectingSegmentsSortedByEnd);
        node.left = build(leftSegmentsSortedByStart, leftSegmentsSortedByEnd);
        node.right = build(rightSegmentsSortedByStart,
                rightSegmentsSortedByEnd);
        return node;
    }

    public static void main(String[] args) {
        IntervalTree intervalTree = new IntervalTree();
        TreeSet<Segment> segmentsSortedByStartX =
                new TreeSet<>(Utils.getSegmentStartXComparator());
        TreeSet<Segment> segmentsSortedByEndX =
                new TreeSet<>(Utils.getSegmentEndXComparator());
        Set<Integer> seenX = new HashSet<>();
        Set<Integer> seenY = new HashSet<>();
        do {
            int startX = Utils.getRandomPositiveInteger(40);
            int startY = Utils.getRandomPositiveInteger(40);
            int endX = startX + Utils.getRandomPositiveInteger(20);
            // Interval trees can only locate segments that are axis parallel.
            // But the range trees do not allow for x coordinates or y
            // coordinates of any 2 points to match.
            int endY = startY + 1;
            if (seenX.contains(startX) || seenY.contains(startY) ||
                    seenX.contains(endX) || seenY.contains(endY)) {
                continue;
            }
            seenX.add(startX);
            seenY.add(startY);
            seenX.add(endX);
            seenY.add(endY);
            Segment segment = new Segment(startX, startY, endX, endY);
            segmentsSortedByStartX.add(segment);
            segmentsSortedByEndX.add(segment);
        } while (segmentsSortedByStartX.size() < 10);

        System.out.println("Input segments:");
        for (Segment segment: segmentsSortedByStartX) {
            System.out.println(segment);
        }

        IntervalNode root = intervalTree.build(
                new ArrayList<>(segmentsSortedByStartX),
                new ArrayList<>(segmentsSortedByEndX)
        );

        QueryLine queryLine = new QueryLine(15, 15, 40);
        List<Segment> outputSegments = new ArrayList<>();
        intervalTree.findSegments(root, queryLine, outputSegments);
        System.out.println("Output segments:");
        for (Segment segment: outputSegments) {
            System.out.println(segment);
        }
    }
}