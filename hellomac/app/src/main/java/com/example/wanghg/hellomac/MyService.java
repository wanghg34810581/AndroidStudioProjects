package com.example.wanghg.hellomac;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by wanghg on 2017/2/6.
 */

public class MyService extends Service {
    public MyService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    Binder binder = new DataService.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, java.lang.String aString) throws android.os.RemoteException
        {

        }

        @Override
        public int getData(String arg) throws RemoteException {
            Log.i("wahaha", "service getData ---- arg : " + arg);
            if(arg.equals("a")) {
                return 1;
            }
            return 0;
        }
    };
}
