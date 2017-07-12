package de.uni_s.ipvs.mcl.assignment5;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import de.uni_s.ipvs.mcl.assignment5.BLEScanner;

import de.uni_s.ipvs.mcl.assignment5.WeatherService;

public class MainActivity extends AppCompatActivity implements WeatherService.WeatherServiceCallback{

    private static final String TAG = MainActivity.class.getName();

    private BluetoothAdapter adapter;
    private WeatherService weatherService;
    private BLEScanner scanner;
    private Button button;
    private TextView tempView;



    private Handler mHandler;

    public boolean butStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        if (adapter == null){
            Log.e(TAG, "Bluetooth Adapter not available");
            //TODO later show a notice
            return;
        }
        if (!adapter.isEnabled()) {
            Log.d(TAG, "onCreate: Bluetooth is not turned on, turning on bluetooth");
            adapter.enable();
        }
        tempView = (TextView) findViewById(R.id.Temper);
        weatherService = new WeatherService(this, adapter, this);
        button = (Button) findViewById(R.id.connection);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //start the weather service
                if (butStatus==false) {
                    weatherService.startService();// Perform action on click
                    button.setText("Connecting...");
                    Toast.makeText(getApplicationContext(),"Connecting...",Toast.LENGTH_SHORT).show();

                }
                else{
                    weatherService.stopService();
                    button.setText("CONNECT TO THE WEATHER SERVICE");
                    butStatus=false;
                }

            }



        });



    }


    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: View Started");
    }

    //MARK: - Weather Service

    @Override
    public void onTemperatureChanged(final float value) {
        runOnUiThread(new Runnable() {

            public void run() {
                tempView.setText(String.format("%2.1f", value));
            }
        });

    }


    @Override
    public void onWeatherServiceStarted() {

        weatherService.registerForTemperature();
        runOnUiThread(new Runnable() {

            public void run() {

                Toast.makeText(getApplicationContext(),"Connection estabished",Toast.LENGTH_SHORT).show();
                button.setText("DISCONNECT");
                butStatus=true;
            }
        });


    }



    public void onTemperatureServiceRegistered() {
        Log.d(TAG, "onTemperatureServiceRegistered: Registered for temperature");

    }

    /*public void onHumidityServiceRegistered() {
        Log.d(TAG, "onHumidityServiceRegistered: Registered for humidity");
    }*/

    @Override
    public void onWeatherServiceFailedStart(WeatherService.FailureReason reason) {

        runOnUiThread(new Runnable() {

            public void run() {

                Toast.makeText(getApplicationContext(),"Connection failed",Toast.LENGTH_SHORT).show();
                button.setText("CONNECT TO THE WEATHER SERVICE");
                butStatus=false;
                //scanner.stopScan();
            }
        });
    }





}
