package com.dsoapclient.kc.scapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kc.wsdl.OnTaskCompleted;
import com.kc.wsdl.SoapProxy;
import com.kc.wsdl.model.TaskCompleteResponse;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SoapProxy sp = new SoapProxy();
        Map<String, String> mapValues = new HashMap<String, String>();
        mapValues.put("CityName", "Kolkata");
        mapValues.put("CountryName", "India");
        sp.call("http://www.webservicex.com/globalweather.asmx?WSDL", "GetWeather", mapValues, new OnTaskCompleted() {
            @Override
            public void OnTaskCompleted(TaskCompleteResponse taskCompleteResponse) {

            }
        });
    }
}
