package com.example.wanghg.sockettest;

import android.os.Handler;
import android.os.Message;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private Button button;
    private Handler handler;
    private ClientThread clientThread;

    private Button httpGetButton;
    private Button httpPostButton;
    private String mUri = "http://cloud.bmob.cn/0906a62b462a3082/";
    private String mMethod = "getMemberBySex";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        textView.setTextSize(20);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = 0x345;
                msg.obj = "android 网络编程之socket通讯";
                clientThread.revHandler.sendMessage(msg);

            }
        });

        httpGetButton = (Button) findViewById(R.id.button2);
        httpGetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doGet("boy");
            }
        });

        httpPostButton = (Button) findViewById(R.id.button3);
        httpPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPost("boy");
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x123) {
                    textView.append("\n" + msg.obj.toString());
                }
            }
        };

        clientThread = new ClientThread(handler);

        new Thread(clientThread).start();
        Log.i("wanghg------", "main thread id : " + android.os.Process.myTid());
    }

    private void doGet(String s) {
        final String serverAddress = mUri + mMethod + "?" + "sex=" + s;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(serverAddress);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    if (connection.getResponseCode() == 200) {
                        InputStream is = connection.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        StringBuffer sb = new StringBuffer();
                        String readLine = "";
                        while ((readLine = br.readLine()) != null) {
                            sb.append(readLine);
                        }
                        br.close();
                        is.close();
                        connection.disconnect();

                        Log.i("wanghg------", "http get : " + sb.toString());
                    } else {
                        Log.i("wanghg------", "failed to connect server !");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void doPost(final String s) {
        final String postAddress = mUri + mMethod;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(postAddress);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Accept-Charset", "utf-8");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    connection.connect();
                    OutputStream os = connection.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    String content = "sex=" + s;
                    dos.writeBytes(content);

                    dos.flush();
                    dos.close();
                    os.flush();
                    os.close();

                    if (connection.getResponseCode() == 200) {
                        InputStream is = connection.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        StringBuffer sb = new StringBuffer();
                        String readline = "";
                        while ((readline = br.readLine()) != null) {
                            sb.append(readline);
                        }

                        br.close();
                        is.close();
                        connection.disconnect();

                        Log.i("wanghg------", "http post : " + sb.toString());
                    } else {
                        Log.i("wanghg------", "failed to connect server !");
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
