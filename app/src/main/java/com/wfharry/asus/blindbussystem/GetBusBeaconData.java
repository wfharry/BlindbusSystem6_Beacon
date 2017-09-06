package com.wfharry.asus.blindbussystem;

/**
 * Created by asus on 2016/3/3.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Handler;
import android.os.Message;

public class GetBusBeaconData extends Thread {

    Handler handler;
    String url;

    public GetBusBeaconData(){}

    public GetBusBeaconData(Handler handler, String url){
        this.handler = handler;
        this.url = url;
    }

    @Override
    public void run() {
        super.run();

        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(this.url).openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("GET");

            if(httpURLConnection.getResponseCode()==200){
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder(bufferedReader.readLine());

                Message message = new Message();
                message.what = BusStandApplication.HTTP_FLOW_COMPLETE;
                message.obj = result.toString();
                handler.sendMessage(message);

            }
            else{

                Message message = new Message();
                message.what = BusStandApplication.HTTP_FLOW_ERROR;
                handler.sendMessage(message);

            }
        }
        catch (IOException e) {

            Message message = new Message();
            message.what = BusStandApplication.HTTP_FLOW_ERROR;
            handler.sendMessage(message);
            e.printStackTrace();
            return;

        }

    }

}
