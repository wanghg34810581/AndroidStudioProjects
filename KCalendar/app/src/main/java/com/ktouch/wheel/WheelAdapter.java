
package com.ktouch.wheel;

import android.view.View;
import android.view.ViewGroup;

public interface WheelAdapter {
	public int getItemsCount();

	public String getItem(int index);

	public int getMaximumLength();

}
