package com.strolink.whatsUp.helpers.phone;

import android.database.Cursor;
import android.net.Uri;

/**
 * Created by Abderrahim El imame on 2019-06-12.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class ColumnMapper {

    // Utility class -> No instances allowed
    private ColumnMapper() {
    }

    public static void mapInVisibleGroup(Cursor cursor, Contact contact, int columnIndex) {
        contact.setInVisibleGroup(cursor.getInt(columnIndex));
    }

    public static void mapDisplayName(Cursor cursor, Contact contact, int columnIndex) {
        String displayName = cursor.getString(columnIndex);
        if (displayName != null && !displayName.isEmpty()) {
            contact.setDisplayName(displayName);
        }
    }

    public static void mapEmail(Cursor cursor, Contact contact, int columnIndex) {
        String email = cursor.getString(columnIndex);
        if (email != null && !email.isEmpty()) {
            contact.getEmails().add(email);
        }
    }

    public static void mapPhoneNumber(Cursor cursor, Contact contact, int columnIndex) {
        String phoneNumber = cursor.getString(columnIndex);
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            // Remove all whitespaces
            phoneNumber = phoneNumber.replaceAll("\\s+", "");
            contact.getPhoneNumbers().add(phoneNumber);
        }
    }

    public static void mapPhoto(Cursor cursor, Contact contact, int columnIndex) {
        String uri = cursor.getString(columnIndex);
        if (uri != null && !uri.isEmpty()) {
            contact.setPhoto(Uri.parse(uri));
        }
    }

    public static void mapStarred(Cursor cursor, Contact contact, int columnIndex) {
        contact.setStarred(cursor.getInt(columnIndex) != 0);
    }

    public static void mapThumbnail(Cursor cursor, Contact contact, int columnIndex) {
        String uri = cursor.getString(columnIndex);
        if (uri != null && !uri.isEmpty()) {
            contact.setThumbnail(Uri.parse(uri));
        }
    }
}
