package com.my.kiki.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.my.kiki.model.PairedDevices;


@Database(entities = {PairedDevices.class}, version = 1)
public abstract class MyDatabase extends RoomDatabase {
    public abstract PairedDevicesDAO pairedDevicesDAO();
    private static MyDatabase instance = null;
    public static MyDatabase getDataBase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, MyDatabase.class, "bt_devices_database").allowMainThreadQueries().build();
        }
        return instance;
    }
}
