package com.kc.wsdl;

import com.kc.wsdl.model.TaskCompleteResponse;

import java.util.Map;

public class SoapProxy implements  OnTaskCompleted{
	
	private String namespaceName;
	private String serviceName;
	WSDLReaderManager manager = new WSDLReaderManager();

    public boolean call(String wsdlLink, String operationName, Map<String, String> mapValues, final OnTaskCompleted onTaskCompleted){
    	boolean bRet = false;
		final String opName = operationName;
		final Map<String, String> mValues = mapValues;
        final OnTaskCompleted listener = new OnTaskCompleted() {
            @Override
            public void OnTaskCompleted(TaskCompleteResponse taskCompleteResponse) {
                onTaskCompleted.OnTaskCompleted(null);
            }
        };
    	try{
    		
    		if (wsdlLink == null || wsdlLink.isEmpty() || operationName == null || operationName.isEmpty()){
    			throw new Exception("Invalid input.");
    		}

			OnTaskCompleted taskCompleted = new OnTaskCompleted() {

                @Override
                public void OnTaskCompleted(TaskCompleteResponse taskCompleteResponse) {
                    namespaceName = manager.getNamespaceName();
                    serviceName = manager.getServiceName();
                    //        	ArrayList list = manager.getParameterList(operationName);
                    String xml = manager.createXML(opName, mValues);
                    String soapAction = manager.getSOAPAction(opName);
                    String httpLocation = manager.getHttpLocation();
                    manager.write(httpLocation, xml, soapAction, listener);
                    System.out.println(xml);
                }
            };
			manager.process(wsdlLink, taskCompleted);

    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return bRet;
    }

    @Override
    public void OnTaskCompleted(TaskCompleteResponse taskCompleteResponse) {

    }
}
