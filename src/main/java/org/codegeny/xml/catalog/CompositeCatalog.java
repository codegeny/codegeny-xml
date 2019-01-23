package org.codegeny.xml.catalog;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Composite pattern for catalog. Allows multiple catalogs to be used as one. 
 * 
 * @author Xavier Dury
 */
public class CompositeCatalog implements Catalog {
	
	private final Collection<Catalog> catalogs = new LinkedList<>();

	public CompositeCatalog(Collection<? extends Catalog> catalogs) {
		this.catalogs.addAll(catalogs);
	}

	public Optional<String> mapURI(String uri) {
		return catalogs.stream().flatMap(c -> stream(c.mapURI(uri))).findFirst();
	}

	public Optional<String> mapExternalIdentifier(String publicId, String systemId) {
		return catalogs.stream().flatMap(c -> stream(c.mapExternalIdentifier(publicId, systemId))).findFirst();
	}
	
	private <T> Stream<T> stream(Optional<T> optional) {
		return optional.map(Stream::of).orElseGet(Stream::empty);
	}
}
