package com.orange.srs.refreport.business.delegate;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.ReportUserDAO;
import com.orange.srs.refreport.model.ReportUser;
import com.orange.srs.refreport.technical.exception.BusinessException;

@Stateless
public class ReportUserDelegate {

	@EJB
	private ReportUserDAO reportUserDAO;

	public ReportUser findReportUserByUserPk(Long srsId) throws BusinessException {
		ReportUser user = reportUserDAO.findById(srsId);

		if (user == null) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION + " : reportUser with pk " + srsId,
					BusinessException.ENTITY_NOT_FOUND_EXCEPTION_CODE);
		}

		return user;
	}

}
