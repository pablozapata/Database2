package de.uni_s.ipvs.mcl.assignment5;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Here the write, read, display and average functions should be implemented
 * Created by pablo on 8/07/17.
 */

public class DatabaseManager extends BluetoothGattCallback implements BLEScanner.BLEScannerCallback {

    private static final String TAG = DatabaseManager.class.getName();
    private static final String TARGET_DEVICE_NAME = "";


    public interface DatabaseManagerCallback {

        void onDatabaseManagerFailedStart(FailureReason reason);
    }

    public enum FailureReason {
        DEVICE_NOT_FOUND,
        CONNECTION_FAILED,
        UNKNOWN
    }

    private enum InternalFailureReason {
        SCANNER_FAILED,
        CHARACTERISTIC_NOT_FOUND,
        CHARACTERISTIC_NOTI_FAILED,
        CHARACTERISTIC_READ_FAILED,
        DESCRIPTOR_WRITE_FAILED,
        DESCRIPTOR_NOT_FOUND,
        SERVICE_NOT_FOUND,
        DISCOVER_SERVICES_FAILED,
        GATT_BUSY
    }

    private BLEScanner scanner;
    private boolean mRunning;
    private boolean mDeviceConnected;
    private BluetoothGatt mGatt;
    private Context mContext;
    private DatabaseManagerCallback mCallback;


    public DatabaseManager(Context context, BluetoothAdapter adapter, DatabaseManagerCallback callback) {
        this.scanner = new BLEScanner(adapter, this);
        this.mCallback = callback;
        mRunning = false;
        this.mContext = context;
        this.mDeviceConnected = false;
        this.mRunning = false;


    }


    public void startService() {
        if (!mRunning) {
            mRunning = true;
            Log.d(TAG, "startService: Booting up service");
            Log.d(TAG, "startService: Initiating Scan for target named: " + TARGET_DEVICE_NAME);
            scanner.startScan(TARGET_DEVICE_NAME);
        } else {
            Log.i(TAG, "startService: Service already running");
        }
    }

    public void stopService() {
        mRunning = false;
        if (mDeviceConnected) {
            mGatt.disconnect();
        }
    }

//TODO here add new functions

    public void writeData (){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
       
    }

    @Override
    public void onDeviceFound (BluetoothDevice device){
        Log.d(TAG, "onDeviceFound: Found Target Device");
        //connect to the target device
        scanner.stopScan();
        Log.d(TAG, "onDeviceFound: Connecting to target device");
        mGatt = device.connectGatt(mContext, false, this);
    }

    @Override
    public void onScanFailed (BLEScanner.ScannerFailureReason reason){
        serviceErrorHandler(InternalFailureReason.SCANNER_FAILED);
    }

    @Override
    public void onScanCompleted() {
        Log.d(TAG, "onScanCompleted: Scan Completed");
        Log.d(TAG, "onScanCompleted: Scan completed but the system is not running");
        if (mCallback != null) {
            mCallback.onDatabaseManagerFailedStart(FailureReason.DEVICE_NOT_FOUND);
        }
    }
    //MARK: - Bluetooth Gatt Callback methods

    private void serviceErrorHandler(InternalFailureReason reason) {
        if (mCallback != null) {
            mCallback.onDatabaseManagerFailedStart(FailureReason.UNKNOWN);
        }

        if (mDeviceConnected && mGatt != null) {
            //disconnect from device
            mGatt.disconnect();
        } else {
            mDeviceConnected = false;
        }

        //the system is not running
        mRunning = false;
    }
}