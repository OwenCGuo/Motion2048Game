package lab4_204_40.uwaterloo.ca.lab4_204_40;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;

//import ca.uwaterloo.sensortoy.LineGraphView;

public class MainActivity extends AppCompatActivity {
    //declare accelerometer handler class
    AccelerometerSensorEventListener accelerometerSensorEventListener;

    public static final int GAMEBOARD_DIMENSION = 1080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this is for the overall layout
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relLay);

        //Initialize the layout for the gameboard
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        //set gameboard layout dimensions
        relativeLayout.getLayoutParams().width = GAMEBOARD_DIMENSION;
        relativeLayout.getLayoutParams().height = GAMEBOARD_DIMENSION;
        //set background of gameboard layout to the gameboard png image
        relativeLayout.setBackgroundResource(R.drawable.gameboard);

        //Retrive the accelerometer and FSM textboxes
        //TextView accelerometerText = (TextView)findViewById(R.id.txtAccelerometer);
        TextView FSMText = (TextView)findViewById(R.id.txtFSMState);

        //Instantiate a Timer for the game loop
        Timer myGameLoop = new Timer();

        //Instantiate a GameLoopTask for the myGameLoop Timer
        //pass in Mainactivity, gameboard relative layout, and application context
        GameLoopTask myGameLoopTask = new GameLoopTask(MainActivity.this, relativeLayout, getApplicationContext());

        //Timer.schedule for myGameLoop with myGameLoopTask as "TimerTask" and 50 ms period
        myGameLoop.schedule(myGameLoopTask, 50, 50);

        //Instantiate the accelerometer handler and being to process the results
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accelerometerSensorEventListener = new AccelerometerSensorEventListener(FSMText, myGameLoopTask);
        sensorManager.registerListener(accelerometerSensorEventListener, accelSensor, SensorManager.SENSOR_DELAY_GAME);

        if (savedInstanceState != null) {
            accelerometerSensorEventListener.highAccel = savedInstanceState.getFloatArray(Constants.HIGHEST_ACCELEROMETER);
        }
    }

    /*
    public void SaveData(View view)
    {
        //on click, accelerometer data is saved in csv
        Utilities.writeXBy3FloatArrayToFile("Lab2_accelerometer_readings.csv", getExternalFilesDir("Lab2 Data"),
                100, accelerometerSensorEventListener.accelerometerData);
    }

    public void ClearData(View view)
    {
        accelerometerSensorEventListener.ResetData();
    }
    */

    @Override
    protected void onSaveInstanceState(Bundle b){
        super.onSaveInstanceState(b);
        //Saves the historical high values
        b.putFloatArray(Constants.HIGHEST_ACCELEROMETER, accelerometerSensorEventListener.highAccel);
    }
}
