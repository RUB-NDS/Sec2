package de.adesso.mobile.android.sec2.model;

import java.io.File;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class SecureFileChooser extends ListItem {

    public String existPath;

    public SecureFileChooser(String title, boolean isDir, File file, Intent intent, Drawable image, String existPath) {
        super(title, isDir, file, intent, image);
        this.existPath = existPath;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\nSecureFileChooser(");
        builder.append("title: " + title);
        builder.append(", \n");
        builder.append("isDir: " + isDir);
        builder.append(", \n");
        builder.append("filePath: " + file.getAbsolutePath());
        builder.append(", \n");
        builder.append("existPath: " + existPath);
        builder.append(", \n");
        builder.append("intent: " + intent);
        builder.append(")");
        return builder.toString();
    }

}
