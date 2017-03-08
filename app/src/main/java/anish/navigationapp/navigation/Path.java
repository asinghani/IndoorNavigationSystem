package anish.navigationapp.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;

import anish.navigationapp.location.Coordinate;
import anish.navigationapp.location.PriorityCoordinate;

public class Path {

    public static ArrayList<Coordinate> path(Map map, Coordinate start, Coordinate end){
        PriorityQueue<PriorityCoordinate> frontier = new PriorityQueue<>();
        HashMap<Coordinate, Coordinate> prev = new HashMap<>();
        HashMap<Coordinate, Integer> totalCost = new HashMap<>();

        frontier.add(new PriorityCoordinate(start, 0));
        prev.put(start, null);
        totalCost.put(start, 0);

        while(!frontier.isEmpty()){
            PriorityCoordinate current = frontier.poll();

            if(current.equals(end)) break;

            for(Coordinate n : map.getNeighbors(current)){
                int cost = totalCost.get(start) + 1;
                if(!totalCost.containsKey(n) || cost < totalCost.get(n)){
                    if(totalCost.containsKey(n)) totalCost.remove(n);
                    totalCost.put(n, cost);
                    int priority = cost + heuristic(end, n);
                    frontier.add(new PriorityCoordinate(n, priority));
                    if(prev.containsKey(n)) prev.remove(n);
                    prev.put(n, current);
                }
            }
        }

        ArrayList<Coordinate> path = new ArrayList<>();
        Coordinate p = end;
        while (p != null){
            path.add(p);
            p = prev.get(p);
        }

        Collections.reverse(path);

        return path;
    }

    private static int heuristic(Coordinate destination, Coordinate start){
        return (int) (Math.abs(destination.getX() - start.getX()) + Math.abs(destination.getY() - start.getY())); // Manhattan distance
    }
}
