package com.my.kiki.db;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.my.kiki.model.PairedDevices;

import java.util.List;


@Dao
public interface PairedDevicesDAO {

	@Query("SELECT * FROM PairedDevices")
	List<PairedDevices> getAll();


	@Insert
	void insertPairedDevice(PairedDevices... pairedDevices);

	@Query("SELECT * FROM PairedDevices WHERE deviceName = :deviceName and deviceMac =:deviceMac")
    List<PairedDevices> getPairedDevice(String deviceName, String deviceMac);


	@Delete
	void deletePairedDevice(PairedDevices... pairedDevices);

	@Update
	void update(PairedDevices pairedDevices);

	@Query("DELETE FROM PairedDevices WHERE deviceName = :deviceName and deviceMac =:deviceMac")
	void deletePairedDevice(String deviceName, String deviceMac);




	@Query("DELETE FROM PairedDevices")
	void deleteAll();



}
