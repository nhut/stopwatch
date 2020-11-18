package fi.donhut.stopwatch;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import fi.donhut.common.activity.MyActivity;
import fi.donhut.stopwatch.model.StopWatch;

/**
 * 
 * @author Nhut Do @ 2012
 *
 */
public class MainActivity extends MyActivity {
	
	private StopWatch stopWatch;
	final private int REFRESH_RATE = 10;
	
	private int lap = 1;
	
	//Android handler
	private Handler mHandler = null;
	
	private Runnable stopWatchThread = null;
	
	//View elements
	private LinearLayout layoutStoppedState, layoutStartedState = null;
	
	//LAPS history
	private List<StopWatch> stopWatchsHistory = new ArrayList<StopWatch>();
	
	//For sending. (Settings)
	private List<String> sendEmailAddress = new LinkedList<String>();
	private List<String> sendSMSNumbers = new LinkedList<String>();
	
	final private static NumberFormat lapNumberFormatter = new DecimalFormat("00");
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //INITIALIZE DEFAULT VALUES
        mHandler = new Handler();
        stopWatch = new StopWatch(mHandler);
        stopWatchThread = new ClockTickThread();
        setClockTime(stopWatch);
        
        layoutStoppedState = (LinearLayout) findViewById(R.id.layoutStoppedState);
        layoutStartedState = (LinearLayout) findViewById(R.id.layoutStartedState);
        
        /*
        if(Constants.devMode) {
	        for(int i=0; i<5; i++) {
		        stopWatch = new StopWatch(mHandler);
		        Random random = new Random();
		        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTF-8"));
		        cal.set(Calendar.HOUR_OF_DAY, 0);
		        cal.set(Calendar.MINUTE, random.nextInt(8));
		        cal.set(Calendar.SECOND, random.nextInt(60));
		        cal.set(Calendar.MILLISECOND, random.nextInt(1000));
		        stopWatch.setElapsedTime(cal.getTimeInMillis());
		        doLap(null);
	        }
        }
        */
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(super.onOptionsItemSelected(item)) {
    		return true;
    	}
    	SharedPreferences privatePreferences = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	switch (item.getItemId()) {
		case R.id.menu_send_email:
			sendEmailAddress.clear();
			String emailAddress = privatePreferences.getString(Constants.PREFERENCE_DEFAULT_EMAIL_RECEIVER, "");
			Log.d(LOG_TAG, Constants.PREFERENCE_DEFAULT_EMAIL_RECEIVER+"="+emailAddress);
			if(emailAddress != null && emailAddress.length() > 0) {
				sendEmailAddress.add(emailAddress);
			}
			sendEmail(getTextForSending(isIncludeTakenTime(Constants.PREFERENCE_DEFAULT_EMAIL_INCLUDE_TAKETIME), true), sendEmailAddress);
			return true;
			
		case R.id.menu_send_sms:
			sendSMSNumbers.clear();
			String phoneNumber = privatePreferences.getString(Constants.PREFERENCE_DEFAULT_SMS_RECEIVER, "");
			Log.d(LOG_TAG, Constants.PREFERENCE_DEFAULT_SMS_RECEIVER+"="+phoneNumber);
			if(phoneNumber != null && phoneNumber.length() > 0) {
				sendSMSNumbers.add(phoneNumber);
			}
			sendSMS(getTextForSending(isIncludeTakenTime(Constants.PREFERENCE_DEFAULT_SMS_INCLUDE_TAKETIME), false), sendSMSNumbers);
			return true;
			
		case R.id.menu_about:
			PackageInfo packageInfo = null;
			try {
		        packageInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);
		    } catch (NameNotFoundException e) {}
			final String APPNAME = getText(R.string.app_name)+" "+(packageInfo == null ? "" : packageInfo.versionName);
			String message = getString(R.string.menu_about_message, APPNAME, getText(R.string.author), getText(R.string.app_created));
			new AlertDialog.Builder(this)
		    .setTitle(getText(R.string.menu_about))
		    .setMessage(message)
		    .setPositiveButton(getText(R.string.button_ok), null)
		    .show();
			return true;
			
		case R.id.menu_settings:
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingsIntent);
			return true;
			
		default:
			break;
		}
    	return super.onOptionsItemSelected(item);
    }
    
	@Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	stopClock();
    }
	
	@Override
	public void onBackPressed() {
		//HANDLES BACK-BUTTON PRESS
		if(stopWatch != null && stopWatch.isRunning()) {
			AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
			alertDlg.setMessage(getText(R.string.exit_confirm));
			alertDlg.setCancelable(false); // We avoid that the dialong can be cancelled, forcing the user to choose one of the options
			alertDlg.setPositiveButton(getText(R.string.button_yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					MainActivity.super.onBackPressed();
				}
			});
			alertDlg.setNegativeButton(getText(R.string.button_no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// We do nothing
				}
			});
			//SHOW CONFIRMATION DIALOG
			alertDlg.create().show();
			return ;
		}
		MainActivity.super.onBackPressed();
	}
    
	/**
     * Common move to send email.
     * @param emailAddress
     */
    private void sendEmail(String message, List<String> emailAddress) {
    	if(emailAddress == null) {
    		return ;
    	}
    	StringBuilder sbEmailAddresses = new StringBuilder();
    	for(int i=0; i<emailAddress.size(); i++) {
    		if(i != 0) {
    			sbEmailAddresses.append(";");
    		}
    		sbEmailAddresses.append(emailAddress.get(i));
    	}
    	
    	Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"+sbEmailAddresses.toString()+"?subject=" +
    		    Uri.encode(getString(R.string.send_email_subject)) +  "&body=" +
    		    Uri.encode(message)));
    	
    	final String text = "Move to send email "+emailAddress;
    	if(isIntentSafe(intent)) {
    		Log.d(MainActivity.LOG_TAG, text+"; OK");
    		startActivity(intent);
//    		startActivityForResult(intent, INTENT_ACTION_REQUEST.SEND_EMAIL.ordinal()); //DIDN'T WORK
    	} else {
    		Log.e(MainActivity.LOG_TAG, text+"; FAIL!");
    	}
    }
    
    /**
     * Common send sms message
     * @param message
     * @param receivers
     */
    private void sendSMS(String message, List<String> receivers) {
    	Intent intent = null;
    	Uri uri = null;
    	if(receivers != null && !receivers.isEmpty()) {
	    	String smsNumbers = TextUtils.join(",", receivers);
	    	uri = Uri.parse("smsto:" + smsNumbers);
	    	intent = new Intent(Intent.ACTION_SENDTO, uri);
	    	
    	} else {
	    	uri = Uri.parse("sms:");
	    	intent = new Intent(Intent.ACTION_VIEW);
    	}
    	intent.putExtra("sms_body", message); 
    	final String text = "Move to send sms "+receivers;
    	if(isIntentSafe(intent)) {
    		Log.d(MainActivity.LOG_TAG, text+"; OK");
    		startActivity(intent);
    	} else {
    		Log.e(MainActivity.LOG_TAG, text+"; FAIL!");
    	}
	}
    
    private String getTextForSending(boolean includeTakenTime, boolean isForEmail) {
    	Calendar startTimeCal = GregorianCalendar.getInstance();
    	Calendar stopTimeCal = GregorianCalendar.getInstance();
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    	
    	StringBuilder stringBuilder = new StringBuilder();
    	if(stopWatchsHistory.size() > 0) {
    		boolean firstLoop = true;
	    	for(StopWatch stopWatch : stopWatchsHistory) {
	    		if(!firstLoop) {
	    			stringBuilder.append("\n");
	    		} else {
	    			firstLoop = false;
	    		}
	    		stringBuilder.append(getResources().getString(R.string.text_lap)).append(" ").append(lapNumberFormatter.format(stopWatch.getLap())).append(".\t").append(stopWatch.getFormattedTime());
	    		if(includeTakenTime) {
	    			startTimeCal.setTimeInMillis(stopWatch.getStartTime());
	    			stopTimeCal.setTimeInMillis(stopWatch.getStopTime());
	    			
	    			stringBuilder.append("\t("+simpleDateFormat.format(startTimeCal.getTime())+" - "+simpleDateFormat.format(stopTimeCal.getTime())+")");
	    		}
	    	}
    	} else {
    		stringBuilder.append(stopWatch.getFormattedTime());
    		
    		if(includeTakenTime) {
    			startTimeCal.setTimeInMillis(stopWatch.getStartTime());
    			stopTimeCal.setTimeInMillis(stopWatch.getStopTime());
    			
    			stringBuilder.append("\t("+simpleDateFormat.format(startTimeCal.getTime())+" - "+simpleDateFormat.format(stopTimeCal.getTime())+")");
    		}
    	}
    	return stringBuilder.toString();
    }
    
    private boolean isIncludeTakenTime(String elementName) {
    	SharedPreferences privatePreferences = PreferenceManager.getDefaultSharedPreferences(this);
    	return privatePreferences.getBoolean(elementName, false);
    }
    
    /**
     * Clear/Reset to start.
     * @param view
     */
    public void doClear(View view) {
//    	Log.i("Test", "package name="+view.getContext().getPackageName());
    	clearLaps();
    	resetCounter();
    }
    
    /**
     * Reset counter to default.
     */
    private void resetCounter() {
    	stopWatch = new StopWatch(mHandler);
    	stopClock();
    	setClockTime(stopWatch);
    }
    
    /**
     * Clear saved lap history.
     */
    private void clearLaps() {
    	stopWatchsHistory.clear();
		LinearLayout lapLayout = (LinearLayout) findViewById(R.id.lapLayout);
		lapLayout.removeAllViews();
    }
    
    public void doStart(View view) {
    	if(stopWatch.isRunning()) {
    		stopClock();
    	}
		startClock();
		
		layoutStartedState.setVisibility(LinearLayout.VISIBLE);
    	layoutStoppedState.setVisibility(LinearLayout.GONE);
    }
    
    public void doStop(View view) {
    	stopClock();
    	
    	layoutStartedState.setVisibility(LinearLayout.GONE);
    	layoutStoppedState.setVisibility(LinearLayout.VISIBLE);
    }
    
    public void doLap(View view) {
    	stopClock();
    	
    	// add element to layout
    	LinearLayout lapLayout = (LinearLayout) findViewById(R.id.lapLayout);
    	stopWatch.setLap(lapLayout.getChildCount()+1);
    	
    	TextView newLapTextView = new TextView(this);
    	newLapTextView.setText(getText(R.string.text_lap)+" "+lapNumberFormatter.format(stopWatch.getLap())+". "+stopWatch.getFormattedTime());
    	newLapTextView.setTextSize(20);
    	LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    	layoutParams.setMargins(10, 0, 0,0);
    	newLapTextView.setLayoutParams(layoutParams);
    	
    	lapLayout.addView(newLapTextView, 0);
    	
    	StopWatch cloneStopWatch = null;
		try {
			cloneStopWatch = (StopWatch) stopWatch.clone();
		} catch (CloneNotSupportedException e) {
			
		}
    	stopWatchsHistory.add(cloneStopWatch);
    	
    	resetCounter();
    	doStart(view);
    }
    
    private void setClockTime(StopWatch stopWatch) {
    	TextView textClock = (TextView)findViewById(R.id.textClock);
    	TextView textClockMSec = (TextView)findViewById(R.id.TextClockMSec);
    	textClock.setText(stopWatch.getFormattedHHmmss());
    	textClockMSec.setText(stopWatch.getFormattedMilliseconds());
    }
    
    private void startClock() {
    	stopWatch.startClock();
    	mHandler.removeCallbacks(stopWatchThread);
        mHandler.postDelayed(stopWatchThread, 0);
    }
    
    private void stopClock() {
    	if(stopWatchThread != null) {
    		stopWatch.setRunning(false);
    		mHandler.removeCallbacks(stopWatchThread);
    	}
    }
    
    public class ClockTickThread implements Runnable {
    	
		public void run() {
			stopWatch.stopClock();
			setClockTime(stopWatch);
	    	mHandler.postDelayed(this, REFRESH_RATE);
		}
    }

	public int getLap() {
		return lap;
	}

	public void setLap(int lap) {
		this.lap = lap;
	}

//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		//DIDN'T WORK, EMAIL AND SMS DOESN'T RETURN CORRECT RESULT CODE!
//		/* 
//		if(requestCode == INTENT_ACTION_REQUEST.SEND_EMAIL.ordinal()) {
//			if(resultCode == Activity.RESULT_OK) {
//				Toast.makeText(this, getText(R.string.email_sent), Toast.LENGTH_SHORT).show();
//				
//			} else if(resultCode == RESULT_CANCELED){
//				Toast.makeText(this, getText(R.string.email_not_sent), Toast.LENGTH_SHORT).show();
//				
//			} else {
//				Toast.makeText(this, getText(R.string.send_error), Toast.LENGTH_SHORT).show();
//			}
//			
//		} else
//		
//		if(requestCode == INTENT_ACTION_REQUEST.SEND_SMS.ordinal()) {
//			if(resultCode == RESULT_OK) {
//				Toast.makeText(this, getText(R.string.sms_sent), Toast.LENGTH_SHORT).show();
//				
//			} else if(resultCode == RESULT_CANCELED) {
//				Toast.makeText(this, getText(R.string.sms_not_sent), Toast.LENGTH_SHORT).show();
//				
//			} else {
//				Toast.makeText(this, getText(R.string.send_error), Toast.LENGTH_SHORT).show();
//			}
//		}
//		*/
//	}
	
}
