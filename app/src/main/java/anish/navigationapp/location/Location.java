package anish.navigationapp.location;


public class Location {
    private long x;
    private long y;
    private String name;
    private String desc;

    private int type; // 0 = exit, 1 = location

    public Location(long x, long y, long type, String name, String desc) {
        this.x = x;
        this.y = y;
        this.type = (int) type;
        this.name = type == 1 ? name : "Exit";
        this.desc = type == 1 ? desc : "";
    }

    public Location(long x, long y, long type, String name) {
        this.x = x;
        this.y = y;
        this.type = (int) type;
        this.name = type == 1 ? name : "Exit";
        this.desc = type == 1 ? "No description available" : "";
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d): %s %s", x, y, name, type == 1 ? "("+desc+")" : "");
    }
}
