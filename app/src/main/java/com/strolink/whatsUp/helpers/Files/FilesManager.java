package com.strolink.whatsUp.helpers.Files;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.strolink.whatsUp.BuildConfig;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.media.GiphyActivity;
import com.strolink.whatsUp.activities.media.LocationActivity;
import com.strolink.whatsUp.activities.media.PickerActivity;
import com.strolink.whatsUp.api.APIService;
import com.strolink.whatsUp.api.FilesDownloadService;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.permissions.Permissions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by Abderrahim El imame on 6/12/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class FilesManager {


    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IMAGE_GIF = "image/gif";
    public static final String AUDIO_AAC = "audio/aac";
    public static final String AUDIO_UNSPECIFIED = "audio/*";
    public static final String VIDEO_UNSPECIFIED = "video/*";
    /**
     * ********************************************************************************* ************************************************
     * *************************************************** Methods to create  Files path ************************************************
     * **********************************************************************************************************************************
     */

    /**
     * method to create root  directory
     *
     * @param mContext
     * @return root directory
     */
    public static File getMainPath(Context mContext) {

        File storage = Environment.getExternalStorageDirectory();

        if (!storage.canWrite()) {
            // throw new NoExternalStorageException();
            AppHelper.LogCat("NoExternalStorageException can't Write " + Environment.getExternalStorageState());
        }
        // External sdcard location
        File mediaStorageDir = new File(storage, mContext.getApplicationContext().getString(R.string.app_name));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }


    /* --------------------------  cached fies       ---------------------------------------*/


    /**
     * method to create cached images directory
     *
     * @param mContext
     * @return return value
     */
    public static File getFilesCachePath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.data_cache_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + "_" + mContext.getApplicationContext().getString(R.string.data_cache_directory) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }
    /* --------------------------  received fies       ---------------------------------------*/

    /**
     * method to create root images directory
     *
     * @param mContext
     * @return return value
     */
    public static File getImagesPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.images_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create root gif directory
     *
     * @param mContext
     * @return return value
     */
    public static File getGifPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.gifs_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create root videos directory
     *
     * @param mContext
     * @return return value
     */
    private static File getVideosPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.videos_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create root audio directory
     *
     * @param mContext
     * @return return value
     */
    private static File getAudiosPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.audios_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create root documents directory
     *
     * @param mContext
     * @return return value
     */
    private static File getDocumentsPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.documents_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create root wallpaper directory
     *
     * @return return value
     */
    private static File getWallpaperPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.directory_wallpaper));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create images profile directory
     *
     * @param mContext
     * @return return value
     */
    private static File getProfilePhotosPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.profile_photos));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }


    /* --------------------------  sent fies       ---------------------------------------*/

    /**
     * method to create sent images  directory
     *
     * @param mContext
     * @return return value
     */
    private static File getImagesSentPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getImagesPath(mContext), mContext.getApplicationContext().getString(R.string.directory_sent));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create sent gif  directory
     *
     * @param mContext
     * @return return value
     */
    private static File getGifSentPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getGifPath(mContext), mContext.getApplicationContext().getString(R.string.directory_sent));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create sent videos  directory
     *
     * @param mContext
     * @return return value
     */
    private static File getVideosSentPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getVideosPath(mContext), mContext.getApplicationContext().getString(R.string.directory_sent));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create thumb videos  directory
     *
     * @param mContext
     * @return return value
     */
    private static File getVideosThumbnailPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getVideosPath(mContext), mContext.getApplicationContext().getString(R.string.video_thumbnail));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create sent audio  directory
     *
     * @param mContext
     * @return return value
     */
    private static File getAudiosSentPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getAudiosPath(mContext), mContext.getApplicationContext().getString(R.string.directory_sent));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * method to create sent images  directory
     *
     * @param mContext
     * @return return value
     */
    private static File getDocumentsSentPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getDocumentsPath(mContext), mContext.getApplicationContext().getString(R.string.directory_sent));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }
    /**
     * ********************************************************************************* ************************************************
     * *************************************************** Methods to get Files absolute path string ************************************
     * **********************************************************************************************************************************
     */

    /**
     * @param mContext
     * @return Wallpaper path string
     */
    private static String getWallpaperPathString(Context mContext) {
        return String.valueOf(getWallpaperPath(mContext));
    }

    /* --------------------------  received fies       ---------------------------------------*/

    /**
     * @param mContext
     * @return Videos path string
     */
    private static String getVideosPathString(Context mContext) {
        return String.valueOf(getVideosPath(mContext));
    }

    /**
     * @param mContext
     * @return Images path string
     */
    public static String getImagesPathString(Context mContext) {
        return String.valueOf(getImagesPath(mContext));
    }

    /**
     * @param mContext
     * @return gif path string
     */
    public static String getGifPathString(Context mContext) {
        return String.valueOf(getGifPath(mContext));
    }

    /**
     * @param mContext
     * @return Audios path string
     */
    private static String getAudiosPathString(Context mContext) {
        return String.valueOf(getAudiosPath(mContext));
    }

    /**
     * @param mContext
     * @return Documents path string
     */
    private static String getDocumentsPathString(Context mContext) {
        return String.valueOf(getDocumentsPath(mContext));
    }

    /**
     * @param mContext
     * @return Images profile path string
     */
    public static String getProfilePhotosPathString(Context mContext) {
        return String.valueOf(getProfilePhotosPath(mContext));
    }

    /**
     * @param mContext
     * @return Images profile path string
     */
    public static String getDataCachedPathString(Context mContext) {
        return String.valueOf(getFilesCachePath(mContext));
    }
    /* --------------------------  sent fies       ---------------------------------------*/

    /**
     * @param mContext
     * @return sent Images path string
     */
    private static String getImagesSentPathString(Context mContext) {
        return String.valueOf(getImagesSentPath(mContext));
    }

    /**
     * @param mContext
     * @return sent gif path string
     */
    private static String getGifSentPathString(Context mContext) {
        return String.valueOf(getGifSentPath(mContext));
    }

    /**
     * @param mContext
     * @return sent Document path string
     */
    private static String getDocumentsSentPathString(Context mContext) {
        return String.valueOf(getDocumentsSentPath(mContext));
    }

    /**
     * @param mContext
     * @return sent Videos  path string
     */
    private static String getVideosSentPathString(Context mContext) {
        return String.valueOf(getVideosSentPath(mContext));
    }

    /**
     * @param mContext
     * @return thumbnail Videos  path string
     */
    private static String getVideosThumbnailPathString(Context mContext) {
        return String.valueOf(getVideosThumbnailPath(mContext));
    }

    /**
     * @param mContext
     * @return sent Audio path string
     */
    private static String getAudiosSentPathString(Context mContext) {
        return String.valueOf(getAudiosSentPath(mContext));
    }


/**
 * ********************************************************************************* ************************************************
 * *************************************************** Methods to Check if Files exists *********************************************
 * **********************************************************************************************************************************
 */
    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFilePhotoProfileExists method
     * @param mContext this is the second parameter isFilePhotoProfileExists method
     * @return Boolean
     */
    public static boolean isFilePhotoProfileExists(Context mContext, String Id) {
        File file = new File(getProfilePhotosPathString(mContext), Id);
        return file.exists();
    }

    /* --------------------------  received fies       ---------------------------------------*/

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileVideosExists method
     * @param mContext this is the second parameter isFileVideosExists method
     * @return Boolean
     */
    public static boolean isFileVideosExists(Context mContext, String Id) {
        File file = new File(getVideosPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileVideosExists method
     * @param mContext this is the second parameter isFileVideosExists method
     * @return Boolean
     */
    public static boolean isFileAudioExists(Context mContext, String Id) {
        File file = new File(getAudiosPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Path this is the first parameter isFileVideosExists method
     * @return Boolean
     */
    public static boolean isFileRecordExists(String Path) {
        File file = new File(Path);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileVideosExists method
     * @param mContext this is the second parameter isFileVideosExists method
     * @return Boolean
     */
    public static boolean isFileDocumentsExists(Context mContext, String Id) {
        File file = new File(getDocumentsPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileImagesExists method
     * @param mContext this is the second parameter isFileImagesExists method
     * @return Boolean
     */
    public static boolean isFileImagesExists(Context mContext, String Id) {
        File file = new File(getImagesPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileGifExists method
     * @param mContext this is the second parameter isFileGifExists method
     * @return Boolean
     */
    public static boolean isFileGifExists(Context mContext, String Id) {
        File file = new File(getGifPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileImagesExists method
     * @param mContext this is the second parameter isFileImagesExists method
     * @return Boolean
     */
    public static boolean isFileWallpaperExists(Context mContext, String Id) {
        File file = new File(getWallpaperPathString(mContext), Id);
        return file.exists();
    }


    /* --------------------------  sent fies       ---------------------------------------*/

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileDocumentsSentExists method
     * @param mContext this is the second parameter isFileDocumentsSentExists method
     * @return Boolean
     */
    public static boolean isFileDataCachedExists(Context mContext, String Id) {

        File file = new File(getDataCachedPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileDocumentsSentExists method
     * @param mContext this is the second parameter isFileDocumentsSentExists method
     * @return Boolean
     */
    public static boolean isFileDocumentsSentExists(Context mContext, String Id) {

        File file = new File(getDocumentsSentPathString(mContext), Id);
        return file.exists();
    }


    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileImagesSentExists method
     * @param mContext this is the second parameter isFileImagesSentExists method
     * @return Boolean
     */
    public static boolean isFileImagesSentExists(Context mContext, String Id) {
        File file = new File(getImagesSentPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileGifSentExists method
     * @param mContext this is the second parameter isFileGifSentExists method
     * @return Boolean
     */
    public static boolean isFileGifSentExists(Context mContext, String Id) {
        File file = new File(getGifSentPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileAudiosSentExists method
     * @param mContext this is the second parameter isFileAudiosSentExists method
     * @return Boolean
     */
    public static boolean isFileAudiosSentExists(Context mContext, String Id) {
        File file = new File(getAudiosSentPathString(mContext), Id);
        return file.exists();
    }

    /**
     * Check file if exists method
     *
     * @param Id       this is the first parameter isFileVideosSentExists method
     * @param mContext this is the second parameter isFileVideosSentExists method
     * @return Boolean
     */
    public static boolean isFileVideosSentExists(Context mContext, String Id) {
        File file = new File(getVideosSentPathString(mContext), Id);
        return file.exists();
    }


    /**
     * ********************************************************************************* ************************************************
     * *************************************************** Methods to get Files *********************************************************
     * **********************************************************************************************************************************
     */

    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileImageSent method
     * @return file
     */
    public static File getFileWallpaper(Context mContext, String Identifier) {
        return new File(getFileWallpaperPath(mContext, Identifier));
    }

    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileImageSent method
     * @return file
     */
    public static File getFileDataCached(Context mContext, String Identifier) {
        return new File(getFileDataCachedPath(mContext, Identifier));
    }

    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileImageSent method
     * @return file
     */
    public static File getFileProfilePhoto(Context mContext, String Identifier) {
        return new File(getFileProfilePhotoPath(mContext, Identifier));
    }

    /* --------------------------  sent fies       ---------------------------------------*/


    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileImageSent method
     * @return file
     */
    public static File getFileImageSent(Context mContext, String Identifier) {
        return new File(getFileImagesSentPath(mContext, Identifier));
    }

    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileGifSent method
     * @return file
     */
    public static File getFileGifSent(Context mContext, String Identifier) {
        return new File(getFileGifSentPath(mContext, Identifier));
    }


    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileVideoSent method
     * @return file
     */
    public static File getFileVideoSent(Context mContext, String Identifier) {
        return new File(getFileVideosSentPath(mContext, Identifier));
    }


    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileAudioSent method
     * @return file
     */
    public static File getFileAudioSent(Context mContext, String Identifier) {
        return new File(getFileAudiosSentPath(mContext, Identifier));
    }


    /**
     * method to get sent file
     *
     * @param Identifier this is  parameter of getFileDocumentSent method
     * @return file
     */
    public static File getFileDocumentSent(Context mContext, String Identifier) {
        return new File(getFileDocumentsSentPath(mContext, Identifier));
    }

    /* --------------------------  received fies       ---------------------------------------*/

    /**
     * method to get file
     *
     * @param Identifier this is  parameter of getFileImage method
     * @return file
     */
    public static File getFileImage(Context mContext, String Identifier) {
        return new File(getFileImagesPath(mContext, Identifier));
    }

    /**
     * method to get file
     *
     * @param Identifier this is  parameter of getFileImage method
     * @return file
     */
    public static File getFileGif(Context mContext, String Identifier) {
        return new File(getFileGifPath(mContext, Identifier));
    }

    /**
     * method to get file
     *
     * @param Identifier this is  parameter of getFileVideo method
     * @return file
     */
    public static File getFileVideo(Context mContext, String Identifier) {
        return new File(getFileVideoPath(mContext, Identifier));
    }

    /**
     * method to get file
     *
     * @param Identifier this is  parameter of getFileAudio method
     * @return file
     */
    public static File getFileAudio(Context mContext, String Identifier) {
        return new File(getFileAudioPath(mContext, Identifier));
    }

    /**
     * method to get file
     *
     * @param Path this is a parameter of getFileRecord method
     * @return file
     */
    public static File getFileRecord(String Path) {
        return new File(Path);
    }

    /**
     * method to get file
     *
     * @param Identifier this is  parameter of getFileAudio method
     * @return file
     */
    public static File getFileDocument(Context mContext, String Identifier) {
        return new File(getFileDocumentsPath(mContext, Identifier));
    }

    /**
     * ********************************************************************************* ************************************************
     * *************************************************** Methods to get Files Paths (use those methods in other classes to check the file path) **************
     * **********************************************************************************************************************************
     */

    public static String getDataCached(String Identifier) {
        return String.format("Data-%s", Identifier);
    }

    public static String getWallpaper(String Identifier) {
        return String.format("WP-%s", Identifier + ".jpg");
    }

    public static String getProfileImage(String Identifier) {
        return String.format("IMG-Profile-%s", Identifier + ".jpg");
    }

    public static String getImage(String Identifier) {
        return String.format("IMG-%s", Identifier + ".jpg");
    }

    public static String getGif(String Identifier) {
        return String.format("IMG-%s", Identifier + ".gif");
    }

    public static String getAudio(String Identifier) {
        return String.format("AUD-%s", Identifier + ".mp3");
    }

    public static String getDocument(String Identifier) {
        return String.format("DOC-%s", Identifier + ".pdf");
    }

    public static String getVideo(String Identifier) {
        return String.format("VID-%s", Identifier + ".mp4");
    }

    public static File getFileThumbnail(Context mContext, Bitmap bmp) throws java.io.IOException {
        File file = new File(getFileThumbnailPath(mContext));
        file.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
        out.close();
        return file;
    }

    /**
     * **************************************************************** *****************************************************************
     * *************************************************** Methods to get String Paths **************************************************
     * **********************************************************************************************************************************
     */

    /**
     * @param Identifier this is parameter of getFileImagesSentPath method
     * @return String path
     */
    public static String getFileDataCachedPath(Context mContext, String Identifier) {
        return String.format(getDataCachedPathString(mContext) + File.separator + "Data-%s", Identifier);
    }

    /**
     * @param Identifier this is parameter of getFileImagesSentPath method
     * @return String path
     */
    public static String getFileProfilePhotoPath(Context mContext, String Identifier) {
        return String.format(getProfilePhotosPathString(mContext) + File.separator + "IMG-Profile-%s", Identifier + ".jpg");
    }

    /**
     * @param Identifier this is parameter of getFileImagesSentPath method
     * @return String path
     */
    public static String getFileImagesSentPath(Context mContext, String Identifier) {
        return String.format(getImagesSentPathString(mContext) + File.separator + "IMG-%s", Identifier + ".jpg");
    }

    /**
     * @param Identifier this is parameter of getFileImagesSentPath method
     * @return String path
     */
    public static String getFileGifSentPath(Context mContext, String Identifier) {
        //return String.format(getGifSentPathString(mContext) + File.separator + "IMG-%s", Identifier + ".gif");todo nchof hadi
        return String.format(getGifSentPathString(mContext) + File.separator + "IMG-%s", Identifier + "");
    }

    /**
     * @param Identifier this is parameter of getFileVideosSentPath method
     * @return String path
     */
    public static String getFileVideosSentPath(Context mContext, String Identifier) {
        return String.format(getVideosSentPathString(mContext) + File.separator + "VID-%s", Identifier + ".mp4");
    }

    /**
     * @param Identifier this is parameter of getFileImagesSentPath method
     * @return String path
     */
    public static String getFileAudiosSentPath(Context mContext, String Identifier) {
        return String.format(getAudiosSentPathString(mContext) + File.separator + "AUD-%s", Identifier + ".mp3");
    }

    /**
     * @param Identifier this is parameter of getFileImagesSentPath method
     * @return String path
     */
    public static String getFileDocumentsSentPath(Context mContext, String Identifier) {
        return String.format(getDocumentsSentPathString(mContext) + File.separator + "DOC-%s", Identifier + ".pdf");
    }


    /**
     * @param Identifier this is first parameter of getFileVideoPath method
     * @return String path
     */
    public static String getFileVideoPath(Context mContext, String Identifier) {
        return String.format(getVideosPathString(mContext) + File.separator + "VID-%s", Identifier + ".mp4");
    }

    /**
     * @param Identifier this is first parameter of getFileAudioPath method
     * @return String path
     */
    public static String getFileAudioPath(Context mContext, String Identifier) {
        return String.format(getAudiosPathString(mContext) + File.separator + "AUD-%s", Identifier + ".mp3");
    }

    /**
     * @return String path
     */
    public static String getFileRecordPath(Context mContext) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault()).format(new Date());
        return String.format(getAudiosSentPathString(mContext) + File.separator + "record-%s", timeStamp + ".mp3");
    }

    /**
     * @param Identifier this is first parameter of getFileImagesPath method
     * @return String path
     */
    public static String getFileImagesPath(Context mContext, String Identifier) {
        return String.format(getImagesPathString(mContext) + File.separator + "IMG-%s", Identifier + ".jpg");
    }

    /**
     * @param Identifier this is first parameter of getFileGifPath method
     * @return String path
     */
    public static String getFileGifPath(Context mContext, String Identifier) {
        return String.format(getGifPathString(mContext) + File.separator + "IMG-%s", Identifier + ".gif");
    }

    /**
     * @param Identifier this is first parameter of getFileWallpaperPath method
     * @return String path
     */
    public static String getFileWallpaperPath(Context mContext, String Identifier) {
        return String.format(getWallpaperPathString(mContext) + File.separator + "WP-%s", Identifier + ".jpg");
    }

    /**
     * @param Identifier this is first parameter of getFileDocumentsPath method
     * @return String path
     */
    public static String getFileDocumentsPath(Context mContext, String Identifier) {
        return String.format(getDocumentsPathString(mContext) + File.separator + "DOC-%s", Identifier + ".pdf");
    }

    /**
     * @param mContext
     * @return String path
     */
    public static String getFileThumbnailPath(Context mContext) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return String.format(getVideosThumbnailPathString(mContext) + File.separator + "THUMB_%s", timeStamp + ".jpg");
    }

    /**
     * **************************************************************** *****************************************************************
     * *************************************************** Methods to get downloads files ***********************************************
     * **********************************************************************************************************************************
     */


    @SuppressLint("CheckResult")
    public static void downloadMediaFile(Context mContext, Bitmap bitmap, String Identifier, String type) {
        try {
            boolean deleted = true;
            if (isFileImagesSentExists(mContext, FilesManager.getImage(Identifier))) {
                deleted = getFileImageSent(mContext, Identifier).delete();
            } else if (isFileImagesExists(mContext, FilesManager.getImage(Identifier))) {
                deleted = getFileImage(mContext, Identifier).delete();
            }

            if (!deleted) {
                AppHelper.LogCat(" not deleted downloadMediaFile");
            } else {
                AppHelper.LogCat("deleted downloadMediaFile");
                String filePath = null;
                switch (type) {
                    case AppConstants.SENT_IMAGE:
                        filePath = getFileImagesSentPath(mContext, Identifier);
                        break;
                    case AppConstants.RECEIVED_IMAGE:
                        filePath = getFileImagesPath(mContext, Identifier);
                        break;
                    case AppConstants.PROFILE_IMAGE:
                        filePath = getFileProfilePhotoPath(mContext, Identifier);
                        break;
                }
                final String finalPath = filePath;
                Observable.create((ObservableOnSubscribe<String>) subscriber -> {
                    try {
                        FileOutputStream out = new FileOutputStream(finalPath);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.close();
                        subscriber.onNext("The is saved :" + Identifier);
                        subscriber.onComplete();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }).ignoreElements()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {

                        }, AppHelper::LogCat);
            }

        } catch (Exception e) {
            AppHelper.LogCat("save file Exception" + e);
        }

    }


    /**
     * method to do
     *
     * @param mContext   this is the first parameter downloadFilesToDevice method
     * @param fileUrl    this is the second parameter downloadFilesToDevice method
     * @param Identifier this is the third parameter downloadFilesToDevice method
     */
    @SuppressLint("CheckResult")
    public static void downloadFilesToDevice(Context mContext, String fileUrl, String Identifier, String type) {

        APIService apiService = new APIService(mContext);
        final FilesDownloadService downloadService = apiService.RootService(FilesDownloadService.class, BuildConfig.BACKEND_BASE_URL);

        Observable.create((ObservableOnSubscribe<String>) subscriber -> {
            Call<ResponseBody> call = downloadService.downloadSmallFileSizeUrlSync(fileUrl);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        AppHelper.LogCat("server contacted and has file");
                        try {
                            writeResponseBodyToDisk(mContext, response.body(), Identifier, type);
                            subscriber.onNext("The file is saved :" + Identifier);
                            subscriber.onComplete();
                        } catch (Exception e) {
                            AppHelper.LogCat("file download was a failed");
                            subscriber.onError(e);
                        }


                        //AppHelper.LogCat("file download was a success? " + writtenToDisk);
                    } else {
                        AppHelper.LogCat("server contact failed");
                    }

                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    AppHelper.LogCat("download failed " + t.getMessage());
                }


            });

        }).ignoreElements()
                .subscribeOn(Schedulers.computation())
                .subscribe(() -> AppHelper.LogCat("saved"), AppHelper::LogCat);
    }

    /**
     * @param body       this is the first parameter writeResponseBodyToDisk method
     * @param Identifier this is the first parameter writeResponseBodyToDisk method
     * @return boolean
     */
    private static void writeResponseBodyToDisk(Context mContext, ResponseBody body, String Identifier, String type) {
        boolean deleted = true;
        if (isFileImagesSentExists(mContext, FilesManager.getImage(Identifier))) {
            deleted = getFileImageSent(mContext, Identifier).delete();
        } else if (isFileVideosSentExists(mContext, FilesManager.getVideo(Identifier))) {
            deleted = getFileVideoSent(mContext, Identifier).delete();
        } else if (isFileAudiosSentExists(mContext, FilesManager.getAudio(Identifier))) {
            deleted = getFileAudioSent(mContext, Identifier).delete();
        } else if (isFileDocumentsSentExists(mContext, FilesManager.getDocument(Identifier))) {
            deleted = getFileDocumentSent(mContext, Identifier).delete();
        } else if (isFileDataCachedExists(mContext, FilesManager.getDataCached(Identifier))) {
            deleted = getFileDataCached(mContext, Identifier).delete();
        } else if (isFileGifSentExists(mContext, FilesManager.getGif(Identifier))) {
            deleted = getFileGif(mContext, Identifier).delete();
        }

        if (!deleted) {
            AppHelper.LogCat(" not deleted ");
        } else {
            AppHelper.LogCat("deleted");
            File downloadedFile = null;
            switch (type) {
                case AppConstants.SENT_IMAGES:
                    downloadedFile = new File(getFileImagesSentPath(mContext, Identifier));
                    break;
                case AppConstants.SENT_AUDIO:
                    downloadedFile = new File(getFileAudiosSentPath(mContext, Identifier));
                    break;
                case AppConstants.SENT_DOCUMENTS:
                    downloadedFile = new File(getFileDocumentsSentPath(mContext, Identifier));
                    break;
                case AppConstants.SENT_VIDEOS:
                    downloadedFile = new File(getFileVideosSentPath(mContext, Identifier));
                    break;
                case AppConstants.DATA_CACHED:
                    downloadedFile = new File(getFileDataCachedPath(mContext, Identifier));
                    break;
                case AppConstants.SENT_GIF:
                    downloadedFile = new File(getFileGifSentPath(mContext, Identifier));
                    break;
            }

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

            /*long fileSize = body.contentLength();
            long fileSizeDownloaded = 0;*/

                inputStream = body.byteStream();
                try {
                    if (downloadedFile != null) {
                        outputStream = new FileOutputStream(downloadedFile);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                while (true) {
                    int read = 0;
                    try {
                        read = inputStream.read(fileReader);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (read == -1) {
                        break;
                    }

                    try {
                        if (outputStream != null) {
                            outputStream.write(fileReader, 0, read);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    /*fileSizeDownloaded += read;*/

                    /*AppHelper.LogCat("file download: " + fileSizeDownloaded + " of " + fileSize);*/
                }

                try {
                    if (outputStream != null) {
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static @Nullable
    String getMimeType(Context context, Uri uri) {
        if (uri == null) return null;


        String type = context.getContentResolver().getType(uri);
        if (type == null) {
            final String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return getCorrectedMimeType(type);
    }

    public static @Nullable
    String getCorrectedMimeType(@Nullable String mimeType) {
        if (mimeType == null) return null;

        switch (mimeType) {
            case "image/jpg":
                return MimeTypeMap.getSingleton().hasMimeType(IMAGE_JPEG)
                        ? IMAGE_JPEG
                        : mimeType;
            default:
                return mimeType;
        }
    }

    /**
     * method to get mime type of files
     *
     * @param url
     * @return
     */
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static @NonNull
    String getMimeTypeFromExtension(@NonNull Uri uri) {
        final String mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    public static boolean isVideo(String contentType) {
        return !TextUtils.isEmpty(contentType) && contentType.trim().startsWith("video/");
    }

    public static boolean isGif(String contentType) {
        return !TextUtils.isEmpty(contentType) && contentType.trim().equals("image/gif");
    }

    public static boolean isImageType(String contentType) {
        return (null != contentType) && contentType.startsWith("image/");
    }

    public static String getFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {


        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String filePath = "";
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {

                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {

                    if (Build.VERSION.SDK_INT > 20) {
                        //getExternalMediaDirs() added in API 21
                        File extenal[] = context.getExternalMediaDirs();
                        if (extenal.length > 1) {
                            filePath = extenal[1].getAbsolutePath();
                            filePath = filePath.substring(0, filePath.indexOf("Android")) + split[1];
                        }
                    } else {
                        filePath = "/storage/" + type + "/" + split[1];
                    }
                    return filePath;
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                //  final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                final String column = "_data";
                final String[] projection = {column};

                try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        final int index = cursor.getColumnIndexOrThrow(column);
                        filePath = cursor.getString(index);
                        cursor.close();
                        return filePath;
                    }
                }
                //return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {

                // Return the remote address
                if (isGooglePhotosUri(uri))
                    return uri.getLastPathSegment();
                try {

                    return getDataColumn(context, uri, null, null);
                } catch (Exception e) {
                    AppHelper.LogCat(e.toString());
                    return null;
                }
            }
            // Other Providers
            else {
                try {
                    InputStream attachment = context.getContentResolver().openInputStream(uri);
                    if (attachment != null) {
                        String filename = getContentName(context.getContentResolver(), uri);
                        if (filename != null) {
                            File file = new File(context.getCacheDir(), filename);
                            FileOutputStream tmp = new FileOutputStream(file);
                            byte[] buffer = new byte[1024];
                            while (attachment.read(buffer) > 0) {
                                tmp.write(buffer);
                            }
                            tmp.close();
                            attachment.close();
                            return file.getAbsolutePath();
                        }
                    }
                } catch (Exception e) {
                    AppHelper.LogCat(e.toString());
                    return null;
                }
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static String getContentName(ContentResolver resolver, Uri uri) {
        Cursor cursor = resolver.query(uri, null, null, null, null);
        cursor.moveToFirst();
        int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
        if (nameIndex >= 0) {
            String name = cursor.getString(nameIndex);
            cursor.close();
            return name;
        }
        return null;
    }


    public static File getCacheDir() {
        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
        if (state == null || state.startsWith(Environment.MEDIA_MOUNTED)) {
            try {
                File file = WhatsCloneApplication.getInstance().getExternalCacheDir();
                if (file != null) {
                    return file;
                }
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        }
        try {
            File file = WhatsCloneApplication.getInstance().getCacheDir();
            if (file != null) {
                return file;
            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
        return new File("");
    }

    public static String copyDocumentToCache(byte[] byteArray, String ext) {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(byteArray);
        Bitmap bitmap = BitmapFactory.decodeStream(arrayInputStream);

        FileOutputStream output = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault()).format(new Date());


            File f = new File(WhatsCloneApplication.getInstance().getCacheDir(), String.format(Locale.US, "%s%s", timeStamp, ext));

            output = new FileOutputStream(f.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output);

            return f.getAbsolutePath();
        } catch (Exception e) {
            AppHelper.LogCat(e);
        } finally {

            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception e2) {
                AppHelper.LogCat(e2);
            }
        }
        return null;
    }

    public static String copyDocumentToCache(Uri uri, String ext) {
        ParcelFileDescriptor parcelFD = null;
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault()).format(new Date());
            parcelFD = WhatsCloneApplication.getInstance().getContentResolver().openFileDescriptor(uri, "r");
            input = new FileInputStream(parcelFD.getFileDescriptor());
            File f = new File(getCacheDir(), String.format(Locale.US, "%s%s", timeStamp, ext));
            output = new FileOutputStream(f);
            input.getChannel().transferTo(0, input.getChannel().size(), output.getChannel());
            return f.getAbsolutePath();
        } catch (Exception e) {
            AppHelper.LogCat(e);
        } finally {
            try {
                if (parcelFD != null) {
                    parcelFD.close();
                }
            } catch (Exception e2) {
                AppHelper.LogCat(e2);
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e2) {
                AppHelper.LogCat(e2);
            }
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception e2) {
                AppHelper.LogCat(e2);
            }
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        String result = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                result = cursor.getString(index);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return result;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static void copyFile(File src, File dst) throws IOException {

        FileInputStream fileInputStream = new FileInputStream(src);
        FileOutputStream fileOutputStream = new FileOutputStream(dst);
        byte[] var4 = new byte[1024];

        int var5;
        while ((var5 = fileInputStream.read(var4)) > 0) {
            fileOutputStream.write(var4, 0, var5);
        }
        fileInputStream.close();
        fileOutputStream.close();
     /*   src.delete();
        if (src.exists()) {
            src.getCanonicalFile().delete();
            if (src.exists()) {
                WhatsCloneApplication.getInstance().deleteFile(src.getName());
            }
        }*/
    }

    public static Uri getFile(File file) {
        if (Build.VERSION.SDK_INT > M) {
            return FileProvider.getUriForFile(WhatsCloneApplication.getInstance(), BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }


    public static String convertImageFile(Uri urx, Context context)
            throws IOException {

        ContentResolver test = context.getContentResolver();
        InputStream initialStream = test.openInputStream(urx);


        byte[] buffer = new byte[initialStream.available()];
        initialStream.read(buffer);

        File xpath = context.getExternalCacheDir();


        File targetFile = new File(xpath, context.getString(R.string.app_name) + ".jpg");
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);

        return targetFile.getPath();

    }

    public static String convertVideoFile(Uri urx, Context context)
            throws IOException {

        ContentResolver test = context.getContentResolver();
        InputStream initialStream = test.openInputStream(urx);


        byte[] buffer = new byte[initialStream.available()];
        initialStream.read(buffer);

        File xpath = context.getExternalCacheDir();


        File targetFile = new File(xpath, context.getString(R.string.app_name) + ".mp4");
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);

        return targetFile.getPath();

    }

    private static @NonNull
    File getExternalDir(Context context) throws IOException {
        final File externalDir = context.getExternalCacheDir();
        if (externalDir == null) throw new IOException("no external files directory");
        return externalDir;
    }

    public static Uri createForExternal(@NonNull Context context, @NonNull String mimeType) throws IOException {
        File target = new File(getExternalDir(context), String.valueOf(System.currentTimeMillis()) + "." + getExtensionFromMimeType(mimeType));
        return getFile(target);
    }

    private static @NonNull
    String getExtensionFromMimeType(String mimeType) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
    }

    public static Uri getImageFile(Activity activity) {
        Uri outputFileUri = null;
        File getImage = activity.getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = FilesManager.getFile(new File(getImage.getPath(), activity.getString(R.string.app_name) + ".jpg"));
        }
        return outputFileUri;

    }

    public static Uri getVideoFile(Activity activity) {
        Uri outputFileUri = null;
        File getImage = activity.getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = FilesManager.getFile(new File(getImage.getPath(), activity.getString(R.string.app_name) + ".mp4"));
        }
        return outputFileUri;

    }


    /**
     * methods to launch the  choosers
     */


    public static void selectDocument(Activity activity, int requestCode) {

        Permissions.with(activity)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .ifNecessary()
                .withPermanentDenialDialog(activity.getString(R.string.write_storage_permission_message))
                .onAllGranted(() -> selectMediaType(activity, "application/*", null, requestCode))
                .execute();
    }

    public static void selectGallery(Activity activity, int requestCode) {

        Permissions.with(activity)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .ifNecessary()
                .withPermanentDenialDialog(activity.getString(R.string.write_storage_permission_message))
                .onAllGranted(() -> selectMediaType(activity, "image/*", new String[]{"image/*", "video/*"}, requestCode))
                .execute();
    }

    public static void selectAudio(Activity activity, int requestCode) {

        Permissions.with(activity)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .ifNecessary()
                .withPermanentDenialDialog(activity.getString(R.string.write_storage_permission_message))
                .onAllGranted(() -> selectMediaType(activity, "audio/*", null, requestCode))
                .execute();
    }

    public static void selectContactInfo(Activity activity, int requestCode) {

        Permissions.with(activity)
                .request(Manifest.permission.WRITE_CONTACTS)
                .ifNecessary()
                .withPermanentDenialDialog(activity.getString(R.string.write_contacts_permission_message))
                .onAllGranted(() -> {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    activity.startActivityForResult(intent, requestCode);
                })
                .execute();
    }

    public static void selectLocation(Activity activity, int requestCode) {

        Permissions.with(activity)
                .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .ifNecessary()
                .withPermanentDenialDialog(activity.getString(R.string.location_permission_message))
                .onAllGranted(() -> {
                    activity.startActivityForResult(new Intent(activity, LocationActivity.class), requestCode);
                })
                .execute();
    }


    public static void selectGif(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, GiphyActivity.class);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivityForResult(intent, requestCode);
    }

    private @Nullable
    static Uri mProcessingPhotoUri;

    @Nullable
    public static Uri getmProcessingPhotoUri() {
        return mProcessingPhotoUri;
    }

    public static void cleanmProcessingPhotoUri() {
        mProcessingPhotoUri = null;
    }

    public static void capturePhoto(AppCompatActivity activity, int requestCode, boolean forStory) {
        Permissions.with(activity)
                .request(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                .ifNecessary()
                .withRationaleDialog(activity.getString(R.string.to_call_s_app_needs_access_to_your_microphone_and_camera, activity.getString(R.string.app_name)), R.drawable.ic_mic_white_24dp, R.drawable.ic_videocam_white_24dp)
                .withPermanentDenialDialog(activity.getString(R.string.app_needs_the_microphone_and_camera_permissions_in_order_to_call_s, activity.getString(R.string.app_name)))
                .onAllGranted(() -> {
                    PickerActivity.start(activity,                    //Activity or Fragment Instance
                            requestCode,                //Request code for activity results
                            1, //Number of images to restrict selection count
                            forStory);

                })
                .execute();
    }

    private static void selectMediaType(Activity activity, @NonNull String type, @Nullable String[] extraMimeType, int requestCode) {
        final Intent intent = new Intent();
        intent.setType(type);

        if (extraMimeType != null && Build.VERSION.SDK_INT >= 19) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeType);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            try {
                activity.startActivityForResult(intent, requestCode);
                return;
            } catch (ActivityNotFoundException anfe) {
                AppHelper.LogCat("couldn't complete ACTION_OPEN_DOCUMENT, no activity found. falling back.");
            }
        }

        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException anfe) {
            AppHelper.LogCat("couldn't complete ACTION_GET_CONTENT intent, no activity found. falling back.");
            Toast.makeText(activity, R.string.Attach_cant_open_media_selection, Toast.LENGTH_LONG).show();
        }
    }

    public static final char EXTENSION_SEPARATOR = '.';
    private static final char WINDOWS_SEPARATOR = '\\';
    private static final char UNIX_SEPARATOR = '/';
    private static final int NOT_FOUND = -1;

    public static String getExtension(File file) {
        return getExtension(file.getName());
    }

    public static String getExtension(final String filename) {
        if (filename == null) {
            return null;
        }
        final int index = indexOfExtension(filename);
        if (index == NOT_FOUND) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    public static int indexOfExtension(final String filename) {
        if (filename == null) {
            return NOT_FOUND;
        }
        final int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        final int lastSeparator = indexOfLastSeparator(filename);
        return lastSeparator > extensionPos ? NOT_FOUND : extensionPos;
    }

    public static int indexOfLastSeparator(final String filename) {
        if (filename == null) {
            return NOT_FOUND;
        }
        final int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        final int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    public static String getDuration(Context context, String Path) {
        MediaPlayer mp = MediaPlayer.create(context, Uri.parse(Path));
        /*
        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
        retriever.setDataSource(Path);
        String time = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
        AppHelper.LogCat("getDuration " + time);
        retriever.release();*/
        return String.valueOf(mp.getDuration());
    }

    public static String getName(String Path) {

        File file = new File(Path);
        AppHelper.LogCat("getName " + file.getName());
        return file.getName();
    }


    @SuppressLint("CheckResult")
    public static void downloadFile(Context context, Bitmap bitmap, final String Id) {
        String path = null;
        try {
            if (isFileDataCachedExists(context, Id)) {
                getFileDataCached(context, Id).delete();
                return;
            }
        } catch (Exception ignored) {
        }
        try {
            Observable.create((ObservableOnSubscribe<String>) subscriber -> {
                try {
                    FileOutputStream out = new FileOutputStream(getFileDataCached(context, Id));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.close();
                    subscriber.onNext("done saving file:" + Id);
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }

            }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(string -> {
               // path = getFileDataCachedPath(context,Id);
            }, AppHelper::LogCat);
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }finally {

        }

    }
}
