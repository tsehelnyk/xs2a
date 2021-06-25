/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.authorisation.Xs2aStartAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiStartAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DecoupledPisScaAuthorisationService extends AbstractPisScaAuthorisationService {

    private final ScaApproachResolver scaApproachResolver;
    private final PaymentAuthorisationSpi paymentAuthorisationSpi;
    private final SpiContextDataProvider spiContextDataProvider;
    private final Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    private final Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;


    public DecoupledPisScaAuthorisationService(PisAuthorisationService authorisationService, Xs2aPisCommonPaymentMapper pisCommonPaymentMapper, ScaApproachResolver scaApproachResolver, PaymentAuthorisationSpi paymentAuthorisationSpi, SpiContextDataProvider spiContextDataProvider, Xs2aPisCommonPaymentService xs2aPisCommonPaymentService, Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory) {
        super(authorisationService, pisCommonPaymentMapper);
        this.scaApproachResolver = scaApproachResolver;
        this.paymentAuthorisationSpi = paymentAuthorisationSpi;
        this.spiContextDataProvider = spiContextDataProvider;
        this.xs2aPisCommonPaymentService = xs2aPisCommonPaymentService;
        this.xs2aToSpiPaymentMapper = xs2aToSpiPaymentMapper;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
    }

    @Override
    public ScaApproach getScaApproachServiceType() {
        return ScaApproach.DECOUPLED;
    }

    @Override
    public Optional<Xs2aStartAuthorisationResponse> startAuthorisation(String paymentId, PaymentType paymentType, PsuIdData psuData) {

        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiPayment spiPayment = getSpiPayment(paymentId);
        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);
        SpiResponse<SpiStartAuthorisationResponse> spiResponse = paymentAuthorisationSpi.startAuthorization(contextData, scaApproachResolver.resolveScaApproach(), spiPayment, aspspConsentDataProvider);

        return Optional.of(getResponse(spiResponse));
    }

    private SpiPayment getSpiPayment(String encryptedPaymentId) {
        Optional<PisCommonPaymentResponse> commonPaymentById = xs2aPisCommonPaymentService.getPisCommonPaymentById(encryptedPaymentId);
        return commonPaymentById
                   .map(xs2aToSpiPaymentMapper::mapToSpiPayment)
                   .orElse(null);
    }

    private Xs2aStartAuthorisationResponse getResponse(SpiResponse<SpiStartAuthorisationResponse> response) {
        Xs2aStartAuthorisationResponse resultResponse = new Xs2aStartAuthorisationResponse();
        resultResponse.setPsuMessage(response.getPayload().getPsuMessage());
        resultResponse.setScaApproach(response.getPayload().getScaApproach());
        resultResponse.setTppMessageInformation(response.getPayload().getTppMessageInformation());

        return resultResponse;
    }
}
