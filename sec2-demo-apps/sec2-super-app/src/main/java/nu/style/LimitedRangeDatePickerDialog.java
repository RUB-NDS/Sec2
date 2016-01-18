package nu.style;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.format.DateUtils;
import android.widget.DatePicker;

public class LimitedRangeDatePickerDialog extends DatePickerDialog {

    private final Calendar minDate;
    private final Calendar maxDate;

    private final Context mContext;

    public LimitedRangeDatePickerDialog(Context context,
            DatePickerDialog.OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth,
            Calendar minDate, Calendar maxDate) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
        this.minDate = minDate;
        this.maxDate = maxDate;

        this.mContext = context;

    }

    @Override
    public void onDateChanged(DatePicker view, int year, int month, int day) {
        Calendar newDate = Calendar.getInstance();
        newDate.set(year, month, day);

        if (minDate != null && minDate.after(newDate)) {
            view.init(minDate.get(Calendar.YEAR), minDate.get(Calendar.MONTH),
                    minDate.get(Calendar.DAY_OF_MONTH), this);
            setTitle(DateUtils.formatDateTime(mContext, minDate.getTimeInMillis(),
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
                            | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_MONTH
                            | DateUtils.FORMAT_ABBREV_WEEKDAY));
        } else if (maxDate != null && maxDate.before(newDate)) {
            view.init(maxDate.get(Calendar.YEAR), maxDate.get(Calendar.MONTH),
                    maxDate.get(Calendar.DAY_OF_MONTH), this);
            setTitle(DateUtils.formatDateTime(mContext, maxDate.getTimeInMillis(),
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
                            | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_MONTH
                            | DateUtils.FORMAT_ABBREV_WEEKDAY));
        } else {
            view.init(year, month, day, this);
            setTitle(DateUtils.formatDateTime(mContext, newDate.getTimeInMillis(),
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
                            | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_MONTH
                            | DateUtils.FORMAT_ABBREV_WEEKDAY));
        }
    }
}
