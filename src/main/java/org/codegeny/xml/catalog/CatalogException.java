package org.codegeny.xml.catalog;

/**
 * General catalog exception.
 * 
 * @author Xavier Dury
 */
public class CatalogException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CatalogException(String message, Throwable cause) {
		super(message, cause);
	}

	public CatalogException(String message) {
		super(message);
	}
}
