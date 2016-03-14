package com.mbientlab.metawear.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.mbientlab.metawear.RouteManager;

import java.io.File;
import java.util.ArrayList;

public abstract class SensorFragment extends ModuleFragmentBase {
    private static final float FPS= 30.f;
    private static final long UPDATE_PERIOD= (long) ((1 / FPS) * 1000L);

    protected final ArrayList<String> chartXValues= new ArrayList<>();
    protected LineChart chart;
    protected int sampleCount;

    protected float min, max;
    protected RouteManager streamRouteManager= null;

    private byte globalLayoutListenerCounter= 0;
    private final int layoutId;

    private final Runnable updateChartTask= new Runnable() {
        @Override
        public void run() {
            chart.notifyDataSetChanged();

            moveViewToLast();

            chartHandler.postDelayed(updateChartTask, UPDATE_PERIOD);
        }
    };
    private final Handler chartHandler= new Handler();

    protected SensorFragment(int sensorResId, int layoutId, float min, float max) {
        super(sensorResId);
        this.layoutId= layoutId;
        this.min= min;
        this.max= max;
    }

    private void moveViewToLast() {
        chart.setVisibleXRangeMaximum(120);
        if (sampleCount > 120) {
            chart.moveViewToX(sampleCount - 121);
        } else {
            chart.moveViewToX(sampleCount);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);

        View v= inflater.inflate(layoutId, container, false);
        final View scrollView = v.findViewById(R.id.scrollView);
        if (scrollView != null) {
            globalLayoutListenerCounter= 1;
            scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    LineChart.LayoutParams params = chart.getLayoutParams();
                    params.height = scrollView.getHeight();
                    chart.setLayoutParams(params);

                    globalLayoutListenerCounter--;
                    if (globalLayoutListenerCounter < 0) {
                        scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }

        return v;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chart = (LineChart) view.findViewById(R.id.data_chart);

        initializeChart();
        resetData(false);
        chart.invalidate();
        chart.setDescription(null);

        Button clearButton= (Button) view.findViewById(R.id.layout_two_button_left);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshChart(true);
            }
        });
        clearButton.setText(R.string.label_clear);

        ((Switch) view.findViewById(R.id.sample_control)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    moveViewToLast();
                    setup();
                    chartHandler.postDelayed(updateChartTask, UPDATE_PERIOD);
                } else {
                    chart.setVisibleXRangeMaximum(sampleCount);
                    clean();
                    if (streamRouteManager != null) {
                        streamRouteManager.remove();
                        streamRouteManager = null;
                    }
                    chartHandler.removeCallbacks(updateChartTask);
                }
            }
        });

        Button saveButton= (Button) view.findViewById(R.id.layout_two_button_right);
        saveButton.setText(R.string.label_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filename = saveData();

                if (filename != null) {
                    File dataFile = getActivity().getFileStreamPath(filename);
                    Uri contentUri = FileProvider.getUriForFile(getActivity(), "com.mbientlab.metawear.app.fileprovider", dataFile);

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, filename);
                    intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    startActivity(Intent.createChooser(intent, "Saving Data"));
                }
            }
        });
    }

    protected void refreshChart(boolean clearData) {
        chart.resetTracking();
        chart.clear();
        resetData(clearData);
        chart.invalidate();
    }

    protected void initializeChart() {
        ///< configure axis settings
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setStartAtZero(false);
        leftAxis.setAxisMaxValue(max);
        leftAxis.setAxisMinValue(min);
        chart.getAxisRight().setEnabled(false);
    }

    protected abstract void setup();
    protected abstract void clean();
    protected abstract String saveData();
    protected abstract void resetData(boolean clearData);
}
