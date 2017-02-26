package com.kc.wsdl;

import java.util.HashMap;
import java.util.Map;

public class Main {

	public static void main(String[] args) {
		SoapProxy sp = new SoapProxy();
		Map<String, String> mapValues = new HashMap<String, String>();
		mapValues.put("CityName", "Kolkata");
		mapValues.put("CountryName", "India");
		sp.call("http://www.webservicex.com/globalweather.asmx?WSDL", "GetWeather", mapValues, null);
	}

}
