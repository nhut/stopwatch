package fi.donhut.stopwatch.model;

import java.util.Set;

import android.content.Context;
import android.widget.ArrayAdapter;
import fi.donhut.common.model.AutoCompleteEditTextPreference;

/**
 * This auto complete gets its data from build in phonebook phone numbers.
 * @author Nhut Do @ 2012
 *
 */
public class AutoCompletePhoneNumberEditTextPreference extends AutoCompleteEditTextPreference {

	public AutoCompletePhoneNumberEditTextPreference(Context context) {
		super(context);
		
	}

	@Override
	public void setContent(Context context, ArrayAdapter<String> adapter) {
		Set<String> listContent = getContactPhoneNumbers(context);
		for(String content : listContent) {
			adapter.add(content);
		}
	}
	
}
