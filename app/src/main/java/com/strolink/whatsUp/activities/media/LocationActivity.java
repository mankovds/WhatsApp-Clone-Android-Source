/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package com.strolink.whatsUp.activities.media;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import java.io.FileOutputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LocationActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private TextView distanceTextView;
    private Marker userMarker;
    private Location myLocation;
    private Location userLocation;

    private AppCompatImageView user_image;
    private TextView nameTextView;
    private boolean userLocationMoved = false;
    private boolean firstWas = false;

    private View bottomView;
    private AppCompatButton sendButton;

    private String userId;
    private String urlImage;
    private double lat;
    private double lon;

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = getIntent().getStringExtra("userId");
        lat = getIntent().getDoubleExtra("lat", 0);
        lon = getIntent().getDoubleExtra("long", 0);

        initializerView();
    }


    private void initializerView() {

        if (lat != 0) {
            setContentView(R.layout.activity_location_view_layout);
        } else {
            setContentView(R.layout.activity_location_attach_layout);
        }

        user_image = (AppCompatImageView) findViewById(R.id.location_avatar_view);
        nameTextView = (TextView) findViewById(R.id.location_name_label);
        distanceTextView = (TextView) findViewById(R.id.location_distance_label);
        bottomView = findViewById(R.id.location_bottom_view);
        sendButton = (AppCompatButton) findViewById(R.id.location_send_button);
        if (sendButton != null) {
            sendButton.setText(getString(R.string.send_location));
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);


        setupToolbar();
    }


    private void setupToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (lat != 0) {
            actionBar.setTitle(getString(R.string.ChatLocation));
        } else {
            actionBar.setTitle(getString(R.string.ShareLocation));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @SuppressLint("StaticFieldLeak")
    private void updateUserData(String userId) {
        if (lat != 0 && user_image != null) {


            UsersModel mContactsModel = UsersController.getInstance().getUserById(userId);

            Drawable drawable;
            if (mContactsModel != null && mContactsModel.getUsername() != null) {
                drawable = AppHelper.getDrawable(this, R.drawable.holder_user);
                nameTextView.setText(mContactsModel.getUsername());
            } else {
                drawable = AppHelper.getDrawable(this, R.drawable.holder_user);
                nameTextView.setText(mContactsModel.getPhone());
            }
            String ImageUrl = mContactsModel.getImage();
            String recipientId = mContactsModel.get_id();

            if (ImageUrl != null) {


                BitmapImageViewTarget target = new BitmapImageViewTarget(user_image) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        user_image.setImageBitmap(resource);
                    }


                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        user_image.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        user_image.setImageDrawable(placeHolderDrawable);
                    }
                };
                GlideApp.with(LocationActivity.this)
                        .asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + recipientId + "/" + ImageUrl))
                        .signature(new ObjectKey(ImageUrl))
                        .centerCrop()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(drawable)
                        .error(drawable)
                        .override(AppConstants.EDIT_PROFILE_IMAGE_SIZE, AppConstants.EDIT_PROFILE_IMAGE_SIZE)
                        .into(target);
            } else {
                user_image.setImageDrawable(drawable);
            }


        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (lat != 0) {
            getMenuInflater().inflate(R.menu.location_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.location_send_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.map_list_menu_map:
                if (googleMap != null) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                break;
            case R.id.map_list_menu_satellite:
                if (googleMap != null) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
                break;
            case R.id.map_list_menu_hybrid:
                if (googleMap != null) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
                break;
            case R.id.map_to_my_location:
                if (myLocation != null) {
                    LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    if (googleMap != null) {
                        CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 8);
                        googleMap.animateCamera(position);
                    }
                }
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }


    private void positionMarker(Location location) {
        if (location == null) {
            return;
        }
        myLocation = location;
        if (lat != 0) {
            if (userLocation != null && distanceTextView != null) {
                float distance = location.distanceTo(userLocation);
                if (distance < 1000) {
                    distanceTextView.setText(String.format("%d %s", (int) (distance), getString(R.string.MetersAway)));
                } else {
                    distanceTextView.setText(String.format("%.2f %s", distance / 1000.0f, getString(R.string.KMetersAway)));
                }
            }
        } else {
            if (!userLocationMoved && googleMap != null) {
                userLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                userMarker.setPosition(latLng);
                if (firstWas) {
                    CameraUpdate position = CameraUpdateFactory.newLatLng(latLng);
                    googleMap.animateCamera(position);
                } else {
                    firstWas = true;
                    CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 8);
                    googleMap.moveCamera(position);
                }
            }
        }
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    @Override
    public void onMapReady(GoogleMap googleMap1) {

        googleMap = googleMap1;
        //setting map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.setOnMyLocationChangeListener(location -> positionMarker(location));
        myLocation = googleMap.getMyLocation();


        if (sendButton != null) {
            userLocation = new Location("network");
            userLocation.setLatitude(20.659322);
            userLocation.setLongitude(-11.406250);
            LatLng latLng = new LatLng(20.659322, -11.406250);
            userMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(AppHelper.convertToBitmap(AppHelper.getDrawable(this, R.drawable.ic_place_black_24dp), 64, 64)))
                    .draggable(true));

            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppHelper.LogCat("userLocation.getLatitude() " + userLocation.getLatitude() + " userLocation.getLongitude() " + userLocation.getLongitude());

                    if (userLocation.getLatitude() == 0 || userLocation.getLongitude() == 0) return;
                    sendButton.setEnabled(false);

                    googleMap.snapshot(bitmap -> {

                        String Id = "IMG_" + System.currentTimeMillis();

                        Observable.create((ObservableOnSubscribe<String>) subscriber -> {
                            try {
                                FileOutputStream out = new FileOutputStream(FilesManager.getFileDataCached(LocationActivity.this, Id));
                                // Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, 250, 250, false);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                                out.close();
                                subscriber.onNext("done saving file:" + Id);
                                subscriber.onComplete();
                            } catch (Exception e) {
                                subscriber.onError(e);
                            }

                        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(string -> {
                            AppHelper.runOnUIThread(() -> sendButton.setEnabled(true));
                            //String path = FilesManager.getFileDataCachedPath(LocationActivity.this, Id);

                            String path = FilesManager.copyDocumentToCache(Uri.fromFile(FilesManager.getFileDataCached(LocationActivity.this, Id)), ".jpg");
                            if (FilesManager.isFileDataCachedExists(LocationActivity.this, Id)) {
                                FilesManager.getFileDataCached(LocationActivity.this, Id).delete();

                            }
                            AppHelper.LogCat("userLocation path " + path + "userLocation.getLatitude() " + userLocation.getLatitude() + " userLocation.getLongitude() " + userLocation.getLongitude());

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("longitude", String.valueOf(userLocation.getLongitude()));
                            resultIntent.putExtra("latitude", String.valueOf(userLocation.getLatitude()));
                            resultIntent.putExtra("image", path);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }, AppHelper::LogCat);
                    });
                }
            });

            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                    userLocationMoved = true;
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    LatLng latLng = marker.getPosition();
                    userLocation.setLatitude(latLng.latitude);
                    userLocation.setLongitude(latLng.longitude);
                }
            });
        }

        if (bottomView != null) {
            bottomView.setOnClickListener(view -> {
                if (userLocation != null) {
                    LatLng latLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                    CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 8);
                    googleMap.animateCamera(position);
                }
            });
        }

        if (lat != 0) {

            if (lat == 1) {
                updateUserData(PreferenceManager.getInstance().getID(this));
            } else {
                updateUserData(userId);
            }
            userLocation = new Location("network");
            userLocation.setLatitude(lat);
            userLocation.setLongitude(lon);
            LatLng latLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            userMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(AppHelper.convertToBitmap(AppHelper.getDrawable(this, R.drawable.ic_place_black_24dp), 64, 64))));
            CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 8);
            googleMap.moveCamera(position);
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        }

        positionMarker(myLocation);
    }


}
