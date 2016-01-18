package de.adesso.mobile.android.sec2.model;

import java.io.File;

import android.graphics.drawable.Drawable;

public class Sec2FileItem extends ListItem {

    public Sec2FileItem(final String title, final boolean isDir, final File file, final Drawable image) {
        super(title, isDir, file, image);
    }

    public Sec2FileItem(final String title, final Drawable image) {
        super(title, false, null, image);
    }

}
