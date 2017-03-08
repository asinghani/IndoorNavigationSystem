package anish.navigationapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import anish.navigationapp.beacon.MapDownloadCallback;
import anish.navigationapp.beacon.MapDownloader;
import anish.navigationapp.location.Coordinate;
import anish.navigationapp.location.Location;
import anish.navigationapp.navigation.Map;
import anish.navigationapp.navigation.Navigation;
import anish.navigationapp.navigation.NavigationCallback;
import anish.navigationapp.userinterface.MapManager;
import anish.navigationapp.userinterface.UserInterface;

public class MainActivity extends Activity implements NavigationCallback, MapDownloadCallback, MapManager {

    public static MainActivity ctx;
    int recent = -1;
    int recent2 = -1;
    int recent3 = -1;
    boolean first = true;
    long lastTime = 0;
    long beepTime = 0;
    Navigation n;
    MapDownloader d;

    UserInterface u;

    ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 70);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;

        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (bt == null) {
            Toast.makeText(this, "No Bluetooth", Toast.LENGTH_LONG).show();
            return;
        }
        if (!bt.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        } else init();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK) init();
    }


    void init(){
        d = new MapDownloader(this);
        d.setCallback(this);
        d.firstTime = true;
        d.startScanning();

        n = new Navigation((SensorManager)getSystemService(SENSOR_SERVICE));
        n.setCallback(this);

        u = new UserInterface(this);
        u.setCallback(this);
    }

    @Override
    public Coordinate getLocation() {
        return n.loc.getLocation();
    }

    @Override
    public void enterBuilding() {
        u.enterBuilding();
    }

    @Override
    public void map(Map map) {
        if(map == null){
            if(u.isTTSReady()) u.speak("Map Download Error");
            return;
        }
        u.downloadComplete(map);
        n.setMap(map);
        startLoc();
    }

    @Override
    public void downloadError() {
        u.speak("An error has occurred while downloading the map. Automatically trying again in 5 seconds. Please do not move further into the building");
    }

    void startLoc(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Coordinate c = n.loc.getLocation();
                if(c == null || c.getX() == -1 || c.getY() == -1) startLoc();
                else u.startLocation(c);
            }
        }, 1000);
    }

    @Override
    public void navigate(Location l) {
        n.beginNavigation(l);
        first = true;
    }

    @Override
    public void move(final int direction) {
        String text = "";
        if(direction == 0) text = "Move Forward";
        else if(direction == 1) text = "Turn Right until the beep";
        else if(direction == 2) text = "Turn Left until the beep";
        else if(direction == 3) text = "Make a U-turn";

        if(!first && System.nanoTime() - lastTime > 15000000000L){
            Log.d("Time", ""+( System.nanoTime()-lastTime));
            lastTime = System.nanoTime();
            if(u.isTTSReady()) u.speak("Continue to "+text);
        }

        if(first){
            first = false;
            lastTime = System.nanoTime();
            beepTime = System.nanoTime();
            u.speak("Navigation has started");
            if(direction == 1) {
                if(u.isTTSReady()) u.speak("Move Forward");
                return;
            }
        }

        recent3 = recent2;
        recent2 = recent;
        recent = direction;

        if(direction == 0){
            if(System.nanoTime() - beepTime > 250000000L && !u.pauseSound){
                beepTime = System.nanoTime();
                try {
                    tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 50);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } else if(recent3 != direction){
            if(recent2 == direction && recent == direction) {
                if (u.isTTSReady()) u.speak(text);
                lastTime = System.nanoTime();
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.textView2))
                        .setText(String.format("Debug: orientation=%d, direction=%d, location=%s, status=%d",
                                n.loc.getOrientation(), n.direction, n.loc.getLocation().toString(), n.status));
            }
        });
    }

    @Override
    public void cancelNav() {
        n.stop();
    }

    @Override
    public void reroute() {
    }

    @Override
    public void arrived() {
        u.arrived();
    }

    @Override
    public void searching() {
        if(u.isTTSReady()) u.speak("Searching for location");
    }

    public void voiceRecognize(View v){
        if(u != null) u.btn1();
    }
}
