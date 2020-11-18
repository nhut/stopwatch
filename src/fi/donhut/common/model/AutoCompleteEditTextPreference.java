package fi.donhut.common.model;

import java.util.LinkedHashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.preference.EditTextPreference;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import fi.donhut.common.util.Permission;

/**
 * 
 * @author Nhut Do @ 2012
 *
 */
public class AutoCompleteEditTextPreference extends EditTextPreference {
	
	private static AutoCompleteTextView mEditText = null;
	
	public AutoCompleteEditTextPreference(Context context) {
		super(context);
		
		mEditText = new AutoCompleteTextView(context);
	    mEditText.setThreshold(0);
	    mEditText.setTextColor(context.getResources().getColor(android.R.color.black));
	    //The adapter of your choice
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_item);
	    setContent(context, adapter);
	    mEditText.setAdapter(adapter);
	}
	
	/**
	 * This need to be override!
	 * @param context
	 * @param adapter
	 */
	public void setContent(Context context, ArrayAdapter<String> adapter) {
		//NOTHING HERE ON PURPOSE
	}
	
	@Override
	protected void onBindDialogView(View view) {
	    AutoCompleteTextView editText = mEditText;
	    editText.setText(getText());
	    
	    ViewParent oldParent = editText.getParent();
	    if (oldParent != view) {
	        if (oldParent != null) {
	            ((ViewGroup) oldParent).removeView(editText);
	        }
	        onAddEditTextToDialogView(view, editText);
	    }
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
	    if (positiveResult) {
	        String value = mEditText.getText().toString();
	        if (callChangeListener(value)) {
	            setText(value);
	        }
	    }
	}
	
	@SuppressLint("NewApi")
	public static Set<Cursor> getContacts(Context context) {
		Set<Cursor> content = new LinkedHashSet<Cursor>();
		Cursor cursor = null;
		//CHECK IF HAVE ACCESS TO READ PHONE BOOK CONTACTS
		if (Permission.phoneBookReadAccess(context)) {
			//WE HAVE THE PERMISSION
			ContentResolver contentResolver = context.getContentResolver();
			cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,  null,  null,  null);
			if(cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					content.add(cursor);
				}
			}
		}
		
		if(cursor != null) {
			cursor.close();
		}
		return content;
	}
	
	@SuppressLint("NewApi")
	public static Set<String> getContactEmails(Context context) {
		Set<String> content = new LinkedHashSet<String>();
		
		//CHECK IF HAVE ACCESS TO READ PHONE BOOK CONTACTS
		if (Permission.phoneBookReadAccess(context)) {
		    //WE HAVE THE PERMISSION
			String emailIdOfContact = null;
			String contactName = null;

			ContentResolver cr = context.getContentResolver();
			Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			if (cur.getCount() > 0) {
				while (cur.moveToNext()) {
					String id = cur.getString(cur.getColumnIndex(BaseColumns._ID));
					contactName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

					Cursor emails = cr.query(Email.CONTENT_URI, null, Email.CONTACT_ID + " = " + id, null, null);
					while (emails.moveToNext()) {
						emailIdOfContact = emails.getString(emails.getColumnIndex(Email.DATA));
						if(emailIdOfContact != null && !emailIdOfContact.equals("")) {
							//ADD TO SUGGESTION LIST
							content.add(emailIdOfContact);
						}
					}
					emails.close();

				}
			}// end of contact name cursor
			cur.close();
		}
		
		return content;
	}
	
	@SuppressLint("NewApi")
	public static Set<String> getContactPhoneNumbers(Context context) {
		Set<String> content = new LinkedHashSet<String>();
		
		//CHECK IF HAVE ACCESS TO READ PHONE BOOK CONTACTS
		if (Permission.phoneBookReadAccess(context)) {
		    //WE HAVE THE PERMISSION
			String emailIdOfContact = null;
			String contactName = null;

			ContentResolver cr = context.getContentResolver();
			Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			if (cur.getCount() > 0) {
				while (cur.moveToNext()) {
					String id = cur.getString(cur.getColumnIndex(BaseColumns._ID));
					contactName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

					Cursor emails = cr.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + id, null, null);
					while (emails.moveToNext()) {
						emailIdOfContact = emails.getString(emails.getColumnIndex(Phone.DATA));
						if(emailIdOfContact != null && !emailIdOfContact.equals("")) {
							//ADD TO SUGGESTION LIST
							content.add(emailIdOfContact);
						}
					}
					emails.close();

				}
			}// end of contact name cursor
			cur.close();
		}
		
		return content;
	}
}
