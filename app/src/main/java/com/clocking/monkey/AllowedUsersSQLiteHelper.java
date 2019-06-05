package com.clocking.monkey;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AllowedUsersSQLiteHelper extends SQLiteOpenHelper {

    String sqlCreate = "CREATE TABLE AllowedUsers(id INTEGER PRIMARY KEY AUTOINCREMENT, email VARCHAR(200), rol VARCHAR(100))";

    public AllowedUsersSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS AllowedUsers");
        db.execSQL(sqlCreate);
    }
}

