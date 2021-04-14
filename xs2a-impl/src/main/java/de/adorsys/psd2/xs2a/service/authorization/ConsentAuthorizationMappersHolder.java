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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPiisConsentMapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ConsentAuthorizationMappersHolder {
    private final Xs2aToSpiPiisConsentMapper piisConsentMapper;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final SpiErrorMapper spiErrorMapper;

    public SpiPiisConsent mapToSpiPiisConsent(PiisConsent consent) {
        return piisConsentMapper.mapToSpiPiisConsent(consent);
    }

    public ErrorHolder mapToErrorHolder(SpiResponse<SpiConsentConfirmationCodeValidationResponse> spiResponse, ServiceType serviceType) {
        return spiErrorMapper.mapToErrorHolder(spiResponse, serviceType);
    }

    public SpiAccountConsent mapToSpiAccountConsent(AisConsent consent) {
        return aisConsentMapper.mapToSpiAccountConsent(consent);
    }
}
