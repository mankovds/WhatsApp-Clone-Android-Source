package  com.strolink.whatsUp.jobs.utils;

import androidx.annotation.NonNull;

import com.strolink.whatsUp.jobs.files.PendingFilesTask;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by Abderrahim El imame on 10/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class UploadProgressRequestBody extends RequestBody {

    private final RequestBody delegate;
    private Listener listener;
    private String messageId;

    public UploadProgressRequestBody(RequestBody delegate, Listener listener, String messageId) {
        this.delegate = delegate;
        this.listener = listener;
        this.messageId = messageId;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }


    @Override
    public long contentLength() {
        try {
            return delegate.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        CountingSink countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);

        delegate.writeTo(bufferedSink);

        bufferedSink.flush();
    }


    final class CountingSink extends ForwardingSink {

        private long bytesWritten = 0;

        CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(@NonNull Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            if (PendingFilesTask.containsFile(messageId)) {
                bytesWritten += byteCount;
                if (listener != null)
                    listener.onRequestProgress(bytesWritten, contentLength());
            } else {
                listener = null;
            }
        }

    }

    public interface Listener {
        void onRequestProgress(long bytesWritten, long contentLength);
    }

}
