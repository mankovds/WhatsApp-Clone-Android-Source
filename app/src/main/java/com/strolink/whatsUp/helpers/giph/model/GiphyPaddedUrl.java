package com.strolink.whatsUp.helpers.giph.model;


import androidx.annotation.NonNull;


import com.bumptech.glide.load.Key;
import com.strolink.whatsUp.helpers.AppHelper;


import java.security.MessageDigest;

public class GiphyPaddedUrl implements Key {

  private final String target;
  private final long   size;

  public GiphyPaddedUrl(@NonNull String target, long size) {
    this.target = target;
    this.size   = size;
  }

  public String getTarget() {
    return target;
  }

  public long getSize() {
    return size;
  }

  @Override
  public void updateDiskCacheKey(MessageDigest messageDigest) {
    messageDigest.update(target.getBytes());
    messageDigest.update(AppHelper.longToByteArray(size));
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || !(other instanceof GiphyPaddedUrl)) return false;

    GiphyPaddedUrl that = (GiphyPaddedUrl)other;

    return this.target.equals(that.target) && this.size == that.size;
  }

  @Override
  public int hashCode() {
    return target.hashCode() ^ (int)size;
  }

}
