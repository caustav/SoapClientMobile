package com.kc.wsdl;

import android.util.Log;

import com.kc.wsdl.model.ReadWSDLTask;
import com.kc.wsdl.model.TaskCompleteResponse;
import com.kc.wsdl.model.WriteSoapRequestTask;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class WSDLReaderManager implements  OnTaskCompleted{
	
	private String wsdlContent;
	private XPath xpath;
    private Map<String, String> mapNamespaces = new HashMap<String, String>();
    private Map<String, ArrayList<Parameter>> mapMethodParams = new HashMap<String, ArrayList<Parameter>>();
    Document doc;
    private OnTaskCompleted selfListener = null;

	private boolean read(String strUrl) {
		
		boolean bRet = false;
		try{
            ReadWSDLTask readWSDLTask = new ReadWSDLTask();
            readWSDLTask.setListener(this);
            readWSDLTask.execute(strUrl);
		} catch(Exception ex){
			bRet = false;
			ex.printStackTrace();
		}
		return bRet;
	}
	
	private boolean prepareXMLForXPath(){
		boolean bRet = false;
		try{
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
			xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new NamespaceContext() {
				@SuppressWarnings("rawtypes")
				@Override
			    public Iterator getPrefixes(String arg0) {
			        return null;
			    }
			
			    @Override
			    public String getPrefix(String arg0) {
			        return null;
			    }
			
			    @Override
			    public String getNamespaceURI(String arg0) {
			    	String nameSpaceValue = mapNamespaces.get(arg0).replace("\"", "");
			        return nameSpaceValue;
			    }
			 });
			DocumentBuilder builder = domFactory.newDocumentBuilder();
      		doc = builder.parse(new InputSource(new StringReader(wsdlContent)));
      		bRet = true;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return bRet;
	}
	
	private NodeList returnXMLNode(String path){
		NodeList nodeList = null;
		try{
			if (path.isEmpty()){
				throw new Exception("xpath is empty");
			}
			XPathExpression exprWSDL = xpath.compile(path);
	        Object result = exprWSDL.evaluate(doc, XPathConstants.NODESET);
	        nodeList = (NodeList) result;
		}catch(Exception ex){
			nodeList = null;
		}
		return nodeList;
	}
	
	private boolean readOperationDetails(String operationName){
		boolean bRet = false;
		try{
			NodeList nodeListWSDL = returnXMLNode("/wsdl:definitions");
			NodeList nodeListTypes = returnXMLNode("/wsdl:definitions/wsdl:types");
			if (nodeListWSDL == null || nodeListTypes == null){
				throw new Exception("Problem in WSDL content");
			}
			if (nodeListTypes.item(0) == null){
				throw new Exception("Problem in WSDL content");
			}
			
			NodeList nodesTypesChildren = nodeListTypes.item(0).getChildNodes();
	        int noOfChildren = nodesTypesChildren.getLength();
	         for (int i = 0;  i < noOfChildren; i ++){
	        	 if (nodesTypesChildren.item(i) == null ||
	        			 nodesTypesChildren.item(i).getAttributes() == null || 
	        			 nodesTypesChildren.item(i).getAttributes().getNamedItem("targetNamespace") == null){
	        		 continue;
	        	 }
	        	 
	        	 Node nodeChild = nodesTypesChildren.item(i).getAttributes().getNamedItem("targetNamespace");
	        	 Node nodeParent = nodeListWSDL.item(0).getAttributes().getNamedItem("targetNamespace");
	        	 
	        	 if (nodeChild == null || nodeParent == null){
	        		 break;
	        	 }
	        	 String strValChild = nodeChild.getNodeValue();
	        	 String strValParent = nodeParent.getNodeValue();
	        	 
	        	 if (strValChild.equals(strValParent)){
	        		 Node node = nodesTypesChildren.item(i);
	        		 String prefix = node.getPrefix();
					 String strPath = "/wsdl:definitions/wsdl:types/" + node.getNodeName() + "[@targetNamespace=" + "'" + strValParent + "'" + "]" + "/" +
	        				 prefix + ":" + "element[@name=" + "'" + operationName + "'"  + "]" + "/" + prefix + ":complexType/" + prefix + ":" + "sequence/" +
							 prefix + ":element";
	    	         NodeList nodesMethodParams = returnXMLNode(strPath);    
	    	         ArrayList<Parameter> params = new ArrayList<Parameter>();
	    	         for (int j = 0; j < nodesMethodParams.getLength(); j ++){
	    	        	 if (nodesMethodParams.item(j) == null || nodesMethodParams.item(j).getAttributes() == null || 
	    	        			 nodesMethodParams.item(j).getAttributes().getNamedItem("name") == null ||
	    	        			 nodesMethodParams.item(j).getAttributes().getNamedItem("type") == null){
	    	        		 continue;
	    	        	 }
	    	        	 String paramName = nodesMethodParams.item(j).getAttributes().getNamedItem("name").getNodeValue();
	    	        	 String paramType = nodesMethodParams.item(j).getAttributes().getNamedItem("type").getNodeValue();
	    	        	 if (paramType.indexOf(":") != -1){
	    	        		 String [] str = paramType.split(":");
	    	        		 if (str.length > 1){
	    	        			 paramType = str[1];
	    	        		 }
	    	        	 }
	    	        	 params.add(new Parameter(paramName, paramType));
	    	         }
	    	         mapMethodParams.put(operationName, params);
	        	 }
	        	 
	        	 bRet = true;
	         }
		}catch(Exception ex){
			bRet = false;
		}
		return bRet;
	}
	
    private void readAllNamespacesValue(){
    	
    	String findStr = "xmlns:";
    	int beginIndex = 0;
    	int endIndex = 0;
    	while(beginIndex != -1){
    		beginIndex = wsdlContent.indexOf(findStr, beginIndex);
    		endIndex = wsdlContent.indexOf("\"", beginIndex);
    		endIndex = wsdlContent.indexOf("\"", endIndex + 1);
    		if (endIndex == -1 || beginIndex == -1){
    			break;
    		}
    		
    	    String valueString = wsdlContent.substring(beginIndex  + findStr.length(), endIndex);
    	    if (valueString != null){
    	    	System.out.println(valueString);
    	    	String [] strArr = valueString.split("=");
    	    	if (strArr != null && strArr.length > 1){
    	    		mapNamespaces.put(strArr[0], strArr[1].trim());
    	    	}
    	    }
    	    beginIndex = endIndex;
    	}
    }
    
    public String createXML(String operationName, Map<String, String> mapValues){
    	String xmlRet = "";
    	try{
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	    factory.setNamespaceAware(true);
    	    DocumentBuilder builder = factory.newDocumentBuilder();
    	    Document doc = builder.newDocument();
    	    Element rootNode = doc.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soapenv:Envelope");
    	    String namespaceName = getNamespaceName();
    	    rootNode.setAttribute("xmlns:web", namespaceName);
    	    rootNode.appendChild(doc.createElement("soapenv:Header"));
    	    Element bodyElement = doc.createElement("soapenv:Body");
    	    rootNode.appendChild(bodyElement);
    	    Element operationElement = doc.createElement("web:" + operationName);
    	    bodyElement.appendChild(operationElement);
    	    ArrayList<Parameter> list = getParameterList(operationName);
    	    for(Parameter p : list){
    	    	Element paramNode = doc.createElement("web:" + p.getParamName());
    	    	paramNode.appendChild(doc.createTextNode(mapValues.get(p.getParamName())));
    	    	operationElement.appendChild(paramNode);
    	    }
    	    xmlRet = nodeToString(rootNode);
    	}catch(Exception ex){
    		xmlRet = null;
    		ex.printStackTrace();
    	}
    	return xmlRet;
    }
    
    private String nodeToString(Node node) throws TransformerException
	{
    	String str = "";
    	try{
    	    StringWriter buf = new StringWriter();
    	    Transformer xform = TransformerFactory.newInstance().newTransformer();
    	    xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    	    xform.transform(new DOMSource(node), new StreamResult(buf));	
    	    str = buf.toString();
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}

	    return str;
	}
	
	public void process(String url, OnTaskCompleted taskCompleted){
		try{
            final OnTaskCompleted listener = taskCompleted;
            selfListener = new OnTaskCompleted() {
                @Override
                public void OnTaskCompleted(TaskCompleteResponse taskCompleteResponse) {
                    readAllNamespacesValue();
                    if (!prepareXMLForXPath()){
                        Log.e("SC-APP", "Failed to prepare for XPATH");
                    }

                    if (listener != null){
                        listener.OnTaskCompleted(null);
                    }
                }
            };

            if (!read(url)){
                throw new Exception("Reading URL fails.");
            }

			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public ArrayList<Parameter> getParameterList(String opertionName){
		if (mapMethodParams.get(opertionName) == null){
			readOperationDetails(opertionName);
		}
		return mapMethodParams.get(opertionName);
	}
	
	public String getServiceName(){
		String serviceName = "";
		NodeList nodeListWSDL = returnXMLNode("/wsdl:definitions/wsdl:service");
		serviceName = nodeListWSDL.item(0).getAttributes().getNamedItem("name").getNodeValue();
		return serviceName;
	}
	
	public String getNamespaceName(){
		String value = "";
		NodeList nodeListWSDL = returnXMLNode("/wsdl:definitions");
		value = nodeListWSDL.item(0).getAttributes().getNamedItem("targetNamespace").getNodeValue();
		return value;
	}
	
	public String getSOAPAction(String operationName){
		String value = "";
		String strPath = "/wsdl:definitions/wsdl:binding/wsdl:operation" + "[@name=" + "'" + operationName + "'" + "]";
		NodeList nodeListBindingOperation = returnXMLNode(strPath);
		NodeList nodesTypesChildren = nodeListBindingOperation.item(0).getChildNodes();
		for (int  i = 0; i < nodesTypesChildren.getLength(); i ++){
			Node node = nodesTypesChildren.item(i);
			if (node.getAttributes() != null && node.getAttributes().getNamedItem("soapAction") != null){
				value = node.getAttributes().getNamedItem("soapAction").getNodeValue();
				break;
			}
		}
		return value;
	}
	
	public String getHttpLocation(){
		String value = "";
		String strPath = "/wsdl:definitions/wsdl:service/wsdl:port/http:address";
		NodeList nodeList = returnXMLNode(strPath);
		value = nodeList.item(0).getAttributes().getNamedItem("location").getNodeValue();
		return value;
	}
	
	public boolean write(String strUrl, String xml, String soapAction, OnTaskCompleted listener){
		boolean bRet = false;
		try{

            WriteSoapRequestTask writeSoapRequestTask = new WriteSoapRequestTask();
            writeSoapRequestTask.setListener(listener);
            writeSoapRequestTask.execute(strUrl, xml, soapAction);
    		bRet = true;
		} catch(Exception ex){
			wsdlContent = null;
			bRet = false;
			ex.printStackTrace();
		}
		return bRet;
	}

    @Override
    public void OnTaskCompleted(TaskCompleteResponse taskCompleteResponse) {
        this.wsdlContent = taskCompleteResponse.getData();
        if (selfListener != null){
            selfListener.OnTaskCompleted(null);
            selfListener = null;
        }
    }
}
