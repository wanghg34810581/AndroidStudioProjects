package com.guli.secmanager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guli.secmanager.Utils.FileSizeFormatter;
import com.guli.secmanager.widget.ImageAndListWidget;
import com.guli.secmanager.widget.ListViewForFinish;

public class V2FinishActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public static final String TAG = "V2FinishActivity";

    private ImageButton mActionBarButton;
    private LinearLayout mLayoutActionbar;
    private LayoutInflater mInflater;
    private ListViewForFinish mListView;
    private ViewAdapter mAdapter;
    private TextView mActionbarTitle;

    private String mFrom;
    private Long mTotalCleanSize;
    private static Context mStaticContext;

    @Override
    protected void onDestroy() {
        mStaticContext = this;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            //        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            //        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            //window.setNavigationBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.v2_finish_activity);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        InitActionBar();

        Intent intent = getIntent();
        if (intent != null) {
            mFrom = intent.getStringExtra("from");
            if(mFrom.equals("V2GarbageCleanActivity")) {
                mTotalCleanSize = intent.getLongExtra("totalSize", 0);
                mActionbarTitle.setText(getResources().getString(R.string.garbageclean));
            }
            else if(mFrom.equals("V2VirusScanActivity")) {
                mActionbarTitle.setText(getResources().getString(R.string.virus_scan_name));
            }
        }

        /*((Button)findViewById(R.id.complete_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });;

        ImageAndListWidget imageAndList = (ImageAndListWidget) findViewById(R.id.view_image_and_list);
        imageAndList.setContentView();*/
        mListView = (ListViewForFinish) findViewById(R.id.listview);
        mAdapter = new ViewAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    private void InitActionBar() {
        mActionBarButton = (ImageButton) findViewById(R.id.actionbar_icon);
        mLayoutActionbar = (LinearLayout) findViewById(R.id.layout_actionbar);
        mActionbarTitle = (TextView) findViewById(R.id.actionbar_title);
        mActionBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            mActionBarButton.setBackgroundResource(outValue.resourceId);
            //noinspection deprecation
            getWindow().setStatusBarColor(getResources().getColor(R.color.green));
        } else {
            mActionBarButton.setBackgroundResource(R.drawable.actionbar_btn_on_selector);
        }
        //noinspection deprecation
        mLayoutActionbar.setBackgroundColor(getResources().getColor(R.color.green));
    }

    public void setBackgroundColor(int color) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            getWindow().setStatusBarColor(getResources().getColor(color));
        }

        if(mLayoutActionbar != null) {
            //noinspection deprecation
            mLayoutActionbar.setBackgroundDrawable(getResources().getDrawable(color));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mAdapter.getItemViewType(position) == ViewAdapter.VIEW_TYPE_BUTTON) {
            finish();
        }
    }

    private final class ViewAdapter extends BaseAdapter {

        public static final int VIEW_TYPE_ANIMATION = 0;
        public static final int VIEW_TYPE_ADS = 1;
        public static final int VIEW_TYPE_BUTTON = 2;

        private static final int VIEW_TYPE_COUNT = 3;

        private final int[] mViewType = new int[] {VIEW_TYPE_ANIMATION,
                VIEW_TYPE_ADS, VIEW_TYPE_ADS, VIEW_TYPE_ADS, VIEW_TYPE_BUTTON};
        private static final int VIEW_ITEM_COUNT = 5;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            switch (getItemViewType(position)) {
                case VIEW_TYPE_ANIMATION:
                    return getAnimationView(position, convertView, parent);
                case VIEW_TYPE_ADS:
                    return getAdviertisementView(position, convertView, parent);
                case VIEW_TYPE_BUTTON:
                    return getButtonView(position, convertView, parent);
                default:
                    throw new IllegalStateException("Invalid view type ID " +
                            getItemViewType(position));
            }
        }



        private View getAnimationView(int position, View convertView, ViewGroup parent) {
            View result = convertView;

            if(convertView == null) {
                result = mInflater.inflate(R.layout.v2_finish_activity_list_animation, parent, false);
                RelativeLayout okAnimationLayout = (RelativeLayout) result.findViewById(R.id.ok_animation_layout);
                RelativeLayout resultMessageLayout = (RelativeLayout) result.findViewById(R.id.result_message_layout);
                RelativeLayout arrowAnimationLayout = (RelativeLayout) result.findViewById(R.id.arrow_animation_layout);
                ((ImageAndListWidget)result).setContentView(okAnimationLayout, resultMessageLayout, arrowAnimationLayout);
                mListView.setOnItemTouchListener(((ImageAndListWidget) result));

                TextView tv_result_title = (TextView) result.findViewById(R.id.tv_result_title);
                TextView tv_result_description = (TextView) result.findViewById(R.id.tv_result_description);
                if(mFrom.equals("V2GarbageCleanActivity")) {
                    tv_result_title.setText(FileSizeFormatter.transformShortType(mTotalCleanSize) + getString(R.string.B));
                    tv_result_description.setText(getString(R.string.garbage_cleaned));
                }
                else if(mFrom.equals("V2VirusScanActivity")) {
                    tv_result_title.setText(getString(R.string.result_big_safe_info));
                    tv_result_description.setText(getString(R.string.no_found_danger));
                }
            }

            return result;
        }

        private View getAdviertisementView(int position, View convertView, ViewGroup parent) {
            final View result = (convertView != null) ? convertView :
                    mInflater.inflate(R.layout.v2_finish_activity_list_ads, parent, false);

            return result;
        }

        private View getButtonView(int position, View convertView, ViewGroup parent) {
            final View result = (convertView != null) ? convertView :
                    mInflater.inflate(R.layout.v2_finish_activity_list_button, parent, false);

            return result;
        }

        @Override
        public int getCount() {
            return VIEW_ITEM_COUNT;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return mViewType[position];
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean areAllItemsEnabled() {
            // Header will always be an item that is not enabled.
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }
    }
}
