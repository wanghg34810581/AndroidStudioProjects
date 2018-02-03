package com.example.wanghg.httpdownloadimagetest;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private final static String ALBUM_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "download_test" + File.separator;
    private ImageView imageView;
    private Button button;
    private ProgressDialog mSaveDialog = null;
    private Bitmap mBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSaveDialog = ProgressDialog.show(MainActivity.this, "保存图片", "正在保存图片。。。。。");
                new Thread(new SaveFileRunnable()).start();
            }
        });

        new Thread(new NetRunnable()).start();
    }

    final class NetRunnable implements Runnable {
        @Override
        public void run() {
            final String PATH = "https://www.baidu.com/img/bd_logo1.png";
            InputStream is = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(PATH);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");

                Log.i("wanghg-------", "responseCode : " + connection.getResponseCode());
                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    is = connection.getInputStream();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            Message msg = new Message();
            msg.what = 100;
            msg.obj = bitmap;
            mBitmap = bitmap;
            mHandler.sendMessage(msg);

            try {
                if(is != null) {
                    is.close();
                }
                if(connection != null) {
                    connection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 100) {
                Bitmap bitmap = (Bitmap) msg.obj;
                imageView.setImageBitmap(bitmap);
            } else if(msg.what == 200) {
                mSaveDialog.dismiss();
                boolean success = (boolean) msg.obj;
                Toast.makeText(MainActivity.this, success ? "保存成功！" : "保存失败！", Toast.LENGTH_SHORT);
            }
        }
    };

    final class SaveFileRunnable implements Runnable {
        @Override
        public void run() {
            File dir = new File(ALBUM_PATH);
            Log.i("wanghg-------", "创建文件夹 : " + ALBUM_PATH);
            if(!dir.exists()) {
                boolean flag = dir.mkdirs();
                Log.i("wanghg-------", "文件夹创建成功？ " + flag);
            }

            File saveFile = new File(ALBUM_PATH, "baidu.jpg");
            boolean success = false;
            try {
                //saveFile.createNewFile();
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(saveFile));
                //Bitmap bitmap = imageView.getDrawingCache();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                bos.flush();
                bos.close();
                success = true;
            } catch (Exception e) {
                success = false;
                e.printStackTrace();
            }

            Message msg = new Message();
            msg.what = 200;
            msg.obj = success;
            mHandler.sendMessage(msg);
        }
    }
}
