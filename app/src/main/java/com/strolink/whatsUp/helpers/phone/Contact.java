package com.strolink.whatsUp.helpers.phone;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Abderrahim El imame on 2019-06-12.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class Contact implements Comparable<Contact> {

    private final long mId;
    private int mInVisibleGroup;
    @Nullable
    private String mDisplayName;
    private boolean mStarred;
    @Nullable
    private Uri mPhoto;
    @Nullable
    private Uri mThumbnail;
    @NonNull
    private Set<String> mEmails = new HashSet<>();
    @NonNull
    private Set<String> mPhoneNumbers = new HashSet<>();

    public Contact(long id) {
        this.mId = id;
    }

    public long getId() {
        return mId;
    }

    public int getInVisibleGroup() {
        return mInVisibleGroup;
    }

    public void setInVisibleGroup(int inVisibleGroup) {
        mInVisibleGroup = inVisibleGroup;
    }

    @Nullable
    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(@Nullable String displayName) {
        mDisplayName = displayName;
    }

    public boolean isStarred() {
        return mStarred;
    }

    public void setStarred(boolean starred) {
        mStarred = starred;
    }

    @Nullable
    public Uri getPhoto() {
        return mPhoto;
    }

    public void setPhoto(@Nullable Uri photo) {
        mPhoto = photo;
    }

    @Nullable
    public Uri getThumbnail() {
        return mThumbnail;
    }


    public void setThumbnail(@Nullable Uri thumbnail) {
        mThumbnail = thumbnail;
    }

    @NonNull
    public Set<String> getEmails() {
        return mEmails;
    }

    public void setEmails(@NonNull Set<String> emails) {
        mEmails = emails;
    }

    @NonNull
    public Set<String> getPhoneNumbers() {
        return mPhoneNumbers;
    }

    public void setPhoneNumbers(@NonNull Set<String> phoneNumbers) {
        mPhoneNumbers = phoneNumbers;
    }


    @Override
    public int compareTo(@NonNull Contact other) {
        if (mDisplayName != null && other.mDisplayName != null) {
            return mDisplayName.compareTo(other.mDisplayName);
        }

        return -1;
    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Contact contact = (Contact) o;
        return mId == contact.mId;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "mId=" + mId +
                ", mInVisibleGroup=" + mInVisibleGroup +
                ", mDisplayName='" + mDisplayName + '\'' +
                ", mStarred=" + mStarred +
                ", mPhoto=" + mPhoto +
                ", mThumbnail=" + mThumbnail +
                ", mEmails=" + mEmails +
                ", mPhoneNumbers=" + mPhoneNumbers +
                '}';
    }
}