package pl.edu.pw.elka.mmarkiew.dtw_tests;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;
import pl.edu.pw.elka.mmarkiew.dtw.DTW;
import pl.edu.pw.elka.mmarkiew.dtw.struct.TimeData;
import pl.edu.pw.elka.mmarkiew.dtw.struct.TimeSerie;
import pl.edu.pw.elka.mmarkiew.dtw.util.FileUtils;
import pl.edu.pw.elka.mmarkiew.dtw.util.ProcessingMethods;
import pl.edu.pw.elka.mmarkiew.dtw.util.ProcessingMethods.Methods;
import pl.edu.pw.elka.mmarkiew.dtw_tests.draw.DrawActivity;

public class MainActivity extends CollectingBaseActivity implements OnClickListener {

    public static String DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/kj";
    public static String DIR_RESULTS = DIR + "/results";

    private TabHost mTabs;

    /*
     * Save
     */

    protected List<TimeData> mSaveFilteredData = new ArrayList<TimeData>();
    private List<Map<String, String>> mSaveStringData = new ArrayList<Map<String, String>>();
    private ListView mSaveDataListView;

    private Button mSaveButton;
    private Button mSaveResultButton;
    private ToggleButton mSaveFilterButton;
    private Button mScreenCaptureButton;
    private CheckBox mScreenCaptureCheckBox;

    /*
     * Check
     */

    private Map<String, Double> mCheckResults = new HashMap<String, Double>();

    private List<TimeData> mCheckLoadedData = new ArrayList<TimeData>();
    private List<Map<String, String>> mCheckStringResultsData = new ArrayList<Map<String, String>>();
    private ListView mCheckResultsDataListView;

    private Button mCheckLoadButton;
    private Button mCheckDuplicateButton;
    private Button mCheckButton;
    private TextView mCheckLoadedFilenameTextView;
    private TextView mCheckLoadedAmountTextView;

    /*
     * Match
     */

    private Map<String, List<TimeData>> mMatchLoadedData = new HashMap<String, List<TimeData>>();
    private Map<String, Double> mMatchResults = new HashMap<String, Double>();

    private List<Map<String, String>> mMatchStringLoadedData = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> mMatchStringResultsData = new ArrayList<Map<String, String>>();
    private ListView mMatchLoadedDataListView;
    private ListView mMatchResultsDataListView;

    private Button mMatchLoadButton;
    private Button mMatchButton;
    private CheckBox mMatchSortCheckBox;
    private TextView mMatchDirTextView;
    private Spinner mMatchMethodSpinner;

    /*
     * Chart
     */

    private GraphicalView mChart;
    private XYMultipleSeriesDataset mChartDataset;
    private XYMultipleSeriesRenderer mChartRenderer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabs = (TabHost) findViewById(android.R.id.tabhost);
        mTabs.setup();

        TabHost.TabSpec spec = mTabs.newTabSpec("Save");
        spec.setContent(R.id.tab_save);
        spec.setIndicator("Save", getResources().getDrawable(android.R.drawable.arrow_down_float));
        mTabs.addTab(spec);

        spec = mTabs.newTabSpec("Check");
        spec.setContent(R.id.tab_check);
        spec.setIndicator("Check", getResources().getDrawable(android.R.drawable.arrow_up_float));
        mTabs.addTab(spec);

        spec = mTabs.newTabSpec("Match");
        spec.setContent(R.id.tab_match);
        spec.setIndicator("Match", getResources().getDrawable(android.R.drawable.btn_plus));
        mTabs.addTab(spec);

        spec = mTabs.newTabSpec("Chart");
        spec.setContent(R.id.tab_chart);
        spec.setIndicator("Chart", getResources().getDrawable(android.R.drawable.btn_minus));
        mTabs.addTab(spec);

        /*
         * Save
         */

        mSaveButton = (Button) findViewById(R.id.save_save);
        mSaveResultButton = (Button) findViewById(R.id.save_save_result);
        mSaveFilterButton = (ToggleButton) findViewById(R.id.save_filter);
        mSaveDataListView = (ListView) findViewById(R.id.save_values);
        mScreenCaptureButton = (Button) findViewById(R.id.save_screen_capture);
        mScreenCaptureCheckBox = (CheckBox) findViewById(R.id.save_screen_capture_checkbox);

        mSaveButton.setOnClickListener(this);
        mSaveResultButton.setOnClickListener(this);
        mSaveFilterButton.setOnClickListener(this);
        mScreenCaptureButton.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!sIsGathering)
                        mData = new ArrayList<TimeData>();

                    sIsGathering = true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    sIsGathering = false;

                    System.out.println("Gathered: " + mData.size());

                    processGatheredData(mData);
                }

                return true;
            }
        });
        mScreenCaptureCheckBox.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {
                mScreenCaptureButton.setEnabled(mScreenCaptureCheckBox.isChecked());
            }
        });

        mSaveDataListView.setAdapter(new SimpleAdapter(this, mSaveStringData,
                R.layout.simple_list_item_3, new String[] { "v1", "v2", "v3" }, new int[] {
                        R.id.text1, R.id.text2, R.id.text3 }));

        /*
         * Check
         */

        mCheckLoadButton = (Button) findViewById(R.id.check_load);
        mCheckDuplicateButton = (Button) findViewById(R.id.check_dupliacte);
        mCheckButton = (Button) findViewById(R.id.check_check);
        mCheckResultsDataListView = (ListView) findViewById(R.id.check_results);
        mCheckLoadedFilenameTextView = (TextView) findViewById(R.id.check_load_filename);
        mCheckLoadedAmountTextView = (TextView) findViewById(R.id.check_load_amount);

        mCheckLoadButton.setOnClickListener(this);
        mCheckDuplicateButton.setOnClickListener(this);
        mCheckButton.setOnClickListener(this);

        mCheckResultsDataListView.setAdapter(new SimpleAdapter(this, mCheckStringResultsData,
                R.layout.simple_list_item_2, new String[] { "key", "value" }, new int[] {
                        R.id.text1, R.id.text2 }));

        /*
         * Match
         */

        mMatchLoadedDataListView = (ListView) findViewById(R.id.match_loaded_info);
        mMatchResultsDataListView = (ListView) findViewById(R.id.match_results);
        mMatchLoadButton = (Button) findViewById(R.id.match_load);
        mMatchButton = (Button) findViewById(R.id.match_match);
        mMatchSortCheckBox = (CheckBox) findViewById(R.id.match_sort_results);
        mMatchDirTextView = (TextView) findViewById(R.id.match_dir);
        mMatchMethodSpinner = (Spinner) findViewById(R.id.match_method);

        mMatchLoadButton.setOnClickListener(this);
        mMatchButton.setOnClickListener(this);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new String[] { "" + Methods.EUCLIDES,
                        "" + Methods.PLAIN, "" + Methods.CUMULATED_PLAIN, "" + Methods.SEPARATED });
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMatchMethodSpinner.setAdapter(dataAdapter);

        mMatchLoadedDataListView.setAdapter(new SimpleAdapter(this, mMatchStringLoadedData,
                R.layout.simple_list_item_2, new String[] { "key", "value" }, new int[] {
                        R.id.text1, R.id.text2 }));

        mMatchResultsDataListView.setAdapter(new SimpleAdapter(this, mMatchStringResultsData,
                R.layout.simple_list_item_3, new String[] { "v1", "v2", "v3" }, new int[] {
                        R.id.text1, R.id.text2, R.id.text3 }));

        mMatchResultsDataListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2,
                    final long arg3) {
                if (mMatchLoadButton.getVisibility() == View.GONE) {
                    mMatchLoadButton.setVisibility(View.VISIBLE);
                    mMatchDirTextView.setVisibility(View.VISIBLE);
                    mMatchLoadedDataListView.setVisibility(View.VISIBLE);
                    mMatchSortCheckBox.setVisibility(View.VISIBLE);
                    mMatchButton.setVisibility(View.VISIBLE);
                    mMatchMethodSpinner.setVisibility(View.VISIBLE);
                } else {
                    mMatchLoadButton.setVisibility(View.GONE);
                    mMatchDirTextView.setVisibility(View.GONE);
                    mMatchLoadedDataListView.setVisibility(View.GONE);
                    mMatchSortCheckBox.setVisibility(View.GONE);
                    mMatchButton.setVisibility(View.GONE);
                    mMatchMethodSpinner.setVisibility(View.GONE);
                }
            }
        });

        /*
         * Chart
         */

        ((Button) findViewById(R.id.chart_open_x)).setOnClickListener(this);
        ((Button) findViewById(R.id.chart_open_y)).setOnClickListener(this);
        ((Button) findViewById(R.id.chart_open_z)).setOnClickListener(this);
        ((Button) findViewById(R.id.chart_open_cumulated)).setOnClickListener(this);
        ((Button) findViewById(R.id.chart_open_diff_x)).setOnClickListener(this);
        ((Button) findViewById(R.id.chart_open_diff_y)).setOnClickListener(this);
        ((Button) findViewById(R.id.chart_open_diff_z)).setOnClickListener(this);
        ((Button) findViewById(R.id.chart_open_diff_cumulated)).setOnClickListener(this);

        mChartDataset = new XYMultipleSeriesDataset();
        mChartRenderer = new XYMultipleSeriesRenderer();

        mChartRenderer.setBackgroundColor(Color.BLACK);
        mChartRenderer.setMarginsColor(Color.BLACK);
        mChartRenderer.setGridColor(Color.WHITE);
        mChartRenderer.setApplyBackgroundColor(true);
        mChartRenderer.setShowLabels(true);
        mChartRenderer.setShowGridX(true);
        mChartRenderer.setZoomButtonsVisible(true);
        mChartRenderer.setChartTitleTextSize(25);
        mChartRenderer.setAxisTitleTextSize(17);
        mChartRenderer.setLabelsTextSize(16);
        mChartRenderer.setLegendTextSize(20);

        // mChart = ChartFactory.getTimeChartView(this, mChartDataset,
        // mChartRenderer, "ss:SSS");
        // mChart.setBackgroundColor(Color.BLACK);
        //
        // LinearLayout layout = (LinearLayout) findViewById(R.id.tab_chart);
        // layout.addView(mChart);

        requestWritePermission();
    }

    private void requestWritePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1867);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.move_activity:
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            return true;
        case R.id.draw_activity:
            startActivity(new Intent(getApplicationContext(), DrawActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void processGatheredData(final List<TimeData> data) {
        mSaveFilteredData = ProcessingMethods.filterDefault(data);

        if (mSaveFilterButton.isChecked())
            refreshValuesData(mSaveFilteredData);
        else
            refreshValuesData(mData);
    }

    @Override
    public void onBackPressed() {
        finish();

        super.onBackPressed();
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
        case R.id.save_save:
            save();

            break;
        case R.id.save_save_result:
            saveResult();

            break;
        case R.id.save_filter:
            if (mSaveFilterButton.isChecked())
                refreshValuesData(mSaveFilteredData);
            else
                refreshValuesData(mData);

            break;
        case R.id.check_load:
            loadOne(DIR);

            break;
        case R.id.check_dupliacte:
            duplicateCollectedData();

            break;
        case R.id.check_check:
            checkOne();

            break;
        case R.id.match_load:
            loadDir();

            break;
        case R.id.match_match:
            match();

            break;
        case R.id.chart_open_x:
            openChart(0, false);

            break;
        case R.id.chart_open_y:
            openChart(1, false);

            break;
        case R.id.chart_open_z:
            openChart(2, false);

            break;
        case R.id.chart_open_cumulated:
            openChartCumulated(false);

            break;
        case R.id.chart_open_diff_x:
            openChart(0, true);

            break;
        case R.id.chart_open_diff_y:
            openChart(1, true);

            break;
        case R.id.chart_open_diff_z:
            openChart(2, true);

            break;
        case R.id.chart_open_diff_cumulated:
            openChartCumulated(true);

            break;

        default:
            break;
        }
    }

    /*
     * Save
     */

    private void save() {
        if (mSaveFilteredData.isEmpty()) {
            Toast.makeText(this, "No data to save", Toast.LENGTH_SHORT).show();

            return;
        }

        final EditText inputText = new EditText(this);
        inputText.setInputType(InputType.TYPE_CLASS_TEXT);
        inputText.setHint("Eg. filename.txt");
        inputText.setHint("data.txt");

        Builder dialog = new AlertDialog.Builder(this).setTitle("Filename:").setView(inputText)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        final String filename = inputText.getText().toString();

                        if (filename.length() == 0) {
                            Toast.makeText(MainActivity.this, "Not saved", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            if (new File(DIR_RESULTS, filename).exists())
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("File already exists")
                                        .setMessage("Override?")
                                        .setCancelable(false)
                                        .setPositiveButton(android.R.string.ok,
                                                new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(
                                                            final DialogInterface dialog,
                                                            final int which) {
                                                        saveToFile(filename);
                                                    }
                                                })
                                        .setNegativeButton(android.R.string.cancel,
                                                new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(
                                                            final DialogInterface dialog,
                                                            final int which) {
                                                        Toast.makeText(MainActivity.this,
                                                                "File not saved",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }).show();
                            else
                                saveToFile(filename);
                        }
                    }

                    private void saveToFile(final String filename) {
                        try {
                            String data = FileUtils.convertPointsToString(mSaveFilteredData);
                            FileUtils.saveToFile(DIR_RESULTS, filename, data);
                        } catch (Exception e) {
                            e.printStackTrace();

                            Toast.makeText(MainActivity.this, "Error while saving",
                                    Toast.LENGTH_SHORT).show();

                            return;
                        }

                        Toast.makeText(MainActivity.this, "Saved into: " + DIR_RESULTS + "/" + filename,
                                Toast.LENGTH_SHORT).show();
                    }
                });

        dialog.show();
    }

    private void saveResult() {
        if (mSaveFilteredData.isEmpty()) {
            Toast.makeText(this, "No data to save", Toast.LENGTH_SHORT).show();

            return;
        }

        Builder dialog = new AlertDialog.Builder(this).setTitle("Are you sure?").setPositiveButton(
                android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        String dirFilename = new SimpleDateFormat("MM_dd/hh_mm_ss").format(Calendar
                                .getInstance().getTime()) + ".txt";

                        saveToFile(dirFilename);
                    }

                    private void saveToFile(final String filename) {
                        try {
                            String data = FileUtils.convertPointsToString(mSaveFilteredData);
                            FileUtils.saveToFile(DIR_RESULTS, filename, data);
                        } catch (Exception e) {
                            e.printStackTrace();

                            Toast.makeText(MainActivity.this, "Error while saving",
                                    Toast.LENGTH_SHORT).show();

                            return;
                        }

                        Toast.makeText(MainActivity.this,
                                "Saved into: " + DIR_RESULTS + "/" + filename, Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        dialog.show();
    }

    @SuppressLint("DefaultLocale")
    protected void build3Data(final List<Map<String, String>> list, final List<TimeData> values) {
        list.clear();
        list.add(putSimpleListItem3Data("", "Amount=", "" + values.size()));

        String val1 = "";
        String val2 = "";
        String val3 = "";

        for (TimeData data : values) {
            val1 = String.format("%.5f", data.values[0]);
            val2 = (data.values.length > 1 ? String.format("%.5f", data.values[1]) : "");
            val3 = (data.values.length > 2 ? String.format("%.5f", data.values[2]) : "");

            list.add(putSimpleListItem3Data(val1, val2, val3));
        }
    }

    protected HashMap<String, String> putSimpleListItem3Data(final String item1,
            final String item2, final String item3) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("v1", item1);
        item.put("v2", item2);
        item.put("v3", item3);

        return item;
    }

    private void refreshValuesData(final List<TimeData> data) {
        build3Data(mSaveStringData, data);

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ((SimpleAdapter) mSaveDataListView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    /*
     * Check
     */

    protected void loadOne(final String dirPath) {
        File dir = new File(dirPath);
        final List<String> files = new ArrayList<String>();

        if (!dir.exists()) {
            Toast.makeText(this, dirPath + " directory with data doesn't exist", Toast.LENGTH_SHORT)
                    .show();

            return;
        }

        for (File f : dir.listFiles())
            if (f.isFile())
                files.add(f.getName());
            else if (f.isDirectory())
                files.add(f.getName() + "/");

        ListView fileList = new ListView(this);
        fileList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, files));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(fileList);
        builder.setTitle(dirPath + "/");

        final Dialog dialog = builder.create();

        fileList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view,
                    final int item1, final long item2) {
                if (new File(dirPath, files.get(item1)).isDirectory()) {
                    loadOne(dirPath + "/" + files.get(item1));
                } else {
                    try {
                        String dataString = FileUtils.loadFromFile(dirPath, files.get(item1));

                        mCheckLoadedData = FileUtils.convertStringToPoints(dataString);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mCheckLoadedFilenameTextView.setText(dirPath + "\n"
                                        + files.get(item1));
                                mCheckLoadedAmountTextView.setText("" + mCheckLoadedData.size());
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();

                        Toast.makeText(MainActivity.this, "Fail to load data from file",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void duplicateCollectedData() {
        if (mSaveFilteredData == null || mSaveFilteredData.isEmpty()) {
            Toast.makeText(this, "No gathered data", Toast.LENGTH_SHORT).show();

            return;
        }

        mCheckLoadedData = mSaveFilteredData;

        mCheckLoadedFilenameTextView.setText("Collected data (not from file)");
        mCheckLoadedAmountTextView.setText("" + mCheckLoadedData.size());
    }

    private void checkOne() {
        mCheckResults.clear();

        if (mCheckLoadedData.isEmpty()) {
            Toast.makeText(this, "No loaded data", Toast.LENGTH_SHORT).show();

            return;
        }

        if (mSaveFilteredData.isEmpty()) {
            Toast.makeText(this, "No data captured", Toast.LENGTH_SHORT).show();

            return;
        }

        DTW dtw = new DTW();
        TimeSerie s1 = new TimeSerie(mCheckLoadedData);
        TimeSerie s2 = new TimeSerie(mSaveFilteredData);

        mCheckResults.put("" + Methods.EUCLIDES, dtw.processEuclides(s1, s2).getmDistance());
        mCheckResults.put("" + Methods.PLAIN, dtw.processPlain(s1, s2).getmDistance());
        mCheckResults.put("" + Methods.SEPARATED, dtw.processSeparatedPlain(s1, s2));
        mCheckResults.put("" + Methods.CUMULATED_PLAIN, new DTW().processCumulatePlain(s1, s2)
                .getmDistance());

        refreshResultData(mCheckResults);
    }

    private void build2ResultData(final List<Map<String, String>> list,
            final Map<String, Double> values) {
        list.clear();

        Iterator<Entry<String, Double>> it = values.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Double> next = it.next();

            list.add(putSimpleListItem2Data(next.getKey(), "" + next.getValue()));
        }
    }

    protected HashMap<String, String> putSimpleListItem2Data(final String item1, final String item2) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("key", item1);
        item.put("value", item2);

        return item;
    }

    protected void refreshResultData(final Map<String, Double> data) {
        build2ResultData(mCheckStringResultsData, data);

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ((SimpleAdapter) mCheckResultsDataListView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    /*
     * Match
     */

    private void loadDir() {
        File dir = new File(DIR);
        final List<String> files = new ArrayList<String>();

        if (!dir.exists()) {
            Toast.makeText(this, DIR + " directory with data doesn't exist", Toast.LENGTH_SHORT)
                    .show();

            return;
        }

        for (File f : dir.listFiles())
            if (f.isDirectory())
                files.add(f.getName());

        ListView fileList = new ListView(this);
        fileList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, files));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(fileList);

        final Dialog dialog = builder.create();

        fileList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view,
                    final int item1, final long item2) {
                final File dir = new File(DIR + "/" + files.get(item1));
                final List<String> files = new ArrayList<String>();

                for (File f : dir.listFiles())
                    if (f.isFile())
                        files.add(f.getName());

                if (files.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No files in directory", Toast.LENGTH_SHORT)
                            .show();

                    dialog.dismiss();
                    return;
                }

                mMatchLoadedData.clear();

                for (String filename : files) {
                    try {
                        String dataString = FileUtils.loadFromFile(dir.getAbsolutePath(), filename);
                        mMatchLoadedData.put(filename, FileUtils.convertStringToPoints(dataString));
                    } catch (IOException e) {
                        e.printStackTrace();

                        mMatchLoadedData.clear();

                        Toast.makeText(MainActivity.this,
                                "Fail to load data from file=" + filename, Toast.LENGTH_SHORT)
                                .show();

                        break;
                    }
                }

                refreshMatchLoadedData(mMatchLoadedData);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mMatchDirTextView.setText(dir.getAbsolutePath());
                    }
                });

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void match() {
        if (mSaveFilteredData.isEmpty()) {
            Toast.makeText(this, "No captured data", Toast.LENGTH_SHORT).show();

            return;
        }

        if (mMatchLoadedData.isEmpty()) {
            Toast.makeText(this, "No loaded data", Toast.LENGTH_SHORT).show();

            return;
        }

        Methods method = Methods.valueOf(((TextView) mMatchMethodSpinner.getSelectedView())
                .getText().toString());

        mMatchResults.clear();
        DTW dtw = new DTW();
        TimeSerie capturedSerie = new TimeSerie(mSaveFilteredData);

        Iterator<Entry<String, List<TimeData>>> it = mMatchLoadedData.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, List<TimeData>> next = it.next();
            TimeSerie patternSerie = new TimeSerie(next.getValue());
            double result = 0;

            switch (method) {
            case EUCLIDES:
                result = dtw.processEuclides(patternSerie, capturedSerie).getmDistance();
                break;
            case PLAIN:
                result = dtw.processPlain(patternSerie, capturedSerie).getmDistance();
                break;
            case CUMULATED_PLAIN:
                result = dtw.processCumulatePlain(patternSerie, capturedSerie).getmDistance();
                break;
            case SEPARATED:
                result = dtw.processSeparatedPlain(patternSerie, capturedSerie);
                break;
            }

            mMatchResults.put(next.getKey(), result);
        }

        refreshMatchResultsData(mMatchResults);
    }

    private void build2BatchLoadedData(final List<Map<String, String>> list,
            final Map<String, List<TimeData>> values) {
        list.clear();
        list.add(putSimpleListItem2Data("filename", "amount"));

        Iterator<Entry<String, List<TimeData>>> it = values.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, List<TimeData>> next = it.next();

            list.add(putSimpleListItem2Data(next.getKey(), "" + next.getValue().size()));
        }
    }

    protected void refreshMatchLoadedData(final Map<String, List<TimeData>> data) {
        build2BatchLoadedData(mMatchStringLoadedData, data);

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ((SimpleAdapter) mMatchLoadedDataListView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    private void build3BatchResultsData(final List<Map<String, String>> list,
            final Map<String, Double> values) {
        list.clear();
        list.add(putSimpleListItem3Data("filename", "result", "match"));

        // Smallest find
        String filename = "";
        double min = Double.MAX_VALUE;
        Iterator<Entry<String, Double>> it = values.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Double> next = it.next();

            if (next.getValue() < min) {
                filename = next.getKey();
                min = next.getValue();
            }
        }

        it = values.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Double> next = it.next();

            list.add(putSimpleListItem3Data(next.getKey(), String.format("%.5f", next.getValue()),
                    (next.getKey().equals(filename) ? "MATCH" : "")));
        }

        if (mMatchSortCheckBox.isChecked()) {
            Collections.sort(list, new Comparator<Map<String, String>>() {

                @Override
                public int compare(final Map<String, String> lhs, final Map<String, String> rhs) {
                    double l = 0;
                    double r = 1;
                    try {
                        l = Double.parseDouble(lhs.get("v2").replace(",", "."));
                        r = Double.parseDouble(rhs.get("v2").replace(",", "."));
                    } catch (NumberFormatException e) {
                        // ignore label
                    }

                    return (l > r ? 1 : -1);
                }
            });
        }
    }

    protected void refreshMatchResultsData(final Map<String, Double> data) {
        build3BatchResultsData(mMatchStringResultsData, data);

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ((SimpleAdapter) mMatchResultsDataListView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    /*
     * Chart
     */

    // private void createChart() {
    // if (mSaveFilteredData.isEmpty()) {
    // Toast.makeText(this, "No captured data", Toast.LENGTH_SHORT).show();
    //
    // return;
    // }
    //
    // if (mCheckLoadedData.isEmpty()) {
    // Toast.makeText(this, "No loaded check data", Toast.LENGTH_SHORT).show();
    //
    // return;
    // }
    //
    // TimeSeries pattern = new TimeSeries("Pattern");
    // TimeSeries captured = new TimeSeries("Captured");
    //
    // mChartDataset.clear();
    // mChartDataset.addSeries(0, pattern);
    // mChartDataset.addSeries(1, captured);
    //
    // XYSeriesRenderer firstRenderer = new XYSeriesRenderer();
    // XYSeriesRenderer secondRenderer = new XYSeriesRenderer();
    //
    // firstRenderer.setColor(Color.RED);
    // firstRenderer.setLineWidth(2);
    //
    // secondRenderer.setColor(Color.BLUE);
    // secondRenderer.setLineWidth(2);
    //
    // mChartRenderer.removeAllRenderers();
    // mChartRenderer.addSeriesRenderer(0, firstRenderer);
    // mChartRenderer.addSeriesRenderer(1, secondRenderer);
    //
    // long startTime = mCheckLoadedData.get(0).timestamp;
    // for (Data data : mCheckLoadedData)
    // pattern.add(data.timestamp - startTime, data.values[0]);
    //
    // startTime = mSaveFilteredData.get(0).timestamp;
    // for (Data data : mSaveFilteredData)
    // captured.add(data.timestamp - startTime, data.values[0]);
    //
    // mChart.repaint();
    // }

    private boolean checkChartData() {
        if (mSaveFilteredData.isEmpty()) {
            Toast.makeText(this, "No captured data", Toast.LENGTH_SHORT).show();

            return false;
        }

        if (mCheckLoadedData.isEmpty()) {
            Toast.makeText(this, "No loaded check data", Toast.LENGTH_SHORT).show();

            return false;
        }

        return true;
    }

    private void openChart(final int axis, final boolean diff) {
        if (!checkChartData() || axis < 0 || axis > 2)
            return;

        if (diff)
            refreshDiffDatasetRenderer(axis, false);
        else
            refreshDatasetRenderer(axis, false);

        Intent intent = ChartFactory.getTimeChartIntent(getBaseContext(), mChartDataset,
                mChartRenderer, "ss:SSS");
        startActivity(intent);
    }

    private void openChartCumulated(final boolean diff) {
        if (!checkChartData())
            return;

        if (diff)
            refreshDiffDatasetRenderer(0, true);
        else
            refreshDatasetRenderer(0, true);

        Intent intent = ChartFactory.getTimeChartIntent(getBaseContext(), mChartDataset,
                mChartRenderer, "ss:SSS");
        startActivity(intent);
    }

    private void refreshDatasetRenderer(final int axis, final boolean cumulated) {
        if (cumulated) {
            mChartRenderer.setChartTitle("Cumulated values / sqrt(E(v^2))");
        } else {
            String axisLeter = "";

            switch (axis) {
            case 0:
                axisLeter = "X";
                break;
            case 1:
                axisLeter = "Y";
                break;
            case 2:
                axisLeter = "Z";
                break;
            }

            mChartRenderer.setChartTitle(axisLeter + " axis");
        }

        mChartRenderer.setYTitle("Acceleration");
        mChartRenderer.setXTitle("Relative time");

        TimeSeries patternSerie = new TimeSeries("Pattern");
        TimeSeries capturedSerie = new TimeSeries("Captured");

        mChartDataset.clear();
        mChartDataset.addSeries(0, patternSerie);
        mChartDataset.addSeries(1, capturedSerie);

        XYSeriesRenderer patternRenderer = new XYSeriesRenderer();
        XYSeriesRenderer capturedRenderer = new XYSeriesRenderer();

        patternRenderer.setColor(Color.RED);
        patternRenderer.setLineWidth(2);

        capturedRenderer.setColor(Color.BLUE);
        capturedRenderer.setLineWidth(2);

        mChartRenderer.removeAllRenderers();
        mChartRenderer.addSeriesRenderer(0, patternRenderer);
        mChartRenderer.addSeriesRenderer(1, capturedRenderer);

        long startTime = mCheckLoadedData.get(0).timestamp;
        for (TimeData data : mCheckLoadedData)
            patternSerie.add(data.timestamp - startTime, getChartData(data, axis, cumulated));

        startTime = mSaveFilteredData.get(0).timestamp;
        for (TimeData data : mSaveFilteredData)
            capturedSerie.add(data.timestamp - startTime, getChartData(data, axis, cumulated));
    }

    private void refreshDiffDatasetRenderer(final int axis, final boolean cumulated) {
        if (cumulated) {
            mChartRenderer.setChartTitle("Diff cumulated values / sqrt(E(v^2))");
        } else {
            String axisLeter = "";

            switch (axis) {
            case 0:
                axisLeter = "X";
                break;
            case 1:
                axisLeter = "Y";
                break;
            case 2:
                axisLeter = "Z";
                break;
            }

            mChartRenderer.setChartTitle("Diff " + axisLeter + " axis");
        }

        mChartRenderer.setYTitle("Diff acceleration");
        mChartRenderer.setXTitle("Relative time");

        TimeSeries diffSerie = new TimeSeries("Diff");

        mChartDataset.clear();
        mChartDataset.addSeries(0, diffSerie);

        XYSeriesRenderer diffRenderer = new XYSeriesRenderer();

        FillOutsideLine fill = new FillOutsideLine(FillOutsideLine.Type.BOUNDS_ALL);
        fill.setColor(Color.GREEN);
        diffRenderer.addFillOutsideLine(fill);
        diffRenderer.setColor(Color.GREEN);
        diffRenderer.setLineWidth(2);

        mChartRenderer.removeAllRenderers();
        mChartRenderer.addSeriesRenderer(0, diffRenderer);

        List<TimeData> shorterData = null;
        List<TimeData> longerData = null;
        if (mSaveFilteredData.size() < mCheckLoadedData.size()) {
            shorterData = mSaveFilteredData;
            longerData = mCheckLoadedData;
        } else {
            longerData = mSaveFilteredData;
            shorterData = mCheckLoadedData;
        }

        long startTime = shorterData.get(0).timestamp;
        for (int j = 0; j < shorterData.size(); ++j) {
            TimeData dataS = shorterData.get(j);
            TimeData dataL = longerData.get(j);

            diffSerie.add(dataS.timestamp - startTime, getChartData(dataS, axis, cumulated)
                    - getChartData(dataL, axis, cumulated));
        }
    }

    private double getChartData(final TimeData data, final int axis, final boolean cumulated) {
        if (!cumulated)
            return data.values[axis];

        double result = 0;
        for (double d : data.values)
            result += d * d;

        return Math.sqrt(result);
    }

}
