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

import java.text.SimpleDateFormat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

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
    public Button button2;


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
        tempBase = (TextView) findViewById(R.id.tempupt);
        timeBase=(TextView) findViewById(R.id.timeupt);
        tempCur = (TextView) findViewById(R.id.Temper);

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
        String date = getDate();
        /*Calendar c = Calendar.getInstance();
        SimpleDateFormat df=new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate=df.format(c.getTime());*/
        //String date=new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Log.d(TAG, "the date is " + date);
        Log.d(TAG, "adding value...");
        /*String t="25.02";
        mRef.child("teams").child("14").setValue(t);
        Log.d(TAG, "reading value....");*/
        button2=(Button) findViewById(R.id.update);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final TextView tempUpd = (TextView)findViewById(R.id.tempupt);
                final TextView timeUpd = (TextView)findViewById(R.id.timeupt);

                mRef.child("teams").child("14").addValueEventListener(new ValueEventListener() {
                //mRef.child("uuid").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String str = dataSnapshot.getValue(String.class);
                        int size=str.length();
                        String temp=str.substring(size-4,size);
                        String time=str.substring(0,size-5);
                        //float i = Float.parseFloat(str);
                        //Log.w("12342", "TEST");
                        Log.d(TAG, "temp = " + temp);
                        Log.d(TAG, "time = " + time);
                        tempUpd.setText(temp);
                        timeUpd.setText(time);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }


                });
            }
        });
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
        //insert new nodes to both trees
        /*mRef.child("uuid").child(IPVS_WEATHER_UUID).push().setValue(value);
        mRef.child("location").child("Stuttgart").child(getDate()).push().setValue(value);*/
        String curtemp=Float.toString(value);
        String millis=String.valueOf(System.currentTimeMillis());
        millis=millis+":"+curtemp;
        mRef.child("teams").child("14").setValue(millis);
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
