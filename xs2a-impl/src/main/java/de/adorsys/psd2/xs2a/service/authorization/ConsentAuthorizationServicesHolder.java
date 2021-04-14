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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ConsentAuthorizationServicesHolder {
    private final Xs2aAuthorisationService authorisationService;
    private final AuthorisationServiceEncrypted authorisationServiceEncrypted;
    private final Xs2aPiisConsentService xs2aPiisConsentService;
    private final Xs2aAisConsentService aisConsentService;

    public void updateConsentStatus(String consentId, ConsentStatus consentStatus) {
        xs2aPiisConsentService.updateConsentStatus(consentId, consentStatus);
    }

    public Optional<PiisConsent> getPiisConsentById(String consentId) {
        return xs2aPiisConsentService.getPiisConsentById(consentId);
    }

    public CmsResponse<Authorisation> getAuthorisationById(String authorisationId) {
        return authorisationServiceEncrypted.getAuthorisationById(authorisationId);
    }

    public void updateAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        authorisationService.updateAuthorisationStatus(authorisationId, scaStatus);
    }

    public void findAndTerminateOldConsentsByNewConsentId(String consentId) {
        aisConsentService.findAndTerminateOldConsentsByNewConsentId(consentId);
    }

    public Optional<AisConsent> getAccountConsentById(String consentId) {
        return aisConsentService.getAccountConsentById(consentId);
    }
}
