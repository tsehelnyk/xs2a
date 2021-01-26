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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.PaymentServicesHolder;
import de.adorsys.psd2.xs2a.service.PisMappersHolder;
import de.adorsys.psd2.xs2a.service.SpiService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAvailableScaMethodsResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.PSUAUTHENTICATED;

@Service
public class PisCancellationAuthorisationProcessorServiceImpl extends PaymentBaseAuthorisationProcessorService {

    private final SpiService spiService;
    private final PaymentServicesHolder paymentServicesHolder;
    private final Xs2aAuthorisationService xs2aAuthorisationService;

    public PisCancellationAuthorisationProcessorServiceImpl(List<PisScaAuthorisationService> services,
                                                            Xs2aAuthorisationService xs2aAuthorisationService,
                                                            PaymentServicesHolder paymentServicesHolder,
                                                            PisMappersHolder pisMappersHolder, SpiService spiService) {
        super(services, xs2aAuthorisationService, paymentServicesHolder, pisMappersHolder, spiService);
        this.spiService = spiService;
        this.paymentServicesHolder = paymentServicesHolder;
        this.xs2aAuthorisationService = xs2aAuthorisationService;
    }

    @Override
    public void updateAuthorisation(AuthorisationProcessorRequest request, AuthorisationProcessorResponse response) {
        PisScaAuthorisationService authorizationService = getService(request.getScaApproach());
        authorizationService.updateCancellationAuthorisation(request.getUpdateAuthorisationRequest(), response);
    }

    @Override
    public AuthorisationProcessorResponse doScaReceived(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        return request.isUpdatePsuIdentification() && authorisation.getChosenScaApproach() != ScaApproach.DECOUPLED
                   ? applyIdentification(authorisationProcessorRequest)
                   : applyAuthorisation(authorisationProcessorRequest);
    }

    @Override
    SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePayment(Authorisation authorisation,
                                                                                     SpiPayment payment, SpiScaConfirmation spiScaConfirmation,
                                                                                     SpiContextData contextData, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return paymentServicesHolder.verifyScaAuthorisationAndCancelPaymentWithResponse(contextData, spiScaConfirmation, payment, spiAspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider, SpiPsuData spiPsuData, SpiContextData contextData, String authorisationId) {
        return paymentServicesHolder.authorisePsu(contextData, authorisationId, spiPsuData, request.getPassword(),
                                                  payment, aspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider, SpiContextData contextData) {
        return paymentServicesHolder.requestAvailableScaMethods(contextData, payment, aspspConsentDataProvider);
    }

    @Override
    Xs2aUpdatePisCommonPaymentPsuDataResponse executePaymentWithoutSca(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                       PsuIdData psuData, PaymentType paymentType, SpiPayment payment,
                                                                       SpiContextData contextData, ScaStatus resultScaStatus) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        final SpiAspspConsentDataProvider aspspConsentDataProvider = spiService.getSpiAspspDataProviderFor(paymentId);

        writeInfoLog(authorisationProcessorRequest, psuData, "Available SCA methods is empty.");
        SpiResponse<SpiResponse.VoidResponse> executePaymentResponse = paymentServicesHolder.cancelPaymentWithoutSca(contextData, payment, aspspConsentDataProvider);

        if (executePaymentResponse.hasError()) {
            ErrorHolder errorHolder = spiService.mapToErrorHolder(executePaymentResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Cancel payment without SCA has failed.");
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        paymentServicesHolder.updatePaymentStatus(paymentId, TransactionStatus.CANC);
        paymentServicesHolder.updateInternalPaymentStatus(paymentId, InternalPaymentStatus.CANCELLED_FINALISED);

        return Xs2aUpdatePisCommonPaymentPsuDataResponse
                   .buildWithCurrencyConversionInfo(ScaStatus.FINALISED, paymentId, authorisationId, psuData, null);
    }

    @Override
    boolean needProcessExemptedSca(PaymentType paymentType, boolean isScaExempted) {
        return false;
    }

    @Override
    void updatePaymentDataByPaymentResponse(String paymentId, SpiResponse<SpiPaymentExecutionResponse> spiResponse) {
        paymentServicesHolder.updatePaymentStatus(paymentId, TransactionStatus.CANC);
        paymentServicesHolder.updateInternalPaymentStatus(paymentId, InternalPaymentStatus.CANCELLED_FINALISED);
    }

    @Override
    Xs2aUpdatePisCommonPaymentPsuDataResponse applyIdentification(AuthorisationProcessorRequest authorisationProcessorRequest) {

        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String paymentId = request.getPaymentId();
        String authorisationId = request.getAuthorisationId();
        PsuIdData psuData = request.getPsuData();

        if (!isPsuDataCorrect(paymentId, psuData)) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_401)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.UNAUTHORIZED_NO_PSU))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Apply Identification when update payment PSU data has failed. PSU credentials invalid.");
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        return super.applyIdentification(authorisationProcessorRequest);
    }

    @Override
    SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiPayment payment, String authenticationMethodId, SpiContextData spiContextData, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return paymentServicesHolder.requestAuthorisationCode(spiContextData, authenticationMethodId, payment, spiAspspConsentDataProvider);
    }

    @Override
    Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledApproach(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment, String authenticationMethodId) {
        return paymentServicesHolder.proceedDecoupledCancellation(request, payment, authenticationMethodId);
    }

    @Override
    Xs2aUpdatePisCommonPaymentPsuDataResponse getXs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus scaStatus, SpiPayment payment, SpiContextData contextData, SpiAspspConsentDataProvider spiAspspConsentDataProvider, PsuIdData psuData, String authorisationId) {


        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(scaStatus, payment.getPaymentId(), authorisationId, psuData);
    }

    @Override
    Xs2aUpdatePisCommonPaymentPsuDataResponse buildUpdateResponseWhenScaMethodsAreMultiple(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                           PsuIdData psuData,
                                                                                           List<AuthenticationObject> spiScaMethods,
                                                                                           SpiPayment payment,
                                                                                           SpiAspspConsentDataProvider aspspConsentDataProvider,
                                                                                           SpiContextData contextData) {
        xs2aAuthorisationService.saveAuthenticationMethods(request.getAuthorisationId(), spiScaMethods);

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse
                                                                 (PSUAUTHENTICATED,
                                                                  request.getPaymentId(),
                                                                  request.getAuthorisationId(),
                                                                  psuData);
        response.setAvailableScaMethods(spiScaMethods);
        return response;
    }

    private boolean isPsuDataCorrect(String paymentId, PsuIdData psuData) {
        List<PsuIdData> psuIdDataList = paymentServicesHolder.getPsuDataByPaymentId(paymentId);

        return psuIdDataList.stream()
                   .anyMatch(psu -> psu.contentEquals(psuData));
    }
}
