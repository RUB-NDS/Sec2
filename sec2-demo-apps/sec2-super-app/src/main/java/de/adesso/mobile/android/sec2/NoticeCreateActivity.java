
package de.adesso.mobile.android.sec2;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;
import de.adesso.mobile.android.sec2.activity.Sec2Activity;
import de.adesso.mobile.android.sec2.model.Lock;
import de.adesso.mobile.android.sec2.model.Notice;
import de.adesso.mobile.android.sec2.mwadapter.PutRequestProperties;
import de.adesso.mobile.android.sec2.mwadapter.util.MwAdapterPreferenceManager;
import de.adesso.mobile.android.sec2.service.Service;
import de.adesso.mobile.android.sec2.util.CheckedGroupHandler;
import de.adesso.mobile.android.sec2.util.Constants;
import de.adesso.mobile.android.sec2.util.NoticeDomDocumentCreator;
import de.adesso.mobile.android.sec2.util.SpanFlag;
import de.adesso.mobile.android.sec2.util.SpanFlagHelper;

/**
 * NoticeCreateActivity
 * @author hoppe
 */

public class NoticeCreateActivity extends Sec2Activity {

    private static final int NO_KEY_OR_ALGORITHM = 0;
    private static final int ERROR = 1;
    private static final int DB_ERROR = 2;
    private static final int SUCCESS = 3;
    private static final int RESULT_ID = 1094;

    private NoticeDomDocumentCreator mNoticeCreator = null;
    private String mErrorMessage = null;
    private boolean mNewNotice;

    private SpanFlagHelper mSpanFlagHelper;

    private EditText mSubject;
    private EditText mInputArea;
    private ToggleButton mLock;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.noticecreate);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        initViewElements();
        initFromBundle();
        initListeners();
    }

    private void initViewElements() {
        mInputArea = (EditText) findViewById(R.id.noticecreate_text_top);
        mSubject = (EditText) findViewById(R.id.noticecreate_header_text_top);
        mLock = ((ToggleButton) findViewById(R.id.noticecreate_lock));
    }

    private void initFromBundle() {
        mSpanFlagHelper = new SpanFlagHelper(NoticeCreateActivity.this, mInputArea);

        mNoticeCreator = (NoticeDomDocumentCreator) (getIntent()
                .getSerializableExtra(Constants.INTENT_EXTRA_NOTICE));

        // checking if we are going to modify an existing notice or if we create a new one
        if (mNoticeCreator != null && mNoticeCreator.getNotice() != null) {
            mSubject.setText(mNoticeCreator.getNotice().getSubject());
            mInputArea.setText(getInputAreaText());

            mLock.setChecked(mNoticeCreator.getNotice().getLock() == Lock.LOCKED);

            mSpanFlagHelper.initiateText();
        } else {
            mSubject.setText("");
            mInputArea.setText("");
        }
    }

    /**
     * construct the text to display 
     * @return String representation of the text
     */
    private String getInputAreaText() {
        final Notice notice = mNoticeCreator.getNotice();
        final StringBuilder displayedText = new StringBuilder(notice.getNoticeText());
        SpanFlag spanFlag = null;
        final int numberOfSpanFlags = (notice.getPartsToEncrypt() != null ? notice
                .getPartsToEncrypt().size() : 0);
        int position = 0;

        for (int i = 0; i < numberOfSpanFlags; i++) {
            position = displayedText.indexOf(Notice.PLACE_HOLDER, position);
            if (position >= 0) {
                displayedText.replace(position, position + Notice.PLACE_HOLDER.length(), notice
                        .getPartsToEncrypt().get(i));
                spanFlag = new SpanFlag(position, position
                        + notice.getPartsToEncrypt().get(i).length());
                mSpanFlagHelper.addSpanFlag(spanFlag);
                position = spanFlag.end;
            }
        }

        return displayedText.toString();
    }

    /**
     * initialize the listener to detect user interaction
     * 
     * ("NewApi") - needed for EditText API 11+. If the user double-taps or long-clicks on text in the EditText an actionbar 
     * will appear and let the user cut/copy/paste he text. In API < 11 it is a Dialog. 
     */
    @SuppressLint("NewApi")
    private void initListeners() {

        ((Button) findViewById(R.id.noticecreate_save))
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        saveNotice();
                    }
                });

        mInputArea.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before,
                    final int count) {
                if (count > 0) {
                    mLock.setChecked(false);
                    mSpanFlagHelper.splitSpanFlag(start, count);
                } else {
                    mSpanFlagHelper.deleteSpanFlag(start, before);
                }
            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count,
                    final int after) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
            }
        });

        mLock.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                if (mLock.isChecked()) {
                    mSpanFlagHelper.encryptAll();
                } else {
                    mSpanFlagHelper.decryptAll();
                }

                if (!mInputArea.isFocused()) {
                    mInputArea.requestFocus();
                }
            }
        });

        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {

            mInputArea.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

                @Override
                public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(final ActionMode mode) {
                }

                @Override
                public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
                    mode.setTitle("");
                    menu.clear();
                    final MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.event_context_menu, menu);
                    return true;
                }

                @Override
                public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_encrypt:
                            mSpanFlagHelper.encrypt(mInputArea.getSelectionStart(),
                                    mInputArea.getSelectionEnd());
                            return true;
                        case R.id.menu_decrypt:
                            mSpanFlagHelper.decrypt(mInputArea.getSelectionStart(),
                                    mInputArea.getSelectionEnd());
                            return true;
                        default:
                            return false;
                    }
                }
            });
        } else {
            registerForContextMenu(mInputArea);
        }

    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED);
            NoticeCreateActivity.this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     *  additional entries
     */
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View view,
            final ContextMenu.ContextMenuInfo menuInfo) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notice_context_menu_encrypt, menu);
        super.onCreateContextMenu(menu, view, menuInfo);
    }

    /**
     *  functionality additionally added into the contextmenu
     */
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_encrypt_some:
                mSpanFlagHelper.encrypt(mInputArea.getSelectionStart(),
                        mInputArea.getSelectionEnd());
                return true;
            case R.id.menu_decrypt_some:
                mSpanFlagHelper.decrypt(mInputArea.getSelectionStart(),
                        mInputArea.getSelectionEnd());
                return true;
            case R.id.menu_encrypt_all:
                mSpanFlagHelper.encryptAll();
                return true;
            case R.id.menu_decrypt_all:
                mSpanFlagHelper.decryptAll();
                return true;
            case R.id.menu_delete_all:
                mSpanFlagHelper.clear();
                mInputArea.setText("");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void saveNotice() {
        final int spanFlagCount = mSpanFlagHelper.getCount();
        // final String[] partsToEncrypt = new String[spanFlagCount];
        final List<String> partsToEncrypt = new ArrayList<String>();
        final StringBuilder sb = new StringBuilder(mInputArea.getText().toString());
        final Intent intent = new Intent();
        Notice notice = null;
        String documentName = null;
        SpanFlag spanFlag = null;
        int offset = 0;

        final String subject = mSubject.getText().toString();
        // Falls eine neue Notiz erstellt wird, ist noticeCreator == null
        if (mNoticeCreator == null) {
            mNoticeCreator = new NoticeDomDocumentCreator();
            notice = new Notice();
            notice.setCreationDate(new GregorianCalendar());
            documentName = "notice_" + subject + "_" + notice.getCreationDate().getTimeInMillis()
                    + ".xml";
            documentName = documentName.replaceAll("\\s", "_"); // Ersetze Leerstellen durch "_"
            mNoticeCreator.setDocumentName(documentName);
            notice.setFilename(documentName);
            mNewNotice = true;
        }
        // Ansonsten hole die Notiz von noticeCreator
        else {
            notice = mNoticeCreator.getNotice();
            mNewNotice = false;
        }

        notice.setSubject(subject);
        notice.setLock(mSpanFlagHelper.determineLockStatus());

        for (int i = 0; i < spanFlagCount; i++) {
            spanFlag = mSpanFlagHelper.getSpanFlag(i);
            final String subString = sb.substring(spanFlag.start - offset, spanFlag.end - offset);
            partsToEncrypt.add(subString);
            sb.replace(spanFlag.start - offset, spanFlag.end - offset, Notice.PLACE_HOLDER);
            offset += subString.length() - Notice.PLACE_HOLDER.length();
        }

        notice.setNoticeText(sb.toString());
        notice.setPartsToEncrypt(partsToEncrypt);

        mNoticeCreator.setNotice(notice);

        if (spanFlagCount > 0) {
            intent.setClass(NoticeCreateActivity.this, GroupChooserActivity.class);
            intent.putExtra(Constants.INTENT_EXTRA_DOM, mNoticeCreator);
            startActivityForResult(intent, RESULT_ID);
        }
        // If there is nothing to encrypt send the XML document directly
        else {
            try {
                saveNotice(mNoticeCreator.createDomDocument(new CheckedGroupHandler()));
            } catch (final Exception e) {
                if (e.getMessage() != null) {
                    mErrorMessage = e.getMessage();
                } else {
                    mErrorMessage = "";
                }
                showDialog(ERROR);
                setResult(Activity.RESULT_CANCELED);
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                final String xmlDocument = data.getStringExtra(Constants.INTENT_EXTRA_RESULT);
                if (xmlDocument != null && !xmlDocument.isEmpty()) {
                    saveNotice(xmlDocument);
                } else {
                    setResult(Activity.RESULT_CANCELED);
                }
                break;
            case Activity.RESULT_CANCELED:
                setResult(Activity.RESULT_CANCELED);
                break;
            default:
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.notice_create_success_title);

        switch (id) {
            case SUCCESS:
                alertDialogBuilder.setMessage(R.string.notice_create_success_true);
                alertDialogBuilder.setPositiveButton(android.R.string.ok, listenerResultOk);
                break;
            case NO_KEY_OR_ALGORITHM:
                alertDialogBuilder.setMessage(R.string.notice_create_success_false_key);
                alertDialogBuilder.setPositiveButton(android.R.string.ok, listenerResultCanceled);
                break;
            case DB_ERROR:
                alertDialogBuilder.setMessage(R.string.notice_create_success_false_db);
                alertDialogBuilder.setPositiveButton(android.R.string.ok, listenerResultCanceled);
                break;
            case ERROR:
                alertDialogBuilder.setMessage(getString(R.string.notice_create_success_false_error)
                        + mErrorMessage);
                alertDialogBuilder.setPositiveButton(android.R.string.ok, listenerResultCanceled);
                break;
        }

        return alertDialogBuilder.create();
    }

    /**
     * listener that will be used to inform the calling activity of the user action
     */
    private final DialogInterface.OnClickListener listenerResultOk = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            NoticeCreateActivity.this.finish();
        }
    };

    /**
     * listener that will be used to inform the calling activity of the user action
     */
    private final DialogInterface.OnClickListener listenerResultCanceled = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            setResult(Activity.RESULT_CANCELED);
        }
    };

    /**
     * saves a notice in the cloud using the input information
     * 
     * @param xmlContent    xml formatted content that will be stored in the cloud.
     */
    private void saveNotice(final String xmlContent) {
        final MwAdapterPreferenceManager prefManager = new MwAdapterPreferenceManager(
                NoticeCreateActivity.this);
        final String key = prefManager.getAppAuthKey();
        final String algorithm = prefManager.getAppAuthKeyAlgorithm();
        final String noticeName = mNoticeCreator.getDocumentName();
        String path = null;
        PutRequestProperties requestProperties = null;
        PutRequestToUrlTask task = null;

        if (key != null && algorithm != null) {
            path = prefManager.getCloudPath();
            if (path.isEmpty() || path.endsWith("/")) {
                requestProperties = new PutRequestProperties(prefManager.getCloudHostName(), path
                        + noticeName, prefManager.getCloudPort(), xmlContent);
            } else {
                requestProperties = new PutRequestProperties(prefManager.getCloudHostName(), path
                        + "/" + noticeName, prefManager.getCloudPort(), xmlContent);
            }
            task = new PutRequestToUrlTask(NoticeCreateActivity.this, noticeName, key, algorithm,
                    getApplication().getPackageName(), prefManager.getMiddlewarePort(),
                    requestProperties);
            task.execute();
        } else {
            showDialog(NO_KEY_OR_ALGORITHM);
            setResult(Activity.RESULT_CANCELED);
        }
    }

    /**
     * PutRequestToUrlTask - Asynchronous call to save a new notice or update an existing notice
     * 
     * @author hoppe
     *
     */
    private class PutRequestToUrlTask extends Service.PutRequestToUrl {

        private final String noticeName;

        /**
         * Constructor for the asynchronous call to save a new notice or update an existing notice
         * 
         * @param activity      context to the activity. needed for showing a progress dialog which indicates how the current upload progress is
         * @param noticeName    name of the notice which will be stored in the cloud
         * @param key           key for the encryption
         * @param algorithm     algorithm used for the encryption
         * @param appName       name of the application
         * @param port          port of the cloud
         * @param properties    properties which will be used by the middleware to store the notice in the cloud
         */
        public PutRequestToUrlTask(final Activity activity, final String noticeName,
                final String key, final String algorithm, final String appName, final int port,
                final PutRequestProperties properties) {
            super(activity, key, algorithm, appName, port, properties, activity
                    .getString(R.string.service_notice));
            this.noticeName = noticeName;
        }

        @Override
        protected void onPostExecuteWithoutException(final InputStream result) {
            super.onPostExecuteWithoutException(result);
            if (mNewNotice) {
                showDialog(SUCCESS);
                final Intent resultIntent = new Intent(NoticeCreateActivity.this,
                        NoticeListActivity.class);
                resultIntent.putExtra(Constants.INTENT_EXTRA_RESULT, mNoticeCreator);
                setResult(Activity.RESULT_OK, resultIntent);
            } else {
                showDialog(SUCCESS);
                final Intent resultIntent = new Intent();
                resultIntent.putExtra(Constants.INTENT_EXTRA_RESULT, mNoticeCreator);
                setResult(Activity.RESULT_OK, resultIntent);
            }
        }

        @Override
        protected void onPostExecuteWithException(final InputStream result) {
            super.onPostExecuteWithException(result);
            if (mException.getMessage() != null) {
                mErrorMessage = mException.getMessage();
            } else {
                mErrorMessage = "";
            }
            showDialog(ERROR);
            setResult(Activity.RESULT_CANCELED);
        }

    }

}
