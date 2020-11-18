package fi.donhut.stopwatch;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.text.InputType;
import android.view.Menu;
import fi.donhut.common.util.Permission;
import fi.donhut.stopwatch.model.AutoCompleteEmailEditTextPreference;
import fi.donhut.stopwatch.model.AutoCompletePhoneNumberEditTextPreference;

/**
 * @author Nhut Do @ 07.2012
 *
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //add the prefernces.xml layout
        addPreferencesFromResource(R.xml.preferences);
        
        //get the specified preferences using the key declared in preferences.xml
        PreferenceCategory emailConfigCategory = (PreferenceCategory)findPreference("emailConfigCategory");
        PreferenceCategory smsConfigCategory = (PreferenceCategory)findPreference("smsConfigCategory");
        
        Context context = emailConfigCategory.getContext();
        
        EditTextPreference autoCompleteEditTextPreferenceEmail = null;
        EditTextPreference autoCompleteEditTextPreferenceSMS = null;
        
        if(Permission.phoneBookReadAccess(context)) {
        	//IF GOT PERMISSION, THEN ENABLE AUTO COMPLETE INPUT FEATURE
	        autoCompleteEditTextPreferenceEmail = new AutoCompleteEmailEditTextPreference(context);
	        autoCompleteEditTextPreferenceSMS = new AutoCompletePhoneNumberEditTextPreference(context);
        } else {
        	autoCompleteEditTextPreferenceEmail = new EditTextPreference(context);
        	autoCompleteEditTextPreferenceSMS = new EditTextPreference(context);
        }
        
        //INPUT EMAIL
        autoCompleteEditTextPreferenceEmail.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        setEditTextPreference(autoCompleteEditTextPreferenceEmail, Constants.PREFERENCE_DEFAULT_EMAIL_RECEIVER, getString(R.string.email_receiver_title), R.string.email_receiver);
        emailConfigCategory.addPreference(autoCompleteEditTextPreferenceEmail);
        
        //CHECKBOX EMAIL
        CheckBoxPreference checkBoxPreferenceEmail = new CheckBoxPreference(context);
        setCheckBoxPreference(checkBoxPreferenceEmail, Constants.PREFERENCE_DEFAULT_EMAIL_INCLUDE_TAKETIME, getString(R.string.email_include_takentime_title), R.string.email_include_takentime);
        emailConfigCategory.addPreference(checkBoxPreferenceEmail);
        
        //INPUT SMS
        autoCompleteEditTextPreferenceSMS.getEditText().setInputType(InputType.TYPE_CLASS_PHONE);
        setEditTextPreference(autoCompleteEditTextPreferenceSMS, Constants.PREFERENCE_DEFAULT_SMS_RECEIVER, getString(R.string.sms_receiver_title), R.string.sms_receiver);
        smsConfigCategory.addPreference(autoCompleteEditTextPreferenceSMS);
        
        //CHECKBOX SMS
        CheckBoxPreference checkBoxPreferenceSMS = new CheckBoxPreference(context);
        setCheckBoxPreference(checkBoxPreferenceSMS, Constants.PREFERENCE_DEFAULT_SMS_INCLUDE_TAKETIME, getString(R.string.sms_include_takentime_title), R.string.sms_include_takentime);
        smsConfigCategory.addPreference(checkBoxPreferenceSMS);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_settings, menu);
        return true;
    }
    
    /**
     * Set edit text default values
     * @param editTextPreference
     * @param key
     * @param title
     * @param summary
     */
    private void setEditTextPreference(EditTextPreference editTextPreference, final String key, final String title, final int summaryKey) {
    	String savedPreviousValue = null;
    	try {
    		savedPreviousValue = getPreferenceManager().getSharedPreferences().getString(key, null);
    	} catch (Exception e) {
    		//ignored on purpose.
    	};
    	
    	editTextPreference.setTitle(title);
    	editTextPreference.setSummary(getString(summaryKey, (savedPreviousValue == null ? "" : "\n("+savedPreviousValue+")")));
    	editTextPreference.setKey(key);
    	
    	editTextPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String newValueS = (String)newValue;
				EditTextPreference editTextPreference = (EditTextPreference)preference;
				editTextPreference.setSummary(getString(summaryKey,  (newValueS == null || newValueS.equals("") ? "" : "\n("+newValueS+")")));
				return true;
			}
		});
    }
    
    /**
     * Set checkbox preference default value
     * @param checkBoxPreference
     * @param key
     * @param title
     * @param summary
     */
    private void setCheckBoxPreference(CheckBoxPreference checkBoxPreference, final String key, final String title, final int summaryKey) {
    	checkBoxPreference.setKey(key);
    	checkBoxPreference.setTitle(title);
    	checkBoxPreference.setSummary(getString(summaryKey));
    }
}
