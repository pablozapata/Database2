package de.uni_s.ipvs.mcl.assignment5;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
//import android.icu.text.SimpleDateFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements WeatherService.WeatherServiceCallback{

    private static final String TAG = MainActivity.class.getName();
    private static final String IPVS_WEATHER_UUID = "00000002-0000-0000-fdfd-fdfdfdfdfdfd";

    private BluetoothAdapter adapter;
    private WeatherService weatherService;
    private BLEScanner scanner;
    private Button button;
    //private TextView tempBase;
    private TextView timeBase;
    private TextView tempCur;
    private DatabaseManager databaseManager;



    private Handler mHandler;

    public boolean butStatus = false;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button_write;
    private EditText input;


    public DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

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
        tempCur = (TextView)findViewById(R.id.tempRealT);

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


        //write a new value when the "write" bottom is pressed
        button_write = (Button) findViewById(R.id.writevalue);
        button_write.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String temp_towrite = String.valueOf(tempCur.getText());
                writeValue(temp_towrite);


            }
        });


        //Read last value (Task 1.2)
        button2=(Button) findViewById(R.id.readLast);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final TextView tempdisp = (TextView)findViewById(R.id.readLast_res);
                final TextView timedisp = (TextView)findViewById(R.id.readLast_time);
                //DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();


                //mRef.child("uuid").child(IPVS_WEATHER_UUID).addListenerForSingleValueEvent(new ValueEventListener() {
                mRef.child("teams").child("14").addListenerForSingleValueEvent (new ValueEventListener() {
                    //mRef.child("uuid").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       // String str= dataSnapshot.getValue().toString();
                        //String str=dataSnapshot.getKey();
                        String lastval=readNew(dataSnapshot);

                        int size=lastval.length();
                        String temp = lastval.substring(14,size);
                        String time=lastval.substring(0,13);

                        tempdisp.setText(temp + "ºC");
                        timedisp.setText(time);


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
                final TextView tempdisp = (TextView)findViewById(R.id.suscribe_res);
                final TextView timedisp = (TextView)findViewById(R.id.subscribe_time);

                mRef.child("teams").child("14").addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String lastval=readNew(dataSnapshot);

                        int size=lastval.length();
                        String temp = lastval.substring(14,size);
                        String time=lastval.substring(0,13);
                        tempdisp.setText(temp + "ºC");
                        timedisp.setText(time);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }


                });


            }
        });

        // Get average value once

        button4=(Button) findViewById(R.id.average);
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final TextView disp = (TextView)findViewById(R.id.average_res);
                mRef.child("teams").child("14").addListenerForSingleValueEvent (new ValueEventListener() {
                    //mRef.child("uuid").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        float averval=getAver(dataSnapshot);

                        disp.setText(averval + "ºC");

                    }
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });

            }
        });



    }
    //Reading new data once
    public String readNew(DataSnapshot dataSnapshot){

        int i=0;
        long min=0;
        long dif=0;
        long curtime =System.currentTimeMillis();
        String lastval="";
        //Working with database as an array
        for (DataSnapshot messageSnapshot:dataSnapshot.getChildren()){
            String msg=(String) messageSnapshot.getValue(String.class);

            //Some messages was saved incorrectly, we need only messages with size = 18
            if (msg.length()==18){
                //extracting time stamp
                String sub=msg.substring(0,13);
                long mills=Long.parseLong(sub);
                //trying to find minimum difference between current time and time stamp from node in DB
                if (i==0){
                    min=curtime-mills;
                }
                dif=curtime-mills;

                if (dif<mills){
                    lastval=msg;
                }

                i++;
            }

        }

        //rerurn node, which is closer to current time
        return lastval;
    }

    //Calculating average value of temperature
    public float getAver(DataSnapshot dataSnapshot){
        int i=0;
        String msg="";
        float curval=0;
        float sum=0;
        String sub="";

        for (DataSnapshot messageSnapshot:dataSnapshot.getChildren()){

            msg=(String) messageSnapshot.getValue(String.class);

            //Some messages was saved incorrectly, we need only messages with size = 18
            
            if (msg.length()==18){
                sub=msg.substring(14,msg.length());
                curval=Float.parseFloat(sub);
                //Just summing up all values
                sum+=curval;
                //Track number of iterations
                i++;

            }

            }
            // return average value
        return sum/i;
    }


    //we create a new node in both trees
    private void writeValue( String value) {
        //DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        //add value to location tree
        //Random random = new Random();
        //String seq = new BigInteger(40, random).toString(32);

        //mRef.child("location").child("Stuttgart").child(getDate()).push().setValue(seq + ":" + value);

        //  extracting current time
        String time = String.valueOf(System.currentTimeMillis());

        //getting values of temperature
        String temp= value.substring(0,4);


        //mRef.child("uuid").child(IPVS_WEATHER_UUID).push().setValue(time + ":" + value);

        //Writing values to database
        String mes=time+":"+temp;
        Log.d(TAG,"added to database "+mes);
        mRef.child("teams").child("14").push().setValue(mes);

        Toast.makeText(getApplicationContext(),"New value added to database",Toast.LENGTH_SHORT).show();


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
        final TextView temp1 = (TextView)findViewById(R.id.tempRealT);

        String temer=String.valueOf(value);
        runOnUiThread(new Runnable() {

            public void run() {
                tempCur.setText(value+"ºC");
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
