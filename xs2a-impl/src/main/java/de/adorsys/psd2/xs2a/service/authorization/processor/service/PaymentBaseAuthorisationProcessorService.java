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

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
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
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.*;

abstract class PaymentBaseAuthorisationProcessorService extends BaseAuthorisationProcessorService {

    private static final String EMBEDDED_SELECTING_SCA_METHOD_FAILED_MSG = "Proceed embedded approach when performs authorisation depending on selected SCA method has failed.";

    private final List<PisScaAuthorisationService> services;
    private final Xs2aAuthorisationService xs2aAuthorisationService;
    private final PaymentServicesHolder paymentServicesHolder;
    private final PisMappersHolder pisMappersHolder;
    private final SpiService spiService;

    protected PaymentBaseAuthorisationProcessorService(List<PisScaAuthorisationService> services,
                                                       Xs2aAuthorisationService xs2aAuthorisationService,
                                                       PaymentServicesHolder paymentServicesHolder,
                                                       PisMappersHolder pisMappersHolder,
                                                       SpiService spiService) {
        this.services = services;
        this.xs2aAuthorisationService = xs2aAuthorisationService;
        this.paymentServicesHolder = paymentServicesHolder;
        this.pisMappersHolder = pisMappersHolder;
        this.spiService = spiService;
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuIdentified(AuthorisationProcessorRequest authorisationProcessorRequest) {
        return doScaReceived(authorisationProcessorRequest);
    }

    @Override
    public AuthorisationProcessorResponse doScaFinalised(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();

        SpiResponse<SpiCurrencyConversionInfo> spiConversionResponse = getSpipaymentServicesHolderResponse(authorisationProcessorRequest, request);

        return Xs2aUpdatePisCommonPaymentPsuDataResponse
                   .buildWithCurrencyConversionInfo(FINALISED,
                                                    request.getBusinessObjectId(),
                                                    request.getAuthorisationId(),
                                                    request.getPsuData(),
                                                    pisMappersHolder
                                                        .toXs2aCurrencyConversionInfo(spiConversionResponse.getPayload()));
    }

    private SpiResponse<SpiCurrencyConversionInfo> getSpipaymentServicesHolderResponse(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                                           Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        PsuIdData psuData = extractPsuIdData(request, authorisation);
        String authorisationId = request.getAuthorisationId();
        SpiPayment payment = getSpiPayment(request.getPaymentId());
        SpiContextData contextData = spiService.provideWithPsuIdData(psuData);
        SpiAspspConsentDataProvider aspspConsentDataProvider = spiService.getSpiAspspDataProviderFor(request.getPaymentId());

        return paymentServicesHolder.getCurrencyConversionInfo(contextData, payment, authorisationId, aspspConsentDataProvider);
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuAuthenticated(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();

        SpiPayment payment = getSpiPayment(request.getPaymentId());

        if (isDecoupledApproach(request.getAuthorisationId(), request.getAuthenticationMethodId())) {
            xs2aAuthorisationService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return proceedDecoupledApproach(request, payment, request.getAuthenticationMethodId());
        }

        return proceedEmbeddedApproach(authorisationProcessorRequest, payment);
    }

    @Override
    public AuthorisationProcessorResponse doScaMethodSelected(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();

        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        PsuIdData psuData = extractPsuIdData(request, authorisation);
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        // we need to get decrypted payment ID
        String internalId = paymentServicesHolder.getInternalPaymentIdByEncryptedString(paymentId);
        SpiScaConfirmation spiScaConfirmation = pisMappersHolder.buildSpiScaConfirmation(request, authorisation.getParentId(), internalId, psuData);

        SpiContextData contextData = spiService.provideWithPsuIdData(psuData);
        SpiAspspConsentDataProvider aspspConsentDataProvider = spiService.getSpiAspspDataProviderFor(paymentId);

        SpiPayment payment = getSpiPayment(request.getPaymentId());

        SpiResponse<SpiPaymentExecutionResponse> spiResponse = verifyScaAuthorisationAndExecutePayment(authorisation, payment,
                                                                                                       spiScaConfirmation,
                                                                                                       contextData,
                                                                                                       aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiService.mapToErrorHolder(spiResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Verify SCA authorisation and execute payment has failed.");

            SpiPaymentExecutionResponse spiPaymentResponse = spiResponse.getPayload();
            if (spiPaymentResponse != null && spiPaymentResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.ATTEMPT_FAILURE) {
                return new Xs2aUpdatePisCommonPaymentPsuDataResponse(authorisationProcessorRequest.getScaStatus(), errorHolder, paymentId, authorisationId, psuData);
            }

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                xs2aAuthorisationService.updateAuthorisationStatus(authorisationId, FAILED);
            }
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        updatePaymentDataByPaymentResponse(paymentId, spiResponse);

        return getXs2aUpdatePisCommonPaymentPsuDataResponse(FINALISED, payment, contextData, aspspConsentDataProvider, psuData, authorisationId);
    }

    abstract void updatePaymentDataByPaymentResponse(String paymentId, SpiResponse<SpiPaymentExecutionResponse> spiResponse);

    abstract SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiPayment payment, String authenticationMethodId,
                                                                              SpiContextData spiContextData,
                                                                              SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePayment(Authorisation authorisation,
                                                                                              SpiPayment payment,
                                                                                              SpiScaConfirmation spiScaConfirmation,
                                                                                              SpiContextData contextData,
                                                                                              SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment,
                                                                   SpiAspspConsentDataProvider aspspConsentDataProvider, SpiPsuData spiPsuData,
                                                                   SpiContextData contextData, String authorisationId);

    abstract SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiPayment payment,
                                                                                    SpiAspspConsentDataProvider aspspConsentDataProvider,
                                                                                    SpiContextData contextData);

    abstract Xs2aUpdatePisCommonPaymentPsuDataResponse executePaymentWithoutSca(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                                PsuIdData psuData, PaymentType paymentType, SpiPayment payment,
                                                                                SpiContextData contextData, ScaStatus resultScaStatus);

    abstract Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledApproach(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                SpiPayment payment, String authenticationMethodId);


    abstract boolean needProcessExemptedSca(PaymentType paymentType, boolean isScaExempted);

    PisScaAuthorisationService getService(ScaApproach scaApproach) {
        return services.stream().filter(s -> s.getScaApproachServiceType() == scaApproach).findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Pis cancellation authorisation service was not found for approach " + scaApproach));
    }

    Xs2aUpdatePisCommonPaymentPsuDataResponse applyAuthorisation(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        PsuIdData psuData = extractPsuIdData(request, authorisation);
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();
        SpiPayment payment = getSpiPayment(paymentId);

        if (authorisation.getChosenScaApproach() == ScaApproach.DECOUPLED) {
            request.setPsuData(psuData);
        }

        SpiAspspConsentDataProvider aspspConsentDataProvider = spiService.getSpiAspspDataProviderFor(paymentId);

        SpiPsuData spiPsuData = pisMappersHolder.mapToSpiPsuData(psuData);
        SpiContextData contextData = spiService.provideWithPsuIdData(psuData);

        SpiResponse<SpiPsuAuthorisationResponse> authPsuResponse = authorisePsu(request, payment, aspspConsentDataProvider, spiPsuData, contextData, authorisationId);
        if (authPsuResponse.hasError()) {
            ErrorHolder errorHolder = spiService.mapToErrorHolder(authPsuResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Authorise PSU when apply authorisation has failed.");
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        SpiPsuAuthorisationResponse psuAuthorisationResponse = authPsuResponse.getPayload();

        if (psuAuthorisationResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.FAILURE) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_401)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "PSU authorisation failed due to incorrect credentials.");
            xs2aAuthorisationService.updateAuthorisationStatus(authorisationId, FAILED);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        PaymentType paymentType = request.getPaymentService();
        if (needProcessExemptedSca(paymentType, psuAuthorisationResponse.isScaExempted())) {
            writeInfoLog(authorisationProcessorRequest, psuData, "SCA was exempted for the payment after AuthorisationSpi#authorisePsu.");
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, paymentType, payment, contextData, EXEMPTED);
        }

        if (authorisation.getChosenScaApproach() == ScaApproach.DECOUPLED) {
            return proceedDecoupledApproach(request, payment);
        }

        SpiResponse<SpiAvailableScaMethodsResponse> availableScaMethodsResponse = requestAvailableScaMethods(payment, aspspConsentDataProvider, contextData);

        if (availableScaMethodsResponse.hasError()) {
            ErrorHolder errorHolder = spiService.mapToErrorHolder(availableScaMethodsResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Request available SCA methods has failed.");
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        SpiAvailableScaMethodsResponse availableScaMethods = availableScaMethodsResponse.getPayload();

        if (needProcessExemptedSca(paymentType, availableScaMethods.isScaExempted())) {
            writeInfoLog(authorisationProcessorRequest, psuData, "SCA was exempted for the payment after AuthorisationSpi#requestAvailableScaMethods.");
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, paymentType, payment, contextData, EXEMPTED);
        }

        List<AuthenticationObject> spiScaMethods = availableScaMethods.getAvailableScaMethods();

        return processScaMethods(authorisationProcessorRequest, psuData, paymentType, payment, aspspConsentDataProvider,
                                 contextData, spiScaMethods);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse processScaMethods(@NotNull AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                        PsuIdData psuData, PaymentType paymentType, SpiPayment payment,
                                                                        SpiAspspConsentDataProvider aspspConsentDataProvider, SpiContextData contextData,
                                                                        List<AuthenticationObject> spiScaMethods) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();

        if (CollectionUtils.isEmpty(spiScaMethods)) {
            writeInfoLog(authorisationProcessorRequest, psuData, "Available SCA methods is empty.");
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, paymentType, payment, contextData, FINALISED);
        } else if (isSingleScaMethod(spiScaMethods)) {
            return buildUpdateResponseWhenScaMethodIsSingle(authorisationProcessorRequest, psuData, payment, aspspConsentDataProvider, contextData, spiScaMethods);
        } else if (isMultipleScaMethods(spiScaMethods)) {
            return buildUpdateResponseWhenScaMethodsAreMultiple(request, psuData, spiScaMethods, payment, aspspConsentDataProvider, contextData);
        }

        SpiResponse<SpiCurrencyConversionInfo> conversionInfoSpiResponse =
            paymentServicesHolder.getCurrencyConversionInfo(
                contextData, payment, request.getAuthorisationId(), aspspConsentDataProvider
            );

        SpiCurrencyConversionInfo spiCurrencyConversionInfo = conversionInfoSpiResponse.getPayload();

        writeInfoLog(authorisationProcessorRequest, psuData, "Apply authorisation when update payment PSU data set SCA status failed.");
        return Xs2aUpdatePisCommonPaymentPsuDataResponse
                   .buildWithCurrencyConversionInfo(FAILED,
                                                    request.getPaymentId(),
                                                    request.getAuthorisationId(),
                                                    psuData,
                                                    pisMappersHolder
                                                        .toXs2aCurrencyConversionInfo(spiCurrencyConversionInfo));
    }

    Xs2aUpdatePisCommonPaymentPsuDataResponse applyIdentification(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String paymentId = request.getPaymentId();
        String authorisationId = request.getAuthorisationId();
        PsuIdData psuData = request.getPsuData();

        if (!isPsuExist(psuData)) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Apply identification when update payment PSU data has failed. No PSU data available in request.");
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }
        SpiContextData contextData = spiService.provideWithPsuIdData(psuData);
        SpiAspspConsentDataProvider aspspConsentDataProvider = spiService.getSpiAspspDataProviderFor(request.getPaymentId());
        SpiPayment payment = getSpiPayment(request.getPaymentId());

        SpiResponse<SpiCurrencyConversionInfo> conversionInfoSpiResponse =
            paymentServicesHolder.getCurrencyConversionInfo(
                contextData, payment, authorisationId, aspspConsentDataProvider
            );
        SpiCurrencyConversionInfo currencyConversionInfo = conversionInfoSpiResponse.getPayload();
        return Xs2aUpdatePisCommonPaymentPsuDataResponse
                   .buildWithCurrencyConversionInfo(PSUIDENTIFIED, paymentId, authorisationId, psuData,
                                                    pisMappersHolder.toXs2aCurrencyConversionInfo(currencyConversionInfo));
    }

    abstract Xs2aUpdatePisCommonPaymentPsuDataResponse buildUpdateResponseWhenScaMethodsAreMultiple(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                                    PsuIdData psuData,
                                                                                                    List<AuthenticationObject> spiScaMethods,
                                                                                                    SpiPayment payment,
                                                                                                    SpiAspspConsentDataProvider aspspConsentDataProvider,
                                                                                                    SpiContextData contextData);

    Xs2aUpdatePisCommonPaymentPsuDataResponse buildUpdateResponseWhenScaMethodIsSingle(AuthorisationProcessorRequest authorisationProcessorRequest, PsuIdData psuData, SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider, SpiContextData contextData, List<AuthenticationObject> scaMethods) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        xs2aAuthorisationService.saveAuthenticationMethods(request.getAuthorisationId(), scaMethods);
        AuthenticationObject chosenMethod = scaMethods.get(0);

        if (chosenMethod.isDecoupled()) {
            xs2aAuthorisationService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return proceedDecoupledApproach(request, payment, chosenMethod.getAuthenticationMethodId());
        }

        return proceedSingleScaEmbeddedApproach(authorisationProcessorRequest, payment, chosenMethod, contextData, aspspConsentDataProvider, psuData);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedSingleScaEmbeddedApproach(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                                       SpiPayment payment,
                                                                                       AuthenticationObject chosenMethod,
                                                                                       SpiContextData contextData,
                                                                                       SpiAspspConsentDataProvider spiAspspConsentDataProvider,
                                                                                       PsuIdData psuData) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String authorisationId = request.getAuthorisationId();

        SpiResponse<SpiAuthorizationCodeResult> authCodeResponse = requestAuthorisationCode(payment, chosenMethod.getAuthenticationMethodId(), contextData, spiAspspConsentDataProvider);

        if (authCodeResponse.hasError()) {
            ErrorHolder errorHolder = spiService.mapToErrorHolder(authCodeResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Proceed single SCA embedded approach when performs authorisation has failed.");
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), authorisationId, psuData);
        }

        SpiAuthorizationCodeResult authorizationCodeResult = authCodeResponse.getPayload();
        if (needProcessExemptedSca(payment.getPaymentType(), authorizationCodeResult.isScaExempted())) {
            writeInfoLog(authorisationProcessorRequest, psuData, "SCA was exempted for the payment after AuthorisationSpi#requestAuthorisationCode.");
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, payment.getPaymentType(), payment, contextData, EXEMPTED);
        }

        AuthenticationObject authenticationObject = authorizationCodeResult.getSelectedScaMethod();
        ChallengeData challengeData = mapToChallengeData(authorizationCodeResult);

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = getXs2aUpdatePisCommonPaymentPsuDataResponse(SCAMETHODSELECTED, payment, contextData, spiAspspConsentDataProvider, psuData, authorisationId);
        response.setChosenScaMethod(authenticationObject);
        response.setChallengeData(challengeData);
        return response;
    }

    abstract Xs2aUpdatePisCommonPaymentPsuDataResponse getXs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus scaStatus, SpiPayment payment, SpiContextData contextData, SpiAspspConsentDataProvider spiAspspConsentDataProvider, PsuIdData psuData, String authorisationId);

    private boolean isDecoupledApproach(String authorisationId, String authenticationMethodId) {
        return xs2aAuthorisationService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedEmbeddedApproach(AuthorisationProcessorRequest authorisationProcessorRequest, SpiPayment payment) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        String authenticationMethodId = request.getAuthenticationMethodId();
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();
        PsuIdData psuData = extractPsuIdData(request, authorisation);

        SpiContextData contextData = spiService.provideWithPsuIdData(psuData);
        SpiAspspConsentDataProvider aspspConsentDataProvider = spiService.getSpiAspspDataProviderFor(paymentId);

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = requestAuthorisationCode(payment, authenticationMethodId, contextData, aspspConsentDataProvider);

        if (payment == null || spiResponse.hasError()) {
            ErrorHolder errorHolder = spiService.mapToErrorHolder(spiResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, EMBEDDED_SELECTING_SCA_METHOD_FAILED_MSG);

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                xs2aAuthorisationService.updateAuthorisationStatus(authorisationId, FAILED);
            }
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        SpiAuthorizationCodeResult authorizationCodeResult = spiResponse.getPayload();
        if (needProcessExemptedSca(payment.getPaymentType(), authorizationCodeResult.isScaExempted())) {
            writeInfoLog(authorisationProcessorRequest, psuData, "SCA was exempted for the payment after AuthorisationSpi#requestAuthorisationCode.");
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, payment.getPaymentType(), payment, contextData, EXEMPTED);
        }

        if (authorizationCodeResult.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, EMBEDDED_SELECTING_SCA_METHOD_FAILED_MSG);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        AuthenticationObject authenticationObject = authorizationCodeResult.getSelectedScaMethod();
        ChallengeData challengeData = authorizationCodeResult.getChallengeData();

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = getXs2aUpdatePisCommonPaymentPsuDataResponse(SCAMETHODSELECTED, payment, contextData, aspspConsentDataProvider, psuData, authorisationId);
        response.setChosenScaMethod(authenticationObject);
        response.setChallengeData(challengeData);
        return response;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledApproach(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                               SpiPayment payment) {
        return proceedDecoupledApproach(request, payment, null);
    }

    protected SpiPayment getSpiPayment(String encryptedPaymentId) {
        Optional<PisCommonPaymentResponse> commonPaymentById = paymentServicesHolder.getPisCommonPaymentById(encryptedPaymentId);
        return commonPaymentById
                   .map(pisMappersHolder::mapToSpiPayment)
                   .orElse(null);
    }
}
