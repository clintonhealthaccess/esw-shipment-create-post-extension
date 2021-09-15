package org.openlmis.fulfillment.service;

import org.openlmis.fulfillment.service.referencedata.BaseReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.springframework.stereotype.Service;

@Service
public class ProcessingPeriodServiceEswShipment extends BaseReferenceDataService<ProcessingPeriodDto> {

    @Override
    protected String getUrl() {
        return "/api/processingPeriods/";
    }

    @Override
    protected Class<ProcessingPeriodDto> getResultClass() {
        return ProcessingPeriodDto.class;
    }

    @Override
    protected Class<ProcessingPeriodDto[]> getArrayResultClass() {
        return ProcessingPeriodDto[].class;
    }
}

