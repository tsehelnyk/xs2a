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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.CommonDecoupledAisService;
import de.adorsys.psd2.xs2a.service.authorization.piis.CommonDecoupledPiisService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ConsentAuthorisationProcessorServicesHolder {
    private final Xs2aAuthorisationService authorisationService;
    private final Xs2aAisConsentService aisConsentService;
    private final CommonDecoupledAisService commonDecoupledAisService;
    private final AisScaAuthorisationService aisScaAuthorisationService;
    private final PiisScaAuthorisationService piisScaAuthorisationService;
    private final Xs2aPiisConsentService piisConsentService;
    private final CommonDecoupledPiisService commonDecoupledPiisService;

    public void updateScaApproach(String authorisationId, ScaApproach decoupled) {
        authorisationService.updateScaApproach(authorisationId, decoupled);
    }

    public void updateAuthorisationStatus(String authorisationId, ScaStatus failed) {
        authorisationService.updateAuthorisationStatus(authorisationId, failed);
    }

    public void saveAuthenticationMethods(String authorisationId, List<AuthenticationObject> availableScaMethods) {
        authorisationService.saveAuthenticationMethods(authorisationId, availableScaMethods);
    }

    public boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        return authorisationService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    public void findAndTerminateOldConsentsByNewConsentId(String consentId) {
        aisConsentService.findAndTerminateOldConsentsByNewConsentId(consentId);
    }

    public void updateConsentStatus(String consentId, ConsentStatus responseConsentStatus) {
        aisConsentService.updateConsentStatus(consentId, responseConsentStatus);
    }

    public void updateMultilevelScaRequiredAis(String consentId, boolean b) {
        aisConsentService.updateMultilevelScaRequired(consentId, b);
    }
    public void updateMultilevelScaRequiredPiis(String consentId, boolean multilevelScaRequired) {
        piisConsentService.updateMultilevelScaRequired(consentId, multilevelScaRequired);
    }

    public Optional<AisConsent> getAccountConsentById(String consentId) {
        return aisConsentService.getAccountConsentById(consentId);
    }

    public UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, SpiAccountConsent consent, String authenticationMethodId, PsuIdData psuData) {
        return commonDecoupledAisService.proceedDecoupledApproach(consentId, authorisationId, consent, authenticationMethodId, psuData);
    }

    public UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, SpiPiisConsent consent, String authenticationMethodId, PsuIdData psuData) {
        return commonDecoupledPiisService.proceedDecoupledApproach(consentId, authorisationId, consent,authenticationMethodId,psuData);
    }

    public boolean isOneFactorAuthorisation(AisConsent consent) {
        return aisScaAuthorisationService.isOneFactorAuthorisation(consent);
    }

    public boolean isOneFactorAuthorisation(PiisConsent consent) {
        return piisScaAuthorisationService.isOneFactorAuthorisation(consent);
    }

    public Optional<PiisConsent> getPiisConsentById(String consentId) {
        return piisConsentService.getPiisConsentById(consentId);
    }
}
