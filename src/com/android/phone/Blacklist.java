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
    private static final String LOG_TAG = "Blacklist";
    private static final boolean DBG = PhoneGlobals.DBG_LEVEL >= 2;

    private static final String BLFILE = "blacklist.dat";
    private static final int BLFILE_VER = 1;

    private Context mContext;

    public final static String PRIVATE_NUMBER = "0000";

    // Blacklist matching type
    public final static int MATCH_NONE = 0;
    public final static int MATCH_PRIVATE = 1;
    public final static int MATCH_UNKNOWN = 2;
    public final static int MATCH_LIST = 3;
    public final static int MATCH_REGEX = 4;

    public Blacklist(Context context) {
        mContext = context;
    }

    public boolean add(String s) {
        s = stripSeparators(s);

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

        Uri.Builder builder = Uri.withAppendedPath(Telephony.Blacklist.CONTENT_URI, s).buildUpon();
        ContentResolver cr = mContext.getContentResolver();
        int result = MATCH_NONE;

        Cursor c = cr.query(builder.build(), null,
                Telephony.Blacklist.PHONE_MODE + " != 0", null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                result = MATCH_LIST;
            }
            c.close();
        }

        if (result == MATCH_NONE && PhoneUtils.PhoneSettings.isBlacklistRegexEnabled(mContext)) {
            builder.appendQueryParameter(Telephony.Blacklist.REGEX_KEY, "1");
            c = cr.query(builder.build(), null,
                    Telephony.Blacklist.PHONE_MODE + " != 0", null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    result = MATCH_REGEX;
                }
                c.close();
            }
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

    /**
     * Custom stripSeparators() method identical to
     * PhoneNumberUtils.stripSeparators(), to retain '.'s
     * for blacklist regex parsing.
     * There is no difference between the two, this is only
     * done to use the custom isNonSeparator() method below.
     */
    private String stripSeparators(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if (isNonSeparator(c)) {
                ret.append(c);
            }
        }

        return ret.toString();
    }

    /**
     * Custom isNonSeparator() method identical to
     * PhoneNumberUtils.isNonSeparator(), to retain '.'s
     * for blacklist regex parsing.
     * The only difference between the two is that this
     * custom one allows '.'s.
     */
    private boolean isNonSeparator(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+'
                    || c == PhoneNumberUtils.WILD || c == PhoneNumberUtils.WAIT
                    || c == PhoneNumberUtils.PAUSE || c == '.';
    }
}
