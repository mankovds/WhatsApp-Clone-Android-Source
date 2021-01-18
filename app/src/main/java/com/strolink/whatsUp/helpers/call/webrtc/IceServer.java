
package com.strolink.whatsUp.helpers.call.webrtc;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IceServer {

    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("username")
    @Expose
    public String username;
    @SerializedName("credential")
    @Expose
    public String credential;

    public IceServer(String url, String username, String credential){
        this.url=url;
        this.username=username;
        this.credential=credential;
    }

    @Override
    public String toString() {
        return "IceServer{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", credential='" + credential + '\'' +
                '}';
    }
}
