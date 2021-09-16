package org.openlmis.fulfillment.service;

import org.openlmis.fulfillment.service.dtos.RequisitionDtoEswShipment;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.openlmis.fulfillment.service.request.RequestParameters;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Component
public class ESwatiniAMCNotifier {

    private static final XLogger XLOGGER = XLoggerFactory.getXLogger(ESwatiniAMCNotifier.class);

    @Autowired
    private ProcessingPeriodServiceEswShipment processingPeriodService;

    @Autowired
    private RequisitionServiceEswShipment requisitionService;

    @Value("${time.zoneId}")
    private String timeZoneId;

    @Value("${amc.alert.cron}")
    private String amcAlertCron;

    @PostConstruct
    private void postConstruct() {
        XLOGGER.debug("amc.alert.cron is {}", amcAlertCron);
    }

    @Scheduled(cron = "${amc.alert.cron}", zone = "${time.zoneId}")
    public void cronJob() {
        XLOGGER.debug("INIT amcAlertCron");
        LocalDate currentDate = LocalDate.now(ZoneId.of(timeZoneId));
        LocalDate d = LocalDate.of(2017, 5, 1);
        sendAMCAlert(d);
    }

    private void sendAMCAlert(LocalDate currentDate) {
        try {
            XLOGGER.debug("INIT sendAMCAlert");
            ProcessingPeriodDto processingPeriodLastMonth = getProcessingPeriod(currentDate.minusMonths(1));
            XLOGGER.debug("p1 id: {}", processingPeriodLastMonth.getId());
            ProcessingPeriodDto processingPeriodLastMinusOneMonth = getProcessingPeriod(currentDate.minusMonths(2));
            XLOGGER.debug("p2 id: {}", processingPeriodLastMinusOneMonth.getId());
            ProcessingPeriodDto processingPeriodLastMinusTwoMonths = getProcessingPeriod(currentDate.minusMonths(3));
            XLOGGER.debug("p3 id: {}", processingPeriodLastMinusTwoMonths.getId());
            List<RequisitionDtoEswShipment> requisitions = requisitionService.searchAndFilter(getSearchParams(processingPeriodLastMonth));
            XLOGGER.debug("req size: {}", requisitions.size());
            for (RequisitionDtoEswShipment r : requisitions) {
                XLOGGER.debug("r id: {}", r.getId());
                RequisitionDtoEswShipment pastReqMinusOne = getPastRequisition(r, processingPeriodLastMinusOneMonth);
                RequisitionDtoEswShipment pastReqMinusTwo = getPastRequisition(r, processingPeriodLastMinusTwoMonths);
                XLOGGER.debug("Req Id: {}, Req -1 Id: {}, Req -2 Id: {}", r.getId(), pastReqMinusOne.getId(), pastReqMinusTwo.getId());
            }
        } catch(RuntimeException runtimeException) {
            XLOGGER.debug("Error sending amc alert", runtimeException.getCause());
        }
    }

    private RequestParameters getSearchParams(ProcessingPeriodDto processingPeriod) {
        Map<String, Object> params = new HashMap<>();
        params.put("processingPeriod", processingPeriod.getId());
        return RequestParameters.of(params);
    }

    private RequestParameters getSearchParams(ProcessingPeriodDto processingPeriod, UUID programId, UUID facilityId) {
        Map<String, Object> params = new HashMap<>();
        params.put("processingPeriod", processingPeriod.getId());
        params.put("program", programId);
        params.put("facility", facilityId);
        return RequestParameters.of(params);
    }

    RequisitionDtoEswShipment getPastRequisition(RequisitionDtoEswShipment requisition, ProcessingPeriodDto pastProcessingPeriod) {
        RequestParameters searchParams = getSearchParams(pastProcessingPeriod,
                requisition.getProgram().getId(),
                requisition.getFacility().getId());
        return requisitionService
                .searchAndFilter(searchParams)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("past requisition not found"));
    }

    ProcessingPeriodDto getProcessingPeriod(LocalDate currentDate) {
        Page<ProcessingPeriodDto> page = processingPeriodService.getPage(RequestParameters.init());
        Optional<ProcessingPeriodDto> first = page.stream().filter(dto -> isWithinRange(currentDate, dto.getStartDate(), dto.getEndDate())).findFirst();
        return first.orElseThrow(() -> new RuntimeException("Processing Period not found"));
    }

    boolean isWithinRange(LocalDate testDate, LocalDate startDate, LocalDate endDate) {
        return !(testDate.isBefore(startDate) || testDate.isAfter(endDate));
    }
}
