package org.codegeny.xml.catalog;

import javax.xml.transform.URIResolver;

import org.w3c.dom.ls.LSResourceResolver;

/**
 * Default contract for Catalog which can be used to map external identifiers or URIs. Implementation of this interface MUST be thread-safe.
 * 
 * Catalogs can also be used to decorate standard resolvers like {@link URIResolver} or {@link LSResourceResolver}.
 * 
 * @see <a href="https://www.oasis-open.org/committees/download.php/14809/xml-catalogs.html">XML Catalogs</a>
 * @author Xavier Dury
 */
public interface Catalog extends ExternalIdentifierMapper, URIMapper {}
