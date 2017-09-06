package com.wfharry.asus.blindbussystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class BusStop_page extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private SQLiteDatabase db;
    private File f = new File("/sdcard/BlindBusSystem/BlindBusSystem.db");
    Button resultBtn,searchBtn;
    int routeID;
    String route,nameZH;
    Bitmap bitmap;
    private TextToSpeech mTts;
    private boolean anotherpath =false;
//    SharedPreferences save_value = getSharedPreferences("save_value", 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop_page);
        bitmap =getLocalBitmap(this,R.drawable.busstop);
        BitmapDrawable bitmapDrawable =new BitmapDrawable(this.getResources(),bitmap);
        getWindow().setBackgroundDrawable(bitmapDrawable);
        mTts = new TextToSpeech(this, this);

        resultBtn = (Button) findViewById(R.id.result_button);
        searchBtn = (Button) findViewById(R.id.sear_button);
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
//        doGet("http://imp47.5284.com.tw/IMP/jsp/LCDStopJson.jsp?Version=2&IStopID=11730");
        db = SQLiteDatabase.openOrCreateDatabase(f,null);
        Bundle bundleMain =this.getIntent().getExtras();
        routeID = bundleMain.getInt("routeID");
        route =bundleMain.getString("route");

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (talkbackenable()){
                    Toast.makeText(getApplicationContext(), "開啟 google voice語音", Toast.LENGTH_SHORT).show();
                }
                mTts.speak("開啟 google voice語音", TextToSpeech.QUEUE_ADD, null, null);
                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent, 1);

            }
        });
        resultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resultBtn.getText().length() > 0) {
                    Intent intent = new Intent();
                    intent.setClass(BusStop_page.this, result_page.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("nameZH", nameZH);
                    bundle.putString("route",route);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
//            resultBtn.setText(resultCode);
            boolean found =false;
            Log.e("tag", "" + resultCode);
            if (resultCode == RESULT_OK){
//                Toast e =Toast.makeText(getApplicationContext(),"OK3",Toast.LENGTH_LONG);
//                e.show();
                ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                String all ="";
//                String j="";
//                for (j :text) {
//                    all = all + j +"\n";
//                }
//                resultBtn.setText(text.get(0));
                String voiceValue =text.get(0);
                voiceValue=text.get(0).replace('台', '臺');

                Cursor cur = db.rawQuery("SELECT _NameZH FROM stopName WHERE _RouteID ="+routeID,null);
                while (cur.moveToNext()){
                    if (voiceValue.equals(BusStandData.StringFilter(cur.getString(0)))){
                        nameZH =cur.getString(0);
                        anotherpath =false;
                        found =true;
                        break;
                    }
                    if (cur.isLast()){
                        resultBtn.setText("");
                        Toast.makeText(getApplicationContext(), "無此目的地，請重新查詢", Toast.LENGTH_LONG).show();
                        mTts.speak("無此目的地，請重新查詢",TextToSpeech.QUEUE_ADD,null,null);
                        break;
                    }
                }
                if (found) {
                    Log.e("found","found");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("found","doget");
                            doget("http://pda.5284.com.tw/MQS/businfo2.jsp?routename=" + route);
                        }
                    }).start();
                }

                if (found && !anotherpath){
                    resultBtn.setText(voiceValue);
                }
                mTts.speak(voiceValue,TextToSpeech.QUEUE_ADD,null,null);
                Toast h =Toast.makeText(getApplicationContext(),voiceValue,Toast.LENGTH_SHORT);
                h.show();

            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_UP){
//            Intent intent = new Intent();
//            intent.setClass(BusStop_page.this, MainActivity.class);
//            startActivity(intent);
//            Toast.makeText(getApplicationContext(), "重新選取路線", Toast.LENGTH_LONG).show();
//        }

        return super.onTouchEvent(event);
    }
    public Bitmap getLocalBitmap(Context con,int resourceId){
        InputStream inputStream =con.getResources().openRawResource(resourceId);
        return BitmapFactory.decodeStream(inputStream, null, getBitmapOptions(3));
    }
    public BitmapFactory.Options getBitmapOptions(int scale) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inSampleSize = scale;
        return options;
    }
/*
    public void  doGet(String urlstr)
    {

        arry.clear();
        URL bus = null;
        HttpURLConnection conn =null;
        Log.e("tag", "debug1");
        try {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Log.e("tag", "debug2" );
            bus = new URL(urlstr);

            conn = (HttpURLConnection) bus.openConnection();
//            conn.setReadTimeout(1000);
//            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            Log.e("tag", "debug3");
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            InputStreamReader isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
            BufferedReader in  = new BufferedReader(isr);
            String inputLine;
            while ((inputLine = in.readLine()) != null)
            {
                //System.out.println(inputLine);
                JSONArray jsonArray;
                try {

                    jsonArray = new JSONArray(inputLine);
                    List<String> list = new ArrayList<String>();
                    SharedPreferences.Editor editor =save_value.edit();
                    //System.out.println(jsonArray.length());
                    for (int i = 0; i < jsonArray.length(); i++) {

//                        JSONObject jObj = new JSONObject();
                        jObj = jsonArray.getJSONObject(i);

//                        System.out.println(jObj);

                        it = jObj.keys(); //gets all the keys

                        while(it.hasNext())
                        {
                            String key = (String) it.next(); // get key
                            Object o = jObj.get(key); // get value
                            editor.putString(key, String.valueOf(o));
                            System.out.println(key+" -> "+o);// store in session
                            if(key.equals("dest") ) {
                                arry.add(o);
                            }
                        }
                        System.out.println("---------------------------");

//				    list.add(jsonArray.getString(i));
//				    System.out.println(jsonArray.getString(i));
                    }
                    editor.commit();
                    for (Object temp :arry) {
                        System.out.println("-->"+temp);
                    }
                } catch (JSONException e) {

                    e.printStackTrace();
                }

            }

            in.close();


        }catch(Exception e) {
            Log.e("tag", "error1" );
            e.printStackTrace();
        }

    }*/

    private void doget(String urlstr){
        try {
            List<String> listName2 = new ArrayList<String>();
            List<String> listName = new ArrayList<String>();
            SharedPreferences saveData =getSharedPreferences("save_Data", Context.MODE_PRIVATE);
            String standPath =saveData.getString("save_Path", "");
            String pathTable1 ="東向西";
            String pathTable2 ="西向東";
            URL url =new URL(urlstr);
            Document doc = Jsoup.parse(url, 30000);
            // Get first table
            Element table = doc.select("table").get(3);

            // Get td Iterator
            Iterator<Element> ite = table.select("td").iterator();
            // Print content

//            Log.e("standPath",read_Data.getString("save_Path",""));
//            Log.e("standName",read_Data.getString("save_Name",""));

//            if (standPath.equals(pathTable2)){
                int cnt = 0;

                while(ite.hasNext())
                {
                    cnt++;
                    String value =BusStandData.StringFilter(ite.next().text());
                    System.out.println("Value " + cnt + ": " + value);
                    if(cnt%2==1) {
                        listName.add(value);
                    }
//                    if (cnt%2 ==0)
//                        listTime.add(value);
                }
                System.out.println("---------------------");

//            }
            System.out.println("======================");
            Element table2 = doc.select("table").get(4);
            // Get td Iterator
            Iterator<Element> ite2 = table2.select("td").iterator();
            // Print content
//            if (standPath.equals(pathTable1)) {
                int cnt2 = 0;
                while (ite2.hasNext()) {
                    cnt2++;
                    String value = BusStandData.StringFilter(ite2.next().text());
                    System.out.println("Value " + cnt2 + ": " + value);
                    if (cnt2 % 2 == 1)
                        listName2.add(value);
//                    if (cnt2 % 2 == 0)
//                        listTime2.add(value);
                }

                System.out.println("---------------------");
//            }

            if (standPath.equals(pathTable2)){
                boolean match1 =false;
                Log.e("pathTable2",match1+"");
                for (int i=0;i<listName.size();i++){
                    if (listName.get(i).equals(nameZH)){
                        match1 =true;
                        break;
                    }
                }
                if (!match1){
                    for (int i=0;i<listName2.size();i++){
                        if (listName2.get(i).equals(nameZH)){
                            BusStop_page.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    anotherpath =true;
                                    resultBtn.setText("");
                                    Toast.makeText(getApplicationContext(), nameZH + "站不在此方向，請求協助至對向搭車", Toast.LENGTH_LONG).show();
                                    mTts.speak(nameZH + "站不在此方向，請求協助至對向搭車",TextToSpeech.QUEUE_ADD,null,null);
                                }
                            });
                            break;
                        }
                    }
                }
            }

            if (standPath.equals(pathTable1)) {
                boolean match1 =false;
                Log.e("pathTable1",match1+"");
                for (int i=0;i<listName2.size();i++){
                    if (listName2.get(i).equals(nameZH)){
                        match1 =true;
                        break;
                    }
                }
                if (!match1){
                    for (int i=0;i<listName.size();i++){
                        if (listName.get(i).equals(nameZH)){
                            BusStop_page.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    anotherpath =true;
                                    resultBtn.setText("");
                                    Toast.makeText(getApplicationContext(), nameZH + "站不在此方向，請求協助至對向搭車", Toast.LENGTH_LONG).show();
                                    mTts.speak(nameZH + "站不在此方向，請求協助至對向搭車",TextToSpeech.QUEUE_ADD,null,null);
                                }
                            });
                            break;
                        }
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTts.shutdown();
        Log.e("BusStop_page", "onDestroy");
    }

    private boolean talkbackenable(){
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        boolean isAccessibilityEnabled = am.isEnabled();
        boolean isExploreByTouchEnabled = am.isTouchExplorationEnabled();
        Log.e("talkbackenable", "" + isAccessibilityEnabled);
        Log.e("talkbackenable", "" + isExploreByTouchEnabled);
        return  isExploreByTouchEnabled;
    }
    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS)
        {

            int result = mTts.setLanguage(Locale.TAIWAN);
            //設置發音語言
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            //判斷語言是否可用
            {
//                resultBtn.setEnabled(false);
            }
            else
            {
//                mTts.speak("This is an example of speech synthesis.", TextToSpeech.QUEUE_ADD, null,null);

            }
        }
    }
}
