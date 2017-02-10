package com.rafzy.uteproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by root on 2/9/17.
 */
public class FriendsDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "UTE";
    private static final int DB_VERSION = 1;
    public FriendsDatabaseHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE FRIENDS ("
                + "friend_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "NAME TEXT, "
                + "NUMBER INTEGER);");
        insertFriend(db, "Radosław", 501020222);

    }
    public void insertFriend(SQLiteDatabase db, String name,
                             int number){
        ContentValues friendValues = new ContentValues();
        friendValues.put("NAME", name);
        friendValues.put("NUMBER", number);
        db.insert("FRIENDS", null, friendValues);
    }
    public int update(String table,
                      ContentValues values,
                      String whereClause,
                      String[] whereArgs){
        return 1;

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
