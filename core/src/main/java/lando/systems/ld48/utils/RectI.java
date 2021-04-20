package lando.systems.ld48.utils;

public class RectI {

    public int x;
    public int y;
    public int w;
    public int h;

    public RectI() {}

    private RectI(RectI other) {
        set(other);
    }

    private RectI(int x, int y, int w, int h) {
        set(x, y, w, h);
    }

    public static RectI at(RectI other) {
        return new RectI(other);
    }

    public static RectI at(int x, int y, int w, int h) {
        return new RectI(x, y, w, h);
    }

    public void set(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void set(RectI other) {
        set(other.x, other.y, other.w, other.h);
    }

    public boolean overlaps(RectI other) {
        return x < other.x + other.w
            && other.x < x + w
            && y < other.y + other.h
            && other.y < y + h;
    }

    public int left()   { return x; }
    public int right()  { return x + w; }
    public int top()    { return y; }
    public int bottom() { return y + h; }

}
