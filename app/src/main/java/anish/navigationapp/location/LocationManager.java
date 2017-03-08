package anish.navigationapp.location;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import anish.navigationapp.MainActivity;
import anish.navigationapp.beacon.BeaconManager;
import anish.navigationapp.navigation.Map;

public class LocationManager implements SensorEventListener {
    private int x = -1;
    private int y = -1;
    private int orientation;
    private SensorManager sensorManager;

    BeaconManager m;
    Map map;

    private LocationManager(SensorManager s){
        m = BeaconManager.getInstance();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                MainActivity.ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        location();
                    }
                });
            }
        }, 20, 20);
        this.sensorManager = s;
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Coordinate getLocation(){
        return new Coordinate(x, y);
    }

    public void location(){
        if(map == null) return;
        java.util.Map<Integer, Integer> rssi = m.getRssiVal();
        Log.i("rssi", ""+rssi);
        if(rssi.size() < 3) return;
        Iterator<Integer> iterator = rssi.keySet().iterator();

        int max = 0;
        int maxRssi = -200;

        while(iterator.hasNext()){
            int minor = iterator.next();
            int rssiVal = rssi.get(minor);
            if(rssiVal > maxRssi) {
                maxRssi = rssiVal;
                max = minor;
            }
        }

        int beacon = max;
        Log.i("Beacon", ""+beacon);
        for(int x = 0; x < map.getRows(); x++){
            for(int y = 0; y < map.getCols(); y++){
                if(map.getVal(x,y) == beacon){
                    this.x = x;
                    this.y = y;
                    return;
                }
            }
        }
    }

    public int getOrientation() {
        return orientation;
    }

    private static LocationManager inst;
    public static LocationManager getInstance(SensorManager s){
        if (inst == null) inst = new LocationManager(s);
        return inst;
    }

    public static LocationManager getInstance(){
        return inst;
    }

    public Map getMap() {
        return map;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    float[] accel;
    float[] compass;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(map == null) return;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            accel = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            compass = event.values;
        if (accel != null && compass != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            if (SensorManager.getRotationMatrix(R, I, accel, compass)) {
                int tempOrientation;
                float orientationA[] = new float[3];
                SensorManager.getOrientation(R, orientationA);

                if (Math.toDegrees(orientationA[0]) > 0){
                    tempOrientation = (int) Math.toDegrees(orientationA[0]) - map.getDirectionOffset();
                    tempOrientation = 360 + tempOrientation;
                    if(tempOrientation > 360) tempOrientation = tempOrientation - 360;
                } else{
                    tempOrientation = (int) (360+Math.toDegrees(orientationA[0])) - map.getDirectionOffset();
                    tempOrientation = 360+tempOrientation;
                    if(tempOrientation > 360) tempOrientation = tempOrientation - 360;
                }
                this.orientation = tempOrientation;
            }
        }
    }
}
