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

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.AsyncOperation.CompletionHandler;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.app.help.HelpOption;
import com.mbientlab.metawear.app.help.HelpOptionAdapter;
import com.mbientlab.metawear.module.Gpio;
import com.mbientlab.metawear.module.Gpio.AnalogReadMode;
import com.mbientlab.metawear.module.Gpio.PullMode;
import com.mbientlab.metawear.module.Timer;


public class GpioFragment extends SingleDataSensorFragment {

    private Gpio gpioModule;

    public GpioFragment() {
           super(R.string.navigation_fragment_gpio, "adc", R.layout.fragment_gpio, 0, 1023);
    }



    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button fanon = (Button) view.findViewById(R.id.fanon);
        fanon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gpioModule.setPinPullMode((byte) 0x07, PullMode.PULL_DOWN);


            }


        });

            final Button lightson = (Button) view.findViewById(R.id.lightson);
            lightson.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                            gpioModule.setPinPullMode((byte) 0x05, PullMode.PULL_DOWN);

               }


            });



                final Button pumpon = (Button) view.findViewById(R.id.pumpon);
               pumpon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        gpioModule.setPinPullMode((byte) 0x06, PullMode.PULL_DOWN);

                    }


                });


                final Button fanoff = (Button) view.findViewById(R.id.fanoff);
        fanoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                gpioModule.setPinPullMode((byte) 0x07, Gpio.PullMode.PULL_UP);



            }


        });

        final Button lightsoff = (Button) view.findViewById(R.id.lightsoff);
        lightsoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                gpioModule.setPinPullMode((byte) 0x05, Gpio.PullMode.PULL_UP);

            }


        });
        final Button pumpoff = (Button) view.findViewById(R.id.pumpoff);
        pumpoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gpioModule.setPinPullMode((byte) 0x06, Gpio.PullMode.PULL_UP);



            }


        });



    }

    @Override
    protected void boardReady() throws UnsupportedModuleException {
        gpioModule= mwBoard.getModule(Gpio.class);

    }

    @Override
    protected void fillHelpOptionAdapter(HelpOptionAdapter adapter) {
        adapter.add(new HelpOption(R.string.config_name_gpio_pin, R.string.config_desc_gpio_pin));
        adapter.add(new HelpOption(R.string.config_name_gpio_read_mode, R.string.config_desc_gpio_read_mode));
        adapter.add(new HelpOption(R.string.config_name_output_control, R.string.config_desc_output_control));
        adapter.add(new HelpOption(R.string.config_name_pull_mode, R.string.config_desc_pull_mode));
    }

    @Override
    protected void setup() {

    }

    @Override
    protected void clean() {

    }


}
