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

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.pis.Xs2aCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aCurrencyConversionInfoMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PisMappersHolder {
    private final Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    private final Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private final SpiToXs2aCurrencyConversionInfoMapper spiToXs2aCurrencyConversionInfoMapper;

    public SpiPayment mapToSpiPayment(PisCommonPaymentResponse pisCommonPaymentResponse) {
        return xs2aToSpiPaymentMapper.mapToSpiPayment(pisCommonPaymentResponse);
    }

    public SpiScaConfirmation buildSpiScaConfirmation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, String parentId, String internalId, PsuIdData psuData) {
        return xs2aPisCommonPaymentMapper.buildSpiScaConfirmation(request, parentId, internalId, psuData);
    }

    public SpiPsuData mapToSpiPsuData(PsuIdData psuData) {
        return xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuData);
    }

    public Xs2aCurrencyConversionInfo toXs2aCurrencyConversionInfo(SpiCurrencyConversionInfo spiCurrencyConversionInfo) {
        return spiToXs2aCurrencyConversionInfoMapper.toXs2aCurrencyConversionInfo(spiCurrencyConversionInfo);
    }
}
