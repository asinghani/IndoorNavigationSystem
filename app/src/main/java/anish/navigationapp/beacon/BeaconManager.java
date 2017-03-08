package anish.navigationapp.beacon;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class BeaconManager implements BeaconCallback{

    private static BeaconManager instance;
    private BeaconScanning scanner;

    private HashMap<Integer, ArrayList<Integer>> beaconValues;
    private HashMap<Integer, Integer> averageRssi;

    static final int AVERAGE_AMOUNT = 15;

    private BeaconManager(){
        scanner = BeaconScanning.getInstance();
        Log.i("Scanner started", "Scanning started");
        scanner.startScan();
        scanner.setCallback(this);
        beaconValues = new HashMap<>();
        averageRssi = new HashMap<>();
    }

    @Override
    public void beaconFound(Beacon beacon) {
        int index = beacon.getMinor();
        addBeacon(index, beacon.getRssi());
        Log.i("BEACON", ""+index);
    }

    private void calculateAverage(int index) {
        ArrayList<Integer> temp = new ArrayList<>(AVERAGE_AMOUNT);
        temp.addAll(beaconValues.get(index));
        temp.remove(0);
        temp.remove(AVERAGE_AMOUNT - 2);

        int sum = 0;
        for(int i : temp){
            sum += i;
        }
        sum = sum / (AVERAGE_AMOUNT-2);
        if(averageRssi.containsKey(index)) averageRssi.remove(index);
        averageRssi.put(index, sum);
        Log.i("VALUE AVERAGE", averageRssi.toString());
    }

    private void addBeacon(int index, int rssi){
        if(beaconValues.containsKey(index)){
            ArrayList<Integer> rssiVal = beaconValues.get(index);
            rssiVal.add(rssi);
            if(rssiVal.size() > AVERAGE_AMOUNT){
                rssiVal.remove(0);
                calculateAverage(index);
            }
        }else{
            beaconValues.put(index, new ArrayList<Integer>(AVERAGE_AMOUNT+1));
        }
        Log.i("VALUE BEACONS", beaconValues.toString());
    }

    public java.util.Map<Integer, Integer> getRssiVal() {
        return averageRssi;
    }

    public static BeaconManager getInstance(){
        if(instance == null){
            instance = new BeaconManager();
        }
        return instance;
    }
}
