package com.example.wanghg.aidl_client;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.wanghg.hellomac.DataService;

public class MainActivity extends AppCompatActivity {

    private Button button1;
    private Button button2;
    private DataService dataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(DataService.class.getName());
                intent.setPackage("com.example.wanghg.hellomac");
                bindService(intent, serviceConnection, BIND_AUTO_CREATE);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int result = dataService.getData("a");
                    Log.i("wahaha", "onClick result = " + result);
                } catch (Exception e) {
                    Log.i("wahaha", "onClick e = " + e);
                    e.printStackTrace();
                }
            }
        });


    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dataService = DataService.Stub.asInterface(service);
            Log.i("wahaha", "onServiceConnected dataService = " + dataService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
