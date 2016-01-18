package de.adesso.mobile.android.sec2.model;

import java.util.Comparator;

public class FileComparator implements Comparator<ListItem> {

    private boolean caseSensitive = false;

    public int compare(ListItem o1, ListItem o2) {

        if (o1.file.isDirectory() && !o2.file.isDirectory()) {
            return -1;
        } else if (!o1.file.isDirectory() && o2.file.isDirectory()) {
            return 1;
        } else if (caseSensitive) {
            return o1.file.getName().compareTo(o2.file.getName());
        } else {
            return o1.file.getName().toLowerCase().compareTo(o2.file.getName().toLowerCase());
        }

    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

}

//public class FileComparator implements Comparator<File> {
//
//    private boolean caseSensitive = false;
//
//    public int compare(File o1, File o2) {
//
//        if (o1.isDirectory() && !o2.isDirectory()) {
//            return -1;
//        } else if (!o1.isDirectory() && o2.isDirectory()) {
//            return 1;
//        } else if (caseSensitive) {
//            return o1.getName().compareTo(o2.getName());
//        } else {
//            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
//        }
//
//    }
//
//    public boolean isCaseSensitive() {
//        return caseSensitive;
//    }
//
//    public void setCaseSensitive(boolean caseSensitive) {
//        this.caseSensitive = caseSensitive;
//    }
//
//}
