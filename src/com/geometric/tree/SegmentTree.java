package com.geometric.tree;


// Use exact imports.
import java.util.*;
import com.geometric.util.Geometric.*;
import com.geometric.util.Utils;

public class SegmentTree {
    // We use a single dimensional range tree to address the queries.
    static SingleDimensionalRangeTree singleDimensionalRangeTree =
            new SingleDimensionalRangeTree();
    static class SegmentNode {
        Interval mid;
        SingleDimensionalRangeTree.RangeNode canonicalSet;
        Map<Point, Segment> pointToSegmentMap = new HashMap<>();
        SegmentNode left;
        SegmentNode right;
        List<Segment> associatedSegments = new ArrayList<>();
        SegmentNode(Interval mid) {
            this.mid = mid;
        }
    }

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

    // assume left and right are adjacent
    Interval combineNeighboringIntervals(Interval left, Interval right) {
        return new Interval(left.getStart(), right.getEnd(),
                left.getClosedStart(), right.getClosedEnd());
    }

    void insertInterval(SegmentNode node, Segment segment) {
        // If the segment's interval contains the entire Interval of the node,
        // it will be associated with that node.
        // We return when the highest node is associated.
        Interval interval = segment.getXInterval();
        if (interval.contains(node.mid)) {
            node.associatedSegments.add(segment);
            return;
        }
        if (interval.intersects(node.left.mid)) {
            insertInterval(node.left, segment);
        }
        if (node.right != null && interval.intersects(node.right.mid)) {
            insertInterval(node.right, segment);
        }
    }
    
    void findSegments(SegmentNode node, QueryLine queryLine,
                      List<Segment> segments) {
        if (node == null) {
            return;
        }
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
            segments.addAll(uniqueSegments);
        }

        // Should we traverse left or right?
        if (node.left != null && node.left.mid.contains(new Interval(queryX,
                queryX))) {
            findSegments(node.left, queryLine, segments);
        } else {
            findSegments(node.right, queryLine, segments);
        }
    }

    void buildCanonicalSet(SegmentNode node) {
        if (node == null) {
            return;
        }
        TreeSet<Point> points = new TreeSet<>(
                Utils.getPointYComparator());
        for (Segment segment: node.associatedSegments) {
            points.add(segment.getStart());
            points.add(segment.getEnd());
            node.pointToSegmentMap.put(segment.getStart(), segment);
            node.pointToSegmentMap.put(segment.getEnd(), segment);
        }
        if (!points.isEmpty()) {
            node.canonicalSet =
                    singleDimensionalRangeTree.build(new ArrayList<>(points),
                            /*orderByX=*/ false);
        }
        buildCanonicalSet(node.left);
        buildCanonicalSet(node.right);
    }

    SegmentNode build(List<Segment> segments) {
        List<SegmentNode> nodeList = buildElementaryIntervals(segments);
        if (nodeList.isEmpty()) {
            return null;
        }

        // build the segment tree in bottom up order for each level.
        // Address the special handling of the single child usecase.
        while (nodeList.size() > 1) {
            List<SegmentNode> parentNodeList = new ArrayList<>();
            for (int i = 0; i < nodeList.size()/2; i++) {
                SegmentNode parent =
                        new SegmentNode(combineNeighboringIntervals(
                                nodeList.get(2*i).mid,
                                nodeList.get(2*i+1).mid));
                parent.left = nodeList.get(2*i);
                parent.right = nodeList.get(2*i+1);
                parentNodeList.add(parent);
            }

            // for odd numbered children.
            if (nodeList.size() % 2 != 0) {
                SegmentNode parent = new SegmentNode(nodeList.getLast().mid);
                parent.left = nodeList.getLast();
                parentNodeList.add(parent);
            }
            nodeList = parentNodeList;
        }

        SegmentNode root = nodeList.getFirst();

        // We now insert the segments into the canonical sets which are to be
        // 1 dimensional Range trees on the Y coordinate.
        // Could we use the same nomenclature as before for point.x ~
        // interval.start and point.y ~ interval.end ?
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
        List<Segment> outputSegments = new ArrayList<>();
        segmentTree.findSegments(root, queryLine, outputSegments);
        System.out.println("Output segments:");
        for (Segment segment: outputSegments) {
            System.out.println(segment);
        }
    }
}
