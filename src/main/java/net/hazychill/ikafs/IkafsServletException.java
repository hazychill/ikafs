package net.hazychill.ikafs;

import javax.servlet.ServletException;

public class IkafsServletException extends ServletException {

	public IkafsServletException() {
		this(null);
	}
	
	public IkafsServletException(String message) {
		this(message, null);
	}
	

	public IkafsServletException(String message, Throwable innerException) {
		super(message, innerException);
	} 
}
