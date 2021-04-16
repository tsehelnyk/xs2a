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
import de.adorsys.psd2.xs2a.service.authorization.ais.AisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAvailableScaMethodsResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AisAuthorisationProcessorServiceImpl extends ConsentAuthorisationProcessorService<AisConsent> {
    private final List<AisAuthorizationService> services;
    private final AisConsentSpi aisConsentSpi;
    private final ConsentAuthorizationProcessorMappersHolder consentAuthorizationProcessorMappersHolder;
    private final ConsentAuthorisationProcessorServicesHolder consentAuthorisationProcessorServicesHolder;

    public AisAuthorisationProcessorServiceImpl(
        SpiContextDataProvider spiContextDataProvider,
        SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
        List<AisAuthorizationService> services,
        AisConsentSpi aisConsentSpi,
        ConsentAuthorisationProcessorServicesHolder aisAuthorisationProcessorServicesHolder,
        ConsentAuthorizationProcessorMappersHolder aisAuthorizationProcessorMappersHolder
    ) {
        super(spiContextDataProvider, aspspConsentDataProviderFactory,aisAuthorisationProcessorServicesHolder, aisAuthorizationProcessorMappersHolder);
        this.services = services;
        this.aisConsentSpi = aisConsentSpi;
        this.consentAuthorisationProcessorServicesHolder = aisAuthorisationProcessorServicesHolder;
        this.consentAuthorizationProcessorMappersHolder = aisAuthorizationProcessorMappersHolder;
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
        consentAuthorisationProcessorServicesHolder.findAndTerminateOldConsentsByNewConsentId(consentId);
    }

    @Override
    void updateConsentStatus(String consentId, ConsentStatus responseConsentStatus) {
        consentAuthorisationProcessorServicesHolder.updateConsentStatus(consentId, responseConsentStatus);
    }

    @Override
    void updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired) {
        consentAuthorisationProcessorServicesHolder.updateMultilevelScaRequiredAis(consentId, true);
    }

    @Override
    ServiceType getServiceType() {
        return ServiceType.AIS;
    }

    @Override
    UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, AisConsent consent, String authenticationMethodId, PsuIdData psuData) {
        return consentAuthorisationProcessorServicesHolder.proceedDecoupledApproach(consentId, authorisationId, consentAuthorizationProcessorMappersHolder.mapToSpiAccountConsent(consent),
                                                                  authenticationMethodId, psuData);
    }

    @Override
    Optional<AisConsent> getConsentByIdFromCms(String consentId) {
        return consentAuthorisationProcessorServicesHolder.getAccountConsentById(consentId);
    }

    @Override
    SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiContextData provideWithPsuIdData, String authenticationMethodId, AisConsent consent, SpiAspspConsentDataProvider spiAspspDataProviderFor) {
        return aisConsentSpi.requestAuthorisationCode(provideWithPsuIdData,
                                                      authenticationMethodId,
            consentAuthorizationProcessorMappersHolder.mapToSpiAccountConsent(consent),
                                                      spiAspspDataProviderFor);

    }

    @Override
    boolean isOneFactorAuthorisation(AisConsent consent) {
        return consentAuthorisationProcessorServicesHolder.isOneFactorAuthorisation(consent);
    }

    @Override
    SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(SpiContextData spiContextData, String authorisationId, SpiPsuData spiPsuData, String password, AisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisConsentSpi.authorisePsu(spiContextData,
                                          authorisationId,
                                          spiPsuData,
                                          password,
            consentAuthorizationProcessorMappersHolder.mapToSpiAccountConsent(consent),
                                          spiAspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiContextData spiContextData, AisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisConsentSpi.requestAvailableScaMethods(spiContextData,
            consentAuthorizationProcessorMappersHolder.mapToSpiAccountConsent(consent),
                                                        spiAspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(SpiContextData spiContextData, UpdateAuthorisationRequest request, PsuIdData psuData, AisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisConsentSpi.verifyScaAuthorisation(spiContextData,
            consentAuthorizationProcessorMappersHolder.mapToSpiScaConfirmation(request, psuData),
            consentAuthorizationProcessorMappersHolder.mapToSpiAccountConsent(consent),
                                                    spiAspspConsentDataProvider);
    }
}
