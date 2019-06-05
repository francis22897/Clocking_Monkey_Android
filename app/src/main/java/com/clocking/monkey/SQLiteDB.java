package com.clocking.monkey;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SQLiteDB {

    private Context context;
    private AllowedUsersSQLiteHelper ausqlh;
    private SQLiteDatabase db;

    public SQLiteDB(Context context) {
        this.context = context;
        ausqlh = new AllowedUsersSQLiteHelper(context, Utils.DATABASE_NAME, null, Utils.DATABASE_VERSION);

        db = ausqlh.getWritableDatabase();
    }

    protected boolean insertDB(String email, String rol){
        try{
            ContentValues newRecord = new ContentValues();

            newRecord.put("email", email);
            newRecord.put("rol", rol);
            db.insert("AllowedUsers", null, newRecord);

            return true;

        }catch (Exception ex){
            Log.e("INSERCION", ex.getMessage());
            return false;
        }
    }

    protected boolean searchUser(String email){

        boolean foundUser = false;

        try{
            Cursor c = db.rawQuery("SELECT * FROM AllowedUsers WHERE email='" + email + "'", null);

            foundUser = c.moveToFirst();

        }catch(Exception ex){
            Log.e("LECTURA", ex.getMessage());
        }

        return foundUser;
    }


}
