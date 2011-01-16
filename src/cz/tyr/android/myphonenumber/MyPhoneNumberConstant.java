package cz.tyr.android.myphonenumber;

public class MyPhoneNumberConstant {
	/* In ordert to debug this application, add <boolean name="debug" value="true" />
	   into the /data/data/com.android.phone/shared_prefs/cz.tyr.android.myphonenumber.xml */

	// Preference file name
	public static final String PREFS_NAME = MyPhoneNumberStarter.class.getPackage().getName();
	// Name of the preference key where we hold the number
	public static final String KEY_NUMBER = "number";
	public static final String KEY_DEBUG = "debug";
	// Default value
	public static final String DEFAULT_NUMBER = null;
	public static final boolean DEFAULT_DEBUG = false;
}
