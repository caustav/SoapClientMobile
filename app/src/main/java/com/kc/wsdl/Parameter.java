package com.kc.wsdl;

public class Parameter {
	
	private String paramName;
	private String paramType;

	public Parameter(String paramName, String paramType) {
		this.setParamName(paramName);
		this.setParamType(paramType);
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

}
