package de.adesso.mobile.android.sec2.model;

import java.util.Comparator;

public class FileChooserComparator implements Comparator<String> {

    @Override
    public int compare(String str1, String str2) {
        return str1.compareToIgnoreCase(str2);
    }

}
