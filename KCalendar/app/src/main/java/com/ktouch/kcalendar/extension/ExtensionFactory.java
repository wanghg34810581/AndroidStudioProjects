package com.ktouch.kcalendar.extension;

import android.app.Activity;
import android.content.Context;

/**
 * M: the class to produce plug-in extensions
 * it will consider the situation to determine which plugin should be produced
 */
public final class ExtensionFactory {

    /**
     * M: produce the AllInOneOptionsMenu extension plug-in to caller
     * @param context context of AllInOneActivity
     * @return the extension plug-in(maybe the default one)
     */
    public static IOptionsMenuExt getAllInOneOptionMenuExt(Context context) {
        return new ClearAllEventsExt(context);
    }
}
