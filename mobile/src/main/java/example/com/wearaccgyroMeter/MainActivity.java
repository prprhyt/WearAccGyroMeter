package example.com.wearaccgyroMeter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener  {

    private GoogleApiClient mGoogleApiClient;
    private TextView resultview;
    private String resultstr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultview = (TextView) findViewById(R.id.resulttxtview);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addApi(Wearable.API).build();
        resultstr="";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        mGoogleApiClient.connect();

    }
    @Override
    public void onConnected(Bundle bundle){
        Log.d("TAG", "onConnected");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }
    @Override
    public void onConnectionSuspended(int i){
        Log.d("TAG", "onConnectionSuspended");
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){
        Log.e("TAG", "onConnectionFailed");
    }
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d("TAG", "DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d("TAG", "DataItem changed: " + event.getDataItem().getUri());
                DataMap dataMap = DataMap.fromByteArray(event.getDataItem().getData());
                float sensordataArray[] = dataMap.getFloatArray("sensordata");
                resultstr = "AccSensorNum:"
                        + "\nX:" + sensordataArray[0]
                        + "\nY:" + sensordataArray[1]
                        + "\nZ:" + sensordataArray[2]
                        + "\n\n" + "GyroSensorNum:"
                        + "\nX:" + sensordataArray[3]
                        + "\nY:" + sensordataArray[4]
                        + "\nZ:" + sensordataArray[5];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultview.setText(resultstr);
                    }
                });
            }
        }
    }
}
