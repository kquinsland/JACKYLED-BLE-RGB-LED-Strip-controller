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


import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */

public class SampleGattAttributes {

    // Store the UUID -> Human Name (see below for the .put calls)
    private static HashMap<String, String> attributes = new HashMap();


    // the UUIDs that seem to work for my controller
    // TODO: add support for others?
    public static String CLIENT_SERVICE_STRIP = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_COLOR = "0000ffe1-0000-1000-8000-00805f9b34fb";



    static {

        // Sample Services / well defined by the standard
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");

        // called service33 in the disassembled code; appears to be the RGB Driver
        attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "Driver Service");



        // Sample Characteristics / well defined by the standard
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");

        // called characid33 in the disassembled code; appears to be the RGB Driver
        attributes.put("0000ffe1-0000-1000-8000-00805f9b34fb", "Driver Interface Characteristic");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
