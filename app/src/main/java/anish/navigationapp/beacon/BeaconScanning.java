package anish.navigationapp.beacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class BeaconScanning {
    private static BeaconScanning instance;
    private BluetoothLeScanner scanner;
    private ScanSettings settings;

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            getData(result.getScanRecord(), result.getRssi());
        }
    };

    private BeaconCallback callback;

    private BeaconScanning(){
        super();
        configure(BluetoothAdapter.getDefaultAdapter());
        Log.i("Scanner config", "Scanning config");
    }

    private void getData(ScanRecord record, int rssi){
        if(record == null) return;
        byte[] bytes = record.getBytes();
        if(bytes[0] != (byte)0x02) return;
        if(bytes[1] != (byte)0x01) return;
        if(bytes[2] != (byte)0x06) return;
        if(bytes[3] != (byte)0x1A) return;
        if(bytes[4] != (byte)0xFF) return;
        if(bytes[5] != (byte)0x4C) return;
        if(bytes[6] != (byte)0x00) return;
        if(bytes[7] != (byte)0x02) return;
        if(bytes[8] != (byte)0x15) return;
        byte[] uuidBytes = Arrays.copyOfRange(bytes, 9, 25);
        ByteBuffer uuidBuffer = ByteBuffer.wrap(uuidBytes);
        UUID uuid = new UUID(uuidBuffer.getLong(), uuidBuffer.getLong());
        if(!uuid.equals(UUID.fromString("F58904C6-F94B-4E64-AD07-0AE360597479"))) return;
        int major = (bytes[26] | bytes[25] << 8);
        int minor = (bytes[28] | bytes[27] << 8);
        int calibratedRssi = bytes[29];
        Beacon beacon = new Beacon(rssi, calibratedRssi, major, minor, uuid);
        if(callback != null) callback.beaconFound(beacon);
    }

    public BeaconScanning setCallback(BeaconCallback callback) {
        this.callback = callback;
        return this;
    }

    private void configure(BluetoothAdapter adapter){
        scanner = adapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    }

    public void startScan(){
        scanner.startScan(null, settings, scanCallback);
    }

    public void stopScan(){
        scanner.stopScan(scanCallback);
    }

    public static BeaconScanning getInstance(){
        if(instance != null) return instance;
        else{
            instance = new BeaconScanning();
            return instance;
        }
    }
}
