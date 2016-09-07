package com.intelliinvest.data.dao.cache;

import java.util.Collection;

public interface Cache<T> {
	void updateCache(Collection<T> items);
}
