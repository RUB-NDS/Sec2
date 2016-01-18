/**
 * 
 */
package de.adesso.mobile.android.sec2;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import de.adesso.mobile.android.sec2.activity.Sec2ListActivity;
import de.adesso.mobile.android.sec2.adapter.TaskListAdapter;
import de.adesso.mobile.android.sec2.model.TaskListItem;
import de.adesso.mobile.android.sec2.util.BundleHelper;
import de.adesso.mobile.android.sec2.util.LogHelper;

/**
 * @author benner
 *
 */
public class TaskListActivity extends Sec2ListActivity {
	
	 private static final Class<?> c = TaskListActivity.class;
	 private ListView lView;
	 private List<TaskListItem> taskList = new ArrayList<TaskListItem>();
	 private final int OPEN_TASK = 1093;
	 private TaskListAdapter taskListAdapter;
	 private String android_id;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogHelper.logV(c, "onCreate"); 
		getWindow().setFormat(PixelFormat.RGBA_8888);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.task);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

        initTitle();
        initListeners();

//        findViewById(R.id.titlebar_add).setVisibility(View.VISIBLE);
//
//        ListView lView = (ListView) findViewById(android.R.id.list);
//        
//        taskListAdapter = new TaskListAdapter(this);
//        lView.setAdapter(taskListAdapter);
//        
//        TaskListItem taskListItem = new TaskListItem(111, "11.12.2011", "Frank anrufensdf gsdgsdfgsdfgsdfgsdfgsdfgsdfgsdfg", 0, 0, true, "ASDFASDF");
//        taskList.add(taskListItem);
//        
//        taskListAdapter.addAll(taskList);
        
//
//        lView.setOnItemClickListener(new OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent noticeCreateIntent = new Intent(NoticeListActivity.this, NoticeCreateActivity.class);
//                BundleHelper.bundleNoticeListItem(noticeCreateIntent, nla.getItem(position));
//                startActivityForResult(noticeCreateIntent, OPEN_NOTE);
//            }
//        });
//        LoadPreferences();
      //    registerForContextMenu(lView);
//        readNoticeFromDatabase();
	}
	
	private void initTitle() {
        findViewById(R.id.titlebar_add).setVisibility(View.VISIBLE);
        lView = (ListView) findViewById(android.R.id.list);
    }
	
	private void initListeners() {
        ((ImageButton) findViewById(R.id.titlebar_add)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(TaskListActivity.this, TaskCreateActivity.class);
                startActivityForResult(intent, OPEN_TASK);
            }
        });

        taskListAdapter = new TaskListAdapter(this);
        lView.setAdapter(taskListAdapter);

        lView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent taskCreateIntent = new Intent(TaskListActivity.this, TaskCreateActivity.class);
                BundleHelper.bundleTaskListItem(taskCreateIntent, taskListAdapter.getItem(position));
                startActivityForResult(taskCreateIntent, OPEN_TASK);
            }
        });
        registerForContextMenu(lView);

        //        readNoticeFromDatabase();
        //TODO Exist-Server-Communication
        android_id = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
       // new GetXmlFromExistTask(TaskListActivity.this).execute(app.getServer(), app.getDatabase(), android_id);
    }
			
//	/**
//     * GetXmlFromExistTask
//     * @author benner
//     */
//	 private class GetXmlFromExistTask extends TaskService.GetXmlFromExistTask{
//	       
//	        public GetXmlFromExistTask(final Context context) {
//	            super(context);
//	        }
//	      
//	        @Override
//	        protected void onPostExecuteWithoutException(List<?> result) {
//	            super.onPostExecuteWithoutException(result);
//	            taskList = (List<TaskListItem>)result;
//	            taskListAdapter.addAll(taskList);
//	        }
//
//	        @Override
//	        protected void onPostExecuteWithException(List<?> result) {
//	            super.onPostExecuteWithException(result);
//	            taskList.clear();
//	            taskListAdapter.clear();
//	            Toast.makeText(getApplicationContext(), "Couldn't not connect to Server", Toast.LENGTH_LONG).show();
//	        }
//
//	    }
	
	 public void addListItem(View v) {
	        Intent intent = new Intent();
	        intent.setClass(TaskListActivity.this, TaskCreateActivity.class);
	        startActivityForResult(intent, 200);
	    }
}
