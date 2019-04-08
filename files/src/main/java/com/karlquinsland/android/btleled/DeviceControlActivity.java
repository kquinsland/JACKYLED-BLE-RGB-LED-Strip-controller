/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.karlquinsland.android.btleled;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;

import android.widget.Spinner;
import android.widget.ArrayAdapter;

import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


// needed for view / button
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;


/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {

    // Logging
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    // the constants we'll use for extracting Intent info...
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";


    // handles for UI elements
    // the two text labels up top...
    private TextView mLabel_DeviceID;
    private TextView mLabel_ConnectionState;

    // little text element for user feedback / debugging
    private EditText mEditText_Status;

    // the various input values
    // the On/Off toggle
    private Switch mSwitch_OnOff;

    // the macro on off button
    private ToggleButton mToggle_Macros;

    // the macro selector
    private Spinner mSpinner_Macros;

    // the RGB Order selector
    private Spinner mSpinner_RGBOrder;

    // the list of macros we support
    private ArrayAdapter<CharSequence> mMacrosArray;

    // the list of RGB Orders we support
    private ArrayAdapter<CharSequence> mOrdersArray;

    // get handle on the RGB inputs
    //TODO implement a proper color picker...
    private TextView mTextColor_r;
    private TextView mTextColor_g;
    private TextView mTextColor_b;

    private SeekBar mSeekBarColor_r;
    private SeekBar mSeekBarColor_g;
    private SeekBar mSeekBarColor_b;


    // the brightness slider & text next to it
    private SeekBar mSeekBar_Brightness;
    private TextView mText_Brightness;

    // the send button
    private Button mButton_Send;


    // storage for values the caller will send us
    private String mDeviceName;
    private String mDeviceAddress;


    // the CODE values
    private Driver driver;

    // store the list of verified service -> UUIDs here
    private  HashMap<String, ArrayList<String>> verifiedServices;


    // the service connection
    private BluetoothLeService mBluetoothLeService;

    // Store connection state...
    private boolean mConnected = false;


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "onServiceConnected> Unable to initialize Bluetooth");
                finish();
            }

            // Automatically connects to the device upon successful start-up initialization.
            Log.i(TAG, "onServiceConnected> connecting to: " + mDeviceAddress);
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected> componentName: " + componentName);
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();


            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "onReceive> now connected...");
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "onReceive> now disconnected...");
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
                clearVerifiedServices();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "onReceive> services discovered...");

                /*
                    Ideally the DeviceScanActivity will properly filter out devices
                    and only call this activity with proper devices.

                    However, we still need to actually connect to the device and
                    get it's services. With the services, we can then verify
                    the correct characteristic(s) are present.

                 */

                // Verify the services, only once
                if (verifiedServices.isEmpty()){
                    verifiedServices = verifyGattServices(mBluetoothLeService.getSupportedGattServices());

                    // TODO: there's got to be a better way to do this
                    // for now, check that we have any verified OK.  if yes, don't panic
                    // if no... panic?
                    if(verifiedServices.isEmpty()){
                        // TODO: let the user know that we failed to verify
                        // TODO: go back to the last activity / screen?
                        Log.e(TAG, "onReceive> verifiedServices is empty!!!");
                        Toast.makeText(getBaseContext(), R.string.error_no_supported_characteristics, Toast.LENGTH_LONG).show();
                    }
                }

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "onReceive> data available! data: " + intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

            }
        }
    };



    // Clears the UX when disconnected
    private void clearUI() {

        Log.d(TAG, "clearUI> called!");

        // Set disconnected string...
        mLabel_ConnectionState.setText(R.string.disconnected);

    }


    // Clears the UX when disconnected
    private void clearVerifiedServices() {

        Log.d(TAG, "clearVerifiedServices> called!");

        verifiedServices.clear();


    }

    private void doSendButtonClick(){
        Log.i(TAG, "doSendButtonClick> Clicked");

        // since we're sending raw RGB to the controller, disable the macro button
        mToggle_Macros.setChecked(false);
        sendRGBAndBrightness();
    }


    private void setupDriver(){
        driver = new Driver();
    }

    private void attachHandlesToUxElements(){
        Log.d(TAG, "attachHandlesToUxElements> Called...");

        // handles for UI elements
        // the two text labels up top...
        mLabel_DeviceID = findViewById(R.id.device_id);
        mLabel_ConnectionState = findViewById(R.id.connection_state);


        // Brightness slider
        mSeekBar_Brightness = findViewById(R.id.mSeekBar_Brightness);
        mText_Brightness = findViewById(R.id.mText_Brightness);

        // Strip on/off toggle
        mSwitch_OnOff = findViewById(R.id.mSwitch_onoff);


        // Macro controller
        mToggle_Macros =  findViewById(R.id.mToggle_macros);
        mSpinner_Macros = findViewById(R.id.mSpinner_macros);

        // RGB Order
        mSpinner_RGBOrder = findViewById(R.id.mSpinner_RGBOrder);

        // RGB sliders
        mTextColor_r = findViewById(R.id.mTextColor_R);
        mTextColor_g = findViewById(R.id.mTextColor_G);
        mTextColor_b = findViewById(R.id.mTextColor_B);

        mSeekBarColor_r = findViewById(R.id.mSeekBarColor_r);
        mSeekBarColor_g = findViewById(R.id.mSeekBarColor_g);
        mSeekBarColor_b = findViewById(R.id.mSeekBarColor_b);


        // the send button
        mButton_Send = findViewById(R.id.mButton_Send);

        // tex edit for user feedback
        mEditText_Status = findViewById(R.id.mEditText_Status);


    }

    private void setupBrightnessText(){
        Log.d(TAG,"setupBrightnessText> Called!");

        mText_Brightness.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                Log.d(TAG,"setupBrightnessText.onTextChanged> s:" + s);

                // see if it's an empty string
                String t = mText_Brightness.getText().toString();
                if (!t.isEmpty()) {
                    try {
                        mSeekBar_Brightness.setProgress(Integer.parseInt(t));
                    } catch (NumberFormatException nfe) {
                        // Keybaord is numeric and we already check .empty() so there should be no
                        // NFE, but check just in case the user has a way to ignore our keyboard restrictions...
                        Log.d(TAG,"setupBrightnessText.onTextChanged> s:" + s);
                    }

                }

            }
        });

    };

    private void setupRGBText(){
        Log.d(TAG,"setupRGBText> Called!");

        // TODO: there has to be a smarter way to do this other than setting up 3 onChange functions..
        mTextColor_r.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                Log.d(TAG,"setupRGBText.onTextChanged> s:" + s);

                // see if it's an empty string
                String t = mTextColor_r.getText().toString();
                if (!t.isEmpty()) {
                    try {
                        mSeekBarColor_r.setProgress(Integer.parseInt(t));
                    } catch (NumberFormatException nfe) {
                        // Keybaord is numeric and we already check .empty() so there should be no
                        // NFE, but check just in case the user has a way to ignore our keyboard restrictions...
                        Log.d(TAG,"setupBrightnessText.onTextChanged> s:" + s);
                    }

                }

            }
        });

        mTextColor_g.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                Log.d(TAG,"setupRGBText.onTextChanged> s:" + s);

                // see if it's an empty string
                String t = mTextColor_g.getText().toString();
                if (!t.isEmpty()) {
                    try {
                        mSeekBarColor_g.setProgress(Integer.parseInt(t));
                    } catch (NumberFormatException nfe) {
                        // Keybaord is numeric and we already check .empty() so there should be no
                        // NFE, but check just in case the user has a way to ignore our keyboard restrictions...
                        Log.d(TAG,"setupBrightnessText.onTextChanged> s:" + s);
                    }

                }

            }
        });

        mTextColor_b.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                Log.d(TAG,"setupRGBText.onTextChanged> s:" + s);
                // see if it's an empty string
                String t = mTextColor_b.getText().toString();
                if (!t.isEmpty()) {
                    try {
                        mSeekBarColor_b.setProgress(Integer.parseInt(t));
                    } catch (NumberFormatException nfe) {
                        // Keybaord is numeric and we already check .empty() so there should be no
                        // NFE, but check just in case the user has a way to ignore our keyboard restrictions...
                        Log.d(TAG,"setupBrightnessText.onTextChanged> s:" + s);
                    }

                }

            }
        });

    };


    private void setupRGBSliders(){
        Log.d(TAG,"setupRGBSliders> Called!");


        // TODO: there has to be a smarter way to do this other than setting up 3 onChange functions..
        mSeekBarColor_r.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean from_user) {
                mTextColor_r.setText(Integer.toString(progress));
                doSendButtonClick();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarColor_g.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean from_user) {
                // Make sure we have a string that we can turn into an int
                try{
                    mTextColor_g.setText(Integer.toString(progress));
                    doSendButtonClick();

                } catch (NumberFormatException nfe){
                    Log.e(TAG, "mSeekBarColor_g.setOnSeekBarChangeListener> caught number format exception");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarColor_b.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean from_user) {
                mTextColor_b.setText(Integer.toString(progress));
                doSendButtonClick();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        // TODO: allow user to set defaults
        mSeekBarColor_r.setProgress(0);
        mSeekBarColor_g.setProgress(0);
        mSeekBarColor_b.setProgress(0);
    };

    private void setupBrightnessSlider(){
        Log.d(TAG,"setupBrightnessSlider> Called!");

        // TODO: allow user to set defaults
        mSeekBar_Brightness.setProgress(100);

        mSeekBar_Brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean from_user) {
                mText_Brightness.setText(Integer.toString(progress));

                try {
                    mBluetoothLeService.sendData(driver.setBrightness(progress));
                } catch (Exception e) {
                    Log.e(TAG, "onCheckedChanged: SOMETHING FUCKED UP: ");
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    };

    private void setupStrandOutputToggle(){
        Log.d(TAG,"setupStrandOutputToggle> Called!");

        mSwitch_OnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean is_on) {
                Log.i(TAG,"setupStrandOutputToggle.onCheckedChanged> is_on: "+ is_on);

                try {

                    mBluetoothLeService.sendData(driver.setOutputOnOff(is_on));

                } catch (Exception e) {
                    Log.e(TAG, "setupStrandOutputToggle.onCheckedChanged: SOMETHING FUCKED UP: ");
                    e.printStackTrace();
                }
            }
        });

        // we default to true
        // TODO: can we read the current state from the controller?
        mSwitch_OnOff.setChecked(true);

    };

    private void sendRGBAndBrightness(){
        Log.i(TAG, "sendRGBAndBrightness> Alive!");

        try {

            Integer bri = mSeekBar_Brightness.getProgress();

            Integer r = Integer.parseInt(mTextColor_r.getText().toString());
            Integer g = Integer.parseInt(mTextColor_g.getText().toString());
            Integer b = Integer.parseInt(mTextColor_b.getText().toString());

            mBluetoothLeService.sendData(driver.setRGB(r,g,b));
            mBluetoothLeService.sendData(driver.setBrightness(bri));

        } catch (Exception e) {
            Log.e(TAG, "sendRGBAndBrightness SOMETHING FUCKED UP: ");
            e.printStackTrace();
        }

    };

    private void sendMacroID(Integer macroID){
        Log.i(TAG, "sendMacroID> macroID: " + macroID);

        try {

            // make sure to put the controller in RGB mode
            mBluetoothLeService.sendData(driver.setRGBOrder(1));

            // then send the macro code
            mBluetoothLeService.sendData(driver.setMacroByID(macroID));

        } catch (Exception e) {
            Log.e(TAG, "sendRGBAndBrightnessSOMETHING FUCKED UP: ");
            e.printStackTrace();
        }

    };

    private void sendRGBOrderID(Integer orderID){
        Log.i(TAG, "sendMacroID> sendRGBOrderID: " + orderID);

        try {

            // make sure to put the controller in RGB mode
            mBluetoothLeService.sendData(driver.setRGBOrder(orderID));
        } catch (Exception e) {
            Log.e(TAG, "sendRGBOrderID SOMETHING FUCKED UP: " + e );
            e.printStackTrace();
        }

    };


    private Integer getMacroIDFromSpinnerPosition(Integer spinner_position) {

        // User givs us the selected position
        Log.d(TAG,"getMacroIDFromSpinnerPosition> spinner_position: " + spinner_position);

        // We get the item at the position, clean it up
        String spinnerItem = mSpinner_Macros.getItemAtPosition(spinner_position).toString().trim();

        // DO some processing; on the array item to get the string and the code
        Integer macroCode = Integer.parseInt(spinnerItem.split(",")[1]);

        return macroCode;
    };

    private String getMacroNameFromSpinnerPosition(Integer spinner_position) {

        // User gives us the selected position
        Log.d(TAG,"getMacroNameFromSpinnerPosition> spinner_position: " + spinner_position);

        // We get the item at the position, clean it up
        String spinnerItem = mSpinner_Macros.getItemAtPosition(spinner_position).toString().trim();

        // DO some processing; on the array item to get the string and the code
        String macroName = spinnerItem.split(",")[0];

        return macroName;

    };

    private Integer getRGBOrderIDFromSpinnerPosition(Integer spinner_position) {

        // User gives us the selected position
        Log.d(TAG,"getRGBOrderIDFromSpinnerPosition> spinner_position: " + spinner_position);

        // We get the item at the position, clean it up
        String spinnerItem = mSpinner_RGBOrder.getItemAtPosition(spinner_position).toString().trim();

        // DO some processing; on the array item to get the string and the code
        Integer orderID = Integer.parseInt(spinnerItem.split(",")[1]);

        return orderID;

    };


    private String getRGBOrderNameFromSpinnerPosition(Integer spinner_position) {

        // User gives us the selected position
        Log.d(TAG,"getRGBOrderNameFromSpinnerPosition> spinner_position: " + spinner_position);

        // We get the item at the position, clean it up
        String spinnerItem = mSpinner_RGBOrder.getItemAtPosition(spinner_position).toString().trim();

        // DO some processing; on the array item to get the string and the code
        String orderName = spinnerItem.split(",")[0];

        return orderName;

    };



    private void setupMacrosToggle(){
        Log.d(TAG,"setupStrandMacrosToggle> Called!");

        mToggle_Macros.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean is_on) {
                Log.i(TAG,"setupStrandMacrosToggle.onCheckedChanged> is_on: "+ is_on);

                /*

                    When the user selects something from the drop-down we send that signal to the controller ASAP
                    When the user adjusts the RGB sliders, we send that info to the controller ASAP

                    We give the user a toggle button to send the macro info so they dont have to re-select from the drop down

                 */
                try {

                    // Check if the toggle was just pushed...
                    if (is_on) {

                        try{

                            // get the selected
                            Integer position = mSpinner_Macros.getSelectedItemPosition();

                            Log.i(TAG,"setupStrandMacrosToggle.onCheckedChanged> position: "
                                    + position + " (" + mSpinner_Macros.getItemAtPosition(position)
                                    + ")");

                            sendMacroID(getMacroIDFromSpinnerPosition(position));

                        } catch (Exception e) {
                            Log.e(TAG, "setupStrandMacrosSpinner.setOnItemSelectedListener> " +
                                    "Soemthing Fucked Up! e: " + e );
                        }

                    } else{
                        // when we want macros off, we just send the regular RGB data
                        sendRGBAndBrightness();
                    }

                } catch (Exception e) {
                    Log.e(TAG, "setupStrandMacrosToggle.onCheckedChanged: SOMETHING FUCKED UP: ");
                    e.printStackTrace();
                }

            }
        });

    };

    private void setupRGBOrderSpinner(){
        Log.d(TAG,"setupRGBOrderSpinner> Called!");


        // TODO: use code like this to build an array w/ just the names
        //        final String[] macro_tuples = getResources().getStringArray(R.array.array_macros);
        //        String[] macroStrings = new String[macro_tuples.length];
        //
        //        for (int i = 0; i < macro_tuples.length; i++) {
        //            macroStrings[i] = macro_tuples[i].split(",")[0];
        //        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        mOrdersArray = ArrayAdapter.createFromResource(this, R.array.rgb_orders, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        mOrdersArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        mSpinner_RGBOrder.setAdapter(mOrdersArray);

        // Set up the onClick for the spinner
        mSpinner_RGBOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Integer orderID = getRGBOrderIDFromSpinnerPosition(position);
                String orderName = getRGBOrderNameFromSpinnerPosition(position);

                Log.i(TAG, "setupRGBOrderSpinner.setOnItemSelectedListener> " +
                        "Position: " + position + " is :" + orderID + " orderName: " + orderName);

                sendRGBOrderID(orderID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing :)
            }

        });

    }

    private void setupMacrosSpinner(){
        Log.d(TAG,"setupStrandMacrosSpinner> Called!");


        // TODO: use code like this to build an array w/ just the names
        //        final String[] macro_tuples = getResources().getStringArray(R.array.array_macros);
        //        String[] macroStrings = new String[macro_tuples.length];
        //
        //        for (int i = 0; i < macro_tuples.length; i++) {
        //            macroStrings[i] = macro_tuples[i].split(",")[0];
        //        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        mMacrosArray = ArrayAdapter.createFromResource(this, R.array.array_macros, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        mMacrosArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        mSpinner_Macros.setAdapter(mMacrosArray);

        // Set up the onClick for the spinner

        mSpinner_Macros.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        Integer macroID = getMacroIDFromSpinnerPosition(position);
                        String macroName = getMacroNameFromSpinnerPosition(position);

                        Log.i(TAG, "setupStrandMacrosSpinner.setOnItemSelectedListener> " +
                                "Position: " + position + " is :" + macroName + " macroID: " + macroID);

                        sendMacroID(macroID);

                        // make sure the toggle is set so the user has visual indicator that we're in macro mode
                        if(!mToggle_Macros.isChecked()){
                            Log.i(TAG, "setupStrandMacrosSpinner.setOnItemSelectedListener> Setting toggle to activated...");
                            mToggle_Macros.setChecked(true);
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // do nothing :)
                    }

                });


    };

    private void setupSendButton(){
        Log.d(TAG,"setupSendButton> Called!");
        // set up onClick for button

        mButton_Send.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        doSendButtonClick();
                    }
                });

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.device_controller);

        // get UX wired up to objects
        attachHandlesToUxElements();

        // make the objects do things
        setupStrandOutputToggle();

        setupMacrosToggle();
        setupMacrosSpinner();

        setupRGBOrderSpinner();



        setupBrightnessSlider();
        setupBrightnessText();

        setupRGBSliders();
        setupRGBText();

        setupSendButton();

        // get Driver setup
        // TODO: this can be done in initial assignment?
        setupDriver();


        // get the device name / address from the invoker
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Log.d(TAG, "onCreate> mDeviceName: " + mDeviceName +
                "mDeviceAddress: " + mDeviceAddress);


        // Plug the name/address into UX
        mLabel_DeviceID.setText(String.format("%s (%s)", mDeviceName, mDeviceAddress));


        getActionBar().setTitle(mDeviceName);

        // TODO: what does this do?
        getActionBar().setDisplayHomeAsUpEnabled(true);


        // set up the list of supported characteristics
        verifiedServices = new HashMap<String, ArrayList<String>>();


        // Get the Service Intent & Bind to it
        // We'll set up the service call backs in onResume()
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "onResume> Connect request result: " + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLabel_ConnectionState.setText(resourceId);
            }
        });
    }



    /*
        go through every service; see if any are supported
        for each supported service, see if any supported charactersitics are present
     */
    private HashMap<String, ArrayList<String>> verifyGattServices(List<BluetoothGattService> gattServices) {

        // TODO: verify before calling so i can remove this?
        if (gattServices == null) return null;

        // We'll get two UUIDs
        String service_uuid, characteristic_uuid;

        // we store the Service -> Characteristics map here
        HashMap<String, ArrayList<String>> supported_uuids = new HashMap();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            // get the UUID of the current service
            service_uuid = gattService.getUuid().toString();

            // TODO: add support for multiple services
            if(service_uuid.equals(SampleGattAttributes.CLIENT_SERVICE_STRIP)){

                // get the list of characteristics for the service
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                    characteristic_uuid = gattCharacteristic.getUuid().toString();
                    
                    //TODO: add support for multiple characteristics
                    if(characteristic_uuid.equals(SampleGattAttributes.CLIENT_CHARACTERISTIC_COLOR)) {
                        Log.i(TAG, "displayGattServices>  found a supported characteristic_uuid: " + characteristic_uuid );

                        if (supported_uuids.containsKey(service_uuid)){
                            Log.i(TAG, "displayGattServices>  supported_uuids has service_uuid: " + service_uuid);

                            // add the supported characteristic for the service
                            supported_uuids.get(service_uuid).add(characteristic_uuid);
                        } else {
                            Log.i(TAG, "displayGattServices>  supported_uuids DOES NOT HAVE service_uuid: " + service_uuid);
                            ArrayList<String> _t = new ArrayList<>();

                            _t.add(characteristic_uuid);

                            supported_uuids.put(service_uuid, _t);

                        }


                    } else {
                        Log.w(TAG, "displayGattServices> characteristic_uuid " + characteristic_uuid + " is not of interest to us..." );
                    }
                    

                }


            } else {
                Log.w(TAG, "displayGattServices> service_uuid " + service_uuid + " is not of interest to us..." );
            }


        }

        return supported_uuids;


    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
