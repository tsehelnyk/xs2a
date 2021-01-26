/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.ais.CommonDecoupledAisService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAvailableScaMethodsResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AisServicesHolder {
    private final Xs2aAisConsentService aisConsentService;
    private final AisConsentSpi aisConsentSpi;
    private final CommonDecoupledAisService commonDecoupledAisService;

    public void findAndTerminateOldConsentsByNewConsentId(String consentId) {
        aisConsentService.findAndTerminateOldConsentsByNewConsentId(consentId);
    }

    public void updateConsentStatus(String consentId, ConsentStatus responseConsentStatus) {
        aisConsentService.updateConsentStatus(consentId, responseConsentStatus);
    }

    public void updateMultilevelScaRequired(String consentId, boolean isMultilevel) {
        aisConsentService.updateMultilevelScaRequired(consentId, isMultilevel);
    }

    public Optional<AisConsent> getAccountConsentById(String consentId) {
        return aisConsentService.getAccountConsentById(consentId);
    }

    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiContextData spiContextData,
                                                                            String authenticationMethodId,
                                                                            SpiAccountConsent spiAccountConsent,
                                                                            SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisConsentSpi.requestAuthorisationCode(spiContextData, authenticationMethodId, spiAccountConsent, spiAspspConsentDataProvider);
    }

    public SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(SpiContextData spiContextData, String authorisationId,
                                                                 SpiPsuData spiPsuData, String password,
                                                                 SpiAccountConsent spiAccountConsent,
                                                                 SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisConsentSpi.authorisePsu(spiContextData, authorisationId, spiPsuData, password, spiAccountConsent, spiAspspConsentDataProvider);
    }

    public SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiContextData spiContextData,
                                                                                  SpiAccountConsent spiAccountConsent,
                                                                                  SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisConsentSpi.requestAvailableScaMethods(spiContextData, spiAccountConsent, spiAspspConsentDataProvider);
    }

    public SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(SpiContextData spiContextData,
                                                                                 SpiScaConfirmation spiScaConfirmation,
                                                                                 SpiAccountConsent spiAccountConsent,
                                                                                 SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisConsentSpi.verifyScaAuthorisation(spiContextData, spiScaConfirmation, spiAccountConsent, spiAspspConsentDataProvider);
    }

    public UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId,
                                                                 SpiAccountConsent spiAccountConsent, PsuIdData psuData) {
        return commonDecoupledAisService.proceedDecoupledApproach(consentId, authorisationId, spiAccountConsent, psuData);
    }

    public UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId,
                                                                 SpiAccountConsent spiAccountConsent,
                                                                 String authenticationMethodId, PsuIdData psuData) {
        return commonDecoupledAisService.proceedDecoupledApproach(consentId, authorisationId, spiAccountConsent, authenticationMethodId, psuData);
    }
}
