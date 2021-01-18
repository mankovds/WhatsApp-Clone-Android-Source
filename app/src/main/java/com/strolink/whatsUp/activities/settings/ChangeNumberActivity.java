package com.strolink.whatsUp.activities.settings;

import android.os.Bundle;
import androidx.annotation.Nullable;

import android.view.MenuItem;

import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.animations.AnimationsUtil;

/**
 * Created by Abderrahim El imame on 8/17/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class ChangeNumberActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}
