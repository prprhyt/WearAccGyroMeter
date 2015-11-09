package example.com.wearaccgyroMeter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends WearableActivity  implements SensorEventListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =  new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;
    private SensorManager manager;

    private GoogleApiClient mGoogleApiClient;

    private float sensordata[]={0,0,0,0,0,0};
    private boolean sensorchange = false;

    private Handler _handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addApi(Wearable.API).build();
        _handler = new Handler();

        _handler.postDelayed(new Runnable() {//Timer的な
            @Override
            public void run() {

                RefreshSensorNum();

                _handler.postDelayed(this, 250);

            }
        }, 500);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }
    @Override
    protected void onStop(){
        super.onStop();
        manager.unregisterListener(this);
        _handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        ArrayList<List<Sensor>> sensors = new ArrayList<List<Sensor>>();
        sensors.add( manager.getSensorList(Sensor.TYPE_ACCELEROMETER));
        sensors.add( manager.getSensorList(Sensor.TYPE_GYROSCOPE));

        for(List<Sensor> sensor : sensors){
            if(sensor.size()>0){
               manager.registerListener(this,sensor.get(0),SensorManager.SENSOR_DELAY_UI);
            }
        }
        mGoogleApiClient.connect();

    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mTextView.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        }

        @Override
        public void onSensorChanged(SensorEvent sensor_e) {
            // TODO Auto-generated method stub
                    if(sensor_e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        sensordata[0] = sensor_e.values[SensorManager.DATA_X];
                        sensordata[1] = sensor_e.values[SensorManager.DATA_Y];
                        sensordata[2] = sensor_e.values[SensorManager.DATA_Z];
                        //sensorchange=true;
                        }
                    if(sensor_e.sensor.getType()==Sensor.TYPE_GYROSCOPE){
                        sensordata[3] = sensor_e.values[SensorManager.DATA_X];
                        sensordata[4] = sensor_e.values[SensorManager.DATA_Y];
                        sensordata[5] = sensor_e.values[SensorManager.DATA_Z];
                        //sensorchange=true;
                    }
            /*if(sensorchange==true) {
                String str = "AccSensorNum:"
                        + "\nX:" + sensordata[0]
                        + "\nY:" + sensordata[1]
                        + "\nZ:" + sensordata[2]
                        + "\n\n" + "GyroSensorNum:"
                        + "\nX:" + sensordata[3]
                        + "\nY:" + sensordata[4]
                        + "\nZ:" + sensordata[5];
                mTextView.setText(str);
                //SendToHandheld(sensordata, "sensordata","/datapath");
                sensorchange=false;
            }*/
        }

        public void RefreshSensorNum(){
            String str = "AccSensorNum:"
                + "\nX:" + sensordata[0]
                + "\nY:" + sensordata[1]
                + "\nZ:" + sensordata[2]
                + "\n\n" + "GyroSensorNum:"
                + "\nX:" + sensordata[3]
                + "\nY:" + sensordata[4]
                + "\nZ:" + sensordata[5];
            mTextView.setText(str);
            SendToHandheld(sensordata, "sensordata","/datapath");

        }

        @Override
        public void onConnected(Bundle bundle){
            Log.d("TAG", "onConnected");
        }
        @Override
        public void onConnectionSuspended(int i){
            Log.d("TAG","onConnectionSuspended");
        }
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult){
            Log.e("TAG", "onConnectionFailed");
        }

    public void SendToHandheld(float senddata[],String keyname,String datapath){
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(datapath);
        DataMap dataMap = dataMapRequest.getDataMap();
        //Data set
            dataMap.putFloatArray(keyname, senddata);

        // Data Push
        PutDataRequest request = dataMapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d("TAG", "onResult:" + dataItemResult.getStatus().toString());
            }
        });

    }

}
