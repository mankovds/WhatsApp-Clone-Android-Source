package com.strolink.whatsUp.helpers.phone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.permissions.Permissions;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 03/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class UtilsPhone {
    private static volatile UtilsPhone Instance = null;


    private ArrayList<UsersModel> mListContacts = new ArrayList<>();
    private PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.getInstance();


    private static final String[] PROJECTION = {
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.DISPLAY_NAME_PRIMARY,
            ContactsContract.Data.STARRED,
            ContactsContract.Data.PHOTO_URI,
            ContactsContract.Data.PHOTO_THUMBNAIL_URI,
            ContactsContract.Data.DATA1,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.IN_VISIBLE_GROUP
    };


    public static UtilsPhone getInstance() {
        UtilsPhone localInstance = Instance;
        if (localInstance == null) {
            synchronized (UtilsPhone.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new UtilsPhone();
                }
            }
        }
        return localInstance;
    }

    private Cursor createCursor() {

        ContentResolver mResolver = WhatsCloneApplication.getInstance().getContentResolver();
        return mResolver.query(
                ContactsContract.Data.CONTENT_URI,
                PROJECTION,
                null,
                null,
                ContactsContract.Data.CONTACT_ID
        );
    }

    /**
     * method to retrieve all contacts from the book
     *
     * @return return value
     */
    public ArrayList<UsersModel> GetPhoneContacts() {

        HashMap<Long, Contact> contacts = new HashMap<>();
        Cursor cursor = createCursor();
        cursor.moveToFirst();
        int idColumnIndex = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID);
        int inVisibleGroupColumnIndex = cursor.getColumnIndex(ContactsContract.Data.IN_VISIBLE_GROUP);
        int displayNamePrimaryColumnIndex = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME_PRIMARY);
        int starredColumnIndex = cursor.getColumnIndex(ContactsContract.Data.STARRED);
        int photoColumnIndex = cursor.getColumnIndex(ContactsContract.Data.PHOTO_URI);
        int thumbnailColumnIndex = cursor.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI);
        int mimetypeColumnIndex = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE);
        int dataColumnIndex = cursor.getColumnIndex(ContactsContract.Data.DATA1);
        while (!cursor.isAfterLast()) {
            long id = cursor.getLong(idColumnIndex);
            Contact contact = contacts.get(id);
            if (contact == null) {
                contact = new Contact(id);
                ColumnMapper.mapInVisibleGroup(cursor, contact, inVisibleGroupColumnIndex);
                ColumnMapper.mapDisplayName(cursor, contact, displayNamePrimaryColumnIndex);
                ColumnMapper.mapStarred(cursor, contact, starredColumnIndex);
                ColumnMapper.mapPhoto(cursor, contact, photoColumnIndex);
                ColumnMapper.mapThumbnail(cursor, contact, thumbnailColumnIndex);
                contacts.put(id, contact);
            }
            String mimetype = cursor.getString(mimetypeColumnIndex);
            switch (mimetype) {
                case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE: {
                    ColumnMapper.mapEmail(cursor, contact, dataColumnIndex);
                    break;
                }
                case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE: {
                    ColumnMapper.mapPhoneNumber(cursor, contact, dataColumnIndex);
                    break;
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        AppHelper.LogCat("contacts " + contacts.toString());

        for (Long key : contacts.keySet()) {
            Contact contact = contacts.get(key);
          //   AppHelper.LogCat("contact " + contact.toString());

            try {
                if (!contact.getPhoneNumbers().isEmpty() )
                    for (String phone : contact.getPhoneNumbers()) {

                        UsersModel contactsModel = new UsersModel();
                        String name = contact.getDisplayName();

                        String id = String.valueOf(contact.getId());
                        String image_uri = String.valueOf(contact.getPhoto());


                        //     AppHelper.LogCat("number phone --> " + phoneNumber);
                        if (name.contains("\\s+")) {
                            String[] nameArr = name.split("\\s+");
                            contactsModel.setUsername(nameArr[0] + nameArr[1]);
                            // AppHelper.LogCat("Fname --> " + nameArr[0]);
                            // AppHelper.LogCat("Lname --> " + nameArr[1]);
                        } else {
                            contactsModel.setUsername(name);
                            //AppHelper.LogCat("name" + name);
                        }
                        if (phone != null) {

                            String Regex = "[^\\d]";
                            String PhoneDigits = phone.replaceAll(Regex, "");
                            //  boolean isValid = !(PhoneDigits.length() < 6 || PhoneDigits.length() > 13);
                            String phNumberProto = PhoneDigits.replaceAll("-", "");
                            String PhoneNo;
                            if (phNumberProto.contains("+")) {
                                PhoneNo = phNumberProto;
                            } else {
                                if (PhoneDigits.length() != 10) {
                                    PhoneNo = "+";
                                    PhoneNo = PhoneNo.concat(phNumberProto);
                                } else {
                                    PhoneNo = phNumberProto;
                                }
                            }
//                            AppHelper.LogCat("PhoneNo1 --> " + PhoneNo);
                            // AppHelper.LogCat("phoneNumber --> " + phoneNumber);
                            String phoneNumberTmpFinal;
                            Phonenumber.PhoneNumber phoneNumberInter = getPhoneNumber(PhoneNo);
                            if (phoneNumberInter != null) {
                                //  AppHelper.LogCat("phoneNumberInter --> " + phoneNumberInter.getNationalNumber());
                                phoneNumberTmpFinal = String.valueOf(phoneNumberInter.getNationalNumber());

                                // AppHelper.LogCat("phoneNumberTmpFinal --> " + phoneNumberTmpFinal);

                                if (isValid(PhoneNo)) {
                                    //  AppHelper.LogCat("isValid --> " + PhoneNo);
                                    contactsModel.setPhone_qurey(phoneNumberTmpFinal);
                                    contactsModel.setPhone(PhoneNo.trim());
                                    contactsModel.setContactId(Integer.parseInt(id));
                                    contactsModel.setImage(image_uri);

                                    int flag = 0;
                                    int arraySize = mListContacts.size();
                                    if (arraySize == 0) {
                                        mListContacts.add(contactsModel);
                                    }
                                    //remove duplicate numbers
                                    for (int i = 0; i < arraySize; i++) {

                                        if (!mListContacts.get(i).getPhone_qurey().trim().equals(phoneNumberTmpFinal.trim())) {
                                            flag = 1;

                                        } else {
                                            flag = 0;
                                            break;
                                        }
                                    }

                                    if (flag == 1) {
                                        mListContacts.add(contactsModel);
                                    }


                                } else {
                                    AppHelper.LogCat("invalid phone --> ");
                                }
                            }


                        }
                    }
            } catch (Exception e) {
                AppHelper.LogCat("Exception " + e.getMessage());
            }

        }

        AppHelper.LogCat("contacts:" + mListContacts.toString());


        AppHelper.LogCat("mListContacts " + mListContacts.size());
        return mListContacts;
    }


    /**
     * Check if number is valid
     *
     * @return boolean
     */
    @SuppressWarnings("unused")
    public boolean isValid(String phone) {
        Phonenumber.PhoneNumber phoneNumber = getPhoneNumber(phone);
        return phoneNumber != null && mPhoneUtil.isValidNumber(phoneNumber);
    }

    /**
     * Get PhoneNumber object
     *
     * @return PhoneNumber | null on error
     */
    @SuppressWarnings("unused")
    public Phonenumber.PhoneNumber getPhoneNumber(String phone) {

        // get current location iso code
        TelephonyManager telMgr = (TelephonyManager) WhatsCloneApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        String DEFAULT_COUNTRY = telMgr.getNetworkCountryIso().toUpperCase();
        try {
            return mPhoneUtil.parse(phone, DEFAULT_COUNTRY);
        } catch (NumberParseException numberException) {
            AppHelper.LogCat("numberException " + numberException.getMessage());
            return null;

        }
    }

    /**
     * method to get contact ID
     *
     * @param mActivity this is the first parameter for getContactID  method
     * @param phone     this is the second parameter for getContactID  method
     * @return return value
     */
    public static long getContactID(Activity mActivity, String phone) {


        return Observable.create((ObservableOnSubscribe<Long>) subscriber -> {
            try {
                long idPhone = 0;
                if (Permissions.hasAny(mActivity, Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS)) {
                    AppHelper.LogCat("Read contact data permission already granted.");
                    // CONTENT_FILTER_URI allow to search contact by phone number
                    Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
                    // This query will return NAME and ID of contact, associated with phone //number.
                    //Now retrieve _ID from query result

                    try (Cursor mcursor = mActivity.getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null)) {

                        if (mcursor != null) {
                            if (mcursor.moveToFirst()) {
                                idPhone = Long.valueOf(mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup._ID)));
                            }
                        }
                    }

                } else {
                    AppHelper.LogCat("Please request Read contact data permission.");

                    idPhone = 0;
                }
                subscriber.onNext(idPhone);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }


    /**
     * method to check for contact name
     *
     * @param phone this is the second parameter for getContactName  method
     * @return return value
     */
    @SuppressLint("CheckResult")
    public static String getContactName(String phone) {

        return Observable.create((ObservableOnSubscribe<String>) subscriber -> {

            try {

                // CONTENT_FILTER_URI allow to search contact by phone number
                Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
                // This query will return NAME and ID of contact, associated with phone //number.
                //Now retrieve _ID from query result
                String name = null;
                try (Cursor mcursor = WhatsCloneApplication.getInstance().getApplicationContext().getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null)) {

                    if (mcursor != null) {
                        if (mcursor.moveToFirst()) {
                            name = mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                        }
                    }
                }


                if (name == null)
                    subscriber.onNext(phone);
                else
                    subscriber.onNext(name);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    /**
     * method to check if user contact exist
     *
     * @param phone this is the second parameter for checkIfContactExist  method
     * @return return value
     */
    public static boolean checkIfContactExist(Context mContext, String phone) {

        try {

            return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
                try {
                    // CONTENT_FILTER_URI allow to search contact by phone number
                    Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
                    // This query will return NAME and ID of contact, associated with phone //number.
                    //Now retrieve _ID from query result
                    String name = null;
                    try (Cursor mcursor = mContext.getApplicationContext().getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null)) {

                        if (mcursor != null) {
                            if (mcursor.moveToFirst()) {
                                name = mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                            }
                        }
                    }

                    subscriber.onNext(name != null);
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);

                }

            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            return false;
        }
    }
}
