package com.turvo.flashSaleDemo.service;

public interface LockService {

	String acquireLockWithTimeout(final String lockName, final Long acquireTimeout, final Long lockTimeout);

	Boolean releaseLock(final String lockName, final String identifier);

}
