package com.wfharry.asus.blindbussystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

public class result_page extends AppCompatActivity implements TextToSpeech.OnInitListener,Serializable{
    private Timer mTimer;

//    BusStandData standData;
    MainActivity BusData;
    List<String> listName2 = new ArrayList<String>();
    List<String> listTime2 = new ArrayList<String>();
    List<String> listName = new ArrayList<String>();
    List<String> listTime = new ArrayList<String>();
    String route,nameZH;
    public static String first ="common_Data_First";
    public static String second ="common_Data_Second";
    public static String third ="common_Data_Third";
    public static String save_Name="save_Name";
    public static String save_Path ="save_Path";
    private String standPath;
    private String standName;
    TextView resultView;
    private TextToSpeech mTts;
    private static final int REQ_TTS_STATUS_CHECK = 0;
    private static final String TAG = "TTS Demo";
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_page);
        bitmap =getLocalBitmap(this,R.drawable.resultroute);
        BitmapDrawable bitmapDrawable =new BitmapDrawable(this.getResources(),bitmap);
        getWindow().setBackgroundDrawable(bitmapDrawable);
        resultView = (TextView) findViewById(R.id.result_View);
        SharedPreferences read_Data =getSharedPreferences("save_Data", Context.MODE_PRIVATE);
        standName =read_Data.getString(save_Name, "");
        standPath =read_Data.getString(save_Path, "");

//        Intent checkIntent = new Intent();
//        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//        startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);

        mTts = new TextToSpeech(this, this);

        mTimer = new Timer();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        getData();


        setTimerTask();

    }


    private void setTimerTask(){
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String url = "http://pda.5284.com.tw/MQS/businfo2.jsp?routename=";
                doGet(url + route);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mTts.speak(resultView.getText().toString(), TextToSpeech.QUEUE_ADD, null, null);
                Log.e("timeDelay", "update...");
            }
        }, 2000, 60000/* 表示1000毫秒之後，每隔60000毫秒執行一次 */);
    }

    private void stopTimer(){
        if (mTimer !=null){
            mTimer.cancel();
            mTimer =null;
        }
    }
    private void getData(){
        Bundle bundleMain =this.getIntent().getExtras();
        nameZH =bundleMain.getString("nameZH");
        route =bundleMain.getString("route");

    }


    private   String doGet(String urlstr){

        SharedPreferences commonData = getSharedPreferences("save_Data", Context.MODE_PRIVATE);

        String data =route+"/"+" 公車"+"/"+"往 "+"/"+nameZH+"/"+"站"+"/"+standPath+"/"+standName;

        ArrayList data2 = new ArrayList();

//        commonData.edit().putString("common_Data",data).commit();
        ArrayList commData =new ArrayList();

        ArrayList Flag =new ArrayList(Arrays.asList(first,second,third));

        for (int i=0;i<3;i++) {
            if ((commonData.getString(Flag.get(i).toString(),"").equals(null))){
                commData.add(i,"");
//                commData2.add(i,"");
            }
            commData.add(i,commonData.getString(Flag.get(i).toString(), ""));
//            commData2.add(i,commonData.getStringSet(Flag_data.get(i).toString(), null));

        }
//        firstSet =commonData.getStringSet(first_data,null);
        Log.e("Tg1",commData.get(0).toString());
        Log.e("Tg1",commData.get(1).toString());
        Log.e("Tg1",commData.get(2).toString());
        boolean same =false;
        for (int i=0;i<3;i++){
            if (data.equals(commData.get(i))){
                same =true;
                break;
            }
        }
        if(!same){
            commData.add(0,data);
//            commData2.add(0,data2);
            Log.e("ADD", commData.get(0).toString());
        }
        Log.e("Tg",commData.get(0).toString());
        Log.e("Tg",commData.get(1).toString());
        Log.e("Tg",commData.get(2).toString());

        for (int i = 0;i<3;i++) {
            commonData.edit().putString(Flag.get(i).toString(), commData.get(i).toString()).commit();
        }


        try {
            String pathTable1 ="東向西";
            String pathTable2 ="西向東";
            URL url =new URL(urlstr);
            Document doc = Jsoup.parse(url,30000);
            // Get first table
            Element table = doc.select("table").get(3);

            // Get td Iterator
            Iterator<Element> ite = table.select("td").iterator();
            // Print content

//            Log.e("standPath",read_Data.getString("save_Path",""));
//            Log.e("standName",read_Data.getString("save_Name",""));

            if (standPath.equals(pathTable2)){
                int cnt = 0;
                while(ite.hasNext())
                {
                    cnt++;
                    String value =BusStandData.StringFilter(ite.next().text());
                    System.out.println("Value " + cnt + ": " + value);
                    if(cnt%2==1)
                        listName.add(value);
                    if (cnt%2 ==0)
                        listTime.add(value);
                }
                System.out.println("---------------------");
                result_page.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0;i<listName.size();i++)
                        {
//
                            Log.e("standName1111",standName);
                            if ((listName.get(i).equals(standName)) &&( (listTime.get(i).equals("末班已過")) ||
                                    (listTime.get(i).equals("未發車")) || (listTime.get(i).equals("將到站")) || (listTime.get(i).equals("進站中"))  ||
                                    (listTime.get(i).equals("今日未營運")))) {
                                resultView.setText(route+" 公車"+"\n"+"往 "+"\n"+nameZH+"站"+"\n"+listTime.get(i));
                                break;
                            }
                            if (listName.get(i).equals(standName)){
                                resultView.setText(route+" 公車"+"\n"+"往 "+"\n"+nameZH+"站"+"\n"+listTime.get(i)+" 到站 ");
                            }
                            System.out.println(listName.get(i));
                        }
                    }
                });
            }
            System.out.println("======================");
            Element table2 = doc.select("table").get(4);
            // Get td Iterator
            Iterator<Element> ite2 = table2.select("td").iterator();
            // Print content
            if (standPath.equals(pathTable1)) {
                int cnt2 = 0;
                while (ite2.hasNext()) {
                    cnt2++;
                    String value = BusStandData.StringFilter(ite2.next().text());
                    System.out.println("Value " + cnt2 + ": " + value);
                    if (cnt2 % 2 == 1)
                        listName2.add(value);
                    if (cnt2 % 2 == 0)
                        listTime2.add(value);
                }

                System.out.println("---------------------");
                result_page.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < listName2.size(); i++) {
                            Log.e("standName2222",standName);
                            if (((listName2.get(i).equals(standName)) && ((listTime2.get(i).equals("末班已過")) ||
                                    (listTime2.get(i).equals("未發車")) || (listTime2.get(i).equals("將到站")) || (listTime2.get(i).equals("進站中")) ||
                                    (listTime2.get(i).equals("今日未營運"))))) {
                                resultView.setText(route + " 公車" + "\n" + "往 " + "\n" + nameZH + "站" + "\n" + listTime2.get(i));
                                break;
                            }
                            if (listName2.get(i).equals(standName)) {
                                resultView.setText(route + " 公車" + "\n" + "往 " + "\n" + nameZH + "站" + "\n" + listTime2.get(i) + " 到站 ");
                            }
                            System.out.println(listName2.get(i));
                        }
                    }
                });
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS)
        {
//            String pathmeg =standPath.substring(standPath.length()-1);
            String meg ="您目前正在"+standName+"站";
            Log.e("meg", meg);
            if (talkbackenable()) {
                Toast.makeText(getApplicationContext(),meg,Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(),"公車路線查詢",Toast.LENGTH_SHORT).show();
            }
            mTts.speak(meg, TextToSpeech.QUEUE_ADD, null, null);
            mTts.speak("公車路線查詢", TextToSpeech.QUEUE_ADD, null, null);
            int result = mTts.setLanguage(Locale.TAIWAN);
            //設置發音語言
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            //判斷語言是否可用
            {
                Log.v(TAG, "Language is not available");
            }
            else
            {
//                mTts.speak(meg, TextToSpeech.QUEUE_ADD, null, null);
//                mTts.speak("公車路線查詢",TextToSpeech.QUEUE_ADD,null,null);
            }
        }
    }

    private boolean talkbackenable(){
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        boolean isAccessibilityEnabled = am.isEnabled();
        boolean isExploreByTouchEnabled = am.isTouchExplorationEnabled();
        Log.e("talkbackenable",""+isAccessibilityEnabled);
        Log.e("talkbackenable",""+isExploreByTouchEnabled);
        return  isExploreByTouchEnabled;
    }

    public Bitmap getLocalBitmap(Context con,int resourceId){
        InputStream inputStream =con.getResources().openRawResource(resourceId);
        return BitmapFactory.decodeStream(inputStream, null, getBitmapOptions(3));
    }
    public BitmapFactory.Options getBitmapOptions(int scale){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inSampleSize = scale;
        return options;
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.e("onPause", "onPause");
//        if(mTts != null)
//        //activity暫停時也停止TTS
//        {
//            mTts.stop();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();

//        setTimerTask();

    }

    @Override
    protected void onDestroy() {
//        mTts.stop();
        stopTimer();
        mTts.shutdown();
        Log.e("result_page", "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
//        stopTimer();
//        mTts.shutdown();
//        mTts.stop();
        Log.e("onStop","onStop");
        super.onStop();
    }
}
