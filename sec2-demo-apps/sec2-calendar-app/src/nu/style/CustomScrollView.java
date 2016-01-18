package nu.style;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * CustomScrollView
 * Note: Currently not in use.
 * @author hoppe
 */
public class CustomScrollView extends ScrollView {

    @SuppressWarnings ("unused")
    private static final Class<?> c = CustomScrollView.class;

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFadingEdgeLength(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    /**
     * Override this method so that not every occuring event is intercepted by scrollview
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //        LogHelper.logV("getAction(): " + ev.getAction());
        //        LogHelper.logV("getDeviceId(): " + ev.getDeviceId());
        //        switch (ev.getAction()) {
        //            case 0:
        //                return true;
        //            default:
        //                return false;
        //        }

        //        LogHelper.logV("GetX: " + ev.getX() + "  GetY: " + ev.getY());
        //        LogHelper.logV("GetXPrecision: " + ev.getXPrecision() + "  GetYPrecision: " + ev.getYPrecision());
        //        con.getResources().getResourceEntryName(R.id.noticecreate_header_text_top)

        return true;
    }

}
