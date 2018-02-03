package com.realfame.fileexplorer;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
public class FavoriteListAdapter extends ArrayAdapter<FavoriteItem> {
    private Context mContext;

    private LayoutInflater mInflater;

    private FileIconHelper mFileIcon;

    public FavoriteListAdapter(Context context, int resource, List<FavoriteItem> objects, FileIconHelper fileIcon) {
        super(context, resource, objects);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mFileIcon = fileIcon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(R.layout.favorite_item, parent, false);
        }

        FavoriteItem item = getItem(position);
        FileInfo lFileInfo = item.fileInfo;

        Util.setText(view, R.id.file_name, item.title != null ? item.title : lFileInfo.fileName);
        if (lFileInfo.ModifiedDate > 0) {
            Util.setText(view, R.id.modified_time, Util.formatDateString(mContext, lFileInfo.ModifiedDate));
            view.findViewById(R.id.modified_time).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.modified_time).setVisibility(View.GONE);
        }
        view.findViewById(R.id.modified_time).setVisibility(lFileInfo.ModifiedDate > 0 ? View.VISIBLE: View.GONE);
        if (lFileInfo.IsDir) {
            view.findViewById(R.id.file_size).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.file_size).setVisibility(View.VISIBLE);
            Util.setText(view, R.id.file_size, Util.convertStorage(lFileInfo.fileSize));
        }

        ImageView lFileImage = (ImageView) view.findViewById(R.id.file_image);
        ImageView lFileImageFrame = (ImageView) view.findViewById(R.id.file_image_frame);
        lFileImage.setTag(position);
        if (lFileInfo.IsDir) {
            lFileImageFrame.setVisibility(View.GONE);
            lFileImage.setImageResource(R.drawable.folder_fav);
        } else {
		    /*TYRD: weina 20150824 add begin*/
			//mFileIcon.setIcon(lFileInfo, lFileImage, lFileImageFrame);
            mFileIcon.setIcon(lFileInfo, lFileImage, lFileImageFrame,mContext);
			/*TYRD: weina 20150824 end begin*/
        }
        
        return view;
    }

}
