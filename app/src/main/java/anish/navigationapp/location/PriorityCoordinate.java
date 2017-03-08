package anish.navigationapp.location;

public class PriorityCoordinate extends Coordinate implements Comparable<PriorityCoordinate> {

    int priority;

    public PriorityCoordinate(Coordinate c, int priority){
        super(c.getX(), c.getY());
        this.priority = priority;
    }

    @Override
    public int compareTo(PriorityCoordinate other){
        if(this.priority > other.priority) return 1;
        else if(this.priority < other.priority) return -1;
        else return 0;
    }
}
