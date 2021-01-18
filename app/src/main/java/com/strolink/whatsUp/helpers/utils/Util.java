/*
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.strolink.whatsUp.helpers.utils;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class Util {
    private static final String TAG = Util.class.getSimpleName();

    public static Handler handler = new Handler(Looper.getMainLooper());

    public static <T> List<T> asList(T... elements) {
        List<T> result = new LinkedList<>();
        Collections.addAll(result, elements);
        return result;
    }

    public static String join(String[] list, String delimiter) {
        return join(Arrays.asList(list), delimiter);
    }

    public static String join(Collection<String> list, String delimiter) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        for (String item : list) {
            result.append(item);

            if (++i < list.size())
                result.append(delimiter);
        }

        return result.toString();
    }

    public static String join(long[] list, String delimeter) {
        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < list.length; j++) {
            if (j != 0) sb.append(delimeter);
            sb.append(list[j]);
        }

        return sb.toString();
    }


    public static void wait(Object lock, long timeout) {
        try {
            lock.wait(timeout);
        } catch (InterruptedException ie) {
            throw new AssertionError(ie);
        }
    }

    public static void close(InputStream in) {
        try {
            in.close();
        } catch (IOException e) {
            Log.w(TAG, e);
        }
    }

    public static void close(OutputStream out) {
        try {
            out.close();
        } catch (IOException e) {
            Log.w(TAG, e);
        }
    }

    public static long getStreamLength(InputStream in) throws IOException {
        byte[] buffer = new byte[4096];
        int totalSize = 0;

        int read;

        while ((read = in.read(buffer)) != -1) {
            totalSize += read;
        }

        return totalSize;
    }


    public static void readFully(InputStream in, byte[] buffer) throws IOException {
        int offset = 0;

        for (; ; ) {
            int read = in.read(buffer, offset, buffer.length - offset);
            if (read == -1) throw new IOException("Stream ended early");

            if (read + offset < buffer.length) offset += read;
            else return;
        }
    }

    public static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;

        while ((read = in.read(buffer)) != -1) {
            bout.write(buffer, 0, read);
        }

        in.close();

        return bout.toByteArray();
    }

    public static String readFullyAsString(InputStream in) throws IOException {
        return new String(readFully(in));
    }

    public static long copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        long total = 0;

        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            total += read;
        }

        in.close();
        out.close();

        return total;
    }


    public static byte[] trim(byte[] input, int length) {
        byte[] result = new byte[length];
        System.arraycopy(input, 0, result, 0, result.length);

        return result;
    }


 /*   public static String getSecret(int size) {
        byte[] secret = getSecretBytes(size);
        return HttpRequest.Base64.encodeBytes(secret);
    }*/

    public static byte[] getSecretBytes(int size) {
        byte[] secret = new byte[size];
        getSecureRandom().nextBytes(secret);
        return secret;
    }

    public static SecureRandom getSecureRandom() {
        return new SecureRandom();
    }


    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void assertMainThread() {
        if (!isMainThread()) {
            throw new AssertionError("Main-thread assertion failed.");
        }
    }

    public static void runOnMain(final @NonNull Runnable runnable) {
        if (isMainThread()) runnable.run();
        else handler.post(runnable);
    }

    public static void runOnMainDelayed(final @NonNull Runnable runnable, long delayMillis) {
        handler.postDelayed(runnable, delayMillis);
    }

    public static void runOnMainSync(final @NonNull Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            final CountDownLatch sync = new CountDownLatch(1);
            runOnMain(() -> {
                try {
                    runnable.run();
                } finally {
                    sync.countDown();
                }
            });
            try {
                sync.await();
            } catch (InterruptedException ie) {
                throw new AssertionError(ie);
            }
        }
    }

    public static <T> T getRandomElement(T[] elements) {
        try {
            return elements[SecureRandom.getInstance("SHA1PRNG").nextInt(elements.length)];
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    public static boolean equals(@Nullable Object a, @Nullable Object b) {
        return a == b || (a != null && a.equals(b));
    }

    public static int hashCode(@Nullable Object... objects) {
        return Arrays.hashCode(objects);
    }

    public static @Nullable
    Uri uri(@Nullable String uri) {
        if (uri == null) return null;
        else return Uri.parse(uri);
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }

    public static <K, V> V getOrDefault(@NonNull Map<K, V> map, K key, V defaultValue) {
        return map.containsKey(key) ? map.get(key) : defaultValue;
    }

}
