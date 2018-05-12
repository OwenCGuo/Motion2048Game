package lab4_204_40.uwaterloo.ca.lab4_204_40;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

import java.util.Arrays;

//import static lab3_204_40.uwaterloo.ca.lab3_204_40.GameLoopTask.myDirection.LEFT;

//import ca.uwaterloo.sensortoy.LineGraphView;


class AccelerometerSensorEventListener implements SensorEventListener {
    //Acceleration Data
    public float accelerometerData[][] = new float[100][3];
    public float highAccel[] = new float[3];

    //UI Elements
    //private LineGraphView graph;
    //private TextView output;
    private TextView instanceState;     //for displaying FSM state
    private GameLoopTask myGameLoopTask;    //declare a GameLoopTask here

    public AccelerometerSensorEventListener(TextView FSMState, GameLoopTask newGameLoopTask){

        //output = outputView;
        //graph = outputGraph;
        instanceState = FSMState;   //take in TextView for displaying FSM state
        myGameLoopTask = newGameLoopTask;   //take in GameLoopTask to connect it to the FSM

        //set arrays to 0's but im not sure why this is needed since the Java spec
        //guarantees that numerical values are automatically set to zero
        ResetData();
    }

    public void onAccuracyChanged(Sensor s, int i){

    }

    public void ResetData()
    {
        java.util.Arrays.fill(highAccel, 0, 3, 0);
        for (float[] row : accelerometerData)
            Arrays.fill(row, 0);
    }

    public void onSensorChanged(SensorEvent se){
        if(se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            //filter readings upon sensor change
            filterReadings(se.values);

            /*
            String s = String.format("Acceleration: (%f, %f, %f)" +
                            "\nHistorical Acceleration Highs: (%f, %f, %f)\n",accelerometerData[99][0]
                    , accelerometerData[99][1], accelerometerData[99][2] ,
                    highAccel[0], highAccel[1], highAccel[2]);
            */


            //output.setText(s);
        }

        //graph.addPoint(accelerometerData[99]);
    }

    //region FSM variables
    private float FILTER_CONSTANT = 10.0f;

    //Setup FSM states
    private enum myState{WAIT, RISE_R, FALL_R, FALL_L, RISE_L,
        RISE_U, FALL_U, FALL_D, RISE_D, DETERMINED};
    private enum mySig{SIG_R, SIG_L, SIG_U, SIG_D, SIG_X};

    //Setup FSM states and Signatures here
    private myState state = myState.WAIT;
    private mySig signature = mySig.SIG_X;

    //Setup threshold constants here
    private final float[] THRES_A = {0.5f, 2.0f, -0.2f};
    private final float[] THRES_B = {-0.4f, -2.0f, 0.2f};

    //Setup FSM sample counter here
    private final int SAMPLEDEFAULT = 30;
    private int sampleCounter = SAMPLEDEFAULT;
    //endregion

    //region FSM methods
    public void filterReadings(float[] values){
        //record high reading
        for (int j = 0; j < 3; j++){
            if(Math.abs(values[j]) > Math.abs(highAccel[j])){
                highAccel[j] = values[j];
            }
        }

        //shifting accelerometer data indices
        for(int i = 0; i < 99; i++){
            for(int j = 0; j < 3; j++){
                accelerometerData[i][j] = accelerometerData[i + 1][j];
            }
        }

        //filter with filter constant
        for(int k = 0; k < 3; k++){

            accelerometerData[99][k] += (values[k] - accelerometerData[99][k]) / FILTER_CONSTANT;
        }

        //call FSM function here
        callFSM();

        //takes 30 samples from FSM to analyze peaks
        if(sampleCounter <= 0){

            //based on state of FSM, assign movement signature value to appropriate signature
            //set text of FSM textView accordingly as well as GameLoopTask instance's direction
            //using setDirection(GameloopTask.myDirection) method
            if(state == myState.DETERMINED){
                if(signature == mySig.SIG_L) {
                    instanceState.setText("LEFT");
                    myGameLoopTask.setDirection(GameLoopTask.myDirection.LEFT);
                }
                else if(signature == mySig.SIG_R) {
                    instanceState.setText("RIGHT");
                    myGameLoopTask.setDirection(GameLoopTask.myDirection.RIGHT);
                }
                else if(signature == mySig.SIG_U) {
                    instanceState.setText("UP");
                    myGameLoopTask.setDirection(GameLoopTask.myDirection.UP);
                }
                else if(signature == mySig.SIG_D) {
                    instanceState.setText("DOWN");
                    myGameLoopTask.setDirection(GameLoopTask.myDirection.DOWN);
                }
                else {
                    instanceState.setText("Undetermined");
                    myGameLoopTask.setDirection(GameLoopTask.myDirection.NO_MOVEMENT);
                }
            }
            else{
                //if FSM state is wait, set text ot "undetermined" and direction of GameBlockTask
                //to no movement
                state = myState.WAIT;
                instanceState.setText("Undetermined");
                myGameLoopTask.setDirection(GameLoopTask.myDirection.NO_MOVEMENT);
            }

            sampleCounter = SAMPLEDEFAULT;
            state = myState.WAIT;

        }

    }

    //implement FSM
    public void callFSM(){

        float deltaX = accelerometerData[99][0] - accelerometerData[98][0]; //change in x acceleration
        float deltaY = accelerometerData[99][1] - accelerometerData[98][1]; //change in y acceleration

        switch(state){
            //analyze sample data and assign FSM states based on them
            //each direction has two states: one for initial peak/ wave, and one for after the peak
            //when wave goes in opposite direction
            //both must happen for a direction to be assigned and for state to be determined
            case WAIT:
                sampleCounter = SAMPLEDEFAULT;
                signature = mySig.SIG_X;

                //detects changes in acceleration, assigns states based on which signal it starts to match
                if (deltaY > THRES_A[0]){
                    state = myState.RISE_U;
                }
                else if (deltaY < THRES_B[0]){
                    state = myState.FALL_D;
                }
                else if(deltaX > THRES_A[0]){
                    state = myState.RISE_R;
                }
                else if(deltaX < THRES_B[0]){
                    state = myState.FALL_L;
                }
                break;

            case FALL_D:
                if(deltaY >= 0){
                    if(accelerometerData[99][1] <= THRES_B[1]){
                        state = myState.RISE_D;
                    }
                    else{
                        state = myState.DETERMINED;
                    }
                }
                break;
            case FALL_L:
                if(deltaX >= 0){
                    if(accelerometerData[99][0] <= THRES_B[1]){
                        state = myState.RISE_L;
                    }
                    else{
                        state = myState.DETERMINED;
                    }
                }
                break;
            case FALL_R:
                if(deltaX >= 0){
                    if (accelerometerData[99][0] <= THRES_A[2]) {
                        signature = mySig.SIG_R;
                    }
                    state = myState.DETERMINED;
                }
                break;
            case FALL_U:
                if(deltaY >= 0){
                    if (accelerometerData[99][1] <= THRES_A[2]) {
                        signature = mySig.SIG_U;
                    }
                    state = myState.DETERMINED;
                }
                break;
            case RISE_D:
                if(deltaY <= 0){
                    if (accelerometerData[99][1] >= THRES_B[2]) {
                        signature = mySig.SIG_D;
                    }
                    state = myState.DETERMINED;
                }
                break;
            case RISE_L:
                if(deltaX <= 0){
                    if (accelerometerData[99][0] >= THRES_B[2]) {
                        signature = mySig.SIG_L;
                    }
                    state = myState.DETERMINED;
                }
                break;
            case RISE_R:
                if(deltaX <= 0){
                    if(accelerometerData[99][0] >= THRES_A[1]){
                        state = myState.FALL_R;
                    }
                    else{
                        state = myState.DETERMINED;
                    }
                }
                break;
            case RISE_U:
                if(deltaY <= 0){
                    if(accelerometerData[99][1] >= THRES_A[1]){
                        state = myState.FALL_U;
                    }
                    else{
                        state = myState.DETERMINED;
                    }
                }
                break;
            case DETERMINED:    //once signal has been determined, reach this state
                //Log.d("FSM: ", "State DETERMINED " + signature.toString());
                break;
            default:
                state = myState.WAIT;
                break;

        }

        sampleCounter--;

    }
    //endregion
}
