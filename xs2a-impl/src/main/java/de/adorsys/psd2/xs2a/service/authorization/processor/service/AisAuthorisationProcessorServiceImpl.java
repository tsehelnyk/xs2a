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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.AisServicesHolder;
import de.adorsys.psd2.xs2a.service.ConsentMappersHolder;
import de.adorsys.psd2.xs2a.service.SpiService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAvailableScaMethodsResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AisAuthorisationProcessorServiceImpl extends ConsentAuthorisationProcessorService<AisConsent> {
    private final List<AisAuthorizationService> services;
    private final AisServicesHolder aisServicesHolder;
    private final AisScaAuthorisationService aisScaAuthorisationService;
    private final ConsentMappersHolder consentMappersHolder;

    public AisAuthorisationProcessorServiceImpl(Xs2aAuthorisationService authorisationService,
                                                SpiService spiService, ConsentMappersHolder consentMappersHolder,
                                                List<AisAuthorizationService> services,
                                                AisServicesHolder aisServicesHolder,
                                                AisScaAuthorisationService aisScaAuthorisationService) {
        super(authorisationService, spiService, consentMappersHolder);
        this.services = services;
        this.aisServicesHolder = aisServicesHolder;
        this.aisScaAuthorisationService = aisScaAuthorisationService;
        this.consentMappersHolder = consentMappersHolder;
    }

    @Override
    public void updateAuthorisation(AuthorisationProcessorRequest request, AuthorisationProcessorResponse response) {
        AisAuthorizationService authorizationService = getService(request.getScaApproach());
        authorizationService.updateConsentPsuData(request.getUpdateAuthorisationRequest(), response);
    }

    private AisAuthorizationService getService(ScaApproach scaApproach) {
        return services.stream().filter(s -> s.getScaApproachServiceType() == scaApproach).findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Ais authorisation service was not found for approach " + scaApproach));
    }

    @Override
    ErrorType getErrorType400() {
        return ErrorType.AIS_400;
    }

    @Override
    ErrorType getErrorType401() {
        return ErrorType.AIS_401;
    }

    @Override
    void findAndTerminateOldConsentsByNewConsentId(String consentId) {
        aisServicesHolder.findAndTerminateOldConsentsByNewConsentId(consentId);
    }

    @Override
    void updateConsentStatus(String consentId, ConsentStatus responseConsentStatus) {
        aisServicesHolder.updateConsentStatus(consentId, responseConsentStatus);
    }

    @Override
    void updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired) {
        aisServicesHolder.updateMultilevelScaRequired(consentId, true);
    }

    @Override
    ServiceType getServiceType() {
        return ServiceType.AIS;
    }

    @Override
    UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, AisConsent consent, String authenticationMethodId, PsuIdData psuData) {
        return aisServicesHolder.proceedDecoupledApproach(consentId, authorisationId,
                                                                  consentMappersHolder.mapToSpiAccountConsent(consent),
                                                                  authenticationMethodId, psuData);
    }

    @Override
    Optional<AisConsent> getConsentByIdFromCms(String consentId) {
        return aisServicesHolder.getAccountConsentById(consentId);
    }

    @Override
    SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiContextData provideWithPsuIdData, String authenticationMethodId, AisConsent consent, SpiAspspConsentDataProvider spiAspspDataProviderFor) {
        return aisServicesHolder.requestAuthorisationCode(provideWithPsuIdData,
                                                          authenticationMethodId,
                                                          consentMappersHolder.mapToSpiAccountConsent(consent),
                                                          spiAspspDataProviderFor);

    }

    @Override
    UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, AisConsent consent, PsuIdData psuData) {
        return aisServicesHolder.proceedDecoupledApproach(consentId, authorisationId, consentMappersHolder.mapToSpiAccountConsent(consent), psuData);
    }

    @Override
    boolean isOneFactorAuthorisation(AisConsent consent) {
        return aisScaAuthorisationService.isOneFactorAuthorisation(consent);
    }

    @Override
    SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(SpiContextData spiContextData, String authorisationId, SpiPsuData spiPsuData, String password, AisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisServicesHolder.authorisePsu(spiContextData,
                                              authorisationId,
                                              spiPsuData,
                                              password,
                                              consentMappersHolder.mapToSpiAccountConsent(consent),
                                              spiAspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiContextData spiContextData, AisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisServicesHolder.requestAvailableScaMethods(spiContextData,
                                                            consentMappersHolder.mapToSpiAccountConsent(consent),
                                                            spiAspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(SpiContextData spiContextData, UpdateAuthorisationRequest request, PsuIdData psuData, AisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisServicesHolder.verifyScaAuthorisation(spiContextData,
                                                        consentMappersHolder.mapToSpiScaConfirmation(request, psuData),
                                                        consentMappersHolder.mapToSpiAccountConsent(consent),
                                                        spiAspspConsentDataProvider);
    }
}
