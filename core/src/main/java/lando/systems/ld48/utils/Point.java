package lando.systems.ld48.utils;

public class Point {

    public static Point zero() {
        return new Point();
    }

    public static Point at(int x, int y) {
        return new Point(x, y);
    }

    public int x;
    public int y;

    // must be public for Json deserialization
    public Point() {
        this(0, 0);
    }

    private Point(int x, int y) {
        set(x, y);
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean is(int x, int y) {
        return (this.x == x && this.y == y);
    }

    public boolean is(Point point) {
        return is(point.x, point.y);
    }

    public Point add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Point add(Point point) {
        this.x += point.x;
        this.y += point.y;
        return this;
    }

    public Point sub(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Point sub(Point point) {
        this.x -= point.x;
        this.y -= point.y;
        return this;
    }

    public Point mul(int s) {
        this.x *= s;
        this.y *= s;
        return this;
    }

    public Point div(int s) {
        this.x /= s;
        this.y /= s;
        return this;
    }

}
