package com.example.wanghg.bitmaptest2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    private String[] imagePath = Image.path;
    private ListView listView;
    private ImageAdapter adapter = new ImageAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list1);
        listView.setAdapter(adapter);
    }

    private class ImageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return imagePath.length;
        }

        @Override
        public Object getItem(int position) {
            return imagePath[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if(convertView == null) {
                view = View.inflate(MainActivity.this, R.layout.item, null);
            }
            else {
                view = convertView;
            }
            ImageView image = (ImageView) view.findViewById(R.id.imageView);
            //image.setImageBitmap();

            return view;
        }
    }
}
