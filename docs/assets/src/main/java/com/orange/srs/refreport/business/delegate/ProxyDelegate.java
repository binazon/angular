package com.orange.srs.refreport.business.delegate;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.ProxyDAO;
import com.orange.srs.refreport.model.Proxy;
import com.orange.srs.refreport.model.TO.provisioning.ProxyListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ProxyProvisioningTO;
import com.orange.srs.refreport.technical.FSTSerializer;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.report.ProxyTO;

@Stateless
public class ProxyDelegate {

	@EJB
	private ProxyDAO proxyDao;

	public static URI getProxyKey(Proxy proxy) {
		return proxy.getUri();
	}

	public static URI getProxyProvisioningTOKey(ProxyProvisioningTO proxyProvisioningTO) {
		return proxyProvisioningTO.uri;
	}

	public Proxy getProxyById(Long proxyId) throws BusinessException {
		Proxy proxy = proxyDao.findById(proxyId);
		if (proxy == null) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION + ": Proxy with id " + proxyId);
		}
		return proxy;
	}

	public Proxy getProxyByKey(URI uri) throws BusinessException {
		byte[] bytes = null;
		try {
			bytes = FSTSerializer.serialize(uri);
		} catch (IOException e) {
			throw new RuntimeException("Unable to serialized URI: " + e.getMessage(), e);
		}
		List<Proxy> listProxy = proxyDao.findBy(Proxy.FIELD_URI, bytes);
		if (listProxy.isEmpty()) {
			throw new BusinessException(
					BusinessException.ENTITY_NOT_FOUND_EXCEPTION + ": Proxy with key [uri=" + uri + "]");
		}
		return listProxy.get(0);
	}

	public Proxy createProxy(ProxyTO proxyTO) {
		Proxy proxy = new Proxy();
		proxy.setName(proxyTO.name);
		proxy.setUri(proxyTO.uri);
		proxy.setVersion(proxyTO.version);
		proxy.setSsl(proxyTO.isSsl);
		proxyDao.persistAndFlush(proxy);
		return proxy;
	}

	public boolean updateProxyIfNecessary(Proxy proxy, ProxyProvisioningTO proxyProvisioningTO) {
		boolean updated = false;
		if (!proxyProvisioningTO.name.equals(proxy.getName()) || !proxyProvisioningTO.version.equals(proxy.getVersion())
				|| (proxyProvisioningTO.isSsl != proxy.isSsl())) {
			proxy.setName(proxyProvisioningTO.name);
			proxy.setVersion(proxyProvisioningTO.version);
			proxy.setSsl(proxyProvisioningTO.isSsl);
			proxyDao.persistAndFlush(proxy);
			updated = true;
		}
		return updated;
	}

	public void removeProxy(Proxy proxy) {
		proxyDao.remove(proxy);
	}

	public void checkProxyValidity(ProxyTO proxyTO) throws BusinessException {
		if (proxyTO.uri == null) {
			throw new BusinessException(BusinessException.WRONG_PARAMETER_EXCEPTION + " uri is null");
		} else if (proxyTO.name.replaceAll(" ", "").equals("") || proxyTO.name == null) {
			throw new BusinessException(BusinessException.WRONG_PARAMETER_EXCEPTION + " type is null or empty");
		}
	}

	public void checkProxyUnicity(ProxyTO proxyTO) throws BusinessException {
		byte[] bytes = null;
		try {
			bytes = FSTSerializer.serialize(proxyTO.uri);
		} catch (IOException e) {
			throw new RuntimeException("Unable to serialized URI: " + e.getMessage(), e);
		}
		long count = proxyDao.countBy(Proxy.FIELD_URI, bytes);
		if (count != 0) {
			throw new BusinessException(
					BusinessException.ENTITY_NOT_UNIQUE_EXCEPTION + " proxy with [uri=" + proxyTO.uri + "]");
		}
	}

	public List<Proxy> getAllProxySorted() {
		List<Proxy> proxyList = proxyDao.findAll();
		sortProxy(proxyList);
		return proxyList;
	}

	public ProxyListProvisioningTO getProxyListProvisioningTOSorted() {
		ProxyListProvisioningTO proxyListProvisioningTO = new ProxyListProvisioningTO();
		proxyListProvisioningTO.proxyProvisioningTOs = proxyDao.findAllProxyProvisioningTO();
		sortProxyProvisioningTO(proxyListProvisioningTO.proxyProvisioningTOs);
		return proxyListProvisioningTO;
	}

	public static void sortProxy(List<Proxy> proxies) {
		Collections.sort(proxies, new ProxyComparator());
	}

	public static void sortProxyProvisioningTO(List<ProxyProvisioningTO> proxyProvisioningTOs) {
		Collections.sort(proxyProvisioningTOs, new ProxyProvisioningTOComparator());
	}

	private static class ProxyComparator implements Comparator<Proxy> {
		@Override
		public int compare(Proxy firstObj, Proxy secondObj) {
			return getProxyKey(firstObj).compareTo(getProxyKey(secondObj));
		}
	}

	private static class ProxyProvisioningTOComparator implements Comparator<ProxyProvisioningTO> {
		@Override
		public int compare(ProxyProvisioningTO firstObj, ProxyProvisioningTO secondObj) {
			return getProxyProvisioningTOKey(firstObj).compareTo(getProxyProvisioningTOKey(secondObj));
		}
	}
}
