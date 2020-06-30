/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.service.link;


import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReportByPeriodRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionAspectServiceTest {
    private Xs2aTransactionsReportByPeriodRequest xs2aTransactionsReportByPeriodRequest;

    @InjectMocks
    private TransactionAspectService service;

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private Xs2aTransactionsReport transactionsReport;

    private ResponseObject responseObject;
    private JsonReader jsonReader = new JsonReader();


    @Test
    void getTransactionsReportByPeriod_successReport() {
        AspspSettings aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        xs2aTransactionsReportByPeriodRequest = jsonReader.getObjectFromFile("json/Xs2aTransactionsReportByPeriodRequest.json", Xs2aTransactionsReportByPeriodRequest.class);

        when(transactionsReport.getAccountReport()).thenReturn(new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null));

        responseObject = ResponseObject.<Xs2aTransactionsReport>builder()
                             .body(transactionsReport)
                             .build();
        ResponseObject actualResponse = service.getTransactionsReportByPeriod(responseObject, xs2aTransactionsReportByPeriodRequest);

        assertFalse(actualResponse.hasError());
    }

    @Test
    void getTransactionsReportByPeriod_withError_shouldAddTextErrorMessage() {
        xs2aTransactionsReportByPeriodRequest = jsonReader.getObjectFromFile("json/Xs2aTransactionsReportByPeriodRequest.json", Xs2aTransactionsReportByPeriodRequest.class);

        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();


        ResponseObject actualResponse = service.getTransactionsReportByPeriod(responseObject, xs2aTransactionsReportByPeriodRequest);

        assertTrue(actualResponse.hasError());
    }
}
