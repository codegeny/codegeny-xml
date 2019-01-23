package org.codegeny.xml.catalog;

import java.util.Optional;

import org.w3c.dom.ls.LSResourceResolver;

/**
 * Mapper that will resolve a URI based on either a publicId or a systemId or both.  
 * 
 * @author Xavier Dury
 */
public interface ExternalIdentifierMapper {
	
	default LSResourceResolver decorate(LSResourceResolver lsResourceResolver) {
		return (type, namespaceURI, publicId, systemId, baseURI) -> lsResourceResolver.resolveResource(type, namespaceURI, publicId, mapExternalIdentifier(publicId, systemId).orElse(systemId), baseURI);
	}
	
	Optional<String> mapExternalIdentifier(String publicId, String systemId);
}
