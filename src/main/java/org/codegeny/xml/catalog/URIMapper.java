package org.codegeny.xml.catalog;

import java.util.Optional;

import javax.xml.transform.URIResolver;

/**
 * Mapper that will resolve a URI based on an other URI. 
 * 
 * @author Xavier Dury
 */
public interface URIMapper {
	
	default URIResolver decorate(URIResolver uriResolver) {
		return (href, base) -> uriResolver.resolve(mapURI(href).orElse(href), base);
	}
	
	Optional<String> mapURI(String uri);
}
