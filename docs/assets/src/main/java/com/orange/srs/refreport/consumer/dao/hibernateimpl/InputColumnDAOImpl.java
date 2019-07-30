package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.InputColumnDAO;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.model.InputColumn;
import com.orange.srs.refreport.model.InputFormat;
import com.orange.srs.refreport.model.TO.provisioning.InputColumnProvisioningTO;
import com.orange.srs.statcommon.technical.ModelUtils;

@Stateless
public class InputColumnDAOImpl extends AbstractJpaDao<InputColumn, Long> implements InputColumnDAO {

	public List<InputColumnProvisioningTO> findAllInputColumnProvisioningTOForInputFormat(String inputFormatType) {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.inputColumnProvisioningTOBuilder("ic") + " FROM "
				+ getEntityName() + " ic, " + ModelUtils.getEntityNameForClass(InputFormat.class) + " if" + " WHERE if."
				+ InputFormat.FIELD_FORMAT_TYPE + "=:inputFormatType " + " AND ic MEMBER OF if."
				+ InputFormat.FIELD_COLUMNS;
		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("inputFormatType", inputFormatType);
		// Caution : a cast to InputColumnProvisioningTO is done here, which means that
		// the default constructor is called with all fields as paramters
		// -> Make sure the field order in the constructor is the same as the order in
		// the select above
		return query.getResultList();
	}

}
