package de.uni_s.ipvs.mcl.assignment5;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.UUID;

/**
 * Here the write, read, display and average functions should be implemented
 * Created by pablo on 8/07/17.
 */

public class DatabaseManager  {

    private static final String TAG = WeatherService.class.getName();
    private static final String TARGET_DEVICE_NAME = "IPVSWeather";
    private static final String IPVS_WEATHER_UUID = "00000002-0000-0000-fdfd-fdfdfdfdfdfd";
    private static final String IPVS_WEATHER_TEMP_UUID = "00002a1c-0000-1000-8000-00805f9b34fb";
    private static final String DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();


    public boolean writevalue(){
        //create new node
        Log.d(TAG, "adding value...");
        //create another node and set Value to it
        return false;

    }


}