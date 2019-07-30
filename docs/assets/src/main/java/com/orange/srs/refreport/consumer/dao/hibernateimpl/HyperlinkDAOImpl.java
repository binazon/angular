package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.HyperlinkDAO;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.model.Hyperlink;
import com.orange.srs.refreport.model.TO.provisioning.HyperlinkProvisioningTO;

@Stateless
public class HyperlinkDAOImpl extends AbstractJpaDao<Hyperlink, String> implements HyperlinkDAO {

	@Override
	public List<HyperlinkProvisioningTO> findAllHyperlinkProvisioningTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.hyperlinkProvisioningTOBuilder("h", "pt") + " FROM "
				+ getEntityName() + " h" + " LEFT OUTER JOIN h." + Hyperlink.FIELD_ADDITIONAL_FIELD_PARAM_TYPE + " pt";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}
}
