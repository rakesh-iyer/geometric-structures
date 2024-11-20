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
    }
}
