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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import org.springframework.stereotype.Service;

@Service
public class RedirectPisScaAuthorisationService extends AbstractPisScaAuthorisationService {

    private final PisAuthorisationConfirmationService pisAuthorisationConfirmationService;

    public RedirectPisScaAuthorisationService(PisAuthorisationService authorisationService, Xs2aPisCommonPaymentMapper pisCommonPaymentMapper, ScaApproachResolver scaApproachResolver, PaymentAuthorisationSpi paymentAuthorisationSpi, SpiContextDataProvider spiContextDataProvider, Xs2aPisCommonPaymentService xs2aPisCommonPaymentService, Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory, PisAuthorisationConfirmationService pisAuthorisationConfirmationService) {
        super(authorisationService, pisCommonPaymentMapper, scaApproachResolver, paymentAuthorisationSpi, spiContextDataProvider, xs2aPisCommonPaymentService, xs2aToSpiPaymentMapper, aspspConsentDataProviderFactory);
        this.pisAuthorisationConfirmationService = pisAuthorisationConfirmationService;
    }

    @Override
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updateCommonPaymentPsuData(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        return pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);
    }

    @Override
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updateCommonPaymentCancellationPsuData(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        return pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);
    }

    @Override
    public ScaApproach getScaApproachServiceType() {
        return ScaApproach.REDIRECT;
    }
}
