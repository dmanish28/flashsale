package com.turvo.flashSaleDemo.serviceImpl;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.turvo.flashSaleDemo.service.CacheService;
import com.turvo.flashSaleDemo.util.Constants;

@Service
public class CacheServiceImpl<K,V> implements CacheService<K, V>{

	
	private final Map<String,V> concurrentMapCache = new ConcurrentHashMap<String, V>();
	
	@Autowired
    private RedisTemplate<String, V> redisTemplate;


	@Override
	public V get(String prefix, K key) {
		// TODO Auto-generated method stub
		return redisTemplate.opsForValue().get(prefix+"_"+key.toString());
	}

	@Override
	public V getFromMemory(String prefix, K key) {
		// TODO Auto-generated method stub
		return concurrentMapCache.get(prefix+"_"+key.toString());
	}

	@Override
	public void set(String prefix, K key, V value) {
		// TODO Auto-generated method stub
		this.set(prefix, key,value,Constants.DEFAULT_CACHE_TIMEOUT_IN_SECONDS);
	}

	@Override
	public void setInMemory(String prefix, K key, V value) {
		// TODO Auto-generated method stub
		concurrentMapCache.put(prefix+"_"+key.toString(), value);
	}

	@Override
	public void set(String prefix, K key, V value, Integer timeout) {
		// TODO Auto-generated method stub
		redisTemplate.opsForValue().set(prefix + "_" + key.toString(), value);
        redisTemplate.expire(prefix + "_" + key.toString(), timeout, TimeUnit.SECONDS);
	}

	@Override
	public void delete(String prefix, K key) {
		// TODO Auto-generated method stub
		redisTemplate.delete(prefix+ "_" +key.toString());
	}

	@Override
	public void deleteFromMemory(String prefix,K key) {
		// TODO Auto-generated method stub
		concurrentMapCache.remove(prefix+ "_" +key.toString());
	}

}
