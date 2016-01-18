package de.adesso.mobile.android.sec2.util;

import android.content.Intent;
import android.os.Bundle;
import de.adesso.mobile.android.sec2.model.NoticeListItem;
import de.adesso.mobile.android.sec2.model.TaskListItem;

/**
 * BundleHelper
 * @author hoppe
 */
public abstract class BundleHelper {

    @SuppressWarnings ("unused")
    private static final Class<?> c = BundleHelper.class;

    /**
     * bundleNoticeListItem
     */
    public static void bundleNoticeListItem(Intent intent, final NoticeListItem value) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("noticeListItem", value);
        intent.putExtras(bundle);
    }

    /**
     * unbundleNoticeListItem
     */
    public static NoticeListItem unbundleNoticeListItem(final Bundle bundle) {
        if (bundle == null) {
            return null;
        } else {
            return bundle.getParcelable("noticeListItem");
        }
    }
    
    /**
     * bundleTaskListItem
     */
    public static void bundleTaskListItem(Intent intent, final TaskListItem value) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("taskListItem", value);
        intent.putExtras(bundle);
    }

}
