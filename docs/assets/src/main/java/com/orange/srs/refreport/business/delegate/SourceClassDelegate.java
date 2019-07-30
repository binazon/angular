package com.orange.srs.refreport.business.delegate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.SourceClassDAO;
import com.orange.srs.refreport.model.ReportInput;
import com.orange.srs.refreport.model.SourceClass;
import com.orange.srs.refreport.model.TO.SourceClassKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportInputKeyProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.SourceClassListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.SourceClassProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;

@Stateless
public class SourceClassDelegate {

	@EJB
	private SourceClassDAO sourceClassDao;

	public static SourceClassKeyTO getSourceClassKey(SourceClass sourceClass) {
		return new SourceClassKeyTO(sourceClass.getSourceClass());
	}

	public static SourceClassKeyTO getSourceClassProvisioningTOKey(
			SourceClassProvisioningTO sourceClassProvisioningTO) {
		return new SourceClassKeyTO(sourceClassProvisioningTO.sourceClass);
	}

	public SourceClass getSourceClassByKey(String sourceClazz) throws BusinessException {
		SourceClass sourceClass = sourceClassDao.findById(sourceClazz);
		if (sourceClass == null) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION
					+ ": SourceClass with key [sourceClazz=" + sourceClazz + "]");
		}
		return sourceClass;
	}

	public SourceClass createSourceClass(SourceClassProvisioningTO sourceClassProvisioningTO) {
		SourceClass sourceClass = new SourceClass();
		sourceClass.setSourceClass(sourceClassProvisioningTO.sourceClass);
		sourceClassDao.persistAndFlush(sourceClass);
		return sourceClass;
	}

	public void removeSourceClass(SourceClass sourceClass) {
		sourceClassDao.remove(sourceClass);
	}

	public void createSourceClassReportInputLink(SourceClass sourceClass, ReportInput reportInput) {
		if (reportInput.getReportInputSourceClass() != null) {
			reportInput.getReportInputSourceClass().getProducedInputs().remove(reportInput);
		}
		reportInput.setReportInputSourceClass(sourceClass);
		sourceClass.getProducedInputs().add(reportInput);
		sourceClassDao.persistAndFlush(sourceClass);
	}

	public void removeSourceClassReportInputLink(SourceClass sourceClass, ReportInput reportInput) {
		sourceClass.getProducedInputs().remove(reportInput);
		sourceClassDao.persistAndFlush(sourceClass);
	}

	public SourceClassListProvisioningTO getSourceClassListProvisioningTOSorted() {

		SourceClassListProvisioningTO sourceClassListProvisioningTO = new SourceClassListProvisioningTO();

		List<SourceClass> sourceClasses = sourceClassDao.findAll();
		for (SourceClass sourceClass : sourceClasses) {
			SourceClassProvisioningTO sourceClassProvisioningTO = new SourceClassProvisioningTO();
			sourceClassProvisioningTO.sourceClass = sourceClass.getSourceClass();
			for (ReportInput input : sourceClass.getProducedInputs()) {
				ReportInputKeyProvisioningTO reportInputKeyProvisioningTO = new ReportInputKeyProvisioningTO();
				reportInputKeyProvisioningTO.reportInputRef = input.getReportInputRef();
				reportInputKeyProvisioningTO.granularity = input.getGranularity();
				reportInputKeyProvisioningTO.sourceTimeUnit = input.getSourceTimeUnit();

				sourceClassProvisioningTO.reportInputKeyProvisioningTOs.add(reportInputKeyProvisioningTO);
			}
			ReportInputDelegate
					.sortReportInputKeyProvisioningTO(sourceClassProvisioningTO.reportInputKeyProvisioningTOs);
			sourceClassListProvisioningTO.sourceClassProvisioningTOs.add(sourceClassProvisioningTO);
		}

		sortSourceClassProvisioningTO(sourceClassListProvisioningTO.sourceClassProvisioningTOs);
		return sourceClassListProvisioningTO;
	}

	public List<SourceClass> getAllSourceClassSorted() {
		List<SourceClass> sourceClassList = sourceClassDao.findAll();
		Collections.sort(sourceClassList, new SourceClassComparator());
		return sourceClassList;
	}

	public static void sortSourceClassProvisioningTO(List<SourceClassProvisioningTO> sourceClassProvisioningTOs) {
		Collections.sort(sourceClassProvisioningTOs, new SourceClassProvisioningTOComparator());
	}

	private static class SourceClassComparator implements Comparator<SourceClass> {
		@Override
		public int compare(SourceClass firstObj, SourceClass secondObj) {
			return getSourceClassKey(firstObj).compareTo(getSourceClassKey(secondObj));
		}
	}

	private static class SourceClassProvisioningTOComparator implements Comparator<SourceClassProvisioningTO> {
		@Override
		public int compare(SourceClassProvisioningTO firstObj, SourceClassProvisioningTO secondObj) {
			return getSourceClassProvisioningTOKey(firstObj).compareTo(getSourceClassProvisioningTOKey(secondObj));
		}
	}
}
