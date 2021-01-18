package com.strolink.whatsUp.ui;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;

/**
 * Created by Abderrahim El imame on 12/19/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class SpannyBen extends SpannableStringBuilder {

    private int flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;

    public SpannyBen() {
        super("");
    }

    public SpannyBen(CharSequence text) {
        super(text);
    }


    /**
     * Append plain text.
     *
     * @return this {@code Spanny}.
     */
    @Override
    public SpannyBen append(CharSequence text) {
        super.append(text);
        setSpan( length() - text.length(), length());
        return this;
    }

    /**
     * @deprecated use {@link #append(CharSequence text)}
     */
    @Deprecated
    public SpannyBen appendText(CharSequence text) {
        append(text);
        return this;
    }

    /**
     * Change the flag. Default is SPAN_EXCLUSIVE_EXCLUSIVE.
     * The flags determine how the span will behave when text is
     * inserted at the start or end of the span's range
     *
     * @param flag see {@link Spanned}.
     */
    public void setFlag(int flag) {
        this.flag = flag;
    }

    /**
     * Mark the specified range of text with the specified object.
     * The flags determine how the span will behave when text is
     * inserted at the start or end of the span's range.
     */
    private void setSpan( int start, int end) {
        setSpan(new ForegroundColorSpan(AppHelper.getColor(WhatsCloneApplication.getInstance(), R.color.colorSpanSearch)) , start, end, flag);
    }


}