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

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentAuthorizationServicesHolder {
    private final RequestProviderService requestProviderService;
    private final Xs2aAuthorisationService xs2aAuthorisationService;

    public void updateAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        xs2aAuthorisationService.updateAuthorisationStatus(authorisationId, scaStatus);
    }

    public PsuIdData getPsuIdData() {
        return requestProviderService.getPsuIdData();
    }
}

