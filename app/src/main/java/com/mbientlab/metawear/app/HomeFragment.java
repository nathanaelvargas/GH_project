/*
 * Copyright 2015 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights
 * granted under the terms of a software license agreement between the user who
 * downloaded the software, his/her employer (which must be your employer) and
 * MbientLab Inc, (the "License").  You may not use this Software unless you
 * agree to abide by the terms of the License which can be found at
 * www.mbientlab.com/terms . The License limits your use, and you acknowledge,
 * that the  Software may not be modified, copied or distributed and can be used
 * solely and exclusively in conjunction with a MbientLab Inc, product.  Other
 * than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this
 * Software and/or its documentation for any purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
 * PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
 * MBIENTLAB OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT, NEGLIGENCE,
 * STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED
 * TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST
 * PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY
 * DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software,
 * contact MbientLab Inc, at www.mbientlab.com.
 */

package com.mbientlab.metawear.app;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.MetaWearBoard.DeviceInformation;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.app.help.HelpOptionAdapter;
import com.mbientlab.metawear.module.Led;
import com.mbientlab.metawear.module.MultiChannelTemperature;
import com.mbientlab.metawear.module.SingleChannelTemperature;
import com.mbientlab.metawear.module.Switch;
import com.mbientlab.metawear.module.Temperature;
import com.mbientlab.metawear.module.Timer;

import java.util.List;

import javax.xml.transform.Source;

/**
 * Created by etsai on 8/22/2015.
 */


public class HomeFragment extends SingleDataSensorFragment {

    private static final int TEMP_SAMPLE_PERIOD= 500, SINGLE_EXT_THERM_INDEX= 1;
    private Timer timerModule;
    private static final String STREAM_KEY= "temp_stream";
    private Temperature tempModule;
    private List<MultiChannelTemperature.Source> availableSources;

    private final RouteManager.MessageHandler tempMsgHandler= new RouteManager.MessageHandler() {
        @Override
        public void process(Message message) {
            final Float celsius = message.getData(Float.class);



        }
    };



    public HomeFragment() {
        super(R.string.navigation_fragment_home, "celsius", R.layout.fragment_temperature, 15, 45);
    }

    public static class DfuProgressFragment extends DialogFragment {
        private ProgressDialog dfuProgress = null;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            dfuProgress = new ProgressDialog(getActivity());
            dfuProgress.setTitle(getString(R.string.title_firmware_update));
            dfuProgress.setCancelable(false);
            dfuProgress.setCanceledOnTouchOutside(false);
            dfuProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dfuProgress.setProgress(0);
            dfuProgress.setMax(100);
            dfuProgress.setMessage(getString(R.string.message_dfu));
            return dfuProgress;

        }

        public void updateProgress(int newProgress) {
            if (dfuProgress != null) {
                dfuProgress.setProgress(newProgress);
            }
        }
    }

    public static class MetaBootWarningFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity()).setTitle(R.string.title_warning)
                    .setPositiveButton(R.string.label_ok, null)
                    .setCancelable(false)
                    .setMessage(R.string.message_metaboot)
                    .create();

        }
    }

    private View myFragmentView;
    TextView t;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        myFragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        t = (TextView) myFragmentView.findViewById(R.id.spinach);

        Typeface myCustomFont = Typeface.createFromAsset(getActivity().getAssets(), "pacifico.ttf");
        t.setTypeface(myCustomFont);




        return myFragmentView;






    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    private void setupDfuDialog(AlertDialog.Builder builder, int msgResId) {
        builder.setTitle(R.string.title_firmware_update)
                .setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        initiateDfu();
                    }
                })
                .setNegativeButton(R.string.label_no, null)
                .setCancelable(false)
                .setMessage(msgResId);
    }

    private void initiateDfu() {
        final String DFU_PROGRESS_FRAGMENT_TAG = "dfu_progress_popup";
        DfuProgressFragment dfuProgressDialog = new DfuProgressFragment();
        dfuProgressDialog.show(getFragmentManager(), DFU_PROGRESS_FRAGMENT_TAG);

        getActivity().runOnUiThread(new Runnable() {
            final NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            final Notification.Builder checkpointNotifyBuilder = new Notification.Builder(getActivity()).setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setOnlyAlertOnce(true).setOngoing(true).setProgress(0, 0, true);
            final Notification.Builder progressNotifyBuilder = new Notification.Builder(getActivity()).setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setOnlyAlertOnce(true).setOngoing(true).setContentTitle(getString(R.string.notify_dfu_uploading));
            final int NOTIFICATION_ID = 1024;

            @Override
            public void run() {
                mwBoard.updateFirmware(new MetaWearBoard.DfuProgressHandler() {
                    @Override
                    public void reachedCheckpoint(State dfuState) {
                        switch (dfuState) {
                            case INITIALIZING:
                                checkpointNotifyBuilder.setContentTitle(getString(R.string.notify_dfu_bootloader));
                                break;
                            case STARTING:
                                checkpointNotifyBuilder.setContentTitle(getString(R.string.notify_dfu_starting));
                                break;
                            case VALIDATING:
                                checkpointNotifyBuilder.setContentTitle(getString(R.string.notify_dfu_validating));
                                break;
                            case DISCONNECTING:
                                checkpointNotifyBuilder.setContentTitle(getString(R.string.notify_dfu_disconnecting));
                                break;
                        }

                        manager.notify(NOTIFICATION_ID, checkpointNotifyBuilder.build());
                    }

                    @Override
                    public void receivedUploadProgress(int progress) {
                        progressNotifyBuilder.setContentText(String.format("%d%%", progress)).setProgress(100, progress, false);
                        manager.notify(NOTIFICATION_ID, progressNotifyBuilder.build());
                        ((DfuProgressFragment) getFragmentManager().findFragmentByTag(DFU_PROGRESS_FRAGMENT_TAG)).updateProgress(progress);
                    }
                }).onComplete(new AsyncOperation.CompletionHandler<Void>() {
                    final Notification.Builder builder = new Notification.Builder(getActivity()).setOnlyAlertOnce(true)
                            .setOngoing(false).setAutoCancel(true);

                    @Override
                    public void success(Void result) {
                        ((DialogFragment) getFragmentManager().findFragmentByTag(DFU_PROGRESS_FRAGMENT_TAG)).dismiss();
                        builder.setContentTitle(getString(R.string.notify_dfu_success)).setSmallIcon(android.R.drawable.stat_sys_upload_done);
                        manager.notify(NOTIFICATION_ID, builder.build());

                        Snackbar.make(getActivity().findViewById(R.id.drawer_layout), R.string.message_dfu_success, Snackbar.LENGTH_LONG).show();
                        fragBus.resetConnectionStateHandler(5000L);
                    }

                    @Override
                    public void failure(Throwable error) {
                        Log.e("MetaWearApp", "Firmware update failed", error);

                        Throwable cause = error.getCause() == null ? error : error.getCause();
                        ((DialogFragment) getFragmentManager().findFragmentByTag(DFU_PROGRESS_FRAGMENT_TAG)).dismiss();
                        builder.setContentTitle(getString(R.string.notify_dfu_fail)).setSmallIcon(android.R.drawable.ic_dialog_alert)
                                .setContentText(cause.getLocalizedMessage());
                        manager.notify(NOTIFICATION_ID, builder.build());

                        Snackbar.make(getActivity().findViewById(R.id.drawer_layout), error.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                        fragBus.resetConnectionStateHandler(5000L);
                    }
                });





            }
        });
    }



    @Override
    protected void fillHelpOptionAdapter(HelpOptionAdapter adapter) {

    }

    @Override
    public void reconnected() {
        setupFragment(getView());
    }

    private void configureChannel(Led.ColorChannelEditor editor) {
        final short PULSE_WIDTH = 1000;
        editor.setHighIntensity((byte) 31).setLowIntensity((byte) 31)
                .setHighTime((short) (PULSE_WIDTH >> 1)).setPulseDuration(PULSE_WIDTH)
                .setRepeatCount((byte) -1).commit();
    }

    private void setupFragment(final View v) {
        final String METABOOT_WARNING_TAG = "metaboot_warning_tag", SWITCH_STREAM = "switch_stream";

        if (!mwBoard.isConnected()) {
            return;
        }

        if (mwBoard.inMetaBootMode()) {
            if (getFragmentManager().findFragmentByTag(METABOOT_WARNING_TAG) == null) {
                new MetaBootWarningFragment().show(getFragmentManager(), METABOOT_WARNING_TAG);
            }
        } else {
            DialogFragment metabootWarning = (DialogFragment) getFragmentManager().findFragmentByTag(METABOOT_WARNING_TAG);
            if (metabootWarning != null) {
                metabootWarning.dismiss();
            }
        }

    }
    @Override
    protected void boardReady() throws UnsupportedModuleException {
        setupFragment(getView());
        timerModule= mwBoard.getModule(Timer.class);
        tempModule= mwBoard.getModule(Temperature.class);
        availableSources = ((MultiChannelTemperature) tempModule).getSources();


        setup();

    }




    @Override
    protected void setup() {

        final MultiChannelTemperature.NrfDie extTherm = (MultiChannelTemperature.NrfDie)
       // final MultiChannelTemperature.ExtThermistor extTherm= (MultiChannelTemperature.ExtThermistor)
                availableSources.get(MultiChannelTemperature.MetaWearRChannel.NRF_DIE);

       // extTherm.configure((byte) 0, (byte) 1, false);



            ((MultiChannelTemperature) tempModule).routeData().fromSource(extTherm).stream("temp_nrf_stream").commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                @Override
                public void success(RouteManager result) {
                   result.subscribe("temp_nrf_stream", new RouteManager.MessageHandler() {
                       @Override
                       public void process(Message message) {
                           Log.i("HomeFragment", String.format("Ext thermistor: %.3fc",message.getData(Float.class)));
                           t = (TextView) myFragmentView.findViewById(R.id.temperature);
                           t.setText(String.format("Ext thermistor: %.3fc",message.getData(Float.class)));

                       }
                   });
             ((MultiChannelTemperature) tempModule).readTemperature(extTherm);
                }
            });




    }



    @Override
    protected void clean() {

    }


    }

