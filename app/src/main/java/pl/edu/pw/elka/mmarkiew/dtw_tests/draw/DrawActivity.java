package pl.edu.pw.elka.mmarkiew.dtw_tests.draw;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pl.edu.pw.elka.mmarkiew.dtw.struct.TimeData;
import pl.edu.pw.elka.mmarkiew.dtw.util.FileUtils;
import pl.edu.pw.elka.mmarkiew.dtw.util.ProcessingMethods;
import pl.edu.pw.elka.mmarkiew.dtw_tests.MainActivity;
import pl.edu.pw.elka.mmarkiew.dtw_tests.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DrawActivity extends Activity implements OnClickListener {

    public static String DIR = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/kj/drawings";

    private Button mDrawButton;
    private Button mSaveButton;
    private Button mLoadButton;
    private TextView mLabelTextView;
    private TextView mPatternPathTextView;
    private TextView mResultTextView;

    private List<TimeData> mPoints = new ArrayList<TimeData>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_draw);

        mDrawButton = (Button) findViewById(R.id.draw_draw);
        mSaveButton = (Button) findViewById(R.id.draw_save);
        mLoadButton = (Button) findViewById(R.id.draw_load);
        mLabelTextView = (TextView) findViewById(R.id.draw_label);
        mPatternPathTextView = (TextView) findViewById(R.id.draw_pattern_path);
        mResultTextView = (TextView) findViewById(R.id.draw_result);

        mDrawButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);
        mLoadButton.setOnClickListener(this);
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
    public void onClick(final View v) {
        switch (v.getId()) {
        case R.id.draw_draw:
            draw();
            break;
        case R.id.draw_save:
            save();
            break;
        case R.id.draw_load:
            loadOne(DIR);
            break;

        default:
            // ignore
        }
    }

    private void draw() {
        Intent intent = new Intent(this, DrawingResultActivity.class);
        intent.putExtra(DrawingResultActivity.EXTRA_PATTERN_DATA_FILE_PATH,
                mPatternPathTextView.getText());
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case 1:
                // data = points

                mPoints.clear();

                for (int i = 0; i < data.getIntExtra("size", -1); ++i) {
                    long timestamp = data.getLongExtra("timestamp_" + i, -1);
                    double[] values = new double[2];
                    values[0] = data.getDoubleExtra("px_" + i, -1);
                    values[1] = data.getDoubleExtra("py_" + i, -1);

                    mPoints.add(new TimeData(timestamp, values));
                }

                mPoints = ProcessingMethods.filterDifferenceDelta(
                        ProcessingMethods.filterTresholdEpsylon(mPoints, 0), 0.08);

                mLabelTextView.setText("Gathered points: " + mPoints.size());
                mResultTextView.setText(data.getStringExtra("result_string"));

                break;
            }
        } else {
            mLabelTextView.setText("Error");
        }
    }

    private void save() {
        if (mPoints.isEmpty()) {
            System.out.println("No points gathered");

            Toast.makeText(this, "\n\tNo points gathered\n", Toast.LENGTH_SHORT).show();

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
                            Toast.makeText(DrawActivity.this, "Not saved", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            if (new File(DIR, filename).exists())
                                new AlertDialog.Builder(DrawActivity.this)
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
                                                        Toast.makeText(DrawActivity.this,
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
                            String data = FileUtils.convertPointsToString(mPoints);
                            FileUtils.saveToFile(DIR, filename, data);
                        } catch (Exception e) {
                            e.printStackTrace();

                            Toast.makeText(DrawActivity.this, "Error while saving",
                                    Toast.LENGTH_SHORT).show();

                            return;
                        }

                        Toast.makeText(DrawActivity.this, "Saved into: " + DIR + "/" + filename,
                                Toast.LENGTH_SHORT).show();
                    }
                });

        dialog.show();
    }

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
                    mPatternPathTextView.setText(dirPath + "/" + files.get(item1));
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
