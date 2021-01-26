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

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ConsentMappersHolder;
import de.adorsys.psd2.xs2a.service.PiisServicesHolder;
import de.adorsys.psd2.xs2a.service.SpiService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisScaAuthorisationService;
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
public class PiisAuthorisationProcessorServiceImpl extends ConsentAuthorisationProcessorService<PiisConsent> {
    private final List<PiisAuthorizationService> services;
    private final PiisServicesHolder piisServicesHolder;
    private final PiisScaAuthorisationService piisScaAuthorisationService;
    private final ConsentMappersHolder consentMappersHolder;

    public PiisAuthorisationProcessorServiceImpl(Xs2aAuthorisationService authorisationService, SpiService spiService,
                                                 ConsentMappersHolder consentMappersHolder, List<PiisAuthorizationService> services,
                                                 PiisServicesHolder piisServicesHolder,
                                                 PiisScaAuthorisationService piisScaAuthorisationService) {
        super(authorisationService, spiService, consentMappersHolder);
        this.services = services;
        this.piisServicesHolder = piisServicesHolder;
        this.piisScaAuthorisationService = piisScaAuthorisationService;
        this.consentMappersHolder = consentMappersHolder;
    }

    @Override
    public void updateAuthorisation(AuthorisationProcessorRequest request, AuthorisationProcessorResponse response) {
        PiisAuthorizationService authorizationService = getService(request.getScaApproach());
        authorizationService.updateConsentPsuData(request.getUpdateAuthorisationRequest(), response);
    }

    private PiisAuthorizationService getService(ScaApproach scaApproach) {
        return services.stream().filter(s -> s.getScaApproachServiceType() == scaApproach).findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Piis authorisation service was not found for approach " + scaApproach));
    }

    @Override
    ErrorType getErrorType400() {
        return ErrorType.PIIS_400;
    }

    @Override
    ErrorType getErrorType401() {
        return ErrorType.PIIS_401;
    }

    @Override
    void findAndTerminateOldConsentsByNewConsentId(String consentId) {
        // this method is empty because one tpp could have more then one valid piis consent
    }

    @Override
    void updateConsentStatus(String consentId, ConsentStatus responseConsentStatus) {
        piisServicesHolder.updateConsentStatus(consentId, responseConsentStatus);
    }

    @Override
    void updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired) {
        piisServicesHolder.updateMultilevelScaRequired(consentId, true);
    }

    @Override
    ServiceType getServiceType() {
        return ServiceType.PIIS;
    }

    @Override
    SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(SpiContextData spiContextData, UpdateAuthorisationRequest request, PsuIdData psuData, PiisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisServicesHolder.verifyScaAuthorisation(spiContextData,
                                                         consentMappersHolder.toSpiScaConfirmation(request, psuData),
                                                         consentMappersHolder.mapToSpiPiisConsent(consent),
                                                         spiAspspConsentDataProvider);
    }

    @Override
    UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, PiisConsent consent, String authenticationMethodId, PsuIdData psuData) {
        return piisServicesHolder.proceedDecoupledApproach(consentId, authorisationId,
                                                           consentMappersHolder.mapToSpiPiisConsent(consent),
                                                           authenticationMethodId, psuData);
    }

    @Override
    Optional<PiisConsent> getConsentByIdFromCms(String consentId) {
        return piisServicesHolder.getPiisConsentById(consentId);
    }

    @Override
    SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiContextData spiContextData, String authenticationMethodId, PiisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisServicesHolder.requestAuthorisationCode(spiContextData,
                                                           authenticationMethodId,
                                                           consentMappersHolder.mapToSpiPiisConsent(consent),
                                                           spiAspspConsentDataProvider);
    }

    @Override
    UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, PiisConsent consent, PsuIdData psuData) {
        return piisServicesHolder.proceedDecoupledApproach(consentId, authorisationId, consentMappersHolder.mapToSpiPiisConsent(consent), psuData);
    }

    @Override
    boolean isOneFactorAuthorisation(PiisConsent consent) {
        return piisScaAuthorisationService.isOneFactorAuthorisation(consent);
    }

    @Override
    SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(SpiContextData spiContextData, String authorisationId, SpiPsuData spiPsuData, String password, PiisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisServicesHolder.authorisePsu(spiContextData,
                                               authorisationId,
                                               spiPsuData,
                                               password,
                                               consentMappersHolder.mapToSpiPiisConsent(consent),
                                               spiAspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiContextData spiContextData, PiisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisServicesHolder.requestAvailableScaMethods(spiContextData,
                                                             consentMappersHolder.mapToSpiPiisConsent(consent),
                                                             spiAspspConsentDataProvider);
    }
}
