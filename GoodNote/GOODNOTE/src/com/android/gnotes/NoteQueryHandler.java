/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.android.gnotes;

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

class NoteQueryHandler extends AsyncQueryHandler {

	private static final String TAG = "NoteQueryHandler";

	private Context mContext;
	private NotesAdapter mAdapter;

	public NoteQueryHandler(ContentResolver cr, Context context, NotesAdapter adapter) {
		super(cr);
		mContext = context;
		mAdapter = adapter;
	}

	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		Log.d(TAG, "onQueryComplete cursor = " + mAdapter.getItemCount());
		if (token == Notes.MAIN_VIEW) {
			mAdapter.setData(cursor);
			mAdapter.notifyDataSetChanged();
			((MainNoteActivity) mContext).updateEmptyView(mAdapter.getItemCount());
			if(cookie instanceof Integer) {
				int pos = (Integer) cookie;
				((MainNoteActivity) mContext).updateQueryData(pos);
				return;
			}
		}

		if(cookie instanceof ProgressDialog) {
			ProgressDialog mDialog = (ProgressDialog) cookie;
			if (mDialog != null) {
				mDialog.cancel();
			}
		}
	}

	@Override
	public void onDeleteComplete(int token, Object cookie, int result) {
		Log.d(TAG, "onDeleteComplete token=" + token);
		/* wanghg modify for delete note animation  20161227 start */
		// first disappear the progress dialog, then show the toast
		/*ProgressDialog mDialog = (ProgressDialog) cookie;
		if (mDialog != null) {
			mDialog.cancel();
		}*/

		/* wanghg modify end */

		if (token == Notes.DELETE_VIEW) {
			int pos = (Integer) cookie;
			((MainNoteActivity) mContext).updateDeleteData(pos);
		} else if (token == Notes.NOTEREADING_DELETE_TOKEN) {
			View layout = (View) cookie;
			/* wanghg modify for delete note animation  20161227 start */
			AnimationSet animationSet = new AnimationSet(true);
			ScaleAnimation scaleAnimation = new ScaleAnimation(1, 0.1f, 1, 0.1f,
					Animation.RELATIVE_TO_SELF, 0.61f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			scaleAnimation.setDuration(500);
			scaleAnimation.setFillAfter(true);
			scaleAnimation.setFillEnabled(true);
			animationSet.addAnimation(scaleAnimation);

			TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -2000);
			translateAnimation.setDuration(1000);
			translateAnimation.setFillAfter(true);
			translateAnimation.setFillEnabled(true);
			translateAnimation.setAnimationListener(new ScaleAnimationListener());
			animationSet.addAnimation(translateAnimation);

			layout.startAnimation(animationSet);
			//((NoteView) mContext).finish();
			/* wanghg modify end */

			//TY zhencc 20160922 delete for PROD104182100 begin
			//Toast.makeText(mContext, R.string.delete_file_success, Toast.LENGTH_LONG).show();
			//TY zhencc 20160922 delete for PROD104182100 end
		} else if (token == Notes.NOTEEDITING_DELETE_TOKEN) {   //TY zhencc 20160829 add for PROD104174151
			((NoteView) mContext).finish();
		}
	}

	private class ScaleAnimationListener implements Animation.AnimationListener {

		ScaleAnimationListener() {

		}

		@Override
		public void onAnimationStart(Animation var1) {

		};

		@Override
		public void onAnimationEnd(Animation var1) {
			((NoteView) mContext).deleteCurrentNoteEnd();
		};

		@Override
		public void onAnimationRepeat(Animation var1) {

		};
	};
}
