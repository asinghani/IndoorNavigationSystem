package anish.navigationapp.userinterface;

import anish.navigationapp.location.Coordinate;
import anish.navigationapp.location.Location;

public interface MapManager {
    void navigate(Location l);
    Coordinate getLocation();
    void cancelNav();
}
