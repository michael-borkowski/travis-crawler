package at.borkowski.traviscrawler.analysis;

import java.util.LinkedList;
import java.util.List;

public class Scatterplot {

    private List<Point> points = new LinkedList<>();

    public Scatterplot() {

    }

    public List<Point> getPoints() {
        return points;
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public static class Point {
        private long x, y;

        public Point(long x, long y) {
            this.x = x;
            this.y = y;
        }

        public long getX() {
            return x;
        }

        public long getY() {
            return y;
        }
    }
}
