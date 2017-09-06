package com.wfharry.asus.blindbussystem;

import android.app.AlertDialog;
import android.content.ContentValues;
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
import android.os.Vibrator;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener,BusStandApplication.CallBack,Serializable {
    public static final String TAG = MainActivity.class.getSimpleName();
    BusStandApplication application;
    private Button searchBtn,resultBtn;
    private DBHelper DH =null;
    private SQLiteDatabase db;
    private File path = new File("/sdcard/BlindBusSystem");
    private File f = new File("/sdcard/BlindBusSystem/BlindBusSystem.db");
    private static final String TAG1 = "TTS Demo";
    private TextToSpeech mTts;
    private static final int REQ_TTS_STATUS_CHECK = 0;
    JSONObject jObj = new JSONObject();
    Iterator it = jObj.keys(); //gets all the keys
    ArrayList arry = new ArrayList();
    int routeID;
    Bitmap bitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bitmap =getLocalBitmap(this,R.drawable.searchroute);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(this.getResources(),bitmap);
        getWindow().setBackgroundDrawable(bitmapDrawable);
        searchBtn = (Button) findViewById(R.id.button);
        resultBtn = (Button) findViewById(R.id.button3);

        mTts = new TextToSpeech(this, this);

        application = (BusStandApplication) this.getApplication();
        ((BusStandApplication)application).registerCallBack(this);
//        Intent checkIntent = new Intent();
//        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//        startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);

//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
//        doGet("http://imp47.5284.com.tw/IMP/jsp/LCDStopJson.jsp?Version=2&IStopID=11730");
        createfile();
        openDB();
        importCSVOnce();
        addotherwords();

        Log.e(TAG, "onCreate()");




        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (talkbackenable()){
                    Toast.makeText(getApplicationContext(), "開啟 google voice語音", Toast.LENGTH_SHORT).show();
                }
                mTts.speak("開啟 google voice語音", TextToSpeech.QUEUE_ADD, null, null);
//                ((BusStandApplication)application).StopApplication();
                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent, 1);

//                Toast t = Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_LONG);
//                t.show();

            }
        });
        resultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String route;
                route = resultBtn.getText().toString();
                if (resultBtn.getText().length() > 0) {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, BusStop_page.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("routeID", routeID);
                    bundle.putString("route", route);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQ_TTS_STATUS_CHECK) {
//            switch (resultCode) {
//                case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:
//                    //這個返回結果表明TTS Engine可以用
//                {
//                    mTts = new TextToSpeech(this,this);
//                    Log.v(TAG1, "TTS Engine is installed!");
//
//                }
//
//                break;
//                case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:
//                    //需要的語音數據已損壞
//                {
//                    //這三種情況都表明數據有錯,重新下載安裝需要的數據
//                    Log.v(TAG1, "Need language stuff:" + resultCode);
//                    Intent dataIntent = new Intent();
//                    dataIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//                    startActivity(dataIntent);
//
//                }
//                break;
//
//            }
//        } else {
//            //其他Intent返回的結果
//        }

        if (requestCode == 1) {
//            resultBtn.setText(resultCode);
            Log.e("tag", "" + resultCode);
            if (resultCode == RESULT_OK) {
//                Toast e =Toast.makeText(getApplicationContext(),"OK3",Toast.LENGTH_LONG);
//                e.show();
                ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                String all ="";
//                String j="";
//                for (j :text) {
//                    all = all + j +"\n";
//                }
//                resultBtn.setText(text.get(0));
                ArrayList<Integer> id = new ArrayList<Integer>();
                SharedPreferences saveData = getSharedPreferences("save_Data", Context.MODE_PRIVATE);

//               saveData.edit().putString("save_Name", "南京敦化路口小巨蛋").commit();
//               saveData.edit().putString("save_Path","東向西").commit();
                if (talkbackenable()){
                    Toast.makeText(getApplicationContext(), "查詢中請梢候", Toast.LENGTH_SHORT).show();
                }
                mTts.speak("查詢中請梢候", TextToSpeech.QUEUE_ADD, null, null);


//               Cursor cur = db.rawQuery("SELECT _Route, _NameZH FROM stopName INNER JOIN Route ON stopName._RouteID = Route._RouteID ", null);
                Cursor curRoute = db.rawQuery("SELECT * FROM " + DBHelper.Route, null);
                Cursor curName = db.rawQuery("SELECT * FROM " + DBHelper.stopName, null);
                Cursor curOtherwords =db.rawQuery("SELECT * FROM " +DBHelper.InOtherWords,null);
                boolean routeMatch =false;
                boolean nameMatch =false;
                ArrayList<Integer> arrayRouteID = new ArrayList<Integer>();
                if (curName != null){
                    while (curName.moveToNext()){
                        if (BusStandData.StringFilter(curName.getString(2)).equals(saveData.getString("save_Name",""))){
                            arrayRouteID.add(curName.getInt(1));
                            nameMatch =true;
                        }
                        if (curName.isLast() && !nameMatch){
                            resultBtn.setText("");
                            Toast.makeText(getApplicationContext(), "此站點無記錄", Toast.LENGTH_LONG).show();
                            Toast.makeText(getApplicationContext(), text.get(0), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                if (curRoute != null) {
                    while (curRoute.moveToNext()) {
//                        Log.e("Route222",curRoute.getString(2));
                        if (text.get(0).equals(BusStandData.StringFilter(curRoute.getString(2)))) {
//                            resultBtn.setText(text.get(0));
                            routeID = curRoute.getInt(1);
                            routeMatch =true;
                            break;
//                           socket_boolean =false;
                        }
                        if (curRoute.isLast()){
                            break;
                        }
                    }
                }
                if (curRoute.isLast() && !routeMatch && (curOtherwords != null)){
                    Log.e("IN","IN");
                    while (curOtherwords.moveToNext()){
                        if (text.get(0).equals(BusStandData.StringFilter(curOtherwords.getString(1)))){
                            routeMatch =true;
//                            resultBtn.setText(BusStandData.StringFilter(curOtherwords.getString(2)));
//                            Toast.makeText(getApplicationContext(),BusStandData.StringFilter(curOtherwords.getString(2)), Toast.LENGTH_LONG).show();
                            break;
                        }
                        if (curOtherwords.isLast() && !routeMatch){

                            Log.e("First","First");
                            resultBtn.setText("");
                            Toast.makeText(getApplicationContext(), "無此路線，請重新查詢", Toast.LENGTH_LONG).show();
                            Toast.makeText(getApplicationContext(), text.get(0), Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    if (routeMatch){
                        while (curRoute.moveToPrevious()){
                            if (curRoute.getString(2).equals(curOtherwords.getString(2))){
                                routeID = curRoute.getInt(1);
                                Log.e("routeID",""+routeID);
                                break;
                            }
                        }
                    }
                }
                if (nameMatch && routeMatch){
                    boolean match =false;
                    Log.e("arrySize", arrayRouteID.size() + "");
                    for (int tem :arrayRouteID){
                        if (tem == routeID){
                            match =true;
                            resultBtn.setText(BusStandData.StringFilter(curRoute.getString(2)));
                           if (talkbackenable()) {
                               Toast.makeText(getApplicationContext(), "結果顯示 " + BusStandData.StringFilter(curRoute.getString(2)), Toast.LENGTH_SHORT).show();
                           }
                            mTts.speak("結果顯示 "+ BusStandData.StringFilter(curRoute.getString(2)), TextToSpeech.QUEUE_ADD, null, null);
                            break;
                        }
                    }
                    if (!match){
                        resultBtn.setText("");
                        Toast.makeText(getApplicationContext(), "該路線不經過此站，請重新查詢", Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), text.get(0), Toast.LENGTH_SHORT).show();
                    }
                }
//                ArrayList nameList = new ArrayList();
//                if ((curName != null) && routeMatch){
//                    curName.moveToNext();
//                    while (curName.moveToNext()){
//                        if (curName.getInt(1) == routeID);
//                            nameList.add(curName.getString(2));
//
//                        if (curName.isLast()){
//                            for (Object temp :nameList){
//                                if (BusStandData.StringFilter(temp.toString()).equals("南京敦化路口小巨蛋")){
//                                    resultBtn.setText(text.get(0));
//                                    break;
//                                }else {
//                                    Log.e("Second","Second");
//                                    resultBtn.setText("");
//                                    Toast.makeText(getApplicationContext(), "無此路線，請重新查詢", Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        }
//                    }
//                    for (Object tem :nameList){
//                        Log.e("nameList",tem.toString());
//                    }
//                }
/*
       //----------------------------------------------------------------------------------------
                //时间过长
                if (cur != null) {
                    cur.moveToNext();
                    Log.e("leng", cur.getCount() + "");
                    Log.e("qure0", cur.getString(0));
                    Log.e("qure1", cur.getString(1));
                    while (cur.moveToNext()) {
                        if ((BusStandData.StringFilter(cur.getString(1)).equals("南京敦化路口小巨蛋"))  //saveData.getString("save_Name", "")
                                && (BusStandData.StringFilter(cur.getString(0)).equals(text.get(0)))) {
                                     resultBtn.setText(text.get(0));
                            break;
//                            if (BusStandData.StringFilter(cur.getString(0)).equals(text.get(0))) {
//                                resultBtn.setText(text.get(0));
//                            }
                        }

     //----------------------------------------------------------------------------------------
                        if (cur.isLast()) {
                            resultBtn.setText("");
                            Toast.makeText(getApplicationContext(), "無此路線，請重新查詢", Toast.LENGTH_LONG).show();
                        }
                    }
                }*/

//                boolean socket_boolean =true;
//                if(cur != null){
//                    cur.moveToNext();
//                    Log.e("qure0", "" + cur.getInt(0));
//                    Log.e("qure1",""+cur.getInt(1));
//                    Log.e("qure2",cur.getString(2));
//                    while (cur.moveToNext()){
//                        for (int i=0;i<id.size();i++){
//                            Log.e("id",id.get(i)+"");
//                            if (id.get(i) == cur.getInt(1)){
//                                resultBtn.setText(BusStandData.StringFilter(text.get(0)));
//                                routeID =cur.getInt(1);
//                                break;
//                            }
//                        }
//                        if (text.get(0).equals(BusStandData.StringFilter(cur.getString(2)))){
//                            resultBtn.setText(text.get(0));
//                            routeID =cur.getInt(1);
//                            break;
////                        socket_boolean =false;
//                        }
//                if (cur.isLast()) {
//                    resultBtn.setText("");
//                    Toast.makeText(getApplicationContext(), "無此路線，請重新查詢", Toast.LENGTH_LONG).show();
//                }
//                    break;
//                }
//                Toast.makeText(getApplicationContext(), text.get(0), Toast.LENGTH_SHORT).show();

            }
        }
                /*
                for(int i=0;i<arry.size();i++)
                {

//                    Log.e("tag","mis");
                    System.out.println(arry.get(i));
                    if (text.get(0).equals(arry.get(i)) ){
//                        Log.e("tag","resulttttttttttttttt");
                        resultBtn.setText(text.get(0));
                        routeID =text.get(0);
                        socket_boolean =false;
                    }
                }*/
//                if (socket_boolean){
//                    Toast.makeText(getApplicationContext(),"無此路線，請重新查詢",Toast.LENGTH_LONG).show();
//                }




    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_UP){
//            Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).show();
//        }

        return super.onTouchEvent(event);

    }


    public void importCSV(){
//        db =DH.getWritableDatabase();
        String line ="";
       try {
           BufferedReader br_stop = new BufferedReader(new InputStreamReader(getAssets().open("stop.csv"),"UTF-8"));
           BufferedReader br_route = new BufferedReader(new InputStreamReader(getAssets().open("route.csv"),"UTF-8"));
           db.beginTransaction();
           String dbCol0 ="_NameZH",dbCol1="_Route",dbCol2="_RouteID";
//           String str1 ="INSERT INTO"+tableName+"("+ columns +") (";
//           String str2 =");";
           while ((line = br_stop.readLine()) !=null){
               String[] colums = line.split(",");
               ContentValues cv = new ContentValues(30);
               cv.put(dbCol2,colums[0].trim());
               cv.put(dbCol0,colums[1].trim());
               Log.e("cvstring",cv.toString());
               db.insert(DBHelper.stopName,null,cv);
           }
           while ((line =br_route.readLine()) !=null){
               String[] colums = line.split(",");
               ContentValues cv = new ContentValues(30);
               cv.put(dbCol2,colums[0].trim());
               cv.put(dbCol1,colums[1].trim());
               Log.e("cvstring",cv.toString());
               db.insert(DBHelper.Route,null,cv);
           }
       }catch (Exception e){
           Toast.makeText(getApplicationContext(),"import error",Toast.LENGTH_SHORT).show();
           e.printStackTrace();
       }
        db.setTransactionSuccessful();
        db.endTransaction();
    }
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
                    String dest,rout,goback;
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
                            System.out.println(key+" -> "+o);// store in session
                            if (key.equals("dest")){
                                dest=o.toString();
                            }
                            if (key.equals("route")){
                                rout=o.toString();
                            }
                            if(key.equals("route") ) {
                                arry.add(o);
                            }
                    }
                        System.out.println("---------------------------");

//				    list.add(jsonArray.getString(i));
//				    System.out.println(jsonArray.getString(i));
                    }
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

    }




    private void openDB(){
        DH = new DBHelper(this);
//        SQLiteDatabase.openDatabase("/sdcard/BlindBusSystem.db",null,SQLiteDatabase.CREATE_IF_NECESSARY);
        SQLiteDatabase.openOrCreateDatabase(f.getPath(), null);

    }
    private void closeDB(){
        DH.close();
    }


    private void createfile(){
        if(!path.exists()){   //判断目录是否存在
            path.mkdirs();    //创建目录
        }
        if(!f.exists()){      //判断文件是否存在
            try{
                f.createNewFile();  //创建文件
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    private void createTable(){
        String SQL ="CREATE TABLE IF NOT EXISTS " +DBHelper.stopName+"( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "_RouteID INT, "+
                "_NameZH TEXT "+
                " )";
        String SQL2 ="CREATE TABLE IF NOT EXISTS " +DBHelper.Route+"( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "_RouteID INT, " +
                "_Route TEXT "+
                " )";
        String SQL3 ="CREATE TABLE IF NOT EXISTS "+DBHelper.InOtherWords+"( "+
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "_Inotherwords TEXT, "+
                "_Exactlywords TEXT "+
                " )";
        db.execSQL(SQL);
        db.execSQL(SQL2);
        db.execSQL(SQL3);
        Log.e("creatSQL", "手動新建成功");
    }

    private void add(String inotherwords,String exactlywords){
        db =SQLiteDatabase.openDatabase(f.getPath(),null,SQLiteDatabase.OPEN_READWRITE);
        ContentValues values = new ContentValues();
        values.put("_Inotherwords", inotherwords.toString());
        values.put("_Exactlywords", exactlywords.toString());
        db.insert(DBHelper.InOtherWords, null, values);
    }

    private void addotherwords(){
       Cursor cur =db.rawQuery("SELECT * FROM "+DBHelper.InOtherWords,null);
        if (cur.getCount() == 0) {
            add("2284", "288副");
            add("中久", "棕9");
            add("重久", "棕9");
            add("中時", "棕10");
            add("2887", "288區");
        }
    }

    private void importCSVOnce(){
        db =SQLiteDatabase.openDatabase(f.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        createTable();
        Cursor cur =db.rawQuery("SELECT * FROM "+DBHelper.stopName, null);
        Log.e("tagg", "right");
        Log.e("tagg", "" + cur.getCount());
        if(cur.getCount() == 0){
            importCSV();
        }
    }

    @Override
    protected void onDestroy() {
        closeDB();
        Log.e(TAG, "onDestroy()");
        super.onDestroy();
        mTts.shutdown();
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
            scanBeacon();
//            if (talkbackenable()){
//                Toast.makeText(getApplicationContext(), "您目前正在南京敦化路口站", Toast.LENGTH_SHORT).show();
//                Toast.makeText(getApplicationContext(),"公車路線查詢",Toast.LENGTH_SHORT).show();
//            }
//            mTts.speak("您目前正在南京敦化路口站", TextToSpeech.QUEUE_ADD, null, null);
//            mTts.speak("公車路線查詢", TextToSpeech.QUEUE_ADD, null, null);

            int result = mTts.setLanguage(Locale.TAIWAN);
            //設置發音語言
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            //判斷語言是否可用
            {
                Log.v(TAG1, "Language is not available");
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("您未安裝Goole文字轉語音工具")
                        .setMessage("請至Google Play下載")
                        .setPositiveButton("好", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String sParam = "com.google.android.tts";
                                Intent dataIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + sParam));
                                startActivity(dataIntent);
                            }
                        })
                        .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();

//                resultBtn.setEnabled(false);
            }
            else
            {
//                mTts.speak("This is an example of speech synthesis.", TextToSpeech.QUEUE_ADD, null,null);

                resultBtn.setEnabled(true);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause()");
//        if(mTts != null)
//        //activity暫停時也停止TTS
//        {
//            mTts.stop();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume()");

        SharedPreferences saveData = getSharedPreferences("save_Data", Context.MODE_PRIVATE);
        //                当Beacon未被侦测到时
        if (!saveData.getBoolean("BeaconStatus",true)){
            Log.e("BeaconStatus",""+saveData.getBoolean("BeaconStatus",true));
            Toast.makeText(getApplicationContext(),"未侦测到Beacon",Toast.LENGTH_SHORT).show();
            mTts.speak("未侦测到Beacon", TextToSpeech.QUEUE_ADD,null,null);
            saveData.edit().putString("save_Name", "").commit();//南京敦化路口小巨蛋
            saveData.edit().putString("save_Path","").commit();//東向西
            resultBtn.setText("");
        }
        if(application.MyBusStand!=null){
            Log.e(TAG, "目前所在站點 : " + application.MyBusStand.StandName);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.e(TAG, "onBackPressed()");

        ((BusStandApplication)application).StopApplication();

        System.exit(0);
        finish();
    }


    @Override
    public void onFoundBusStand(BusStandData bustand) {
        Log.e(TAG, "onFoundBusStand()");
        Log.e(TAG,"BusStandData : " + bustand.StandName);
        Log.e(TAG, "BusStandData : " + bustand.path);
        Log.e(TAG, "BusStandData : " + bustand.Lat);
        Log.e(TAG, "BusStandData : " + bustand.Lng);

        SharedPreferences save_Data =getSharedPreferences("save_Data",Context.MODE_PRIVATE);

        String co_Name ="";
        String co_Path ="";
        if (co_Name !=bustand.StandName){
            co_Name=bustand.StandName;
            save_Data.edit().putString("save_Name", co_Name).commit();
            Log.e("SSSSSSSSSSS",co_Name);

        }
        if (co_Path !=bustand.path){
            co_Path=bustand.path;
            save_Data.edit().putString("save_Path",co_Path).commit();
            Log.e("XXXXXXXXXXXX", co_Path);
        }
    }
    private void scanBeacon(){
        SharedPreferences saveData = getSharedPreferences("save_Data", Context.MODE_PRIVATE);
        //                当Beacon未被侦测到时
        if (!saveData.getBoolean("BeaconStatus",true)){
            Log.e("BeaconStatus",""+saveData.getBoolean("BeaconStatus",true));
            Toast.makeText(getApplicationContext(),"未侦测到Beacon",Toast.LENGTH_SHORT).show();
            mTts.speak("未侦测到Beacon",TextToSpeech.QUEUE_ADD,null,null);
            saveData.edit().putString("save_Name", "").commit();//南京敦化路口小巨蛋
            saveData.edit().putString("save_Path","").commit();//東向西
        }
        if (saveData.getBoolean("BeaconStatus",true)){
            if (talkbackenable()){
                Toast.makeText(getApplicationContext(), "您目前正在"+saveData.getString("save_Name","")+"站", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(),"公車路線查詢",Toast.LENGTH_SHORT).show();
            }
            mTts.speak("您目前正在"+saveData.getString("save_Name","")+"站", TextToSpeech.QUEUE_ADD, null, null);
            mTts.speak("公車路線查詢", TextToSpeech.QUEUE_ADD, null, null);
        }
//        if (!saveData.getBoolean("save_Message",false)){
//            Toast.makeText(getApplicationContext(),"未侦测到Beacon",Toast.LENGTH_SHORT).show();
//            saveData.edit().putString("save_Name", "南京敦化路口小巨蛋").commit();//南京敦化路口小巨蛋
//            saveData.edit().putString("save_Path","東向西").commit();//東向西
//        }
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
    public void onLeaveBusStand() {
        Log.e(TAG, "onLeaveBusStand()");
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}

