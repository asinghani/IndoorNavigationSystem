package anish.navigationapp.beacon;

import android.app.Activity;
import android.util.Log;

import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanDiscoveryListener;
import com.punchthrough.bean.sdk.BeanListener;
import com.punchthrough.bean.sdk.BeanManager;
import com.punchthrough.bean.sdk.message.BeanError;
import com.punchthrough.bean.sdk.message.ScratchBank;

import java.util.ArrayList;

import anish.navigationapp.navigation.Map;

public class MapDownloader implements BeanDiscoveryListener, BeanListener {
    private ArrayList<Bean> deviceList = new ArrayList<>();
    private Activity ctx;
    private Bean b;
    private String data;
    private Map m;
    private MapDownloadCallback callback;
    private boolean mapFound = false;
    public boolean firstTime = true;


    public MapDownloader(Activity ctx){
        this.ctx = ctx;
    }

    public void setCallback(MapDownloadCallback c){
        this.callback = c;
    }

    public void startScanning(){
        BeanManager.getInstance().cancelDiscovery();
        BeanManager.getInstance().startDiscovery(this);
        mapFound = false;
    }

    private void downloadMap(){
        b.connect(ctx.getApplicationContext(), this);
    }

    @Override
    public void onConnected() {
        if(callback != null) {
            if(firstTime){
                callback.enterBuilding();
                firstTime = false;
            }
        }
        b.endSerialGate();
        b.sendSerialMessage("c".getBytes());
        data = "";
    }

    @Override
    public void onSerialMessageReceived(byte[] data) {
        this.data += new String(data);
        if(this.data.length() % 100 == 0) Log.w("MESSAGE", this.data);
    }

    @Override
    public void onDisconnected() {
        Log.i("downloaded", data);
        try {
            m = Map.fromString(data);
            if(m == null) throw new Exception();
            if(callback != null) callback.map(m);
        } catch (Exception e){
            e.printStackTrace();
            this.startScanning();
            callback.downloadError();
        }
    }

    @Override
    public void onError(BeanError error) {
        mapFound = false;
    }

    @Override
    public void onConnectionFailed() {
        Log.e("ERROR", "connection failed");
        this.startScanning();
    }

    @Override
    public void onBeanDiscovered(Bean bean, int rssi) {
        Log.d("bean", bean.getDevice().getName()+" rssi="+rssi);
        if(rssi > -78){
            Log.d("bean", "range");
            if(bean.getDevice().getName().contains("NavB")){
                if(!mapFound && m == null){
                    mapFound = true;
                    Log.d("bean", "valid");
                    BeanManager.getInstance().cancelDiscovery();
                    this.b = bean;
                    downloadMap();
                }
            }
        }
    }

    @Override
    public void onDiscoveryComplete() {
        if(!mapFound){
            startScanning();
            Log.e("NOT found", "map not yet found");
        }
    }

    @Override
    public void onScratchValueChanged(ScratchBank bank, byte[] value) {

    }
}
