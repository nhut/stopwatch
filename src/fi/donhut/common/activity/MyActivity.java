package fi.donhut.common.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/**
 * @author Nhut Do @ 2012
 *
 */
public class MyActivity extends Activity {
	
	final public static String LOG_TAG = "StopwatchApp";
	
	/*
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    */
	
	/**
     * Check if intent is safe / is there any application which handles intent
     * @param intent
     * @return
     */
    public boolean isIntentSafe(Intent intent) {
    	PackageManager packageManager = getPackageManager();
    	List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
    	boolean isIntentSafe = activities.size() > 0;
    	return isIntentSafe;
    }
}
