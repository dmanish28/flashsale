package com.turvo.flashSaleDemo.serviceImpl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import com.turvo.flashSaleDemo.service.LockService;
import com.turvo.flashSaleDemo.util.Constants;

@Service
public class LockServiceImpl implements LockService {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Override
	public String acquireLockWithTimeout(final String lockName, final Long acquireTimeout, final Long lockTimeout) {

		final String identifier = UUID.randomUUID().toString();
		final String lockKey = getLockKey(lockName);
		final Long end = System.currentTimeMillis() + acquireTimeout;

		while (System.currentTimeMillis() < end) {
			if (redisTemplate.opsForValue().setIfAbsent(lockKey, identifier)) {
				redisTemplate.expire(lockKey, lockTimeout, TimeUnit.MILLISECONDS);
				return identifier;
			}
			if (redisTemplate.getExpire(lockKey) == -1) {
				redisTemplate.expire(lockKey, lockTimeout, TimeUnit.MILLISECONDS);
			}
			try {
				Thread.sleep(Constants.LOCK_ACQUIRE_LOOP_SLEEP);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		return null;
	}

	@Override
	public Boolean releaseLock(final String lockName, final String identifier) {
		final String lockKey = getLockKey(lockName);
		return redisTemplate.execute(new SessionCallback<Boolean>() {
			@Override
			public Boolean execute(RedisOperations operations) throws DataAccessException {
				Boolean flag = Boolean.TRUE;
				while (flag) {
					operations.watch(lockKey);
					if (identifier.equals(operations.opsForValue().get(lockKey))) {
						operations.multi();
						operations.delete(lockKey);
						List results = operations.exec();
						if (results != null) {
							return Boolean.TRUE;
						}
						try {
							Thread.sleep(Constants.LOCK_RELEASE_LOOP_SLEEP);
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
						}
						continue;
					}
					operations.unwatch();
					flag = Boolean.FALSE;
				}
				return Boolean.FALSE;
			}
		});
	}

	private static String getLockKey(final String lockName) {
		return Constants.LOCK_PREFIX + "_" + lockName;
	}
}
