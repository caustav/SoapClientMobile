package com.kc.wsdl.model;

import android.os.AsyncTask;

import com.kc.wsdl.OnTaskCompleted;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kc on 10/7/16.
 */
public class ReadWSDLTask extends AsyncTask<String, Void, String> {

    private Exception exception;
    private OnTaskCompleted listener;

    protected String doInBackground(String... urls) {
        String wsdlContent;
        String strUrl = urls[0];
        try {
            if (strUrl.isEmpty()){
                throw new Exception("URL is not provided.");
            }
            URL url = new URL(strUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            wsdlContent = result.toString();
        } catch (Exception e) {
            wsdlContent = null;
        }
        return wsdlContent;
    }

    protected void onPostExecute(String wsdlContent) {
        TaskCompleteResponse taskResponse = new TaskCompleteResponse();
        taskResponse.setData(wsdlContent);
        taskResponse.setStatus(0);
        listener.OnTaskCompleted(taskResponse);
    }

    public void setListener(OnTaskCompleted listener) {
        this.listener = listener;
    }
}