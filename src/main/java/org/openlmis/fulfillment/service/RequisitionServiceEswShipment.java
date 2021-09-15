/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation either
 * version 3 of the License or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.fulfillment.service;

import org.openlmis.fulfillment.service.dtos.RequisitionDtoEswShipment;
import org.openlmis.fulfillment.service.dtos.StatusLogEntryDtoEswShipment;
import org.openlmis.fulfillment.service.referencedata.BaseReferenceDataService;
import org.openlmis.fulfillment.service.request.RequestParameters;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RequisitionServiceEswShipment extends BaseReferenceDataService<RequisitionDtoEswShipment> {

    @Override
    protected String getUrl() {
        return "/api/requisitions/";
    }

    @Override
    protected Class<RequisitionDtoEswShipment> getResultClass() {
        return RequisitionDtoEswShipment.class;
    }

    @Override
    protected Class<RequisitionDtoEswShipment[]> getArrayResultClass() {
        return RequisitionDtoEswShipment[].class;
    }

    public List<RequisitionDtoEswShipment> searchAndFilter(RequestParameters parameters) {
        List<RequisitionDtoEswShipment> requisitions = getPage("search", parameters, null, HttpMethod.GET, getResultClass()).getContent();
        return filter(requisitions);
    }

    List<RequisitionDtoEswShipment> filter(List<RequisitionDtoEswShipment> requisitions) {
        return requisitions.stream().filter(r -> {
            Map<String, StatusLogEntryDtoEswShipment> statusChanges = r.getStatusChanges();
            return statusChanges.containsKey("SUBMITTED");
        }).collect(Collectors.toList());
    }
}
