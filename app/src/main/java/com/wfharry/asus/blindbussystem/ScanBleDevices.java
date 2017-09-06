package com.wfharry.asus.blindbussystem;

/**
 * Created by asus on 2016/3/3.
 */
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

public class ScanBleDevices extends Thread {

    final int MSG_START_SCAN = 1000;
    final int MSG_STOP_SCAN = 1001;

    final int TIME_FOR_SCAN = 5000;
    final int TIME_FOR_STOP_SCAN = 5000;

    Handler handlerForReceiveIp;

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    //private boolean mScanning;
    //private static final long SCAN_PERIOD = 10000;

    boolean mIsScanning	= false;

    public BeaconData BusStandBeacon;

    Handler mHandler= new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case MSG_START_SCAN:
                    this.sendEmptyMessageDelayed(MSG_STOP_SCAN, TIME_FOR_STOP_SCAN);

                    if(!mIsScanning && bluetoothAdapter.isEnabled())
                    {
                        mIsScanning = true;
                        bluetoothAdapter.startLeScan(leScanCallback);

//						bleDevices.clear();
                    }
                    break;

                case MSG_STOP_SCAN:
                    this.sendEmptyMessageDelayed(MSG_START_SCAN, TIME_FOR_SCAN);

                    if(mIsScanning && bluetoothAdapter.isEnabled())
                    {
                        mIsScanning = false;
                        bluetoothAdapter.stopLeScan(leScanCallback);


                        if(BusStandBeacon!=null){
                            if(System.currentTimeMillis() - BusStandBeacon.TimeMillis > 10000)
                            {
                                BusStandBeacon = null;

                                Message message = new Message();
                                message.what = BusStandApplication.MSG_LEAVE_BEACON;
                                handlerForReceiveIp.sendMessage(message);
                            }
                        }

                    }
                    break;

            }

            super.handleMessage(msg);
        }
    };

//	ArrayList<QBeeDevice> bleDevices;

    public ScanBleDevices(final Handler handler) {
        // TODO Auto-generated constructor stub
        this.handlerForReceiveIp = handler;

//		bleDevices = new ArrayList<QBeeDevice>();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        //super.run();
        mHandler.sendEmptyMessage(MSG_START_SCAN);
    }

    public void stopSearch(){
        mIsScanning = false;
        mHandler.removeMessages(MSG_START_SCAN);
        mHandler.removeMessages(MSG_STOP_SCAN);
        bluetoothAdapter.stopLeScan(leScanCallback);
    }

    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        //ArrayList<String> gotDeviceNames = new ArrayList<String>();
        //ArrayList<String> bondedDeviceNames = new ArrayList<String>();
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            BeaconData Beacon = new BeaconData(device, rssi, scanRecord, System.currentTimeMillis());
            //if(Beacon!=null){
            if( Beacon.Major!=-1&&Beacon.Minor!=-1 ){
                BusStandBeacon = Beacon;

                Message message = new Message();
                message.what = BusStandApplication.MSG_FOUND_BEACON;
                message.obj = BusStandBeacon;
                handlerForReceiveIp.sendMessage(message);
            }

        }


    };

}
