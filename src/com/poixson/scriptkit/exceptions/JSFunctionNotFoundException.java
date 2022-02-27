package com.poixson.scriptkit.exceptions;


public class JSFunctionNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;



	public JSFunctionNotFoundException(final String fileName, final String funcName,
			final Object funcObj) {
		super(
			String.format(
				"Function '%s' not found in script '%s' actual: %s",
				funcName, fileName,
				funcObj.getClass().toString()
			)
		);
	}

	public JSFunctionNotFoundException(final String fileName, final String funcName) {
		super(
			String.format(
				"Function '%s' not found in script '%s'",
				funcName, fileName
			)
		);
	}



}
