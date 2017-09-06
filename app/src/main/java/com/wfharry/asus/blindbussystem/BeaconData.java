package com.wfharry.asus.blindbussystem;

/**
 * Created by asus on 2016/3/3.
 */
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class BeaconData {

    public final static String TAG = BeaconData.class.getSimpleName();

    public String DeviceName = "";
    public String Mac;
    public String UUID;
    public int Major = -1;
    public int Minor = -1;
    public int TxPower;
    public int Rssi;
    public double Distance;
    public long TimeMillis;

    public static final String JSON_TAG_BEACON_DEVICENAME = "devicename";
    public static final String JSON_TAG_BEACON_MAJOR = "major";
    public static final String JSON_TAG_BEACON_MINOR = "minor";
    public static final String JSON_TAG_BEACON_MAC = "mac";
    public static final String JSON_TAG_BEACON_UUID = "uuid";

    public BeaconData(){}

    public BeaconData(final BluetoothDevice device, final int rssi, final byte[] scanRecord, final long time){
        generateBeacon(device, rssi, scanRecord, time);
    }

    public BeaconData generateBeacon(final BluetoothDevice device, final int rssi, final byte[] scanRecord, final long time){

        int startByte = 2;
        boolean patternFound = false;
        // 寻找ibeacon
        // 先依序尋找第2到第8陣列的元素
        while (startByte <= 5) {
            // Identifies an iBeacon
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 &&
                    // Identifies correct data length
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15)
            {

                patternFound = true;
                break;
            }
            startByte++;
        }

        // 如果找到了的话
        if (patternFound) {
            // 轉換16進制
            byte[] uuidBytes = new byte[16];
            // 來源、起始位置
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
            String hexString = bytesToHex(uuidBytes);

            if(device.getName()!=null)
                DeviceName = device.getName();

            // ibeacon的UUID值
            UUID = hexString.substring(0, 8) + "-"
                    + hexString.substring(8, 12) + "-"
                    + hexString.substring(12, 16) + "-"
                    + hexString.substring(16, 20) + "-"
                    + hexString.substring(20, 32);

            // ibeacon的Major值
            Major = (scanRecord[startByte + 20] & 0xff) * 0x100
                    + (scanRecord[startByte + 21] & 0xff);

            // ibeacon的Minor值
            Minor = (scanRecord[startByte + 22] & 0xff) * 0x100
                    + (scanRecord[startByte + 23] & 0xff);

            Mac = device.getAddress();

            TxPower = (scanRecord[startByte + 24]);

            this.Rssi = rssi;

            Distance = calculateAccuracy(TxPower,this.Rssi);

            TimeMillis = time;

            //Log.e(TAG,bytesToHex(scanRecord));
            //Log.e(TAG, "Name：" + DeviceName + "\nMac：" + Mac
            //        + " \nUUID：" + UUID + "\nMajor：" + Major + "\nMinor："
            //        + Minor + "\nTxPower：" + TxPower + "\nrssi：" + this.Rssi);

            //Log.e(TAG,"distance："+Distance);

            return this;
        }
        else{
            Log.e(TAG,"not beacon");
            return null;
        }
    }

    public BeaconData updateTimeMillis(final long time){
        this.TimeMillis = time;
        return this;
    }

    //	將來源轉換為16進制
    public String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    //	計算距離 :
//	此方法是"即時"計算，所以很容易有大幅度的波動
//	建議是 : 累加後均分這樣穩定度相對的高( 收集約 15 次以上後均分 )
    public double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0)
        {
            return -1.0;
        }

        double ratio = rssi * 1.0 / txPower;

        if (ratio < 1.0)
        {
            return Math.pow(ratio, 10);
        }
        else
        {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

    @Override
    public boolean equals(Object o) {

        if( this.Mac.equals(((BeaconData)o).Mac) ){
            return true;
        }
        else{
            return false;
        }

        //return super.equals(o);
    }

    public BeaconData(final JSONObject beaconJsonObject) throws JSONException{
//        String br1="";
        this.DeviceName = beaconJsonObject.getString(JSON_TAG_BEACON_DEVICENAME);
//        this.DeviceName =br1.substring(0,beaconJsonObject.getString(JSON_TAG_BEACON_DEVICENAME).length()-18);
//        Log.e("DeviceName",this.DeviceName);
        this.Major = beaconJsonObject.getInt(JSON_TAG_BEACON_MAJOR);
        this.Minor = beaconJsonObject.getInt(JSON_TAG_BEACON_MINOR);
        this.Mac = beaconJsonObject.getString(JSON_TAG_BEACON_MAC);
        this.UUID = beaconJsonObject.getString(JSON_TAG_BEACON_UUID);
    }

    public final JSONObject toJsonObject() throws JSONException{
        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put(JSON_TAG_BEACON_DEVICENAME, this.DeviceName);
        resultJsonObject.put(JSON_TAG_BEACON_MAJOR, this.Major);
        resultJsonObject.put(JSON_TAG_BEACON_MINOR, this.Minor);
        resultJsonObject.put(JSON_TAG_BEACON_MAC, this.Mac);
        resultJsonObject.put(JSON_TAG_BEACON_UUID, this.UUID);
        return resultJsonObject;
    }

}
