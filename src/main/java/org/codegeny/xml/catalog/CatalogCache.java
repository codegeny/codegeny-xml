package org.codegeny.xml.catalog;

import java.util.function.Supplier;

/**
 * Contract for caching catalogs.
 * 
 * @author Xavier Dury
 */
public interface CatalogCache {

	CatalogCache NO_CACHE = (systemId, supplier) -> supplier.get();

	Catalog resolve(String systemId, Supplier<Catalog> supplier);
}
