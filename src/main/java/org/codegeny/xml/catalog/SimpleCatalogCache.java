package org.codegeny.xml.catalog;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * Simple implementation for catalog cache that uses a ConcurrentHashMap. 
 * 
 * @author Xavier Dury
 */
public class SimpleCatalogCache implements CatalogCache {
	
	private final ConcurrentMap<String, Catalog> cache = new ConcurrentHashMap<>();
	
	@Override
	public Catalog resolve(String systemId, Supplier<Catalog> supplier) {
		return cache.computeIfAbsent(systemId, key -> supplier.get());
	}
}