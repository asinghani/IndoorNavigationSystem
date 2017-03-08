package anish.navigationapp.location;

public class Coordinate {
    long x;
    long y;

    public Coordinate(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        Coordinate c;
        if(o instanceof Coordinate) c = (Coordinate) o;
        else return false;

        return c.getX() == getX() && c.getY() == getY();
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

    @Override
    public int hashCode() {
        return (int) (getX()*100000 + getY());
    }
}
