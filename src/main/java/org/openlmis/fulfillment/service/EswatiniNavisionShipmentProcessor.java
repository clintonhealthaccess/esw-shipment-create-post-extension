/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2021 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.fulfillment.service;

import org.apache.commons.lang.text.StrSubstitutor;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.extension.point.ShipmentCreatePostProcessor;
import org.openlmis.fulfillment.service.notification.NotificationService;
import org.openlmis.fulfillment.service.referencedata.*;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component(value = "EswatiniNavisionShipmentProcessor")
public class EswatiniNavisionShipmentProcessor implements ShipmentCreatePostProcessor {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(
      EswatiniNavisionShipmentProcessor.class);

  @Autowired
  private DefaultShipmentCreatePostProcessor defaultShipmentCreatePostProcessor;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private RequisitionServiceEswShipment requisitionService;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private EswMessageService eswMessageService;

  @Override
  public void process(Shipment shipment) {
    XLOGGER.entry(shipment);
    Profiler profiler = new Profiler("ESWATINI_NAVISION_SHIPMENT_PROCESSOR");
    profiler.setLogger(XLOGGER);

    Map<String, String> orderExtraData = shipment.getOrder().getExtraData();
    String externallyFulfilled = orderExtraData.getOrDefault("externallyFulfilled", "false");
    if (externallyFulfilled.equals("true")) {
      XLOGGER.debug("Order was externally fulfilled, skip post-shipment processing");
    } else {
      XLOGGER.debug("Order was not externally fulfilled, do regular post-shipment processing");
      defaultShipmentCreatePostProcessor.process(shipment);
    }

    sendNotificationToRequisitionAuthor(shipment);

    profiler.stop().log();
    XLOGGER.exit();
  }

  protected boolean sendNotificationToRequisitionAuthor(Shipment shipment) {
    try {
      UUID requisitionId = shipment.getOrder().getExternalId();
      XLOGGER.info("Requisition Id: {}", requisitionId);
      RequisitionDtoEswShipment requisitionDto = requisitionService.findOne(requisitionId);
      if (requisitionDto != null) {
        XLOGGER.info("Requisition Dto: {}", requisitionDto);
        Map statusChanges = requisitionDto.getStatusChanges();
        XLOGGER.info("Status changes {}", statusChanges);
        Map statusLogEntry = (Map) statusChanges.get("INITIATED");
        if (statusLogEntry != null) {
          String authorId = (String) statusLogEntry.get("authorId");
          UserDto authorDto = userReferenceDataService.findOne(UUID.fromString(authorId));
          XLOGGER.debug("Sending order shipped email to user: {}", authorDto.getId());
          String orderCode = shipment.getOrder().getOrderCode();
          ProgramDto programDto = programReferenceDataService.findOne(shipment.getProgramId());
          FacilityDto supplyingFacility = facilityReferenceDataService.findOne(shipment.getSupplyingFacilityId());

          Map<String, String> valuesMap = new HashMap();
          valuesMap.put("orderCode", orderCode);
          valuesMap.put("programName", programDto.getName());
          valuesMap.put("supplyingFacility", supplyingFacility.getName());

          StrSubstitutor strSubstitutor = new StrSubstitutor(valuesMap);
          String subject = strSubstitutor.replace(eswMessageService.getMessage("fulfillment.email.orderShipped.subject"));
          String body = strSubstitutor.replace(eswMessageService.getMessage("fulfillment.email.orderShipped.body"));

          notificationService.notify(authorDto, subject, body);
          return true;
        }
      }
    } catch (Exception e) {
      XLOGGER.debug("Failed to send notification to the requisition author", e);
    }
    return false;
  }
}
