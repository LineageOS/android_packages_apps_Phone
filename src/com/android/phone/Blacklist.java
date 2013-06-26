package com.android.phone;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.CallerInfo;

import java.util.ArrayList;
import java.util.List;

class Blacklist {
    public final static String PRIVATE_NUMBER = "0000";

    // Blacklist matching type
    public final static int MATCH_NONE = 0;
    public final static int MATCH_PRIVATE = 1;
    public final static int MATCH_UNKNOWN = 2;
    public final static int MATCH_LIST = 3;
    public final static int MATCH_REGEX = 4;

    private Context mContext;

    public Blacklist(Context context) {
        mContext = context;
    }

    public boolean add(String s) {
        ContentValues cv = new ContentValues();
        cv.put(Telephony.Blacklist.NUMBER, s);
        cv.put(Telephony.Blacklist.PHONE_MODE, 1);

        Uri uri = mContext.getContentResolver().insert(Telephony.Blacklist.CONTENT_URI, cv);
        return uri != null;
    }

    public void delete(String s) {
        Uri uri = Uri.withAppendedPath(Telephony.Blacklist.CONTENT_URI, s);
        mContext.getContentResolver().delete(uri, null, null);
    }

    /**
     * Check if the number is in the blacklist
     * @param s: Number to check
     * @return one of: MATCH_NONE, MATCH_PRIVATE, MATCH_UNKNOWN, MATCH_LIST or MATCH_REGEX
     */
    public int isListed(String s) {
        if (!PhoneUtils.PhoneSettings.isBlacklistEnabled(mContext)) {
            return MATCH_NONE;
        }

        // Private and unknown number matching
        if (s.equals(PRIVATE_NUMBER)) {
            if (PhoneUtils.PhoneSettings.isBlacklistPrivateNumberEnabled(mContext)) {
                return MATCH_PRIVATE;
            }
            return MATCH_NONE;
        }

        if (PhoneUtils.PhoneSettings.isBlacklistUnknownNumberEnabled(mContext)) {
            CallerInfo ci = CallerInfo.getCallerInfo(mContext, s);
            if (!ci.contactExists) {
                return MATCH_UNKNOWN;
            }
        }

        Uri.Builder builder = Telephony.Blacklist.CONTENT_URI.buildUpon();
        builder.appendPath(s);
        if (PhoneUtils.PhoneSettings.isBlacklistRegexEnabled(mContext)) {
            builder.appendQueryParameter(Telephony.Blacklist.REGEX_KEY, "1");
        }

        int result = MATCH_NONE;
        Cursor c = mContext.getContentResolver().query(builder.build(), null,
                Telephony.Blacklist.PHONE_MODE + " != 0", null, null);
        if (c != null) {
            if (c.getCount() > 1) {
                // as the numbers are unique, this is guaranteed to be a regex match
                result = MATCH_REGEX;
            } else if (c.moveToFirst()) {
                boolean isRegex = c.getInt(c.getColumnIndex(Telephony.Blacklist.IS_REGEX)) != 0;
                result = isRegex ? MATCH_REGEX : MATCH_LIST;
            }
            c.close();
        }

        return result;
    }

    public List<String> getItems() {
        List<String> items = new ArrayList<String>();
        Cursor c = mContext.getContentResolver().query(Telephony.Blacklist.CONTENT_PHONE_URI,
                null, null, null, null);
        if (c != null) {
            int columnIndex = c.getColumnIndex(Telephony.Blacklist.NUMBER);
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                items.add(c.getString(columnIndex));
            }
            c.close();
        }

        return items;
    }
}
