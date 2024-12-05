package com.geometric.tree;

import java.util.*;

import com.geometric.util.Geometric.*;
import com.geometric.util.Utils;

public class IntervalTree {
    // The interval tree stores X or Y axis parallel segments.
    // It is primarily designed to efficiently query for the set of segments
    // that are within a given recatangular window.
    // It organizes the information using the segments start and end points.
    // The differentiation of start and end points are important as the
    // associated information is stored in two-dimensional range trees.

    // Each node stores the following:
    //
    // a. The median for the set of values in the subtree represented by node.
    // b. The start points of the segments containing the median.
    // c. The end points of the segments containing the median.
    // d. Left and Right subtrees.
    //
    // For convenience this implementation stores a map to quickly locate a
    // segment using its start or end point.
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

    // We only need the x median for this implementation of the interval
    // tree, as the input is a set of horizontal axis parallel segments.
    int xMedian(List<Segment> segments) {
        int size = segments.size();
        if (size % 2 == 0) {
            return segments.get(size/2-1).getXInterval().getStart()/2 +
                    segments.get(size/2).getXInterval().getStart()/2;
        } else {
            return segments.get(size/2).getXInterval().getStart();
        }
    }

    // Filter the sorted all segments to only choose the selected segments.
    // This will ensure the selected segments are in sorted order.
    List<Segment> findIntersectingSegments(List<Segment> allSegments,
                                           Set<Segment> selectedSegments) {
        List<Segment> intersectingSegments = new ArrayList<>();
        for (Segment segment: allSegments) {
            if (selectedSegments.contains(segment)) {
                intersectingSegments.add(segment);
            }
        }
        return intersectingSegments;
    }

    // Find the horizontal segments that cross the given vertical line.
    // The algorithm is as follows:
    // a. The X coordinate is derived from the query line.
    // b. The coordinate is compared with the current node mid.
    //    i) If the query value is less than node's mid, We begin with the
    //    search in the left subtree. Then we add all the segments
    //    corresponding to the intersection points less than or equal to query
    //    value. We use the two-dimensional Range tree corresponding to the
    //    start points and the pointToSegment map for this.
    //    ii) If the query value is greater than node's mid, we add all the
    //    segmens with intersecting points greater than or equal to the query
    //    value. We use the two-dimensional range tree corresponding to the
    //    end points and the pointToSegment map for this.
    //    We continue the search in the right subtree.
    //    iii) If the value is equal to the node's mid, we add the
    //    intersecting segments and terminate the search at this node.
    void findSegmentsCrossingLine(IntervalNode node, QueryLine queryLine,
                                  List<Segment> segments) {
        if (node == null) {
            return;
        }
        int queryX = queryLine.getX();
        if (node.mid > queryX) {
            // check those intervals in node that could possibly contain queryX.
            findSegmentsCrossingLine(node.left, queryLine, segments);
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
            findSegmentsCrossingLine(node.right, queryLine, segments);
        } else {
            // queryX = mid.
            // We may want to have this stored in a set independently.
            segments.addAll(new HashSet<>(node.pointToSegmentMap.values()));
        }
    }

    // Build a two-dimensional range tree on the segment start or end points.
    TwoDimensionalRangeTree.RangeNode buildTwoDimensionalRangeTreeForSegment(
            IntervalNode intervalNode, List<Segment> segments,
            boolean isStart) {
        List<Point> pointsX = new ArrayList<>();
        List<Point> pointsY = new ArrayList<>();
        // We use the point to segment map to quickly deduce the segment whose
        // endpoint is found in the range search.
        for (Segment segment: segments) {
            Point point = isStart ? segment.getStart() : segment.getEnd();
            pointsX.add(point);
            pointsY.add(point);
            intervalNode.pointToSegmentMap.put(point, segment);
        }
        // The points are provided pre-sorted to build the 2D range tree.
        Collections.sort(pointsX, Utils.getPointXComparator());
        Collections.sort(pointsY, Utils.getPointYComparator());
        TwoDimensionalRangeTree.RangeNode root =
                twoDimensionalRangeTree.build(pointsX, pointsY);
        return root;
    }

    // Handles building an interval tree for a single segment.
    IntervalNode buildForSingleSegment(Segment segment) {
        IntervalNode node = new IntervalNode();
        node.mid = segment.getXInterval().getStart();
        node.intersectingSegmentsStartPoints =
                buildTwoDimensionalRangeTreeForSegment(node,
                        List.of(segment), /*isStart=*/true);
        node.intersectingSegmentsEndPoints =
                buildTwoDimensionalRangeTreeForSegment(node,
                        List.of(segment), /*isStart=*/false);
        return node;
    }

    // Build an interval tree from given list of segments sorted both by the
    // start and end point.
    IntervalNode build(List<Segment> segmentsSortedByStart,
                       List<Segment> segmentsSortedByEnd) {
        if (segmentsSortedByStart.isEmpty()) {
            return null;
        } else if (segmentsSortedByStart.size() == 1) {
            //  Handle the single segment case separately.
            return buildForSingleSegment(segmentsSortedByStart.getFirst());
        }

        List<Segment> leftSegmentsSortedByStart = new ArrayList<>();
        List<Segment> rightSegmentsSortedByStart = new ArrayList<>();
        List<Segment> intersectingSegmentsSortedByStart = new ArrayList<>();
        int xMedian = xMedian(segmentsSortedByStart);
        IntervalNode node = new IntervalNode();
        node.mid = xMedian;
        // Partition the input segments into 3 lists.
        // a. Segments completely to the left of the median point.
        // b. Segments containing the median point.
        // c. Segments completely to the right of the median point.
        for (int i = 0; i < segmentsSortedByStart.size(); i++) {
            Segment currentSegment =
                    segmentsSortedByStart.get(i);
            Interval currentInterval = currentSegment.getXInterval();
            if (currentInterval.getEnd() < xMedian) {
                leftSegmentsSortedByStart.add(currentSegment);
            } else if (currentInterval.getStart() > xMedian) {
                rightSegmentsSortedByStart.add(currentSegment);
            } else {
                intersectingSegmentsSortedByStart.add(currentSegment);
            }
        }

        // Use the list intersection method to retain the sorted order from
        // the input lists.
        List<Segment> leftSegmentsSortedByEnd =
                findIntersectingSegments(segmentsSortedByEnd,
                        new HashSet<>(leftSegmentsSortedByStart));
        List<Segment> rightSegmentsSortedByEnd =
                findIntersectingSegments(segmentsSortedByEnd,
                        new HashSet<>(rightSegmentsSortedByStart));
        List<Segment> intersectingSegmentsSortedByEnd =
                findIntersectingSegments(segmentsSortedByEnd,
                        new HashSet<>(intersectingSegmentsSortedByStart));

        // Construct the interval node.
        // Build a 2D range tree so as to quickly be able to search by X and
        // Y coordinates.
        // Build for segment start and segment end as we would encounter them
        // while traversing the subtrees.
        node.intersectingSegmentsStartPoints =
                buildTwoDimensionalRangeTreeForSegment(node,
                        intersectingSegmentsSortedByStart, /*isStart=*/true);
        node.intersectingSegmentsEndPoints =
                buildTwoDimensionalRangeTreeForSegment(node,
                        intersectingSegmentsSortedByEnd, /*isStart=*/false);
        node.left = build(leftSegmentsSortedByStart, leftSegmentsSortedByEnd);
        node.right = build(rightSegmentsSortedByStart,
                rightSegmentsSortedByEnd);
        return node;
    }

    public static void main(String[] args) {
        // This Interval tree is for horizontal axis parallel segments.
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
            // But the range trees being used for the implementation do not
            // allow for x coordinates or y coordinates of any 2 points to
            // match. So we use the following workaround which has 0 impact.
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
        System.out.println("Query Line:");
        System.out.println(queryLine);
        List<Segment> outputSegments = new ArrayList<>();
        intervalTree.findSegmentsCrossingLine(root, queryLine, outputSegments);
        System.out.println("Segments that intersect the query line:");
        for (Segment segment: outputSegments) {
            System.out.println(segment);
        }
    }
}