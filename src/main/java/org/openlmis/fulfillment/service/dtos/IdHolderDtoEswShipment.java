package org.openlmis.fulfillment.service.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdHolderDtoEswShipment {
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "IdHolderDtoEswShipment{" +
                "id=" + id +
                '}';
    }
}
