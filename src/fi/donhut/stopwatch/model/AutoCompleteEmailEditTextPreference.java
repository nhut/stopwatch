package fi.donhut.stopwatch.model;

import java.util.Set;

import android.content.Context;
import android.widget.ArrayAdapter;
import fi.donhut.common.model.AutoCompleteEditTextPreference;

/**
 * This auto complete gets its data from build in phonebook emails.
 * @author Nhut Do @ 2012
 *
 */
public class AutoCompleteEmailEditTextPreference extends AutoCompleteEditTextPreference {

	public AutoCompleteEmailEditTextPreference(Context context) {
		super(context);
	}
	
	@Override
	public void setContent(Context context, ArrayAdapter<String> adapter) {
		Set<String> listContent = getContactEmails(context);
		for(String content : listContent) {
			adapter.add(content);
		}
	}

}
