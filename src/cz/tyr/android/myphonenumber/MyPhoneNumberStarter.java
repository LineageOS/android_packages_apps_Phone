package cz.tyr.android.myphonenumber;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;


public class MyPhoneNumberStarter extends BroadcastReceiver {
	private final String TAG = MyPhoneNumberStarter.class.getSimpleName();
	private boolean DEBUG;

	@Override
	public void onReceive(Context context, Intent intent) {
		TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		SharedPreferences prefs = context.getSharedPreferences(MyPhoneNumberConstant.PREFS_NAME, Context.MODE_PRIVATE);
		DEBUG = prefs.getBoolean(MyPhoneNumberConstant.KEY_DEBUG, MyPhoneNumberConstant.DEFAULT_DEBUG);

		String phoneNum = mTelephonyMgr.getLine1Number();
		String savedNum = prefs.getString(MyPhoneNumberConstant.KEY_NUMBER, MyPhoneNumberConstant.DEFAULT_NUMBER);

		if (phoneNum == null) {
			if (DEBUG)
				Log.d(TAG, "Trying to read the phone number from file");

			if (savedNum != null) {
				Phone mPhone = PhoneFactory.getDefaultPhone();
				String alphaTag = mPhone.getLine1AlphaTag();

				if (alphaTag == null || "".equals(alphaTag)) {
					// No tag, set it.
					alphaTag = "Voice Line 1";
				}

				mPhone.setLine1Number(alphaTag, savedNum, null);

				if (DEBUG)
					Log.d(TAG, "Phone number set to: " + savedNum);
			} else if (DEBUG) {
				Log.d(TAG, "No phone number set yet");
			}
		} else if (DEBUG) {
			Log.d(TAG, "Phone number exists. No need to read it from file.");
		}
	}
}
