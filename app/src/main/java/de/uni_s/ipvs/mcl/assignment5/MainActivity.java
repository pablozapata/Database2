package de.uni_s.ipvs.mcl.assignment5;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
//import android.icu.text.SimpleDateFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import android.os.Handler;
import android.widget.TextView;

import java.math.BigInteger;
import java.text.SimpleDateFormat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements WeatherService.WeatherServiceCallback{

    private static final String TAG = MainActivity.class.getName();
    private static final String IPVS_WEATHER_UUID = "00000002-0000-0000-fdfd-fdfdfdfdfdfd";

    private BluetoothAdapter adapter;
    private WeatherService weatherService;
    private BLEScanner scanner;
    private Button button;
    private TextView tempBase;
    private TextView timeBase;
    private TextView tempCur;
    private DatabaseManager databaseManager;



    private Handler mHandler;

    public boolean butStatus = false;
    private Button button2;
    private Button button3;
    private Button button4;


   private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

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


        writeValue("test");




        //Read last value (Task 1.2)
        button2=(Button) findViewById(R.id.readLast);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final TextView disp = (TextView)findViewById(R.id.readLast_res);


                mRef.child("uuid").child(IPVS_WEATHER_UUID).addListenerForSingleValueEvent(new ValueEventListener() {
                    //mRef.child("uuid").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String str = dataSnapshot.getValue(String.class);
                        int size=str.length();
                        String temp = str.substring(14,size);

                        disp.setText(temp + "ºC");


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }


                });


            }
        });

        //Subscribe to changes (Task 2.1)
        button3=(Button) findViewById(R.id.suscribe);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final TextView disp = (TextView)findViewById(R.id.suscribe_res);


                mRef.child("uuid").child(IPVS_WEATHER_UUID).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String str = dataSnapshot.getValue(String.class);
                        int size=str.length();
                        String temp = str.substring(14,size);

                        disp.setText(temp + "ºC");


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }


                });


            }
        });

        // TODO Get the day average (Task 2.2)

        button4=(Button) findViewById(R.id.average);
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final TextView disp = (TextView)findViewById(R.id.average_res);




            }
        });





    }


    //we create a new node in both trees
    private void writeValue( String value) {
        //add value to location tree
        Random random = new Random();
        String seq = new BigInteger(40, random).toString(32);

        mRef.child("location").child("Stuttgart").child(getDate()).push().setValue(seq + ":" + value);

        //  add value to the uuid tree
        String time = Long.toString(System.currentTimeMillis());

        mRef.child("uuid").child(IPVS_WEATHER_UUID).push().setValue(time + ":" + value);


    }






    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: View Started");
    }

    //Get the day in the correct format required by the assignment
    public  String getDate() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = String.valueOf(sdfDate.format(now));
        return strDate;
    }


    //MARK: - Weather Service

    @Override
    public void onTemperatureChanged(final float value) {

        runOnUiThread(new Runnable() {

            public void run() {
                tempCur.setText(String.format("%2.1f", value));
            }
        });
        //we write the new temperature to both trees
        String curtemp=Float.toString(value);
        writeValue(curtemp);
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
