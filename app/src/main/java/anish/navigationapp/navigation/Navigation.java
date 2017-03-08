package anish.navigationapp.navigation;

import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import anish.navigationapp.location.Coordinate;
import anish.navigationapp.location.Location;
import anish.navigationapp.location.LocationManager;

public class Navigation {
    private NavigationCallback callback;
    public LocationManager loc;
    private Map map;
    private ArrayList<Coordinate> path;
    private Coordinate end = null;
    private Location destination = null;
    public int direction;

    Timer t;

    public int status = 0; // 0 = navigate, 1 = turn

    public Navigation(SensorManager s){
        loc = LocationManager.getInstance(s);
    }

    public void beginNavigation(Location destination){
        if(callback == null) return;
        t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                checkLocation();
            }
        }, 5000, 100);
        Coordinate end = new Coordinate(destination.getX(), destination.getY());
        path = new ArrayList<>();
        this.end = end;
        this.destination = destination;
    }

    public void stop(){
        t.cancel();
        t = null;
    }

    public void checkLocation(){
            Coordinate current = loc.getLocation();
            if(current.getX() == -1 || current.getY() == -1) return;

            Log.w("Data", "Orientation: "+loc.getOrientation()+" Status: "+status+" Loc: "+current.toString()+" Dir: "+direction+" Beacon: "+map.getVal((int)current.getX(), (int)current.getY()));

            if(current.equals(new Coordinate(destination.getX(), destination.getY()))){
                callback.arrived();
                t.cancel();
                t = null;
                return;
            }

            path = Path.path(map, current, end);

            if(status == 1){
                if(direction == 0){ // Forward
                    if(range(0, loc.getOrientation(), 30)){
                        callback.move(0);
                        status = 0;
                    } else if(range(180, loc.getOrientation(), 30)){
                        callback.move(3);
                    } else {
                        int o = loc.getOrientation();
                        if (o > 0 && o < 180) {
                            callback.move(2);
                        } else {
                            callback.move(1);
                        }
                    }
                }
                if(direction == 1){ // Right
                    if(range(90, loc.getOrientation(), 30)){
                        callback.move(0);
                        status = 0;
                    } else if(range(270, loc.getOrientation(), 30)){
                        callback.move(3);
                    } else {
                        int o = loc.getOrientation();
                        if (o > 270 || o < 90) {
                            callback.move(1);
                        } else {
                            callback.move(2);
                        }
                    }
                }
                if(direction == 2){ // Left
                    if(range(270, loc.getOrientation(), 30)){
                        callback.move(0);
                        status = 0;
                    } else if(range(90, loc.getOrientation(), 30)){
                        callback.move(3);
                    } else {
                        int o = loc.getOrientation();
                        if (o > 90 && o < 270) {
                            callback.move(1);
                        } else {
                            callback.move(2);
                        }
                    }
                }
                if(direction == 3){ // Back
                    if(range(180, loc.getOrientation(), 30)){
                        callback.move(0);
                        status = 0;
                    } else if(range(0, loc.getOrientation(), 30)){
                        callback.move(3);
                    } else {
                        int o = loc.getOrientation();
                        if (o > 0 && o < 180) {
                            callback.move(1);
                        } else {
                            callback.move(2);
                        }
                    }
                }
                Log.w("Orientation", "Direction: "+loc.getOrientation());
                return;
            }

            if(!path.contains(current)){
                path = Path.path(map, current, end);
                callback.reroute();
            }
            for(int i = 0; i < path.size(); i++){
                Coordinate c = path.get(i);
                if(!c.equals(current)) path.remove(c);
            else{
                path.remove(c);
                break;
            }
        }
        if(path.isEmpty()){
            Log.e("Path", "Empty");
            path = Path.path(map, current, end);
            callback.reroute();
            checkLocation();
            return;
        }
        Coordinate next = path.get(0);
        if(next.getX() > current.getX()) direction = 1;
        else if(next.getX() < current.getX()) direction = 2;
        else if(next.getY() > current.getY()) direction = 3;
        else if(next.getY() < current.getY()) direction = 0;
        else{
            direction = -1;
            Log.e("ERROR", String.format("Error, (%d, %d) -> (%d, %d)", current.getX(), current.getY(), next.getX(), next.getY()));
            return;
        }
        if(direction == 0){ // Forward
            if(range(0, loc.getOrientation(), 30)){
                callback.move(0);
                status = 0;
            } else if(range(180, loc.getOrientation(), 30)){
                callback.move(3);
                status = 1;
            } else {
                int o = loc.getOrientation();
                if (o > 0 && o < 180) {
                    callback.move(2);
                } else {
                    callback.move(1);
                }
                status = 1;
            }
        }
        if(direction == 1){ // Right
            if(range(90, loc.getOrientation(), 30)){
                callback.move(0);
                status = 0;
            } else if(range(270, loc.getOrientation(), 30)){
                callback.move(3);
                status = 1;
            } else {
                int o = loc.getOrientation();
                if (o > 270 || o < 90) {
                    callback.move(2);
                } else {
                    callback.move(1);
                }
                status = 1;
            }
        }
        if(direction == 2){ // Left
            if(range(270, loc.getOrientation(), 30)){
                callback.move(0);
                status = 0;
            } else if(range(90, loc.getOrientation(), 30)){
                callback.move(3);
                status = 1;
            } else {
                int o = loc.getOrientation();
                if (o > 90 || o < 270) {
                    callback.move(2);
                } else {
                    callback.move(1);
                }
                status = 1;
            }
        }
        if(direction == 3) { // Back
            if (range(180, loc.getOrientation(), 30)) {
                callback.move(0);
                status = 0;
            } else if (range(0, loc.getOrientation(), 30)) {
                callback.move(3);
                status = 1;
            } else {
                int o = loc.getOrientation();
                if (o > 0 && o < 180) {
                    callback.move(2);
                } else {
                    callback.move(1);
                }
                status = 1;
            }
        }
    }

    public void setMap(Map map){
        this.map = map;
        loc.setMap(map);
    }

    public void setCallback(NavigationCallback c) {
        this.callback = c;
    }

    boolean range(int a, int b, int lim){
        int a1 = a+lim;
        int a2 = a-lim;

        if(a1 > 0 && a2 < 0){
            Log.d("Range", a1+", "+a2+" - "+b);
            if(a1 >= b || a2+360 <= b) return true;
        }

        Log.d("Range", a1+", "+a2+" - "+b);
        return (a1 >= b) && (a2 <= b);
    }
}
