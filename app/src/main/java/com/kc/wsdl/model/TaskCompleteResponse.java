package com.kc.wsdl.model;

/**
 * Created by kc on 10/7/16.
 */
public class TaskCompleteResponse {

    public int getStatus() {
        return status;
    }

    public String getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public void setStatus(int status) {

        this.status = status;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    int status;
    String data;
    String message;
}
