package com.strolink.whatsUp.api;

import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.messages.FilesResponse;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by Abderrahim El imame on 7/26/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public interface FilesUploadService {
    /**
     * method to upload images
     *
     * @param image this is  the second parameter for  uploadMessageImage method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_IMAGE)
    Observable<FilesResponse> uploadImageFile(@Part MultipartBody.Part image);

    /**
     * method to upload videos
     *
     * @param video this is  the first parameter for  uploadMessageVideo method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_VIDEO)
    Observable<FilesResponse> uploadVideoFile(@Part MultipartBody.Part video);

    /**
     * method to upload audio
     *
     * @param audio this is   parameter for  uploadMessageAudio method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_AUDIO)
    Observable<FilesResponse> uploadAudioFile(@Part MultipartBody.Part audio);


    /**
     * method to upload document
     *
     * @param document this is  parameter for  uploadMessageDocument method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_DOCUMENT)
    Observable<FilesResponse> uploadDocumentFile(@Part MultipartBody.Part document);

    /**
     * method to upload gif
     *
     * @param gif this is  parameter for  uploadMessageGif method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_MESSAGES_GIF)
    Observable<FilesResponse> uploadGifFile(@Part MultipartBody.Part gif);


    /**
     * method to upload images
     *
     * @param image this is  the second parameter for  uploadMessageImage method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_GROUP_IMAGE)
    Observable<FilesResponse> uploadGroupImage(@Part MultipartBody.Part image);
    /**
     * method to upload user image
     *
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_USER_IMAGE)
    Observable<FilesResponse> uploadUserImage(@Part MultipartBody.Part image, @Path("userId") String id);


}
