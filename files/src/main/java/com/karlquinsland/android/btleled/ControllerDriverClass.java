package com.karlquinsland.android.btleled;


import android.util.Log;

import java.io.ByteArrayOutputStream;

class Driver {

    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    Driver(){
        Log.d(TAG, "Driver class created");
    }


    private byte[] intArrayToByteArray(int[] ints){

        Log.i(TAG, "intArrayToByteArray>: ints: " + java.util.Arrays.toString(ints));

        ByteArrayOutputStream byo = new ByteArrayOutputStream();

        for (int integer : ints) {
            byo.write((byte) (integer));
        }

        return byo.toByteArray();

    }

    public int[] setOutputOnOff(boolean should_be_on){
        Log.i(TAG, "setOutputOnOff> should_be_on:" + should_be_on);

        int[] bytes = new int[9];


        if(should_be_on){

            bytes[0] = 126;
            bytes[1] = 4;
            bytes[2] = 4;
            bytes[3] = 1;
            bytes[4] = 255;
            bytes[5] = 255;
            bytes[6] = 255;
            //bytes[7] = NOT_SET;
            bytes[8] = 239;

        } else {

            bytes[0] = 126;
            bytes[1] = 4;
            bytes[2] = 4;
            //bytes[3] = NOT_SET;
            bytes[4] = 255;
            bytes[5] = 255;
            bytes[6] = 255;
            //bytes[7] = NOT_SET;
            bytes[8] = 239;

        }

        return bytes;

    };

    public int[] setMacroByID(int macro_id){
        Log.i(TAG, "setMacroByID> macro_id:" + macro_id);

        int[] bytes = new int[9];

        //TODO: see below list of "valid" macros and validate macro_id

        /*
            appears to be this packet

                code[0] = 126;
                code[1] = 5;
                code[2] = 3;
                code[3] = model;
                code[4] = 3;
                code[5] = 255;
                code[6] = 255;
                code[8] = 239;

            where model is:
                <item>Static red,128</item>
                <item>Static blue,129</item>
                <item>Static green,130</item>
                <item>Static cyan,131</item>
                <item>Static yellow,132</item>
                <item>Static purple,133</item>
                <item>Static white,134</item>
                <item>Tricolor jump,135</item>
                <item>Seven-color jump,136</item>
                <item>Tricolor gradient,137</item>
                <item>Seven-color gradient,138</item>
                <item>Red gradient,139</item>
                <item>Green gradient,140</item>
                <item>Blue gradient,141</item>
                <item>Yellow gradient,142</item>
                <item>Cyan gradient,143</item>
                <item>Purple gradient,144</item>
                <item>White gradient,145</item>
                <item>Red-Green gradient,146</item>
                <item>Red-Blue gradient,147</item>
                <item>Green-Blue gradient,148</item>
                <item>Seven-color flash,149</item>
                <item>Red flash,150</item>
                <item>Green flash,151</item>
                <item>Blue flash,152</item>
                <item>Yellow flash,153</item>
                <item>Cyan flash,154</item>
                <item>Purple flash,155</item>
                <item>White flash,156</item>

            //TODO: fuzz this / see what "other" modes are "supported"
            // TODO: check if there's anythign special to "go back" to RGB mode.
             it might be as aimple as sending an RGB color packet to swtcih to the basic mode

         */



        bytes[0] = 126;
        bytes[1] = 5;
        bytes[2] = 3;
        bytes[3] = macro_id;
        bytes[4] = 3;
        bytes[5] = 255;
        bytes[6] = 255;
        //bytes[7] = NOT_SET;
        bytes[8] = 239;

        return bytes;

    };


    public int[] setBrightness(int brightness) {

        Log.i(TAG, "setBrightness> brightness:" + brightness);

        int[] bytes = new int[9];

        bytes[0] = 126;
        bytes[1] = 4;
        bytes[2] = 1;
        bytes[3] = brightness;
        bytes[4] = 255;
        bytes[5] = 255;
        bytes[6] = 255;
        bytes[8] = 239;

        return bytes;
    }

    public int[] setRGB(int red, int green, int blue) {
        Log.i(TAG, "setRGB> red: " + red + " green:" + green + " blue:" +blue);

        int[] bytes = new int[9];

        bytes[0] = 126;
        bytes[1] = 7;
        bytes[2] = 5;
        bytes[3] = 3;
        //TODO make sure you fire off setRGBOrder(1) first!
        bytes[4] = red;
        bytes[5] = blue;
        bytes[6] = green;
        bytes[8] = 239;

        return bytes;
    }


    // TODO: should use ENUM?
    public int[] setRGBOrder(int rgb_order){

        Log.i(TAG, "setRGB> rgb_order " + rgb_order);

        /*
            <array name="rgb_model">
                <item>RGB,1</item>
                <item>RBG,2</item>
                <item>GRB,3</item>
                <item>GBR,4</item>
                <item>BRG,5</item>
                <item>BGR,6</item>
            </array>

         */
        int[] bytes = new int[9];

        bytes[0] = 126;
        bytes[1] = 4;
        bytes[2] = 8;
        bytes[3] = rgb_order;
        bytes[4] = 255;
        bytes[5] = 255;
        bytes[6] = 255;
        bytes[8] = 239;



        return bytes;
    };

}
