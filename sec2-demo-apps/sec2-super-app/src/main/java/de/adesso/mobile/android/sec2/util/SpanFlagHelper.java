
package de.adesso.mobile.android.sec2.util;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.text.Spannable;
import android.text.style.TextAppearanceSpan;
import android.widget.EditText;
import de.adesso.mobile.android.sec2.R;
import de.adesso.mobile.android.sec2.model.Lock;

/**
 * an utility class to help handling the parts which will be encrypted / decrypted
 * 
 * @author hoppe
 *
 */
public class SpanFlagHelper {

    private final Context mContext;
    private final EditText mText;
    private final ArrayList<SpanFlag> mSpanFlags;

    /**
     * 
     * @param context Context object 
     * @param editText EditText object 
     */
    public SpanFlagHelper(final Context context, final EditText editText) {
        mContext = context;
        mText = editText;
        mSpanFlags = new ArrayList<SpanFlag>();
    }

    /**
     * method to add information about an encrypted area
     * 
     * @param spanFlag SpanFlag Object containing information of the encrypted area
     */
    public final void addSpanFlag(final SpanFlag spanFlag) {
        mSpanFlags.add(spanFlag);
    }

    /**
     * method to remove the information about an encrypted area
     * 
     * @param spanFlag SpanFlag object that will be removed
     */
    public final void removeSpanFlag(final SpanFlag spanFlag) {
        mSpanFlags.remove(spanFlag);
    }

    /**
     * method to reset the list of SpanFlags representing the encrypted areas for the given text 
     */
    public final void clear() {
        mSpanFlags.clear();
    }

    /**
     * method to encrypt the given Text at the given positions
     */
    public final void initiateText() {
        mergeSpanFlag();
        for (int i = 0; i < mSpanFlags.size(); i++) {
            ((Spannable) mText.getText()).setSpan(
                    new TextAppearanceSpan(mContext, R.style.text_red), mSpanFlags.get(i).start,
                    mSpanFlags.get(i).end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * method to encrypt the selected textarea of the EditText
     * 
     * @param startSelection value representing start position of the area that will be encrypted
     * @param endSelection value representing end position of the area that will be encrypted
     */
    public final void encrypt(final int startSelection, final int endSelection) {
        if (startSelection != endSelection) {
            encryptText((startSelection < endSelection ? startSelection : endSelection),
                    (startSelection < endSelection ? endSelection : startSelection));
            mSpanFlags.add(new SpanFlag((startSelection < endSelection ? startSelection
                    : endSelection),
                    (startSelection < endSelection ? endSelection : startSelection)));
            Collections.sort(mSpanFlags, new SpanFlagComparator());
            mergeSpanFlag();
        }
    }

    /**
     * method to encrypt the whole EditText
     */
    public final void encryptAll() {
        encrypt(0, mText.length());
    }

    /**
     * 
     * @param startSelection value representing start position of the area that will be encrypted
     * @param endSelection value representing end position of the area that will be encrypted
     */
    private final void encryptText(final int startSelection, final int endSelection) {
        final Spannable spannableEditText = mText.getText();
        spannableEditText.setSpan(new TextAppearanceSpan(mContext, R.style.text_red),
                startSelection, endSelection, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * method to decrypt the selected textarea of the EditText
     * 
     * @param startSelection value representing start position of the area that will be decrypted
     * @param endSelection value representing end position of the area that will be decrypted
     */
    public final void decrypt(final int startSelection, final int endSelection) {
        if (startSelection != endSelection) {
            decryptText((startSelection < endSelection ? startSelection : endSelection),
                    (startSelection < endSelection ? endSelection : startSelection));
            removeSpanFlag((startSelection < endSelection ? startSelection : endSelection),
                    (startSelection < endSelection ? endSelection : startSelection));
        }
    }

    /**
     * method to decrypt the whole EditText
     */
    public final void decryptAll() {
        decrypt(0, mText.length());
    }

    /**
     * 
     * @param startSelection
     * @param endSelection
     */
    private final void decryptText(final int startSelection, final int endSelection) {
        final Spannable spannableEditText = mText.getText();
        final TextAppearanceSpan[] editTextAppearance = spannableEditText.getSpans(startSelection,
                endSelection, TextAppearanceSpan.class);
        for (int i = 0; i < editTextAppearance.length; i++) {
            spannableEditText.removeSpan(editTextAppearance[i]);
        }
    }

    /**
     * 
     * @param start value representing start position of the area that will be deleted
     * @param end value representing end position of the area that will be deleted
     */
    private final void removeSpanFlag(final int start, final int end) {
        final ArrayList<SpanFlag> temp = new ArrayList<SpanFlag>();
        int i = 0;
        while (i < mSpanFlags.size()) {
            if (mSpanFlags.get(i).start >= start && mSpanFlags.get(i).end <= (end)) {
                // spanFlag is remove-area
                mSpanFlags.remove(i);
            } else {
                if (mSpanFlags.get(i).start < start && mSpanFlags.get(i).end > end) {
                    // delete inner part
                    temp.add(new SpanFlag(mSpanFlags.get(i).start, start));
                    temp.add(new SpanFlag(end, mSpanFlags.get(i).end));
                    encryptText(mSpanFlags.get(i).start, start);
                    encryptText(end, mSpanFlags.get(i).end);
                    mSpanFlags.remove(i);
                } else {
                    if (mSpanFlags.get(i).start >= start && mSpanFlags.get(i).end > end) {
                        // delete left part
                        mSpanFlags.get(i).start = end;
                        encryptText(end, mSpanFlags.get(i).end);
                        i++;
                    } else {
                        if (mSpanFlags.get(i).start < start && mSpanFlags.get(i).end > start
                                && mSpanFlags.get(i).end <= end) {
                            // delete right part
                            mSpanFlags.get(i).end = start;
                            encryptText(mSpanFlags.get(i).start, start);
                            i++;
                        } else {
                            i++;
                        }
                    }
                }
            }
        }
        mSpanFlags.addAll(temp);
        Collections.sort(mSpanFlags, new SpanFlagComparator());
        mergeSpanFlag();
    }

    /**
     * method to split the elements when new text is entered inside the EditText
     * 
     * @param start value representing start position of the area that will be splitted  
     * @param count the actual length of the area that will be splitted
     */
    public final void splitSpanFlag(final int start, final int count) {
        final ArrayList<SpanFlag> temp = new ArrayList<SpanFlag>();
        int i = 0;
        while (i < mSpanFlags.size()) {
            if (mSpanFlags.get(i).start >= start) {
                mSpanFlags.get(i).start = mSpanFlags.get(i).start + count;
                mSpanFlags.get(i).end = mSpanFlags.get(i).end + count;
                i++;
            } else {
                if (mSpanFlags.get(i).start < start && mSpanFlags.get(i).end >= (start + count)) {
                    decryptText(start, (start + count));
                    encryptText(mSpanFlags.get(i).start, start);
                    encryptText((start + count), (mSpanFlags.get(i).end + count));
                    temp.add(new SpanFlag(mSpanFlags.get(i).start, start));
                    temp.add(new SpanFlag((start + count), (mSpanFlags.get(i).end + count)));
                    mSpanFlags.remove(i);
                } else {
                    i++;
                }
            }
        }
        mSpanFlags.addAll(temp);
        Collections.sort(mSpanFlags, new SpanFlagComparator());
    }

    /**
     * method to modify existing areas that are currently encrypted
     * 
     * @param start start value
     * @param before before value
     */
    public final void deleteSpanFlag(final int start, final int before) {
        final ArrayList<SpanFlag> temp = new ArrayList<SpanFlag>();
        int i = 0;
        while (i < mSpanFlags.size()) {
            if (mSpanFlags.get(i).start >= (start + before)) {
                // delete left side
                mSpanFlags.get(i).start = mSpanFlags.get(i).start - before;
                mSpanFlags.get(i).end = mSpanFlags.get(i).end - before;
                i++;
            } else {
                if (mSpanFlags.get(i).start >= start && mSpanFlags.get(i).end <= (start + before)) {
                    // delete all
                    mSpanFlags.remove(i);
                } else {
                    if (mSpanFlags.get(i).start < start && mSpanFlags.get(i).end > (start + before)) {
                        // delete inner part
                        temp.add(new SpanFlag(mSpanFlags.get(i).start, start));
                        temp.add(new SpanFlag(start, (mSpanFlags.get(i).end - before)));
                        mSpanFlags.remove(i);
                    } else {
                        if (mSpanFlags.get(i).start >= start
                                && mSpanFlags.get(i).end > (start + before)) {
                            // delete left part
                            mSpanFlags.get(i).start = start;
                            mSpanFlags.get(i).end = (mSpanFlags.get(i).end - before);
                            i++;
                        } else {
                            if (mSpanFlags.get(i).start < start && mSpanFlags.get(i).end > start
                                    && mSpanFlags.get(i).end <= (start + before)) {
                                // delete right part
                                mSpanFlags.get(i).end = start;
                                i++;
                            } else {
                                i++;
                            }
                        }
                    }
                }
            }
        }
        mSpanFlags.addAll(temp);
        Collections.sort(mSpanFlags, new SpanFlagComparator());
        mergeSpanFlag();
    }

    /**
     * method to merge elements which overlap same textareas
     */
    public final void mergeSpanFlag() {
        int i = 1;
        while (i < mSpanFlags.size()) {
            if (mSpanFlags.get(i - 1).end > mSpanFlags.get(i).end) {
                mSpanFlags.remove(i);
            } else {
                if (mSpanFlags.get(i - 1).end >= mSpanFlags.get(i).start) {
                    mSpanFlags.get(i - 1).end = mSpanFlags.get(i).end;
                    mSpanFlags.remove(i);
                } else {
                    i++;
                }
            }
        }
        int j = 0;
        while (j < mSpanFlags.size()) {
            if (mSpanFlags.get(j).end == mSpanFlags.get(j).start) {
                mSpanFlags.remove(j);
            } else {
                j++;
            }
        }
    }

    /**
     * method to determine the lock status for the given EditText and the List of Spanflags
     * 
     * @return the current lock status
     */
    public final Lock determineLockStatus() {
        final int end = mText.length();

        switch (mSpanFlags.size()) {
            case 0:
                return Lock.UNLOCKED;
            case 1:
                if (mSpanFlags.get(0).start == 0 && mSpanFlags.get(0).end == end) {
                    return Lock.LOCKED;
                }
            default:
                return Lock.PARTIALLY;
        }
    }

    /**
     * method to return the number of elements contained in the list of encrypted flags
     * 
     * @return count of elements
     */
    public final int getCount() {
        return mSpanFlags.size();
    }

    /**
     * method to return a specific SpanFlag
     * 
     * @param i
     * @return SpanFlag for the given position in the List
     */
    public final SpanFlag getSpanFlag(final int i) {
        return mSpanFlags.get(i);
    }

}
