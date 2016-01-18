package de.adesso.mobile.android.sec2.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import de.adesso.mobile.android.sec2.util.LogHelper;

public class FileChooser extends ListItem {

    @SuppressWarnings ("unused")
    private static final Class<?> c = FileChooser.class;

    public FileChooser(final String title, final boolean isDir, final File file, final Intent intent, Drawable image) {
        super(title, isDir, file, intent, image);
    }

    public String fileToString() {
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(this.file);
            byte[] data = new byte[(int) file.length()];
            fileInputStream.read(data);
            fileInputStream.close();
            //TODO need Base64 Encoding
            LogHelper.logV(new String(data, "UTF-8"));
            return new String(data, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\nFileChooser(");
        builder.append("title: " + title);
        builder.append(", \n");
        builder.append("isDir: " + isDir);
        builder.append(", \n");
        builder.append("filePath: " + file.getAbsolutePath());
        builder.append(", \n");
        builder.append("intent: " + intent);
        builder.append(")");
        return builder.toString();
    }

}
