package fi.donhut.common.util;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * 
 * @author Nhut Do @ 2012
 *
 */
public class Permission {

	/**
	 * Check if application have permission to read phone book.
	 * @param context 
	 * @return true=yes, false=no
	 */
	public static boolean phoneBookReadAccess(Context context) {
		PackageManager manager = context.getPackageManager();
		int hasPermission = manager.checkPermission("android.permission.READ_CONTACTS", context.getPackageName());
		return hasPermission == PackageManager.PERMISSION_GRANTED;
	}

}
