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
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisCommonDecoupledService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisExecutePaymentService;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CurrencyConversionInfoSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentServicesHolder {
    private final PisExecutePaymentService pisExecutePaymentService;
    private final Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService;
    private final Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    private final PisCommonDecoupledService pisCommonDecoupledService;
    private final CurrencyConversionInfoSpi currencyConversionInfoSpi;
    private final PisPsuDataService pisPsuDataService;
    private final PisAspspDataService pisAspspDataService;
    private final PaymentCancellationSpi paymentCancellationSpi;

    public SpiResponse<SpiCurrencyConversionInfo> getCurrencyConversionInfo(SpiContextData spiContextData,
                                                                            SpiPayment payment, String authorisationId,
                                                                            SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return currencyConversionInfoSpi.getCurrencyConversionInfo(spiContextData, payment, authorisationId, aspspConsentDataProvider);
    }

    public String getInternalPaymentIdByEncryptedString(String paymentId) {
        return pisAspspDataService.getInternalPaymentIdByEncryptedString(paymentId);
    }

    public Optional<PisCommonPaymentResponse> getPisCommonPaymentById(String encryptedPaymentId) {
        return xs2aPisCommonPaymentService.getPisCommonPaymentById(encryptedPaymentId);
    }

    public SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(SpiContextData spiContextData,
                                                                                                               SpiScaConfirmation spiScaConfirmation,
                                                                                                               SpiPayment payment,
                                                                                                               SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return pisExecutePaymentService.verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(spiContextData, spiScaConfirmation, payment, spiAspspConsentDataProvider);
    }

    public void updateMultilevelSca(String paymentId, boolean isMultilevel) {
        xs2aPisCommonPaymentService.updateMultilevelSca(paymentId, isMultilevel);
    }

    public void updatePaymentStatus(String paymentId, TransactionStatus paymentStatus) {
        updatePaymentAfterSpiService.updatePaymentStatus(paymentId, paymentStatus);
    }

    public Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledInitiation(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                SpiPayment payment,
                                                                                String authenticationMethodId) {
        return pisCommonDecoupledService.proceedDecoupledInitiation(request, payment, authenticationMethodId);
    }

    public SpiResponse<SpiPaymentExecutionResponse> executePaymentWithoutSca(SpiContextData spiContextData,
                                                                             SpiPayment payment,
                                                                             SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return pisExecutePaymentService.executePaymentWithoutSca(spiContextData, payment, spiAspspConsentDataProvider);
    }

    public SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndCancelPaymentWithResponse(SpiContextData spiContextData,
                                                                                                       SpiScaConfirmation spiScaConfirmation,
                                                                                                       SpiPayment payment,
                                                                                                       SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return paymentCancellationSpi.verifyScaAuthorisationAndCancelPaymentWithResponse(spiContextData, spiScaConfirmation, payment, spiAspspConsentDataProvider);
    }

    public SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(SpiContextData contextData, String authorisationId,
                                                                 SpiPsuData spiPsuData, String password,
                                                                 SpiPayment payment,
                                                                 SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return paymentCancellationSpi.authorisePsu(contextData, authorisationId, spiPsuData, password, payment, spiAspspConsentDataProvider);
    }

    public SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiContextData contextData,
                                                                                  SpiPayment payment,
                                                                                  SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentCancellationSpi.requestAvailableScaMethods(contextData, payment, aspspConsentDataProvider);
    }

    public SpiResponse<SpiResponse.VoidResponse> cancelPaymentWithoutSca(SpiContextData contextData, SpiPayment payment,
                                                                         SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentCancellationSpi.cancelPaymentWithoutSca(contextData, payment, aspspConsentDataProvider);
    }

    public void updateInternalPaymentStatus(String paymentId, InternalPaymentStatus internalPaymentStatus) {
        updatePaymentAfterSpiService.updateInternalPaymentStatus(paymentId, internalPaymentStatus);
    }

    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiContextData spiContextData,
                                                                            String authenticationMethodId,
                                                                            SpiPayment payment,
                                                                            SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return paymentCancellationSpi.requestAuthorisationCode(spiContextData, authenticationMethodId, payment, spiAspspConsentDataProvider);
    }

    public Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledCancellation(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                  SpiPayment payment, String authenticationMethodId) {
        return pisCommonDecoupledService.proceedDecoupledCancellation(request, payment, authenticationMethodId);
    }

    public List<PsuIdData> getPsuDataByPaymentId(String paymentId) {
        return pisPsuDataService.getPsuDataByPaymentId(paymentId);
    }
}
