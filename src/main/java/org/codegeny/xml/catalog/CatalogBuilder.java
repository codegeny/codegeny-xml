package org.codegeny.xml.catalog;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Fluent builder for XML Catalogs. With this class, Catalogs can be declared through code instead of XML.
 * 
 * @param <T> The return type for the {@link #end()} method. This type is needed to allow chaining in the fluent builder.
 * @author Xavier Dury
 */
public class CatalogBuilder<T> {
	
	private static class CatalogImpl implements Catalog {
		
		private final Collection<ExternalIdentifierMapper> externalIdentifierMappers;
		private final Collection<URIMapper> uriMappers;
		
		public CatalogImpl(Collection<ExternalIdentifierMapper> externalIdentifierMappers, Collection<URIMapper> uriMappers) {
			this.externalIdentifierMappers = externalIdentifierMappers;
			this.uriMappers = uriMappers;
		}
		
		public Optional<String> mapExternalIdentifier(String publicId, String systemId) {
			return externalIdentifierMappers.stream().flatMap(e -> stream(e.mapExternalIdentifier(publicId, systemId))).findFirst();
		}
		
		public Optional<String> mapURI(String uri) {
			return uriMappers.stream().flatMap(e -> stream(e.mapURI(uri))).findFirst();
		}
		
		private <T> Stream<T> stream(Optional<T> optional) {
			return optional.map(Stream::of).orElseGet(Stream::empty);
		}
	}
	
	private static class Prioritized<T> implements Comparable<Prioritized<T>>, Supplier<T> {
		
		private final int type, size, order;
		private final T wrapped;

		public Prioritized(int type, int size, int order, T wrapped) {
			this.type = type;
			this.size = size;
			this.order = order;
			this.wrapped = wrapped;
		}

		public int compareTo(Prioritized<T> that) {
			return IntStream.of(Integer.compare(this.type, that.type), Integer.compare(that.size, this.size), Integer.compare(this.order, that.order)).filter(i -> i != 0).findFirst().orElse(0);
		}
		
		public T get() {
			return wrapped;
		}
	}
	
	public static CatalogBuilder<Catalog> newCatalog(Prefer prefer, String base) {
		return new CatalogBuilder<>(prefer, base, CatalogImpl::new);
	}
	
	private final String base;
	private final Collection<Prioritized<ExternalIdentifierMapper>> externalIdentifierMappers;
	private final Prefer prefer;
	private final Supplier<T> supplier;
	private final Collection<Prioritized<URIMapper>> uriMappers;

	private CatalogBuilder(Prefer prefer, String base, BiFunction<Collection<ExternalIdentifierMapper>, Collection<URIMapper>, T> builder) {
		this(prefer, base, new TreeSet<>(), new TreeSet<>(), builder);
	}
	
	private CatalogBuilder(Prefer prefer, String base, Collection<Prioritized<ExternalIdentifierMapper>> externalIdentifierMappers, Collection<Prioritized<URIMapper>> uriMappers, BiFunction<Collection<ExternalIdentifierMapper>, Collection<URIMapper>, T> builder) {
		this(prefer, base, externalIdentifierMappers, uriMappers, () -> builder.apply(externalIdentifierMappers.stream().map(Supplier::get).collect(toList()), uriMappers.stream().map(Supplier::get).collect(toList())));
	}
	
	private CatalogBuilder(Prefer prefer, String base, Collection<Prioritized<ExternalIdentifierMapper>> externalIdentifierMappers, Collection<Prioritized<URIMapper>> uriMappers, Supplier<T> supplier) {
		this.prefer = prefer;
		this.base = base;
		this.externalIdentifierMappers = externalIdentifierMappers;
		this.uriMappers = uriMappers;
		this.supplier = supplier;
	}

	private CatalogBuilder<CatalogBuilder<T>> addCatalog(Prefer prefer, String base, Function<Catalog, CatalogBuilder<T>> function) {
		return new CatalogBuilder<>(prefer, base, (externalIdentifierMappers, uriMappers) -> function.apply(new CatalogImpl(externalIdentifierMappers, uriMappers)));
	}
	
	public CatalogBuilder<T> addDelegatePublic(String publicIdStartString, Catalog catalog) {
		return addExternalIdentifierMapper(6, publicIdStartString.length(), prefer().wrap((p, s) -> p != null && p.startsWith(publicIdStartString) ? catalog.mapExternalIdentifier(p, s) : Optional.empty()));
	}

	public CatalogBuilder<CatalogBuilder<T>> addDelegatePublic(String publicIdStartString, Prefer prefer, String base) {
		return addCatalog(prefer, base, catalog -> addDelegatePublic(publicIdStartString, catalog));
	}
	
	public CatalogBuilder<T> addDelegateSystem(String systemIdStartString, Catalog catalog) {
		return addExternalIdentifierMapper(4, systemIdStartString.length(), (p, s) -> s != null && s.startsWith(systemIdStartString) ? catalog.mapExternalIdentifier(p, s) : Optional.empty());
	}
	
	public CatalogBuilder<CatalogBuilder<T>> addDelegateSystem(String systemIdStartString, Prefer prefer, String base) {
		return addCatalog(prefer, base, catalog -> addDelegateSystem(systemIdStartString, catalog));
	}
	
	public CatalogBuilder<T> addDelegateURI(String uriStartString, Catalog catalog) {
		return addURIMapper(4, uriStartString.length(), u -> u != null && u.startsWith(uriStartString) ? catalog.mapURI(u) : Optional.empty());
	}

	public CatalogBuilder<CatalogBuilder<T>> addDelegateURI(String uriStartString, Prefer prefer, String base) {
		return addCatalog(prefer, base, catalog -> addDelegateURI(uriStartString, catalog));
	}
	
	private CatalogBuilder<T> addExternalIdentifierMapper(int type, int size, ExternalIdentifierMapper mapper) {
		externalIdentifierMappers.add(new Prioritized<>(type, size, externalIdentifierMappers.size(), mapper));
		return this;
	}
	
	public CatalogBuilder<CatalogBuilder<T>> addGroup(Prefer prefer, String base) {
		return new CatalogBuilder<>(prefer != null ? null : this.prefer, base != null ? base : this.base, externalIdentifierMappers, uriMappers, () -> this);
	}
	
	public CatalogBuilder<T> addNextCatalog(Catalog catalog) {
		return addExternalIdentifierMapper(7, 0, catalog).addURIMapper(5, 0, catalog);
	}
	
	public CatalogBuilder<CatalogBuilder<T>> addNextCatalog(Prefer prefer, String base) {
		return addCatalog(prefer, base, this::addNextCatalog);
	}
	
	public CatalogBuilder<T> addPublic(String publicId, String uri, String base) {
		return addExternalIdentifierMapper(6, 0, prefer().wrap((p, s) -> publicId.equals(p) ? Optional.of(rebase(uri, base)) : Optional.empty()));
	}

	public CatalogBuilder<T> addRewriteSystem(String systemIdStartString, String rewritePrefix) {
		return addExternalIdentifierMapper(2, systemIdStartString.length(), (p, s) -> s != null && s.startsWith(systemIdStartString) ? Optional.of(s.replace(systemIdStartString, rewritePrefix)) : Optional.empty());
	}

	public CatalogBuilder<T> addRewriteURI(String uriStartString, String rewritePrefix) {
		return addURIMapper(2, uriStartString.length(), u -> u != null && u.startsWith(uriStartString) ? Optional.of(u.replace(uriStartString, rewritePrefix)) : Optional.empty());
	}
	
	public CatalogBuilder<T> addSystem(String systemId, String uri, String base) {
		return addExternalIdentifierMapper(1, 0, (p, s) -> systemId.equals(s) ? Optional.of(rebase(uri, base)) : Optional.empty());
	}
	
	public CatalogBuilder<T> addSystemSuffix(String systemIdSuffix, String uri, String base) {
		return addExternalIdentifierMapper(3, systemIdSuffix.length(), (p, s) -> s != null && s.endsWith(systemIdSuffix) ? Optional.of(rebase(uri, base)) : Optional.empty());
	}
	
	public CatalogBuilder<T> addURI(String name, String uri, String base) {
		return addURIMapper(1, 0, u -> name.equals(u) ? Optional.of(rebase(uri, base)) : Optional.empty());
	}

	private CatalogBuilder<T> addURIMapper(int type, int size, URIMapper mapper) {
		uriMappers.add(new Prioritized<>(type, size, uriMappers.size(), mapper));
		return this;
	}
	
	public CatalogBuilder<T> addURISuffix(String uriSuffix, String uri, String base) {
		return addURIMapper(3, uriSuffix.length(), u -> u != null && u.endsWith(uriSuffix) ? Optional.of(rebase(uri, base)) : Optional.empty());
	}

	private String base(String base) {
		return coalesce(base, () -> this.base, "No 'base' attribute was specified but was needed to resolve a non-absolute URI");
	}
	
	private <V> V coalesce(V first, Supplier<V> second, String exceptionMessage) {
		if (first != null) {
			return first;
		}
		V secondValue = second.get();
		if (secondValue != null) {
			return secondValue;
		}
		throw new CatalogException(exceptionMessage);
	}
	
	public T end() {
		return supplier.get();
	}
	
	private Prefer prefer() {
		return coalesce(this.prefer, Prefer::getSystemDefault, "No 'prefer' attribute was specified");
	}
	
	private String rebase(String uri, String base) {
		return URI.create(uri).isAbsolute() ? uri : URI.create(base(base)).resolve(uri).toASCIIString();
	}
}
