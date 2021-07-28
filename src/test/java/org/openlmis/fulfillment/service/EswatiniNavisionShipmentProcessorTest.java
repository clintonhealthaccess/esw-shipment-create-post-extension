package org.openlmis.fulfillment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openlmis.esw.extension.RequisitionDto;
import org.openlmis.esw.extension.RequisitionService;
import org.openlmis.esw.extension.StatusLogEntryDto;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.service.notification.NotificationService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EswatiniNavisionShipmentProcessorTest {

    @InjectMocks
    private EswatiniNavisionShipmentProcessor testingObject;

    @Mock
    private RequisitionService requisitionService;

    @Mock
    private UserReferenceDataService userReferenceDataService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DefaultShipmentCreatePostProcessor defaultShipmentCreatePostProcessor;

    private Shipment shipment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendNotificationToRequisitionAuthor() {
        Shipment shipment = setupShipment();
        assertTrue(testingObject.sendNotificationToRequisitionAuthor(shipment));
    }

    private Shipment setupShipment() {
        UUID reqUUID = UUID.randomUUID();
        Order order = new Order();
        order.setExternalId(reqUUID);
        HashMap<String, String> extraData = new HashMap<>();
        order.setExtraData(extraData);
        HashMap<String, StatusLogEntryDto> statusChanges = new HashMap<>();
        UUID authorId = UUID.randomUUID();
        StatusLogEntryDto statusLogEntryDto = new StatusLogEntryDto();
        statusLogEntryDto.setAuthorId(authorId);
        statusChanges.put("INITIATED", statusLogEntryDto);
        RequisitionDto requisitionDto = new RequisitionDto();
        requisitionDto.setId(reqUUID);
        requisitionDto.setStatusChanges(statusChanges);
        UserDto userDto = new UserDto();
        userDto.setEmail("a@b.com");
        Mockito.when(requisitionService.findOne(reqUUID)).thenReturn(requisitionDto);
        Mockito.when(userReferenceDataService.findOne(authorId)).thenReturn(userDto);
        Shipment shipment = new Shipment(order, null, null, null, null);
        return shipment;
    }
}