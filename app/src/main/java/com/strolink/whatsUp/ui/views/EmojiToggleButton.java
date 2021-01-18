package com.strolink.whatsUp.ui.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatImageButton;
import android.util.AttributeSet;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.helpers.AppHelper;


public class EmojiToggleButton extends AppCompatImageButton {

    private Drawable emojiToggle;
    private Drawable imeToggle;

    public EmojiToggleButton(Context context) {
        super(context);
        initialize();
    }

    public EmojiToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public EmojiToggleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public void setToEmoji() {
        setImageDrawable(emojiToggle);
    }

    public void setToIme() {
        setImageDrawable(imeToggle);
    }

    private void initialize() {
        int attributes[] = new int[]{R.attr.conversation_emoji_toggle, R.attr.conversation_keyboard_toggle};

        TypedArray drawables = getContext().obtainStyledAttributes(attributes);
        this.emojiToggle = AppHelper.getVectorDrawable(getContext(), drawables.getResourceId(0, R.drawable.ic_emoticon_24dp));
        this.imeToggle = AppHelper.getVectorDrawable(getContext(), drawables.getResourceId(1, R.drawable.ic_keyboard_gray_24dp));
        drawables.recycle();
        setToEmoji();

    }

}
