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

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.piis.CommonDecoupledPiisService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAvailableScaMethodsResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PiisConsentSpi;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PiisServicesHolder {
    private final Xs2aPiisConsentService piisConsentService;
    private final PiisConsentSpi piisConsentSpi;
    private final CommonDecoupledPiisService commonDecoupledPiisService;

    public void updateConsentStatus(String consentId, ConsentStatus responseConsentStatus) {
        piisConsentService.updateConsentStatus(consentId, responseConsentStatus);
    }

    public void updateMultilevelScaRequired(String consentId, boolean isMultilevel) {
        piisConsentService.updateMultilevelScaRequired(consentId, isMultilevel);
    }

    public SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(SpiContextData spiContextData,
                                                                                 SpiScaConfirmation spiScaConfirmation,
                                                                                 SpiPiisConsent spiPiisConsent,
                                                                                 SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.verifyScaAuthorisation(spiContextData, spiScaConfirmation, spiPiisConsent, spiAspspConsentDataProvider);
    }

    public Optional<PiisConsent> getPiisConsentById(String consentId) {
        return piisConsentService.getPiisConsentById(consentId);
    }

    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiContextData spiContextData,
                                                                            String authenticationMethodId,
                                                                            SpiPiisConsent spiPiisConsent,
                                                                            SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.requestAuthorisationCode(spiContextData, authenticationMethodId, spiPiisConsent, spiAspspConsentDataProvider);
    }

    public SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(SpiContextData spiContextData, String authorisationId,
                                                                 SpiPsuData spiPsuData, String password,
                                                                 SpiPiisConsent spiPiisConsent,
                                                                 SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.authorisePsu(spiContextData, authorisationId, spiPsuData, password, spiPiisConsent, spiAspspConsentDataProvider);
    }

    public SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiContextData spiContextData,
                                                                                  SpiPiisConsent spiPiisConsent,
                                                                                  SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.requestAvailableScaMethods(spiContextData, spiPiisConsent, spiAspspConsentDataProvider);
    }

    public UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId,
                                                                 SpiPiisConsent spiPiisConsent, String authenticationMethodId,
                                                                 PsuIdData psuData) {
        return commonDecoupledPiisService.proceedDecoupledApproach(consentId, authorisationId, spiPiisConsent, authenticationMethodId, psuData);
    }

    public UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, SpiPiisConsent spiPiisConsent, PsuIdData psuData) {
        return commonDecoupledPiisService.proceedDecoupledApproach(consentId, authorisationId, spiPiisConsent, psuData);
    }
}
