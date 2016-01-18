package de.adesso.mobile.android.sec2.model;

import java.io.File;

import android.graphics.drawable.Drawable;

public class ListItem {

    public String title;
    public boolean isDir;
    public File file;
    public Drawable image;

    public ListItem() {}

    public ListItem(final String title, final boolean isDir, final File file, Drawable image) {
        this.title = title;
        this.isDir = isDir;
        this.file = file;
        this.image = image;
    }

}
