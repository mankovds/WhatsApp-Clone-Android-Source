package com.strolink.whatsUp.models;

/**
 * Created by Abderrahim El imame on 9/22/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class NetworkModel {


    private boolean success;
    private String message;

    public boolean isConnected() {
        return success;
    }

    public void setConnected(boolean success) {
        this.success = success;
    }

    public String getStatus() {
        return message;
    }

    public void setStatus(String message) {
        this.message = message;
    }
}
