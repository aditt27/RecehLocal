package com.adibu.receh.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by AdityaBudi on 24/07/2017.
 */

public class RecehDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "receh.db";

    public RecehDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Bikin statement buat bikin table SQLite
    String SQL_CREATE_RECEH_TABLE = "CREATE TABLE " + RecehContract.RecehEntry.TABLE_NAME
            + " ("
            + RecehContract.RecehEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + RecehContract.RecehEntry.COLUMN_DATETIME + " TEXT NOT NULL, "
            + RecehContract.RecehEntry.COLUMN_TITLE + " TEXT NOT NULL, "
            + RecehContract.RecehEntry.COLUMN_VALUES + " INTEGER NOT NULL DEFAULT 0, "
            + RecehContract.RecehEntry.COLUMN_TRANSACTION + " INTEGER NOT NULL, "
            + RecehContract.RecehEntry.COLUMN_DESCRIPTION + " TEXT, "
            + RecehContract.RecehEntry.COLUMN_TABLE_CONTENT_ID + " INTEGER NOT NULL"
            + ");";

    String SQL_CREATE_TABLE_CONTENT_TABLE = "CREATE TABLE " + RecehContract.RecehEntry.TABLE_CONTENT_TABLE_NAME
            + " ("
            + RecehContract.RecehEntry.TABLE_CONTENT_COLUMN_TITLE + " TEXT NOT NULL, "
            + RecehContract.RecehEntry.TABLE_CONTENT_ID + " INTEGER PRIMARY KEY"
            + ");";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Execute statement diatas
        sqLiteDatabase.execSQL(SQL_CREATE_RECEH_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_CONTENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }



}
