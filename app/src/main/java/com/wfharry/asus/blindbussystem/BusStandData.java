package com.wfharry.asus.blindbussystem;

/**
 * Created by asus on 2016/3/3.
 */
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class BusStandData {

    String StandName,path,pathData,StandNameData;
    int Major;
    int Minor;
    int StandId;
    double Lat;
    double Lng;
    int Id;

    public BusStandData(){

    }

    public BusStandData(JSONObject jsonObject) throws JSONException {

        int n =4;
        this.Id = jsonObject.getInt("id");
//        this.StandName = jsonObject.getString("name");
//        Log.e("Length",""+jsonObject.getString("name").length());
//        Log.e("LengthStandName",""+this.StandName.length());
        this.StandName = StringFilter(jsonObject.getString("name").substring(0, jsonObject.getString("name").length()-n));
//        this.StandName =jsonObject.getString("name").substring(0, 11);
//        Log.e("StandName",StandName);
        this.path = StringFilter(jsonObject.getString("name").substring(jsonObject.getString("name").length() -n));
//        Log.e("Path",path);
        this.Lat = jsonObject.getDouble("lat");
        this.Lng = jsonObject.getDouble("lon");
        this.StandId = jsonObject.getInt("stopid");
//        Log.e("stopid",""+StandId);
        this.Major = jsonObject.getInt("major");
        this.Minor = jsonObject.getInt("minor");

//        this.pathData = "";
//        this.StandNameData = "";
    }
    public String getPathData(String pathData){
       pathData = this.path;
        return pathData;
    }

    public String getNameData(String nameData){
        nameData = this.StandName;
        return  nameData;
    }

    // 过滤特殊字符
    public   static   String StringFilter(String   str)   throws PatternSyntaxException {
        // 只允许字母和数字
        // String   regEx  =  "[^a-zA-Z0-9]";
        // 清除掉所有特殊字符
        String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern   p   =   Pattern.compile(regEx);
        Matcher   m   =   p.matcher(str);
        return   m.replaceAll("").trim();
    }
}
