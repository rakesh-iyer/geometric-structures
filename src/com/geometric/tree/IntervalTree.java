package com.geometric.tree;

import java.util.*;

import com.geometric.util.Geometric.*;
import com.geometric.util.Utils;

public class IntervalTree {
    static class IntervalNode {
        int mid;
        List<Interval> intersectingIntervalsSortedByStart = new ArrayList<>();
        List<Interval> intersectingIntervalsSortedByEnd = new ArrayList<>();
        IntervalNode left;
        IntervalNode right;
    }

    int median(List<Interval> intervals) {
        int size = intervals.size();
        if (size % 2 == 0) {
            return intervals.get(size/2-1).getStart()/2 + intervals.get(size/2).getStart()/2;
        } else {
            return intervals.get(size/2).getStart();
        }
    }

    List<Interval> findIntersectingIntervals(List<Interval> allIntervals,
                                             List<Interval> selectedIntervals) {
        List<Interval> intersectingIntervals = new ArrayList<>();
        for (Interval interval: allIntervals) {
            if (selectedIntervals.contains(interval)) {
                intersectingIntervals.add(interval);
            }
        }
        return intersectingIntervals;
    }

    void findIntervals(IntervalNode node,
                       int queryX, List<Interval> intervals) {
        if (node == null) {
            return;
        }
        if (node.mid > queryX) {
            // check those intervals in node that could possibly contain queryX.
            findIntervals(node.left, queryX, intervals);
            for (Interval interval: node.intersectingIntervalsSortedByStart) {
                if (interval.getStart() > queryX) {
                    break;
                }
                intervals.add(interval);
            }
        } else if (node.mid < queryX) {
            for (int i = node.intersectingIntervalsSortedByEnd.size() - 1; i >= 0; i--) {
                Interval interval =
                        node.intersectingIntervalsSortedByEnd.get(i);
                if (interval.getEnd() < queryX) {
                    break;
                }
                intervals.add(interval);
            }
            findIntervals(node.right, queryX, intervals);
        } else {
            // queryX = mid.
            intervals.addAll(node.intersectingIntervalsSortedByStart);
        }
    }

    // represent segments as intervals
    // find the segments that intersect a particular vertical line represented
    // by a single x coordinate.
    IntervalNode build(List<Interval> intervalsSortedByStart,
                       List<Interval> intervalsSortedByEnd) {
        if (intervalsSortedByStart.isEmpty()) {
            return null;
        } else if (intervalsSortedByStart.size() == 1) {
            IntervalNode node = new IntervalNode();
            node.mid = intervalsSortedByStart.getFirst().getStart();
            node.intersectingIntervalsSortedByStart.add(intervalsSortedByStart.getFirst());
            node.intersectingIntervalsSortedByEnd.add(intervalsSortedByEnd.getFirst());
            return node;
        }
        // intervals are sorted by start.
        List<Interval> leftIntervalsSortedByStart = new ArrayList<>();
        List<Interval> rightIntervalsSortedByStart = new ArrayList<>();
        int median = median(intervalsSortedByStart);
        IntervalNode node = new IntervalNode();
        node.mid = median;
        for (int i = 0; i < intervalsSortedByStart.size(); i++) {
            Interval currentInterval = intervalsSortedByStart.get(i);
            if (currentInterval.getEnd() < median) {
                leftIntervalsSortedByStart.add(currentInterval);
            } else if (currentInterval.getStart() > median) {
                rightIntervalsSortedByStart.add(currentInterval);
            } else {
                node.intersectingIntervalsSortedByStart.add(currentInterval);
            }
        }
        List<Interval> leftIntervalsSortedByEnd =
                findIntersectingIntervals(intervalsSortedByEnd,
                        leftIntervalsSortedByStart);
        List<Interval> rightIntervalsSortedByEnd =
                findIntersectingIntervals(intervalsSortedByEnd,
                        rightIntervalsSortedByStart);
        node.intersectingIntervalsSortedByEnd.addAll(
                findIntersectingIntervals(intervalsSortedByEnd,
                        node.intersectingIntervalsSortedByStart));
        node.left = build(leftIntervalsSortedByStart, leftIntervalsSortedByEnd);
        node.right = build(rightIntervalsSortedByStart,
                rightIntervalsSortedByEnd);
        return node;
    }

    public static void main(String[] args) {
        IntervalTree intervalTree = new IntervalTree();
        TreeSet<Interval> intervalsSortedByStart = new TreeSet<>(
                Utils.getIntervalStartComparator());
        TreeSet<Interval> intervalsSortedByEnd = new TreeSet<>(
                Utils.getIntervalEndComparator());
        do {
            int start = Utils.getRandomPositiveInteger(20);
            Interval interval = new Interval(start,
                    start + Utils.getRandomPositiveInteger(20));
            System.out.println(interval);
            intervalsSortedByStart.add(interval);
            intervalsSortedByEnd.add(interval);
        } while (intervalsSortedByStart.size() < 10);

        System.out.println("Input Intervals::");
        for (Interval interval: intervalsSortedByStart) {
            System.out.println(interval);
        }
        System.out.println("Input Intervals::");
        for (Interval interval: intervalsSortedByEnd) {
            System.out.println(interval);
        }

        IntervalNode root = intervalTree.build(
                new ArrayList<>(intervalsSortedByStart),
                new ArrayList<>(intervalsSortedByEnd));

        List<Interval> outputIntervals = new ArrayList<>();
        int queryX = 10;
        intervalTree.findIntervals(root, queryX, outputIntervals);
        System.out.println("Output Intervals::");
        for (Interval interval: outputIntervals) {
            System.out.println(interval);
        }
    }
}
