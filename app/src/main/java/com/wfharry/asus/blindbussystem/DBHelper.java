package com.wfharry.asus.blindbussystem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by asus on 2016/2/24.
 */
public class DBHelper extends SQLiteOpenHelper{
    private final static int _DBVersion = 1;
    private final static String _DBName = "BlindBusSystem.db";
    public  static String stopName ="stopName";
    public  static String Route ="Route";
    public  static String InOtherWords ="InOtherWords";
    public DBHelper(Context context) {
        super(context, _DBName, null,_DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL ="CREATE TABLE " +stopName+"( " +
                                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                                    "_NameZH TEXT, "+
                                    "_RoutID VARCHAR(10), " +
                                     "_GoBack INT "+
                " )";
        final String SQL2 ="CREATE TABLE " +Route+"( " +
                                     "_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                                     "_RoutID VARCHAR, " +
                                     "_GoBack INT "+
                " )";
        db.execSQL(SQL);
        Log.e("creatSQL","自動新建成功");
        db.execSQL(SQL2);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL = "DROP TABLE " + stopName;
        final String SQL2 = "DROP TABLE " + Route;
        db.execSQL(SQL);
        db.execSQL(SQL2);
    }


}
