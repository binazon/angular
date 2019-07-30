package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.InputSource;
import com.orange.srs.refreport.model.SourceProxy;
import com.orange.srs.statcommon.model.TO.report.InputSourceConfigurationTO;
import com.orange.srs.statcommon.model.TO.report.InputSourceKey;

public interface InputSourceDAO extends Dao<InputSource, Long> {
	public List<InputSource> findByInputSourceKey(InputSourceKey key);

	public List<SourceProxy> findProxyIndex(InputSourceKey key, Long proxyId);

	public List<InputSourceConfigurationTO> findAllInputSourceKeys(String sourceClass);

	public List<InputSourceKey> findAllInputSourceKeys();
}
