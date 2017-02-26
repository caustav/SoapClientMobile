package com.kc.wsdl.model;

import android.os.AsyncTask;

import com.kc.wsdl.OnTaskCompleted;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Created by kc on 10/7/16.
 */
public class WriteSoapRequestTask extends AsyncTask<String, String, String> {

    private OnTaskCompleted listener;

    @Override
    protected String doInBackground(String... params) {
         String strResponse = null;
         try{
            StringEntity stringEntity = new StringEntity(params[1], "UTF-8");
            stringEntity.setChunked(true);

            HttpPost httpPost = new HttpPost(params[0]);
            httpPost.setEntity(stringEntity);
            httpPost.addHeader("Accept", "text/xml");
            httpPost.addHeader("Content-Type", "text/xml; charset=utf-8");
            httpPost.addHeader("SOAPAction", params[2]);

            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                strResponse = EntityUtils.toString(entity);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return strResponse;
    }

    protected void onPostExecute(String strResponse) {
        if (listener != null){
            TaskCompleteResponse taskResponse = new TaskCompleteResponse();
            taskResponse.setData(strResponse);
            taskResponse.setStatus(0);
            listener.OnTaskCompleted(taskResponse);
        }
    }

    public void setListener(OnTaskCompleted listener) {
        this.listener = listener;
    }
}
