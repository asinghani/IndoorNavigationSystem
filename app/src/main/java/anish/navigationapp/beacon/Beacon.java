package anish.navigationapp.beacon;

import android.util.Log;

import java.io.Serializable;
import java.util.UUID;

public class Beacon implements Serializable{
    private static final long serialVersionUID = 3L;

    private int rssi;
    private int calibratedRssi;
    private int major;
    private int minor;
    private UUID uuid;

    public Beacon(int rssi, int calibratedRssi, int major, int minor, UUID uuid) {
        this.rssi = rssi;
        this.calibratedRssi = calibratedRssi;
        this.major = major;
        this.minor = minor;
        this.uuid = uuid;
    }

    public int getRssi() {
        return rssi;
    }

    public int getCalibratedRssi() {
        return calibratedRssi;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void log(int priority){
        Log.println(priority, "Beacon", toString());
    }

    @Override
    public String toString(){
        return String.format("Beacon: UUID=%s, Major=%d, Minor=%d, RSSI=%d",
                uuid.toString().toUpperCase(), major, minor, rssi);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Beacon)) return false;
        Beacon b = (Beacon)o;
        if(b.getMinor() == getMinor() && b.getMajor() == getMajor() && b.getUuid().equals(getUuid())){
            return true;
        }
        return false;
    }
}
