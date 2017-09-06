package com.wfharry.asus.blindbussystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CommonBusRoute extends AppCompatActivity {
    result_page Flag;
    Button firstBtn,secondBtn,thirdBtn;
    String[] Aarry,Barry,Carry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_bus_boute);

         firstBtn = (Button) findViewById(R.id.commonfirst);
         secondBtn = (Button) findViewById(R.id.commonsecond);
         thirdBtn = (Button) findViewById(R.id.commonthird);
        final SharedPreferences setting = getSharedPreferences("save_Data", MODE_PRIVATE);
        getCommonData();

        firstBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!firstBtn.getText().equals("")) {
                    setting.edit().putString(result_page.save_Path, Aarry[5]).commit();
                    setting.edit().putString(result_page.save_Name, Aarry[6]).commit();
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    intent.setClass(CommonBusRoute.this, result_page.class);
                    bundle.putString("nameZH", Aarry[3]);
                    bundle.putString("route", Aarry[0]);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        secondBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!secondBtn.getText().equals("")) {
                    setting.edit().putString(result_page.save_Path, Barry[5]).commit();
                    setting.edit().putString(result_page.save_Name, Barry[6]).commit();
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    intent.setClass(CommonBusRoute.this, result_page.class);
                    bundle.putString("nameZH", Barry[3]);
                    bundle.putString("route", Barry[0]);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
        thirdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!thirdBtn.getText().equals("")) {
                    setting.edit().putString(result_page.save_Path, Carry[5]).commit();
                    setting.edit().putString(result_page.save_Name, Carry[6]).commit();
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    intent.setClass(CommonBusRoute.this, result_page.class);
                    bundle.putString("nameZH", Carry[3]);
                    bundle.putString("route", Carry[0]);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
    }


    public void getCommonData() {


        SharedPreferences commonData = getSharedPreferences("save_Data", 0);
        Log.e("commonData", commonData.getString(Flag.first, ""));

        String a=commonData.getString(Flag.first, "");
        String b=commonData.getString(Flag.second, "");
        String c=commonData.getString(Flag.third, "");
        Aarry =a.split("/");
        Barry =b.split("/");
        Carry =c.split("/");
        for (String j:Aarry){
            Log.e("Aarry",j);
        }
        if (Aarry.length>2){
             String sum1 =Aarry[0]+Aarry[1]+"\n"+Aarry[2]+"\n"+Aarry[3]+Aarry[4];
            firstBtn.setText(sum1);
        }else {
            firstBtn.setText("");
        }
        if (Barry.length>2) {
            String sum2 = Barry[0] + Barry[1] + "\n" + Barry[2] + "\n" + Barry[3] + Barry[4];
            secondBtn.setText(sum2);
        }else {
            secondBtn.setText("");
        }
        if (Carry.length>2) {
            String sum3 = Carry[0] + Carry[1] + "\n" + Carry[2] + "\n" + Carry[3] + Carry[4];
            thirdBtn.setText(sum3);
        }else {
            thirdBtn.setText("");
        }




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("CommonBusRoute", "onDestroy");
    }
}
