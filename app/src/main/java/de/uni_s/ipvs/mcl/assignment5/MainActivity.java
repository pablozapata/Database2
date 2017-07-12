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


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private BluetoothAdapter adapter;
    private DatabaseManager databaseManager;
    private Button connect_button;

    private boolean butStatus = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseManager.writeData();

        //TODO : CREATE NEW FUNCTIONS FOR WRITE AND READ (IN DATABASEMANAGER FILE!!!!) AND INVOCATE THEM FROM HERE VIA BUTTONS
 ;

    }
}
