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
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPiisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConsentMappersHolder {
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final Xs2aToSpiPsuDataMapper psuDataMapper;
    private final Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper;

    public SpiPsuData mapToSpiPsuData(PsuIdData psuIdData) {
        return psuDataMapper.mapToSpiPsuData(psuIdData);
    }

    public SpiAccountConsent mapToSpiAccountConsent(AisConsent consent) {
        return aisConsentMapper.mapToSpiAccountConsent(consent);
    }

    public SpiScaConfirmation mapToSpiScaConfirmation(UpdateAuthorisationRequest request, PsuIdData psuData) {
        return aisConsentMapper.mapToSpiScaConfirmation(request, psuData);
    }

    public SpiScaConfirmation toSpiScaConfirmation(UpdateAuthorisationRequest request, PsuIdData psuData) {
        return xs2aToSpiPiisConsentMapper.toSpiScaConfirmation(request, psuData);
    }

    public SpiPiisConsent mapToSpiPiisConsent(PiisConsent consent) {
        return xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(consent);
    }
}
