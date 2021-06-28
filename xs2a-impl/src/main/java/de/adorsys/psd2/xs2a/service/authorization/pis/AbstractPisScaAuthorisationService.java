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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.authorisation.Xs2aStartAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
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
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public abstract class AbstractPisScaAuthorisationService implements PisScaAuthorisationService {
    private final PisAuthorisationService authorisationService;
    private final Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;
    private final ScaApproachResolver scaApproachResolver;
    private final PaymentAuthorisationSpi paymentAuthorisationSpi;
    private final SpiContextDataProvider spiContextDataProvider;
    private final Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    private final Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    @Override
    public Optional<Xs2aStartAuthorisationResponse> startAuthorisation(String externalPaymentId, PaymentType paymentType, PsuIdData psuData) {

        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiPayment spiPayment = getSpiPayment(externalPaymentId);
        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(externalPaymentId);
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
        resultResponse.setScaStatus(response.getPayload().getScaStatus());

        return resultResponse;
    }

    @Override
    public Optional<Xs2aCreatePisAuthorisationResponse> createCommonPaymentAuthorisation(String paymentId, PaymentType paymentType, PsuIdData psuData) {
        return pisCommonPaymentMapper.mapToXsa2CreatePisAuthorisationResponse(authorisationService.createPisAuthorisation(paymentId, psuData), paymentType);
    }

    @Override
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updateCommonPaymentPsuData(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        return authorisationService.updatePisAuthorisation(request, getScaApproachServiceType());
    }

    @Override
    public void updateAuthorisation(UpdateAuthorisationRequest request, AuthorisationProcessorResponse response) {
        authorisationService.updateAuthorisation(request, response);
    }

    @Override
    public void updateCancellationAuthorisation(UpdateAuthorisationRequest request, AuthorisationProcessorResponse response) {
        authorisationService.updateCancellationAuthorisation(request, response);
    }

    @Override
    public Optional<Xs2aCreatePisCancellationAuthorisationResponse> createCommonPaymentCancellationAuthorisation(String paymentId, PaymentType paymentType, PsuIdData psuData) {
        return pisCommonPaymentMapper.mapToXs2aCreatePisCancellationAuthorisationResponse(authorisationService.createPisAuthorisationCancellation(paymentId, psuData), paymentType);
    }

    @Override
    public Optional<Xs2aPaymentCancellationAuthorisationSubResource> getCancellationAuthorisationSubResources(String paymentId) {
        return authorisationService.getCancellationAuthorisationSubResources(paymentId)
                   .map(Xs2aPaymentCancellationAuthorisationSubResource::new);
    }

    @Override
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updateCommonPaymentCancellationPsuData(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        return authorisationService.updatePisCancellationAuthorisation(request, getScaApproachServiceType());
    }

    @Override
    public Optional<Xs2aAuthorisationSubResources> getAuthorisationSubResources(String paymentId) {
        return authorisationService.getAuthorisationSubResources(paymentId)
                   .map(Xs2aAuthorisationSubResources::new);
    }

    @Override
    public Optional<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId) {
        return authorisationService.getAuthorisationScaStatus(paymentId, authorisationId);
    }

    @Override
    public Optional<ScaStatus> getCancellationAuthorisationScaStatus(String paymentId, String authorisationId) {
        return authorisationService.getCancellationAuthorisationScaStatus(paymentId, authorisationId);
    }
}
