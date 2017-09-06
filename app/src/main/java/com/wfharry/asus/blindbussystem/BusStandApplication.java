package com.wfharry.asus.blindbussystem;

/**
 * Created by asus on 2016/3/3.
 */
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Message;
import android.util.Log;
import android.os.Handler;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class BusStandApplication extends Application {

    public static final String TAG = BusStandApplication.class.getSimpleName();

    ScanBleDevices scanBeacon;
    ArrayList<CallBack> callBacks = new ArrayList<CallBack>();

    public static final int MSG_FOUND_BEACON = 1111;
    public static final int MSG_LEAVE_BEACON = 2222;

    public static final int HTTP_FLOW_COMPLETE = 6666;
    public static final int HTTP_FLOW_ERROR = 7777;

    public static boolean BeaconStatus = false;
    private boolean temp =true;

    public ArrayList<BusStandData> busStandDatas = new ArrayList<BusStandData>();

    public BusStandData MyBusStand;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "onCreate()");

        Thread checkBusStand = new GetBusBeaconData(myHandler, "http://paas.cmoremap.com.tw/askey_taipei_bus/askey_beacon_json.php");
        checkBusStand.start();

        //scanBeacon = new ScanBleDevices(myHandler);
        //scanBeacon.start();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.e(TAG,"onConfigurationChanged()");

    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        Log.e(TAG,"onTerminate()");
    }

    private Handler myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            SharedPreferences save_data =getSharedPreferences("save_Data", Context.MODE_PRIVATE);
            save_data.edit().putBoolean("BeaconStatus",BeaconStatus).commit();
            save_data.getBoolean("BeaconStatus", false);
            Log.e("BeaconStatus",""+BeaconStatus);
            switch(msg.what){
                case MSG_FOUND_BEACON:
                    save_data.edit().putBoolean("save_Message",true).commit();
                    if (!BeaconStatus && temp ){
                        BeaconStatus =temp;
                        save_data.edit().putBoolean("BeaconStatus",BeaconStatus).commit();
                    }

                    Log.e(TAG,"MSG_FOUND_BEACON");
                    Log.e(TAG,"Mac : " + ((BeaconData)msg.obj).Mac);
                    Log.e(TAG,"Major : " + ((BeaconData)msg.obj).Major);
                    Log.e(TAG,"Minor : " + ((BeaconData)msg.obj).Minor);

                    if(busStandDatas.size()!=0){
                        for(BusStandData busstand : busStandDatas){
                            if( busstand.Major==((BeaconData)msg.obj).Major &&
                                    busstand.Minor==((BeaconData)msg.obj).Minor ){
                                MyBusStand = busstand;

                                for(CallBack callBack : callBacks){
                                    callBack.onFoundBusStand(MyBusStand);
                                }
                            }
                        }
                    }

                    break;
                case MSG_LEAVE_BEACON:

                    Log.e(TAG,"MSG_LEAVE_BEACON");

                    SharedPreferences data = getSharedPreferences("save_Data", Context.MODE_PRIVATE);
                    data.edit().putBoolean("BeaconStatus",false).commit();

                    MyBusStand = null;
                    for(CallBack callBack : callBacks){
                        callBack.onLeaveBusStand();
                    }

                    break;
                case HTTP_FLOW_COMPLETE:

                    Log.e(TAG,"HTTP_FLOW_COMPLETE");
                    Log.e(TAG,(String)msg.obj);

                    try {
                        JSONArray jsonArray = new JSONArray((String)msg.obj);

                        for(int i=0; i<jsonArray.length(); i++){
                            BusStandData busStand = new BusStandData((JSONObject)jsonArray.get(i));
                            busStandDatas.add(busStand);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    scanBeacon = new ScanBleDevices(myHandler);
                    scanBeacon.start();

                    break;
                case HTTP_FLOW_ERROR:

                    Log.e(TAG,"HTTP_FLOW_ERROR");

                    break;
            }

            return true;
        }
    });

    public void StopApplication(){

        if(scanBeacon!=null)
            scanBeacon.stopSearch();



        this.callBacks.clear();
        this.busStandDatas.clear();

    }




    public final void registerCallBack(final CallBack newCallBack){
        if(!this.callBacks.contains(newCallBack)){
            this.callBacks.add(newCallBack);
        }
    }

    public final void unRegisterCallBack(final CallBack oldCallBack){
        if(this.callBacks.contains(oldCallBack)){
            this.callBacks.remove(oldCallBack);
        }
    }

    public interface CallBack{
        public void onFoundBusStand(BusStandData bustand);
        public void onLeaveBusStand();
    }

}
