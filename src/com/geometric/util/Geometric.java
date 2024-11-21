package com.geometric.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Geometric {
    public static class Point {
        int x;
        int y;
        public String toString() {
            return "x:" + x + ", y:" + y;
        }
        public Point(int x) {
            this.x = x;
        }
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public int getX() {
            return x;
        }
        public int getY() {
            return y;
        }
    }

    public static class Window {
        int startX;
        int startY;
        int endX;
        int endY;
        public Window(int startX, int startY, int endX, int endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }
        public int getStartX() {
            return startX;
        }
        public int getEndX() {
            return endX;
        }
        public int getStartY() {
            return startY;
        }
        public int getEndY() {
            return endY;
        }
        public boolean isPointInWindow(Point point) {
            return point.x >= startX && point.x <= endX &&
                    point.y >= startY && point.y <= endY;
        }

        public String toString() {
            return "[ (" + startX + "," + startY + ")" + " -- (" + endX + "," + endY + ") ]";
        }

        // should the regions split be disjoint.
        public Window[] getRegionSplitByLine(Point point,
                                            boolean isXCoordinate) {
            if (isXCoordinate) {
                return new Window[]{
                    new Window(startX, startY, point.getX(), endY),
                        new Window(point.getX(), startY, endX, endY)
                };

            } else {
                return new Window[] {
                        new Window(startX, startY, endX, point.getY()),
                        new Window(startX, point.getY(), endX, endY)
                };
            }
        }

        static class VerticalEdge {
            int x;
            int startY;
            int endY;
            
            VerticalEdge(int x, int startY, int endY) {
                this.x = x;
                this.startY = startY;
                this.endY = endY;
            }
        }

        static class HorizontalEdge {
            int startX;
            int endX;
            int y;
            
            HorizontalEdge(int startX, int endX, int y) {
                this.startX = startX;
                this.endX = endX;
                this.y = y;
            }
        }

        // Use the non-intersecting condition checks.
        private boolean areEdgesIntersecting(HorizontalEdge horizontalEdge1,
                                             HorizontalEdge horizontalEdge2) {
            if (horizontalEdge1.y != horizontalEdge2.y) {
                return false;
            } else if (horizontalEdge1.startX > horizontalEdge2.endX ||
                    horizontalEdge2.startX > horizontalEdge1.endX){
                return false;
            }
            return true;
        }

        private boolean areEdgesIntersecting(VerticalEdge verticalEdge1,
                                             VerticalEdge verticalEdge2) {
            if (verticalEdge1.x != verticalEdge2.x) {
                return false;
            } else if (verticalEdge1.startY > verticalEdge2.endY ||
                    verticalEdge2.startY > verticalEdge1.endY) {
                return false;
            }

            return true;
        }

        private boolean areEdgesIntersecting(HorizontalEdge horizontalEdge,
                                             VerticalEdge verticalEdge) {
            if (horizontalEdge.y > verticalEdge.endY ||
                verticalEdge.startY > horizontalEdge.y ||
                horizontalEdge.startX > verticalEdge.x ||
                verticalEdge.x > horizontalEdge.endX) {
                return false;
            }
            return true;
        }

        public boolean intersects(Window region) {
            // If either region contains the other return true.
            if (contains(region) || region.contains(this)) {
                return true;
            }

            // check if any edge of window intersects with any edge of region.
            VerticalEdge left1 = new VerticalEdge(startX, startY, endY);
            VerticalEdge right1 = new VerticalEdge(endX, startY, endY);
            HorizontalEdge top1 = new HorizontalEdge(startX, endX, startY);
            HorizontalEdge bottom1 = new HorizontalEdge(startX, endX, endY);
            
            VerticalEdge left2 = new VerticalEdge(region.startX,
                    region.startY, region.endY);
            VerticalEdge right2 = new VerticalEdge(region.endX,
                    region.startY, region.endY);
            HorizontalEdge top2 = new HorizontalEdge(region.startX,
                    region.endX, region.startY);
            HorizontalEdge bottom2 = new HorizontalEdge(region.startX,
                    region.endX, region.endY);
            
            return  areEdgesIntersecting(left1, left2) ||
                    areEdgesIntersecting(left1, right2) ||
                    areEdgesIntersecting(top2, left1) ||
                    areEdgesIntersecting(bottom2, left1) ||
                    areEdgesIntersecting(right1, left2) ||
                    areEdgesIntersecting(right1, right2) ||
                    areEdgesIntersecting(top2, right1) ||
                    areEdgesIntersecting(bottom2, right1) ||
                    areEdgesIntersecting(top1, left2) ||
                    areEdgesIntersecting(top1, right2) ||
                    areEdgesIntersecting(top1, top2) ||
                    areEdgesIntersecting(top1, bottom2) ||
                    areEdgesIntersecting(bottom1, left2) ||
                    areEdgesIntersecting(bottom1, right2) ||
                    areEdgesIntersecting(bottom1, top2) ||
                    areEdgesIntersecting(bottom1, bottom2);
        }

        public boolean contains(Window region) {
            return isPointInWindow(new Point(region.startX, region.startY)) &&
                    isPointInWindow(new Point(region.startX, region.endY)) &&
                            isPointInWindow(new Point(region.endX,
                                    region.startY)) &&
                            isPointInWindow(new Point(region.endX,
                                    region.endY));
        }

    }


    public static class Interval {
        int start;
        int end;
        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
        public String toString() {
            return "[ " + start + " - " + end + " ]";
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }
}

