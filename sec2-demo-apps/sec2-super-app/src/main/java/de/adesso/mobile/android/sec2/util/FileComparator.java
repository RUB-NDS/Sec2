package de.adesso.mobile.android.sec2.util;

import java.util.Comparator;
import java.util.Locale;

import de.adesso.mobile.android.sec2.model.ListItem;

public class FileComparator implements Comparator<ListItem> {

    private boolean caseSensitive = false;

    @Override
    public int compare(final ListItem o1, final ListItem o2) {

        if (o1.isDir && !o2.isDir) {
            return -1;
        } else if (!o1.isDir && o2.isDir) {
            return 1;
        } else if (caseSensitive) {
            return o1.title.compareTo(o2.title);
        } else {
            return o1.title.toLowerCase(Locale.getDefault()).compareTo(
                    o2.title.toLowerCase(Locale.getDefault()));
        }

    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(final boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

}
