// DataService.aidl
package com.example.wanghg.hellomac;

// Declare any non-default types here with import statements

interface DataService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    int getData(String arg);
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}
