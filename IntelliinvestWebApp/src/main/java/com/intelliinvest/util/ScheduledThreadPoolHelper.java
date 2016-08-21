package com.intelliinvest.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;

import com.intelliinvest.common.IntelliInvestStore;

public class ScheduledThreadPoolHelper {
	private static Logger logger = Logger.getLogger(ScheduledThreadPoolHelper.class);
	private static ScheduledExecutorService executorService = null;

	@PostConstruct
	public void init() {
		int count = new Integer(IntelliInvestStore.properties.getProperty("schedule.thread.pool.count")).intValue();
		executorService = Executors.newScheduledThreadPool(count);
	}

	@PreDestroy
	public void destroy() {
		executorService.shutdown();
		try {
			executorService.awaitTermination(1, TimeUnit.MINUTES);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static ScheduledExecutorService getScheduledExecutorService() {
		return executorService;
	}
}