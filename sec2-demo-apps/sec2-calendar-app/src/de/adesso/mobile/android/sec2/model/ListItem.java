package de.adesso.mobile.android.sec2.model;

import java.io.File;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class ListItem {

    public String title;
    public boolean isDir;
    public File file;
    public Drawable image;
    public transient Intent intent;

    public ListItem() {}

    public ListItem(final String title, final boolean isDir, final File file, final Intent intent, Drawable image) {
        this.title = title;
        this.isDir = isDir;
        this.file = file;
        this.intent = intent;
        this.image = image;
    }

}
