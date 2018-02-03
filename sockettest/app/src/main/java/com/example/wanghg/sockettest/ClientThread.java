package com.example.wanghg.sockettest;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by wanghg on 2017/2/22.
 */

public class ClientThread implements Runnable {
    private Socket socket;
    Handler uiHandler;
    Handler revHandler;
    BufferedReader br = null;
    OutputStream os = null;

    public ClientThread(Handler handler) {
        this.uiHandler = handler;
    }

    @Override
    public void run() {
        socket = new Socket();
        Log.i("wanghg------", "thread id : " + android.os.Process.myTid());
        Log.i("wanghg----", "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        try {
            socket.connect(new InetSocketAddress("192.168.31.138", 8888), 5000);
            Log.i("wanghg-----", "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            os = socket.getOutputStream();

            new Thread() {
                @Override
                public void run() {
                    String content = null;
                    try {
                        Log.i("wanghg------", "thread2 id : " + android.os.Process.myTid());
                        while ((content = br.readLine()) != null) {
                            Message msg = new Message();
                            msg.what = 0x123;
                            msg.obj = content;
                            uiHandler.sendMessage(msg);
                            Log.i("wanghg-------", content);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            Looper.prepare();
            revHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 0x345) {
                        try {
                            os.write((msg.obj.toString() + "\r\n").getBytes("utf-8"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            Looper.loop();

        } catch (IllegalArgumentException e) {
            Log.i("wanghg------", "socket connection timeout .");
        } catch (SocketTimeoutException e) {
            Message msg = new Message();
            msg.what = 0x123;
            msg.obj = "网络连接超时！！！";
            uiHandler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                os.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
