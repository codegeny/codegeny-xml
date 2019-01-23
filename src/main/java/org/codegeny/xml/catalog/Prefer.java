package org.codegeny.xml.catalog;

import java.util.Optional;

/**
 * A prefer value specifies whether a public id or system id should be preferred when mapping external identifiers. 
 * 
 * @author Xavier Dury
 */
public enum Prefer {

	PUBLIC, SYSTEM;

	public static Prefer getSystemDefault() {
		String value = System.getProperty(Prefer.class.getName().concat(".default"));
		if (value == null || value.isEmpty()) {
			return null;
		}
		return valueOf(value.toUpperCase());
	}
	
	public ExternalIdentifierMapper wrap(ExternalIdentifierMapper mapper) {
		return (p, s) -> (equals(PUBLIC) || s == null) ? mapper.mapExternalIdentifier(p, s) : Optional.empty();
	}
}