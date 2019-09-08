package com.turvo.flashSaleDemo.service;

public interface CacheService<K,V> {
	    V get(final String prefix,final K key );

	    V getFromMemory(final String prefix,final K key );

	    void set(final String prefix,final K key , final V value);

	    void setInMemory(final String prefix,final K key , final V value);

	    void set(final String prefix,final K key , final V value, final Integer timeout);

	    void delete(final String prefix,final K key);

	    void deleteFromMemory(final String prefix,final K key );

}
