package  com.strolink.whatsUp.jobs.utils;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by Abderrahim El imame on 10/21/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class DownloadProgressInterceptor implements Interceptor {

    private DownloadProgressResponseBody.DownloadProgressListener listener;

    public DownloadProgressInterceptor(DownloadProgressResponseBody.DownloadProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        return originalResponse.newBuilder()
                .body(new DownloadProgressResponseBody(originalResponse.body(), listener))
                .build();
    }
}
