package org.openlmis.fulfillment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.service.dtos.RequisitionDtoEswShipment;
import org.openlmis.fulfillment.service.dtos.StatusLogEntryDtoEswShipment;
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
    private RequisitionServiceEswShipment requisitionService;

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
        HashMap<String, StatusLogEntryDtoEswShipment> statusChanges = new HashMap<>();
        UUID authorId = UUID.randomUUID();
        StatusLogEntryDtoEswShipment statusLogEntryDto = new StatusLogEntryDtoEswShipment();
        statusLogEntryDto.setAuthorId(authorId);
        statusChanges.put("INITIATED", statusLogEntryDto);
        RequisitionDtoEswShipment requisitionDto = new RequisitionDtoEswShipment();
        requisitionDto.setId(reqUUID);
        requisitionDto.setStatusChanges(statusChanges);
        UserDto userDto = new UserDto();
        userDto.setEmail("a@b.com");
        Mockito.when(requisitionService.findOne(reqUUID)).thenReturn(requisitionDto);
        Mockito.when(userReferenceDataService.findOne(authorId)).thenReturn(userDto);
        return new Shipment(order, null, null, null, null);
    }
}