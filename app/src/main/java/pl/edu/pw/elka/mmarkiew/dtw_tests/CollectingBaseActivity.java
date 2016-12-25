package pl.edu.pw.elka.mmarkiew.dtw_tests;

import java.util.ArrayList;
import java.util.List;

import pl.edu.pw.elka.mmarkiew.dtw.struct.TimeData;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager.LayoutParams;

public abstract class CollectingBaseActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    protected static boolean sIsGathering;

    protected List<TimeData> mData = new ArrayList<TimeData>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initSensor();
    }

    private void initSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sIsGathering = false;
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sIsGathering = false;
        mSensorManager.unregisterListener(this);
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_VOLUME_UP:
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (!sIsGathering)
                    mData = new ArrayList<TimeData>();

                sIsGathering = true;
            } else {
                sIsGathering = false;

                System.out.println("Gathered: " + mData.size());

                processGatheredData(mData);
            }

            return true;
        default:
            return super.dispatchKeyEvent(event);
        }
    }

    protected abstract void processGatheredData(List<TimeData> data);

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        System.out.println("onAccuracyChanged: sensor=" + sensor + ", accuracy=" + accuracy);
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (!sIsGathering)
            return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        mData.add(new TimeData(System.currentTimeMillis(), new double[] { x, y, z }));
    }

}
