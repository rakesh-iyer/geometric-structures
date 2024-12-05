package com.geometric.tree;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import com.geometric.util.Geometric.*;
import com.geometric.util.Utils;

public class SegmentTree {
    // A segment tree is a data structure built from the Locus principle.
    // It efficiently queries arbitrary segments that intersect a window.
    //
    // The idea is as follows:
    //
    // Build Base Tree
    // ===============
    // a. Start with Elementary intervals built from an ordered set of segment
    // start and end points that correspond to the leaf nodes of the tree.
    // b. Build the higher level nodes by merging pairs of lower level nodes
    // and build the entire tree in bottom up manner. Every interval of an
    // internal node corresponds to the merged intervals of its child nodes.
    //
    // Add Input Intervals
    // ===================
    // For each interval do the following::
    // a. Starting from segment tree's root, find the highest level node whose
    // interval is completely covered by the input interval. Add the input
    // interval to this node and stop the search.
    // b. If the node's interval is not covered by the input interval, find
    // if the input interval intersects the left child node's interval and if
    // so repeat the process with the left node. Similarly check if the input
    // interval intersects the right child node's interval and if so repeat
    // the process with the right node.
    //

    // We use a singleton dimensional range tree to address the queries.
    static SingleDimensionalRangeTree singleDimensionalRangeTree =
            new SingleDimensionalRangeTree();
    static class SegmentNode {
        Interval midInterval;
        SingleDimensionalRangeTree.RangeNode canonicalSet;
        Map<Point, Segment> pointToSegmentMap = new HashMap<>();
        SegmentNode left;
        SegmentNode right;
        List<Segment> associatedSegments = new ArrayList<>();
        SegmentNode(Interval midInterval) {
            this.midInterval = midInterval;
        }
    }

    // Add elementary intervals built from sorted set of the start points and
    // end points of the segments.
    // The list of elementary intervals are ::
    // a. An open interval between a point and its neighbor in the sorted list.
    // b. A closed interval containing each point.
    // c. An open interval between -infinity and the first point.
    // d. An open interval between the last point and +infinity.
    List<SegmentNode> buildElementaryIntervals(List<Segment> segments) {
        List<SegmentNode> nodeList = new ArrayList<>();
        List<Integer> endpoints = new ArrayList<>();
        for (Segment segment: segments) {
            endpoints.add(segment.getXInterval().getStart());
            endpoints.add(segment.getXInterval().getEnd());
        }
        Collections.sort(endpoints);
        nodeList.add(new SegmentNode(new Interval(Integer.MIN_VALUE,
                endpoints.getFirst(), /*closedStart=*/false, /*closedEnd
                =*/false)));

        for (int i = 0; i < endpoints.size() - 1; i++) {
            // This is a closed interval.
            nodeList.add(new SegmentNode(new Interval(endpoints.get(i),
                    endpoints.get(i))));
            // This is an open interval
            nodeList.add(new SegmentNode(new Interval(endpoints.get(i),
                    endpoints.get(i+1), /*closedStart=*/false, /*closedEnd
                    =*/false)));
        }
        // This is a closed interval
        nodeList.add(new SegmentNode(new Interval(endpoints.getLast(),
                endpoints.getLast())));
        // This is an open interval
        nodeList.add(new SegmentNode(new Interval(endpoints.getLast(),
                Integer.MAX_VALUE, /*closedStart=*/false, /*closedEnd
                =*/false)));

        return nodeList;
    }

    // Assume left and right intervals are in order and are combinable according
    // to open intervals and closed intervals semantics.
    Interval combineNeighboringIntervals(Interval left, Interval right) {
        return new Interval(left.getStart(), right.getEnd(),
                left.getClosedStart(), right.getClosedEnd());
    }

    // Find the arbitrary segments that intersect the query line.
    // a. Start with the X coordinate of the query line.
    // b. If the canonical set of the node is not null, find in the
    // single-dimensional range tree all the points within the window
    // covering all the points within the Y range. The start or end points
    // represent segments that will intersect the query line.
    // c. Visit the subtree containing query line's X coordinate if any.
    //
    // Caveat::
    // This implementation and much of the theory glosses over the fact that
    // the one-dimensional range query is not sufficient to identify the
    // arbitrary oriented segments intersecting the query line.
    // To correctly do this we need to compute the intersection points for
    // the segments with the query line.
    // This means that part is no O (logn + k), k is count of added segments.
    // Open Question:: Why theory glosses over this except for one paper.
    void findSegments(SegmentNode node, QueryLine queryLine,
                      List<Segment> segments) {
        // Note we dont have a node null check as this is unexpected.
        int queryX = queryLine.getX();
        // A node may have no segments associated with it.
        if (node.canonicalSet != null) {
            // We actually need to test for the point of intersection of the
            // segment with the queryLine and not the end points.
            // It's interesting that the theory does not mention this fact.
            Window window = new Window(Integer.MIN_VALUE, queryLine.getStartY(),
                    Integer.MAX_VALUE, queryLine.getEndY());
            List<Point> points = new ArrayList<>();
            singleDimensionalRangeTree.findPoints(node.canonicalSet, window,
                    points, /*orderByX=*/false);
            Set<Segment> uniqueSegments = new HashSet<>();
            for (Point point : points) {
                uniqueSegments.add(node.pointToSegmentMap.get(point));
            }
            // We may need to filter the segments that really intersect the
            // line. Is it possible due to the non-crossing nature of these
            // segments, the time complexity still remains 0(logn + k).
            segments.addAll(uniqueSegments);
        }
        if (node.left != null &&
            node.left.midInterval.contains(new Interval(queryX, queryX))) {
            findSegments(node.left, queryLine, segments);
        } else if (node.right != null) {
            // If we did not find queryX in the left subtree interval it
            // should be in the right subtree interval.
            findSegments(node.right, queryLine, segments);
        }
    }

    // The canonical set of a node contains the segments start and end points
    // of the associated segments for a node.
    // Its purpose is for quick execution of the windowing query.
    //
    // NOTE:
    // Here we are only querying with the vertical edge of the window with the
    // expectation taht the logic could be extended to cover all window edges.
    void buildCanonicalSet(SegmentNode node) {
        if (node == null) {
            return;
        }
        List<Point> points = new ArrayList<>();
        for (Segment segment: node.associatedSegments) {
            points.add(segment.getStart());
            points.add(segment.getEnd());
            node.pointToSegmentMap.put(segment.getStart(), segment);
            node.pointToSegmentMap.put(segment.getEnd(), segment);
        }
        // The canonical set is built on the Y coordinates, so we can filter
        // only the found intervals that intersect with the query line and have
        // have Y coordinates within the query line's Y coordinate range.
        // Note that this is the key differentiator from the interval tree, in
        // which case the axis parallel-ness made this check unnecessary.
        if (!points.isEmpty()) {
            Collections.sort(points, Utils.getPointYComparator());
            node.canonicalSet =
                    singleDimensionalRangeTree.build(new ArrayList<>(points),
                            /*orderByX=*/ false);
        }
        buildCanonicalSet(node.left);
        buildCanonicalSet(node.right);
    }


    // Repeated for easier correlation with the implementation.
    // For each interval do the following::
    // a. Starting from segment tree's root, find the highest level node whose
    // interval is completely covered by the input interval. Add the input
    // interval to this node and stop the search.
    // b. If the node's interval is not covered by the input interval, find
    // if the input interval intersects the left child node's interval and if
    // so repeat the process with the left node. Similarly check if the input
    // interval intersects the right child node's interval and if so repeat
    // the process with the right node.
    void insertInterval(SegmentNode node, Segment segment) {
        // If the segment's interval contains the entire Interval of the node,
        // it will be associated with that node.
        // We return when the highest node is associated.
        Interval interval = segment.getXInterval();
        if (interval.contains(node.midInterval)) {
            node.associatedSegments.add(segment);
            return;
        }
        // Every non leaf node must have a left child but not necessarily a
        // right child.
        if (interval.intersects(node.left.midInterval)) {
            insertInterval(node.left, segment);
        }
        if (node.right == null) {
            return;
        }
        if (interval.intersects(node.right.midInterval)) {
            insertInterval(node.right, segment);
        }
    }

    // Build the segment tree starting from elementary intervals.
    SegmentNode build(List<Segment> segments) {
        List<SegmentNode> nodeList = buildElementaryIntervals(segments);
        if (nodeList.isEmpty()) {
            return null;
        }

        // Build the segment tree in bottom up order for each level.
        // Address the special handling of the single child usecase.
        while (nodeList.size() > 1) {
            List<SegmentNode> parentNodeList = new ArrayList<>();
            // Address all but the last single child if any.
            for (int i = 0; i < nodeList.size()/2; i++) {
                SegmentNode leftChild = nodeList.get(2*i);
                SegmentNode rightChild = nodeList.get(2*i+1);
                SegmentNode parent =
                        new SegmentNode(combineNeighboringIntervals(
                                leftChild.midInterval, rightChild.midInterval));
                parent.left = leftChild;
                parent.right = rightChild;
                parentNodeList.add(parent);
            }
            // For single child parent.
            if (nodeList.size() % 2 != 0) {
                SegmentNode lastChild = nodeList.getLast();
                SegmentNode parent = new SegmentNode(lastChild.midInterval);
                parent.left = lastChild;
                parentNodeList.add(parent);
            }
            nodeList = parentNodeList;
        }

        SegmentNode root = nodeList.getFirst();
        // Insert the X intervals of the segments into the canonical sets which
        // are one-dimensional Range trees on the Y coordinate.
        for (Segment segment: segments) {
            insertInterval(root, segment);
        }
        // The canonical set will allow optimal windowing searches.
        buildCanonicalSet(root);
        return root;
    }

    public static void main(String[] args) {
        SegmentTree segmentTree = new SegmentTree();
        List<Segment> segments = new ArrayList<>();
        Set<Integer> seenX = new HashSet<>();
        Set<Integer> seenY = new HashSet<>();
        do {
            int startX = Utils.getRandomPositiveInteger(40);
            int startY = Utils.getRandomPositiveInteger(40);
            int endX = startX + Utils.getRandomPositiveInteger(20);
            int endY = startY + Utils.getRandomPositiveInteger(20);
            // The single dimensional range tree does not allow for y
            // coordinates to match, so adjust it to avoid the scenario.
            if (endY == startY) {
                endY++;
            }
            if (seenX.contains(startX) || seenY.contains(startY) ||
                    seenX.contains(endX) || seenY.contains(endY)) {
                continue;
            }
            seenX.add(startX);
            seenY.add(startY);
            seenX.add(endX);
            seenY.add(endY);
            Segment segment = new Segment(startX, startY, endX, endY);
            segments.add(segment);
        } while (segments.size() < 10);

        SegmentNode root = segmentTree.build(segments);
        System.out.println("Input segments:");
        for (Segment segment: segments) {
            System.out.println(segment);
        }

        QueryLine queryLine = new QueryLine(9, 10, 40);
        System.out.println("Query Line::");
        System.out.println(queryLine);
        List<Segment> outputSegments = new ArrayList<>();
        segmentTree.findSegments(root, queryLine, outputSegments);
        System.out.println("Output segments:");
        for (Segment segment: outputSegments) {
            System.out.println(segment);
        }
    }
}
