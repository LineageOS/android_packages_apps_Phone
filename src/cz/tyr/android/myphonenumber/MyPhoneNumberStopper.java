package cz.tyr.android.myphonenumber;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MyPhoneNumberStopper extends BroadcastReceiver {
	private final String TAG = MyPhoneNumberStopper.class.getSimpleName();
	private boolean DEBUG;

	@Override
	public void onReceive(Context context, Intent intent) {
		TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		SharedPreferences prefs = context.getSharedPreferences(MyPhoneNumberConstant.PREFS_NAME, Context.MODE_PRIVATE);
		DEBUG = prefs.getBoolean(MyPhoneNumberConstant.KEY_DEBUG, MyPhoneNumberConstant.DEFAULT_DEBUG);

		String phoneNum = mTelephonyMgr.getLine1Number();
		String savedNum = prefs.getString(MyPhoneNumberConstant.KEY_NUMBER, MyPhoneNumberConstant.DEFAULT_NUMBER);
		Editor editor = prefs.edit();

		if (phoneNum == null && savedNum == null) {
			Log.d(TAG, "No phone number set yet");
		} else {
			// Remove whitespaces
			phoneNum = phoneNum.trim();
			// If there is no string, treat it as null
			if (phoneNum.length() == 0) {
				phoneNum = null;
			}

			if (phoneNum != null && phoneNum.equals(savedNum) == false) {
				/* Save phone number only if there is some number set and
				   it is not equal to the already saved one */
				if (DEBUG)
					Log.d(TAG, "Saving phone number: " + phoneNum);

				editor.putString(MyPhoneNumberConstant.KEY_NUMBER, phoneNum);
				editor.commit();
			} else if (phoneNum == null && savedNum != null) {
				/* Remove saved number only if there is some saved and
				   there is no number set */
				if (DEBUG)
					Log.d(TAG, "Removing phone number");

				editor.remove(MyPhoneNumberConstant.KEY_NUMBER);
				editor.commit();
			} else if (DEBUG) {
				Log.d(TAG, "No change");
			}
		}
	}
}
