package pl.edu.pw.elka.mmarkiew.dtw_tests.draw;

import java.io.IOException;
import java.util.ArrayList;

import pl.edu.pw.elka.mmarkiew.dtw.DTW;
import pl.edu.pw.elka.mmarkiew.dtw.struct.TimeData;
import pl.edu.pw.elka.mmarkiew.dtw.struct.TimeSerie;
import pl.edu.pw.elka.mmarkiew.dtw.util.FileUtils;
import pl.edu.pw.elka.mmarkiew.dtw.util.ProcessingMethods;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;

public class DrawingResultActivity extends Activity implements OnTouchListener {

    public static final String EXTRA_PATTERN_DATA_FILE_PATH = "patter_file";

    private ArrayList<TimeData> mPoints = new ArrayList<TimeData>();

    private int mWidth;
    private int mHeight;

    private DrawingView mDrawingView;

    private TimeSerie mPatternSerie = new TimeSerie();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // TODO make it right
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;

        ArrayList<Pair<Float, Float>> pattern = new ArrayList<Pair<Float, Float>>();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String path = extras.getString(EXTRA_PATTERN_DATA_FILE_PATH);

            if (path != null && !path.isEmpty()) {
                try {
                    String dataString = FileUtils.loadFromFile(path, "");
                    mPatternSerie = new TimeSerie(dataString);

                    for (int i = 0; i < mPatternSerie.getSize(); ++i) {
                        double[] values = mPatternSerie.getData(i);
                        pattern.add(new Pair<Float, Float>((float) (mWidth * values[0]),
                                (float) (mHeight * values[1])));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        mDrawingView = new DrawingView(this, null, pattern);

        mDrawingView.setOnTouchListener(this);

        setContentView(mDrawingView);
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        mDrawingView.onTouchEvent(event);

        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mPoints.clear();
            break;
        case MotionEvent.ACTION_MOVE:
            mPoints.add(new TimeData(System.currentTimeMillis(), new double[] { touchX / mWidth,
                    touchY / mHeight }));
            break;
        case MotionEvent.ACTION_UP:
            fin();
            break;
        default:
            return false;
        }
        return true;
    }

    private void fin() {
        Intent intent = getIntent();

        intent.putExtra("size", mPoints.size());

        for (int i = 0; i < mPoints.size(); ++i) {
            TimeData data = mPoints.get(i);

            intent.putExtra("timestamp_" + i, data.timestamp);
            intent.putExtra("px_" + i, data.values[0]);
            intent.putExtra("py_" + i, data.values[1]);
        }

        if (mPatternSerie.getSize() > 0 && mPoints.size() > 0) {
            DTW dtw = new DTW();

            TimeSerie ts = new TimeSerie(ProcessingMethods.filterDifferenceDelta(
                    ProcessingMethods.filterTresholdEpsylon(mPoints, 0), 0.08));
            double a = dtw.processEuclides(mPatternSerie, ts).getmDistance();
            double b = dtw.processPlain(mPatternSerie, ts).getmDistance();
            double c = dtw.processCumulatePlain(mPatternSerie, ts).getmDistance();
            double d = dtw.processSeparatedPlain(mPatternSerie, ts);

            String res = "Euclides:\t\t\t" + a;
            res += "\nPlain:\t\t\t\t\t" + b;
            res += "\nCumulated:\t" + c;
            res += "\nSeparated:\t\t" + d;
            res += "\n\nAverage:\t\t\t" + ((a + b + c + d) / 4);

            intent.putExtra("result_string", res);
        }

        setResult(RESULT_OK, intent);
        finish();
    }

}
