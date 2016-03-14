package com.mbientlab.metawear.app;

import android.content.Context;
import android.graphics.Color;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public abstract class SingleDataSensorFragment extends SensorFragment {
    private static final float UNSPECIFIED_SAMPLING_PERIOD= 0;
    protected String csvHeaderDataName, filenameExtraString= "";
    protected float samplingPeriod;

    private final ArrayList<Entry> sensorData= new ArrayList<>();

    protected SingleDataSensorFragment(int stringResId, String dataName, int layoutId, float samplingPeriod, float min, float max) {
        super(stringResId, layoutId, min, max);
        csvHeaderDataName= dataName;
        this.samplingPeriod= samplingPeriod;
    }

    protected SingleDataSensorFragment(int stringResId, String dataName, int layoutId, float min, float max) {
        this(stringResId, dataName, layoutId, UNSPECIFIED_SAMPLING_PERIOD, min, max);
    }

    @Override
    protected String saveData() {
        final String CSV_HEADER = String.format("time,%s%n", csvHeaderDataName);
        String filename = String.format("%s_%tY%<tm%<td-%<tH%<tM%<tS%<tL.csv", getContext().getString(sensorResId), Calendar.getInstance());
        if (!filenameExtraString.isEmpty()) {
            filename+= "_" + filenameExtraString;
        }

        try {
            FileOutputStream fos = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(CSV_HEADER.getBytes());

            LineData data = chart.getLineData();
            List<String> chartXValues= data.getXVals();
            LineDataSet tempDataSet = data.getDataSetByIndex(0);
            if (samplingPeriod == UNSPECIFIED_SAMPLING_PERIOD) {
                for (int i = 0; i < chartXValues.size(); i++) {
                    fos.write(String.format("%s,%.3f%n", chartXValues.get(i), tempDataSet.getEntryForXIndex(i).getVal()).getBytes());
                }
            } else {
                for (int i = 0; i < data.getXValCount(); i++) {
                    fos.write(String.format("%.3f,%.3f%n", i * samplingPeriod, tempDataSet.getEntryForXIndex(i).getVal()).getBytes());
                }
            }
            fos.close();
            return filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void resetData(boolean clearData) {
        if (clearData) {
            chartXValues.clear();
            sensorData.clear();
            sampleCount = 0;
        }

        LineDataSet tempDataSet = new LineDataSet(sensorData, csvHeaderDataName);
        tempDataSet.setColor(Color.MAGENTA);
        tempDataSet.setDrawCircles(false);

        LineData data= new LineData(chartXValues);
        data.addDataSet(tempDataSet);
        data.setDrawValues(false);
        chart.setData(data);
    }
}
