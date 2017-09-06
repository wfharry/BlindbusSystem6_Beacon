package com.wfharry.asus.blindbussystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;

import java.io.InputStream;
import java.util.Locale;

public class FirstScreen extends AppCompatActivity {
    private static final int REQ_TTS_STATUS_CHECK = 0;
    private TextToSpeech mTts;
    private static final String TAG1 = "TTS Demo";
    Bitmap bmparrival ;
    Bitmap bmpcommon ;
    private boolean foundMessage =false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);

        bmparrival =getLocalBitmap(this,R.drawable.arrivalreminders);
        bmpcommon =getLocalBitmap(this, R.drawable.commonbusroute);

        Button Arrival_remindersBtn = (Button) findViewById(R.id.Arrival_reminderBtn);
        Button Common_bus_routeBtn = (Button) findViewById(R.id.Common_bus_routeBtn);


        BitmapDrawable bitmaparrival = new BitmapDrawable(this.getResources(),bmparrival);
        BitmapDrawable bitmapcommon = new BitmapDrawable(this.getResources(),bmpcommon);
        Arrival_remindersBtn.setBackground(bitmaparrival);
        Common_bus_routeBtn.setBackground(bitmapcommon);

        SharedPreferences saveMessage =getSharedPreferences("save_Data",Context.MODE_PRIVATE);
//        if (foundMessage == saveMessage.getBoolean("save_Message",false)){
//
//        }

        talkbackenable();
        Arrival_remindersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(FirstScreen.this, MainActivity.class);
                startActivity(intent);

            }
        });

        Common_bus_routeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(FirstScreen.this,CommonBusRoute.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_TTS_STATUS_CHECK) {
            switch (resultCode) {
                case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:
                    //這個返回結果表明TTS Engine可以用
                {
                    mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if(status == TextToSpeech.SUCCESS)
                            {
                                int result = mTts.setLanguage(Locale.TAIWAN);
                                //設置發音語言
                                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                                //判斷語言是否可用
                                {
                                    Log.v(TAG1, "Language is not available");
                                    new AlertDialog.Builder(FirstScreen.this)
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
//                                    mTts.speak("This is an example of speech synthesis.", TextToSpeech.QUEUE_ADD, null,null);
                                }
                            }
                        }
                    });
//                    Log.v(TAG1, "TTS Engine is installed!");

                }

                break;
                case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:
                    //需要的語音數據已損壞
                {
                    //這三種情況都表明數據有錯,重新下載安裝需要的數據
                    Log.v(TAG1, "Need language stuff:" + resultCode);
                    Intent dataIntent = new Intent();
                    dataIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(dataIntent);

                }
                break;

            }
        } else {
            //其他Intent返回的結果
        }
    }

    public Bitmap getLocalBitmap(Context con,int resourceId){
        InputStream inputStream =con.getResources().openRawResource(resourceId);
        return BitmapFactory.decodeStream(inputStream,null,getBitmapOptions(3));
    }
    public BitmapFactory.Options getBitmapOptions(int scale){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inSampleSize = scale;
        return options;
    }

    private void talkbackenable(){
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        boolean isAccessibilityEnabled = am.isEnabled();
        boolean isExploreByTouchEnabled = am.isTouchExplorationEnabled();
        Log.e("talkbackenable",""+isAccessibilityEnabled);
        Log.e("talkbackenable",""+isExploreByTouchEnabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences InitMessage =getSharedPreferences("save_Data",Context.MODE_PRIVATE);
        InitMessage.edit().putBoolean("save_Message",false).commit();
        mTts.shutdown();
        Log.e("FirstScreen","onDestroy");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("TAG","onStop");

    }
}
