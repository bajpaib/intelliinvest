package com.intelliinvest.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intelliinvest.common.exception.IntelliInvestPropertyHoler;

public class ScheduledThreadPoolHelper {
	private static Logger LOGGER = LoggerFactory.getLogger(ScheduledThreadPoolHelper.class);
	private static ScheduledExecutorService executorService = null;
	
	public ScheduledThreadPoolHelper(){
		int count = new Integer(IntelliInvestPropertyHoler.getProperty("schedule.thread.pool.count")).intValue();
		executorService = Executors.newScheduledThreadPool(count);
	}

	@PreDestroy
	public void destroy() {
		executorService.shutdown();
		try {
			executorService.awaitTermination(1, TimeUnit.MINUTES);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	public static ScheduledExecutorService getScheduledExecutorService() {
		return executorService;
	}
}