package com.orange.srs.refreport.technical.concurrent;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.statcommon.technical.concurrent.AbstractExecutorService;

@Stateless
@LocalBean
public class ReportingGroupPartitionsExecutorService extends AbstractExecutorService {

	@Override
	protected int getNbThreadPool() {
		return Configuration.groupPartitionsStatusThreadsNumber;
	}

}
