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

import java.util.Map;
import java.util.UUID;

import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.extension.point.ShipmentCreatePostProcessor;
import org.openlmis.fulfillment.service.notification.NotificationService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
  private RequisitionService requisitionService;

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
      RequisitionDto requisitionDto = requisitionService.findOne(requisitionId);
      if (requisitionDto != null) {
        Map<String, StatusLogEntryDto> statusChanges = requisitionDto.getStatusChanges();
        XLOGGER.info("Status changes {}", statusChanges);
        StatusLogEntryDto statusLogEntryDto = statusChanges.get("INITIATED");
        if (statusLogEntryDto != null) {
          UUID authorId = statusLogEntryDto.getAuthorId();
          UserDto authorDto = userReferenceDataService.findOne(authorId);
          XLOGGER.debug("Sending order shipped email to {}", authorDto.getEmail());
          String orderShippedText = String.format("Your order %s have been shipped", shipment.getOrder().getOrderCode());
          notificationService.notify(authorDto, orderShippedText, orderShippedText);
          return true;
        }
      }
    } catch (Exception e) {
      XLOGGER.error("Failed to send notification to the requisition author", e);
    }
    return false;
  }
}
