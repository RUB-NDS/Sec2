package de.adesso.mobile.android.sec2.service;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import de.adesso.mobile.android.sec2.R;
import de.adesso.mobile.android.sec2.dialog.WaitingDialog;
import de.adesso.mobile.android.sec2.exceptions.AlgorithmNotFoundException;
import de.adesso.mobile.android.sec2.exceptions.KeyNotFoundException;
import de.adesso.mobile.android.sec2.model.Event;
import de.adesso.mobile.android.sec2.model.Lock;
import de.adesso.mobile.android.sec2.model.Notice;
import de.adesso.mobile.android.sec2.model.Task;
import de.adesso.mobile.android.sec2.mwadapter.DeleteRequestProperties;
import de.adesso.mobile.android.sec2.mwadapter.GetRequestProperties;
import de.adesso.mobile.android.sec2.mwadapter.MwAdapter;
import de.adesso.mobile.android.sec2.mwadapter.PutRequestProperties;
import de.adesso.mobile.android.sec2.mwadapter.exceptions.HttpConnectionException;
import de.adesso.mobile.android.sec2.mwadapter.exceptions.MwAdapterException;
import de.adesso.mobile.android.sec2.mwadapter.exceptions.XMLParseException;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.model.User;
import de.adesso.mobile.android.sec2.mwadapter.util.EmailToUserNameMapper;
import de.adesso.mobile.android.sec2.util.AlertHelper;
import de.adesso.mobile.android.sec2.util.EventDomDocumentCreator;
import de.adesso.mobile.android.sec2.util.FileHelper;
import de.adesso.mobile.android.sec2.util.IDialog;
import de.adesso.mobile.android.sec2.util.MwAdapterHelper;
import de.adesso.mobile.android.sec2.util.NoticeDomDocumentCreator;
import de.adesso.mobile.android.sec2.util.TaskDomDocumentCreator;
import de.adesso.mobile.android.sec2.xml.EventParser;
import de.adesso.mobile.android.sec2.xml.NoticeSaxParser;
import de.adesso.mobile.android.sec2.xml.QueryBuilder;
import de.adesso.mobile.android.sec2.xml.TaskParser;
import de.adesso.mobile.android.sec2.xml.XmlHandlerFiles;

/**
 * Service
 * 
 * @author mschmitz
 */
public abstract class Service {

	private static final String TAG = "Service";

	/**
	 * method to handle the occuring exceptions
	 * 
	 * @param context
	 *            Context object used to inflate a dialog
	 * @param exception
	 *            the exception which occured
	 */
	public static void handleExceptions(final Context context,
			final Exception exception) {

		if (exception == null) {
			return;
		}

		if (exception instanceof UnknownHostException
				|| exception instanceof SocketException
				|| exception instanceof ClientProtocolException
				|| exception instanceof SSLException
				|| exception instanceof ConnectTimeoutException
				|| exception instanceof SocketTimeoutException) {
			AlertHelper.showWirelessDialog(context, null);
			return;
		}
	}

	/**
	 * DeleteRequestToUrl
	 * 
	 * @author hoppe
	 * 
	 */
	public static class DeleteRequestToUrl extends
			AbstractSec2Task<Void, Void, InputStream> {

		private final String mKey;
		private final String mAlgorithm;
		private final String mAppName;
		private final DeleteRequestProperties mProperties;
		private final int mPort;

		public DeleteRequestToUrl(final Activity activity, final String key,
				final String algorithm, final String appName, final int port,
				final DeleteRequestProperties properties,
				final String objectName) {
			super(activity, String.format(
					activity.getString(R.string.service_delete), objectName),
					true);
			this.mKey = key;
			this.mAlgorithm = algorithm;
			this.mAppName = appName;
			this.mPort = port;
			this.mProperties = properties;
		}

		@Override
		protected InputStream doInBackground(final Void... params) {
			final MwAdapter mwAdapter = MwAdapter.getInstance();
			InputStream inputStream = null;

			try {
				inputStream = mwAdapter.deleteRequestToUrl(mKey, mAlgorithm,
						mAppName, mPort, mProperties);
			} catch (final Exception e) {
				mException = e;
			}

			return inputStream;
		}
	}

	/**
	 * PutRequestToUrl
	 * 
	 * @author hoppe
	 * 
	 */
	public static class PutRequestToUrl extends
			AbstractSec2Task<Void, Void, InputStream> {

		private final String mKey;
		private final String mAlgorithm;
		private final String mAppName;
		private final PutRequestProperties mProperties;
		private final int mPort;

		public PutRequestToUrl(final Activity activity, final String key,
				final String algorithm, final String appName, final int port,
				final PutRequestProperties properties, final String objectName) {
			super(activity, String.format(
					activity.getString(R.string.service_save), objectName),
					true);
			this.mKey = key;
			this.mAlgorithm = algorithm;
			this.mAppName = appName;
			this.mPort = port;
			this.mProperties = properties;
		}

		@Override
		protected InputStream doInBackground(final Void... params) {
			final MwAdapter mwAdapter = MwAdapter.getInstance();
			InputStream inputStream = null;

			try {
				inputStream = mwAdapter.putRequestToUrl(mKey, mAlgorithm,
						mAppName, mPort, mProperties);
			} catch (final Exception e) {
				mException = e;
			}

			return inputStream;
		}
	}

	/**
	 * AbstractSec2Task
	 * 
	 * @author hoppe
	 * 
	 * @param <Params>
	 *            doInBackground(Params...)
	 * @param <Progress>
	 *            onProgressUpdate(Progress...)
	 * @param <Result>
	 *            onPostExecute(Result...)
	 */
	public abstract static class AbstractSec2Task<Params, Progress, Result>
			extends AsyncTask<Params, Progress, Result> {

		protected WeakReference<Activity> mActivity;
		protected Exception mException;
		protected WaitingDialog mWaitingDialog;
		protected String mMessage;
		protected boolean mShowDialog;

		/**
		 * @param activity
		 *            Context to the calling activity. Used to create a request
		 *            and showing a ProgressDialog
		 * @param message
		 *            message that will be shown in the ProgressDialog
		 * @param showDialog
		 *            boolean toggle to decide if the ProgressDialog will be
		 *            shown or not
		 */
		protected AbstractSec2Task(final Activity activity,
				final String message, final boolean showDialog) {
			this.mActivity = new WeakReference<Activity>(activity);
			this.mMessage = message;
			this.mShowDialog = showDialog;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mActivity.get() != null && !isCancelled() && mShowDialog) {
				mWaitingDialog = new WaitingDialog(mActivity.get(), null);
				mWaitingDialog.setMessage(mMessage);
				mWaitingDialog.show();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(final Progress... values) {
			if (values.length == 1 && values instanceof String[]) {
				if (mWaitingDialog != null) {
					mWaitingDialog.setMessage((String) values[0]);
				} else {
					mMessage = (String) values[0];
				}
			}
		};

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(final Result result) {
			if (mActivity.get() != null && !isCancelled() && mShowDialog) {
				mWaitingDialog.hide();
				mWaitingDialog.dismiss();
			}
			if (mException == null) {
				onPostExecuteWithoutException(result);
			} else {
				onPostExecuteWithException(result);
			}

		}

		/**
		 * method that will be called when an Exception during
		 * doInBackground(...) occurs
		 * 
		 * @param result
		 *            the result that will be returned by doInBackground(...)
		 */
		protected void onPostExecuteWithException(final Result result) {
			handleExceptions(mActivity.get(), mException);
		}

		/**
		 * method that will be called when no Exception during
		 * doInBackground(...) occurs
		 * 
		 * @param result
		 *            the result that will be returned by doInBackground(...)
		 */
		protected void onPostExecuteWithoutException(final Result result) {
		}

	}

	/**
	 * 
	 * Class to load a list of all Users that are known to the current user
	 * 
	 * @author hoppe
	 * 
	 */
	public static class GetAllUsersTask extends
			AbstractTask<Void, Void, User[]> {

		/**
		 * 
		 * @param activity
		 *            calling activity
		 */
		public GetAllUsersTask(final Activity activity, final IDialog iDialog) {
			super(activity, iDialog, activity
					.getString(R.string.service_get_group));
		}

		@Override
		protected User[] doInBackground(final Void... params) {
			User[] user = null;
			try {
				final MwAdapterHelper adapterHelper = new MwAdapterHelper(
						mActivity);
				user = MwAdapter.getInstance().getAllUsers(
						adapterHelper.getAppAuthKey(),
						adapterHelper.getAppAuthKeyAlgorithm(),
						mActivity.getPackageName(),
						adapterHelper.getMiddlewarePort());
				if (user != null) {
					for (int i = 0; i < user.length; i++) {
						EmailToUserNameMapper.emailToUserName(mActivity,
								user[i]);
						// Set the username
						if (user[i].getUserName() == null) {
							user[i].setUserName(mActivity
									.getString(R.string.event_username_not_found));
						}
					}
				}
			} catch (final KeyNotFoundException e) {
				mException = e;
			} catch (AlgorithmNotFoundException e) {
				mException = e;
			} catch (HttpConnectionException e) {
				mException = e;
			} catch (MwAdapterException e) {
				mException = e;
			} catch (XMLParseException e) {
				mException = e;
			}
			return user;
		}
	}

	/**
	 * GetFileTask
	 * 
	 * Class to load a File from the Cloud
	 * 
	 * @author hoppe
	 * 
	 */
	public static class GetFileTask extends AbstractTask<Void, String, File> {

		private final String mFileName;

		protected GetFileTask(final Activity activity, final String fileName,
				final IDialog iDialog) {
			super(activity, iDialog, String.format(
					activity.getString(R.string.service_get_file), fileName));
			this.mFileName = fileName;
		}

		@Override
		protected File doInBackground(final Void... params) {

			try {
				final MwAdapterHelper mAdapterHelper = new MwAdapterHelper(
						mActivity);

				final SAXParser xmlParser = SAXParserFactory.newInstance()
						.newSAXParser();
				final GetRequestProperties requestProperties = new GetRequestProperties(
						mAdapterHelper.getCloudHostName(),
						mAdapterHelper.getCloudPath() + mFileName + ".xml",
						mAdapterHelper.getCloudPort());
				final InputStream is = MwAdapter.getInstance().getRequestToUrl(
						mAdapterHelper.getAppAuthKey(),
						mAdapterHelper.getAppAuthKeyAlgorithm(),
						mActivity.getPackageName(),
						mAdapterHelper.getMiddlewarePort(), requestProperties);

				if (is == null) {
					throw new FileNotFoundException();
				} else {
					final XmlHandlerFiles xmlHandler = new XmlHandlerFiles();
					xmlParser.parse(is, xmlHandler);

					// a file is either encrypted or unencrypted
					if (xmlHandler.getSec2File().getLock().equals(Lock.LOCKED)) {
						xmlHandler.getSec2File().setPlainText(
								xmlHandler.getSec2File().getPartsToEncrypt());
					}

					return FileHelper.decodeBase64(mFileName, xmlHandler
							.getSec2File().getPlainText());
				}

			} catch (final IOException e) {
				mException = e;
			} catch (final ParserConfigurationException e) {
				mException = e;
			} catch (final SAXException e) {
				mException = e;
			} catch (final HttpConnectionException e) {
				mException = e;
			} catch (final MwAdapterException e) {
				mException = e;
			} catch (final XMLParseException e) {
				mException = e;
			} catch (final AlgorithmNotFoundException e) {
				mException = e;
			} catch (final KeyNotFoundException e) {
				mException = e;
			}
			return null;
		}
	}

	/**
	 * GetEventListTask
	 * 
	 * Class to load a list of all Events from the Cloud
	 * 
	 * @author hoppe
	 * 
	 */
	public static class GetEventListTask extends
			AbstractTask<Object, String, List<EventDomDocumentCreator>> {

		protected GetEventListTask(final Activity activity,
				final IDialog iDialog) {
			super(activity, iDialog, activity
					.getString(R.string.service_get_events));
		}

		@Override
		protected List<EventDomDocumentCreator> doInBackground(
				final Object... params) {
			InputStream is = null;

			try {
				final List<EventDomDocumentCreator> events = new LinkedList<EventDomDocumentCreator>();

				is = getRequestToUrl(mActivity, "?_query=/event");

				if (is != null) {
					final List<Event> eventList = EventParser.parseList(is);
					for (Event event : eventList) {
						events.add(new EventDomDocumentCreator(event
								.getFilename(), event));
					}
				}
				return events;
			} catch (HttpConnectionException e) {
				mException = e;
			} catch (MwAdapterException e) {
				mException = e;
			} catch (XMLParseException e) {
				mException = e;
			} catch (KeyNotFoundException e) {
				mException = e;
			} catch (AlgorithmNotFoundException e) {
				mException = e;
			} catch (SAXException e) {
				mException = e;
			} catch (IOException e) {
				mException = e;
			} finally {
				closeStream(is);
			}
			return null;
		}
	}

	/**
	 * GetNoticeListTask
	 * 
	 * Class to load a list of all Notices from the Cloud
	 * 
	 * @author hoppe
	 * 
	 */
	public static class GetNoticeListTask extends
			AbstractTask<Object, String, List<NoticeDomDocumentCreator>> {

		protected GetNoticeListTask(final Activity activity,
				final IDialog iDialog) {
			super(activity, iDialog, activity
					.getString(R.string.service_get_notices));
		}

		@Override
		protected List<NoticeDomDocumentCreator> doInBackground(
				final Object... params) {
			InputStream is = null;
			try {
				final List<NoticeDomDocumentCreator> notices = new LinkedList<NoticeDomDocumentCreator>();

				is = getRequestToUrl(mActivity, "?_query=/notice");

				if (is != null) {
					final List<Notice> noticeList = NoticeSaxParser
							.parseList(is);
					for (Notice notice : noticeList) {
						notices.add(new NoticeDomDocumentCreator(notice
								.getFilename(), notice));
					}
				}
				return notices;
			} catch (HttpConnectionException e) {
				mException = e;
			} catch (MwAdapterException e) {
				mException = e;
			} catch (XMLParseException e) {
				mException = e;
			} catch (KeyNotFoundException e) {
				mException = e;
			} catch (AlgorithmNotFoundException e) {
				mException = e;
			} catch (SAXException e) {
				mException = e;
			} catch (IOException e) {
				mException = e;
			} finally {
				closeStream(is);
			}
			return null;
		}
	}

	/**
	 * GetTaskListFromExist
	 * 
	 * Class to load a list of all Tasks from the Cloud
	 * 
	 * @author hoppe
	 * 
	 */
	public static class GetTaskListTask extends
			AbstractTask<Object, String, List<TaskDomDocumentCreator>> {

		protected GetTaskListTask(final Activity activity, final IDialog iDialog) {
			super(activity, iDialog, activity
					.getString(R.string.service_get_tasks));
		}

		@Override
		protected List<TaskDomDocumentCreator> doInBackground(
				final Object... params) {
			InputStream is = null;
			try {
				final List<TaskDomDocumentCreator> tasks = new LinkedList<TaskDomDocumentCreator>();

				is = getRequestToUrl(mActivity, "?_query=/task");

				if (is != null) {
					final List<Task> taskList = TaskParser.parseList(is);
					for (Task task : taskList) {
						tasks.add(new TaskDomDocumentCreator(
								task.getFilename(), task));
					}
				}
				return tasks;
			} catch (HttpConnectionException e) {
				mException = e;
			} catch (MwAdapterException e) {
				mException = e;
			} catch (XMLParseException e) {
				mException = e;
			} catch (KeyNotFoundException e) {
				mException = e;
			} catch (AlgorithmNotFoundException e) {
				mException = e;
			} catch (SAXException e) {
				mException = e;
			} catch (IOException e) {
				mException = e;
			} finally {
				closeStream(is);
			}
			return null;
		}
	}

	/**
	 * AbstractTask
	 * 
	 * @author hoppe
	 * 
	 * @param <Params>
	 *            doInBackground(Params...)
	 * @param <Progress>
	 *            onProgressUpdate(Progress...)
	 * @param <Result>
	 *            onPostExecute(Result...)
	 */
	public abstract static class AbstractTask<Params, Progress, Result> extends
			AsyncTask<Params, Progress, Result> {

		protected Activity mActivity;
		protected IDialog mIDialog;
		protected Exception mException;
		private final String mMessage;

		/**
		 * Constructor
		 * 
		 * @param activity
		 *            Context to the calling activity. Used to create a request
		 *            and showing a ProgressDialog
		 * @param iDialog
		 *            Interface to manage start stop and update of the
		 *            ProgressDialog
		 */
		protected AbstractTask(final Activity activity, final IDialog iDialog) {
			mActivity = activity;
			mIDialog = iDialog;
			mMessage = "";
		}

		/**
		 * @param activity
		 *            Context to the calling activity. Used to create a request
		 *            and showing a ProgressDialog
		 * @param iDialog
		 *            Interface to manage start stop and update of the
		 *            ProgressDialog
		 * @param message
		 *            message that will be shown in the ProgressDialog
		 */
		protected AbstractTask(final Activity activity, final IDialog iDialog,
				final String message) {
			mActivity = activity;
			mIDialog = iDialog;
			mMessage = message;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIDialog.start(mMessage);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(final Progress... values) {
			if (values.length == 1 && values instanceof String[]) {
				mIDialog.update((String) values[0]);
			}
		};

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(final Result result) {
			mIDialog.stop();
			if (mException == null) {
				onPostExecuteWithoutException(result);
			} else {
				onPostExecuteWithException(result);
			}
		}

		/**
		 * method that will be called when an Exception during
		 * doInBackground(...) occurs
		 * 
		 * @param result
		 *            the result that will be returned by doInBackground(...)
		 */
		protected void onPostExecuteWithException(final Result result) {
			handleExceptions(mActivity, mException);
		}

		/**
		 * method that will be called when no Exception during
		 * doInBackground(...) occurs
		 * 
		 * @param result
		 *            the result that will be returned by doInBackground(...)
		 */
		protected void onPostExecuteWithoutException(final Result result) {
		}

	}

	/**
	 * Method to close a given closeable object
	 * 
	 * @param closeable
	 *            <Closeable> that will be closed
	 */
	private static void closeStream(final Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (final IOException e) {
			Log.e(TAG, "Exception occured while closing stream");
		}
	}

	/**
	 * method to retrieve an InputStream containing data depending on the given
	 * query
	 * 
	 * @param activity
	 *            used to interact with the middleware server
	 * @param query
	 *            the actual query for the request
	 * @return an InputStream containing the requested data
	 * @throws HttpConnectionException
	 *             thrown when an Exception occures
	 * @throws MwAdapterException
	 *             thrown when an Exception occures
	 * @throws XMLParseException
	 *             thrown when an Exception occures
	 * @throws KeyNotFoundException
	 *             thrown when an Exception occures
	 * @throws AlgorithmNotFoundException
	 *             thrown when an Exception occures
	 */
	private static InputStream getRequestToUrl(final Activity activity,
			final String query) throws HttpConnectionException,
			MwAdapterException, XMLParseException, KeyNotFoundException,
			AlgorithmNotFoundException {

		final MwAdapterHelper adapterHelper = new MwAdapterHelper(activity);
		
		final Group[] groups = MwAdapter.getInstance()
				.getGroupsForRegisteredUser(adapterHelper.getAppAuthKey(),
						adapterHelper.getAppAuthKeyAlgorithm(),
						activity.getPackageName(),
						adapterHelper.getMiddlewarePort());
		
		final String queryURL = adapterHelper.getCloudPath()
				+ QueryBuilder.createQuery(groups, query);

		final GetRequestProperties requestProperties = new GetRequestProperties(
				adapterHelper.getCloudHostName(), queryURL,
				adapterHelper.getCloudPort());
		
		Log.d(TAG, "queryURL: " + adapterHelper.getCloudHostName() + ":"
				+ adapterHelper.getCloudPort() + queryURL);

		InputStream inputStream = MwAdapter.getInstance().getRequestToUrl(
				adapterHelper.getAppAuthKey(),
				adapterHelper.getAppAuthKeyAlgorithm(),
				activity.getPackageName(), adapterHelper.getMiddlewarePort(),
				requestProperties);
		
		return inputStream;
	}
}
