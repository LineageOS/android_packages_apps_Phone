package com.android.phone;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.util.HashSet;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.Telephony;

class Blacklist {

    private Context mContext;

    public Blacklist(Context context) {
        mContext = context;
        migrateOldDataIfPresent();
    }

    // legacy migration code start

    private static class PhoneNumber implements Externalizable {
        static final long serialVersionUID = 32847013274L;
        String phone;

        public PhoneNumber() {
        }

        public void writeExternal(ObjectOutput out) throws IOException {
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            phone = (String) in.readObject();
        }

        @Override
        public int hashCode() {
            return phone != null ? phone.hashCode() : 0;
        }
    }

    private static final String BLFILE = "blacklist.dat";
    private static final int BLFILE_VER = 1;

    public void migrateOldDataIfPresent() {
        ObjectInputStream ois = null;
        HashSet<PhoneNumber> data = null;

        try {
            ois = new ObjectInputStream(mContext.openFileInput(BLFILE));
            Object o = ois.readObject();
            if (o != null && o instanceof Integer) {
                // check the version
                Integer version = (Integer) o;
                if (version == BLFILE_VER) {
                    Object numbers = ois.readObject();
                    if (numbers instanceof HashSet) {
                        data = (HashSet<PhoneNumber>) numbers;
                    }
                }
            }
        } catch (IOException e) {
            // Do nothing
        } catch (ClassNotFoundException e) {
            // Do nothing
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    // Do nothing
                }
                mContext.deleteFile(BLFILE);
            }
        }
        if (data != null) {
            ContentResolver cr = mContext.getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(Telephony.Blacklist.PHONE_MODE, 1);

            for (PhoneNumber number : data) {
                Uri uri = Uri.withAppendedPath(
                        Telephony.Blacklist.CONTENT_FILTER_BYNUMBER_URI, number.phone);
                cv.put(Telephony.Blacklist.NUMBER, number.phone);
                cr.update(uri, cv, null, null);
            }
        }
    }

    // legacy migration code end
}
