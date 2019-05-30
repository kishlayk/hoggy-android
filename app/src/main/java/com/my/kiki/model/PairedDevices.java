package com.my.kiki.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;


@Entity
public class PairedDevices implements Serializable {

    private static final String CREATE_TABLE_CART = "CREATE TABLE table_cart(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER,name TEXT,images TEXT,price REAL,quantity INTEGER,attribute TEXT,is_selected INTEGER)";


    @PrimaryKey(autoGenerate = true)
    public long did;


    @ColumnInfo
    private String deviceName;
    @ColumnInfo
    private String deviceMac;


    @Override
    public String toString() {
        return "PairedDevices{" +
                ", did='" + did + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceMac='" + deviceMac + '\'' +
                '}';
    }

    public PairedDevices() {

    }

    @Ignore
    public PairedDevices(String deviceName, String deviceMac) {

        this.deviceName = deviceName;
        this.deviceMac = deviceMac;
    }


    public long getDid() {
        return did;
    }

    public void setDid(long did) {
        this.did = did;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }
}
