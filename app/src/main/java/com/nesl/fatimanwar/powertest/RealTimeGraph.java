package com.nesl.fatimanwar.powertest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import com.jjoe64.graphview.series.OnDataPointTapListener;

import java.io.*;
import android.media.MediaScannerConnection;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import android.widget.Toast;

import android.os.BatteryManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

public class RealTimeGraph extends ActionBarActivity {

    String TAG = "RealTimeGraph";

    private final static String STORETEXT="storetext.txt";

    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    //private Runnable mTimer2;
    private LineGraphSeries<DataPoint> mSeries1;
    //private LineGraphSeries<DataPoint> mSeries2;
    //private double graph2LastXValue = 5d;

    double referenceTime = System.currentTimeMillis() / 1000;
    double powerOut;
    GraphView graph;
    File traceFile;

    //TextView voltage;
    //TextView current;
/*
    int v = 0;
    long c = 0;
    BatteryManager mBatteryManager;
    BigDecimal batteryPower = new BigDecimal("0.000");;
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_power);
/*
        this.registerReceiver(this.batteryInfoReceiver,	new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        mBatteryManager =
                (BatteryManager) getSystemService(Context.BATTERY_SERVICE);


*/
        //voltage = (TextView) findViewById(R.id.voltage);
        //current = (TextView) findViewById(R.id.current);
        try {
            traceFile = new File(getExternalFilesDir(null), "TraceFile.txt");
        if (traceFile.exists()) {
            traceFile.delete();
        }
            traceFile.createNewFile();
        }catch (IOException e)
        {
            Log.e(TAG, "Unable to write to the TraceFile.txt file.");
        }

        graph = (GraphView) findViewById(R.id.graph);
        mSeries1 = new LineGraphSeries<DataPoint>(generateData());
        graph.addSeries(mSeries1);

        //GraphView graph2 = (GraphView) rootView.findViewById(R.id.graph2);
        //mSeries2 = new LineGraphSeries<DataPoint>();
        //graph2.addSeries(mSeries2);
        graph.setTitle("Aggregate Power Graph (Watt)");
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(500);

        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(1.85);
        graph.getViewport().setMaxY(1.95);

        //graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getGridLabelRenderer().setNumVerticalLabels(11);
        //graph.getGridLabelRenderer().setNumHorizontalLabels(5);
        //graph.getGridLabelRenderer().setVerticalAxisTitle("Power(mW)");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("time(sec)");

        mSeries1.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(getApplicationContext(), "Power: "+ dataPoint.getY()+" W", Toast.LENGTH_SHORT).show();
            }
        });
    }
/*
    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            v = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);
            c = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            BigDecimal bg1 = new BigDecimal("v");
            BigDecimal bg2 = new BigDecimal("c");
            BigDecimal bg3 = bg1.multiply(bg2);
            BigDecimal bg4 = new BigDecimal("1000000000");
            batteryPower = bg3.divide(bg4);
            //Log.e(TAG, ""+bg1);
            //Log.e(TAG, ""+bg2);
            Log.d(TAG, "" + batteryPower);
        }
    };
*/
    private DataPoint[] generateData() {
        //Log.i(TAG, "entered generate data");
        int count = 10;
        DataPoint[] values = new DataPoint[1];
        //for (int i=0; i<count; i++) {
        //double x = i;
        //double f = mRand.nextDouble()*0.15+0.3;
        //double y = Math.sin(i*f+2) + mRand.nextDouble()*0.3;
        double time = (System.currentTimeMillis()/1000) - referenceTime;
        //Log.d(TAG, ""+time);
        double power = powerOut;
        //Log.d(TAG, ""+powerOut);
        DataPoint v = new DataPoint(time, power);
        values[0] = v;
        //}
        return values;
    }



    @Override
    public void onResume() {
        super.onResume();
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                new ReadFile(mSeries1, referenceTime, getBaseContext(), traceFile).execute();
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(mTimer1, 1000);
/*
        mTimer2 = new Runnable() {
            @Override
            public void run() {
                graph2LastXValue += 1d;
                mSeries2.appendData(new DataPoint(graph2LastXValue, getRandom()), true, 40);
                mHandler.postDelayed(this, 200);
            }
        };
        mHandler.postDelayed(mTimer2, 1000);
        */
    }

    @Override
    public void onPause() {
        //mHandler.removeCallbacks(mTimer1);
        //mHandler.removeCallbacks(mTimer2);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mTimer1);
        //mHandler.removeCallbacks(mTimer2);
        super.onDestroy();
    }

    double mLastRandom = 2;
    Random mRand = new Random();
    private double getRandom() {
        return mLastRandom += mRand.nextDouble()*0.5 - 0.25;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_read_power, menu);
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
}

class ReadFile extends AsyncTask<Void, Void, BigDecimal> {

    String TAG = "RealTimeGraph";

    private double powerInWatt;
    private double refTime;
    private Context context;
    private File traceFile;

    LineGraphSeries<DataPoint> threadSeries;
    public ReadFile(LineGraphSeries<DataPoint> threadSeries, double refTime, Context context, File traceFile){
        this.threadSeries = threadSeries;
        this.refTime = refTime;
        this.traceFile = traceFile;
    }

    @Override
    protected BigDecimal doInBackground(Void... params) {
        BufferedReader buffered_reader_voltage=null;
        BufferedReader  buffered_reader_current=null;
        //Log.i(TAG, "background");

        BigDecimal power = new BigDecimal("0.000");
        try {
            //InputStream istream = Runtime.getRuntime().exec(STORETEXT).getInputStream();
            //InputStreamReader istream_reader = new InputStreamReader(istream);
            //BufferedReader buffered_reader = new BufferedReader(istream_reader);
            buffered_reader_voltage = new BufferedReader(new FileReader("/sys/class/power_supply/battery/voltage_now"));
            buffered_reader_current = new BufferedReader(new FileReader("/sys/class/power_supply/battery/current_now"));
            String line_voltage;
            String line_current;

            while (((line_voltage = buffered_reader_voltage.readLine()) != null) && ((line_current = buffered_reader_current.readLine()) != null)){
                //voltage.setText(line_voltage);
                //current.setText(line_current);
              //  power = (Integer)((BigInteger(line_voltage) * Integer.valueOf(line_current))) / 1000000;
         //       byte input = {(byte)power};
                BigDecimal bg1 = new BigDecimal(line_voltage);
                BigDecimal bg2 = new BigDecimal(line_current);
                BigDecimal bg3 = bg1.multiply(bg2);
                BigDecimal bg4 = new BigDecimal("1000000000");
                power = bg3.divide(bg4);
                Log.d(TAG, ""+bg1);
                Log.d(TAG, ""+bg2);
                Log.e(TAG, "" + power);


                // Adds a line to the trace file
                BufferedWriter writer = new BufferedWriter(new FileWriter(traceFile, true /*append*/));
                writer.write(""+power+",");
                writer.close();
                // Refresh the data so it can seen when the device is plugged in a
                // computer. You may have to unplug and replug the device to see the
                // latest changes. This is not necessary if the user should not modify
                // the files.
              /*  MediaScannerConnection.scanFile(context,
                        new String[] { traceFile.toString() },
                        null,
                        null);*/
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to write to the TraceFile.txt file.");
            e.printStackTrace();
        }finally
        {
            try
            {
                if (buffered_reader_voltage != null)
                    buffered_reader_voltage.close();
                if (buffered_reader_current != null)
                    buffered_reader_current.close();
            }
            catch (IOException ex)
            {
                // TODO
                ex.printStackTrace();
            }
        }
        return power;
    }

    protected void onProgressUpdate() {
        //called when the background task makes any progress
    }

    protected void onPreExecute() {
        //called before doInBackground() is started
    }

    @Override
    protected void onPostExecute(BigDecimal result) {
        double time = (System.currentTimeMillis()/1000) - refTime;
        //Log.d(TAG, ""+time);
        double power = result.doubleValue();
        //Log.d(TAG, ""+power);
        DataPoint v = new DataPoint(time, power);
        threadSeries.appendData(v, true, 10000);
    }
}