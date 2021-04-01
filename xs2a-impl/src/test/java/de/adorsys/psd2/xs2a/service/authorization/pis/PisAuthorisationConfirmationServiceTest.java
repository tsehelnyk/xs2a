/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.Xs2aTransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.Xs2aScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aCurrencyConversionInfoMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CurrencyConversionInfoSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisAuthorisationConfirmationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String CONFIRMATION_CODE = "123456";
    private static final String SCA_AUTHENTICATION_DATA = "54321";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final SpiSinglePayment SPI_SINGLE_PAYMENT = new SpiSinglePayment(PAYMENT_PRODUCT);
    private final static JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private PisAuthorisationConfirmationService pisAuthorisationConfirmationService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;
    @Mock
    private Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    @Mock
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private PisCheckAuthorisationConfirmationService pisCheckAuthorisationConfirmationService;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiAspspConsentDataProvider aspspConsentDataProvider;
    @Mock
    private Xs2aUpdatePaymentAfterSpiService xs2aUpdatePaymentAfterSpiService;
    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @Mock
    private CurrencyConversionInfoSpi currencyConversionInfoSpi;
    @Mock
    private SpiToXs2aCurrencyConversionInfoMapper spiToXs2aCurrencyConversionInfoMapper;

    @Test
    void processAuthorisationConfirmation_success_checkOnSpi() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        Xs2aTransactionStatus transactionStatus = Xs2aTransactionStatus.ACSP;
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(
            Xs2aScaStatus.FINALISED, PAYMENT_ID, AUTHORISATION_ID, psuIdData, null);
        Authorisation authorisationResponse = buildGetPisAuthorisationResponse();

        SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest = new SpiCheckConfirmationCodeRequest(request.getConfirmationCode(), AUTHORISATION_ID);
        SpiContextData contextData = getSpiContextData();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a())
            .thenReturn(false);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(authorisationResponse)
                            .build());
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID))
            .thenReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(commonPaymentResponse)
                            .build());
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(SPI_SINGLE_PAYMENT);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData))
            .thenReturn(contextData);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(pisCheckAuthorisationConfirmationService.checkConfirmationCode(contextData, spiCheckConfirmationCodeRequest, SPI_SINGLE_PAYMENT, aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                            .payload(new SpiPaymentConfirmationCodeValidationResponse(Xs2aScaStatus.FINALISED, transactionStatus))
                            .build());

        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(contextData, SPI_SINGLE_PAYMENT, authorisationResponse.getAuthorisationId(), aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
        verify(xs2aUpdatePaymentAfterSpiService).updatePaymentStatus(PAYMENT_ID, transactionStatus);
    }

    @Test
    void processAuthorisationConfirmation_success_checkOnXs2a() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(
            Xs2aScaStatus.FINALISED, PAYMENT_ID, AUTHORISATION_ID, psuIdData, null);
        Authorisation authorisationResponse = buildGetPisAuthorisationResponse();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(pisCheckAuthorisationConfirmationService.checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider)).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<Authorisation>builder()
                                                                                                  .payload(authorisationResponse)
                                                                                                  .build());

        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID)).thenReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                                                                                               .payload(commonPaymentResponse)
                                                                                               .build());
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(SPI_SINGLE_PAYMENT);

        SpiContextData contextData = getSpiContextData();
        SpiPaymentConfirmationCodeValidationResponse response = new SpiPaymentConfirmationCodeValidationResponse(Xs2aScaStatus.FINALISED, Xs2aTransactionStatus.ACSP);
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder().payload(response).build();
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)).thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(contextData);
        when(pisCheckAuthorisationConfirmationService.notifyConfirmationCodeValidation(contextData, true, SPI_SINGLE_PAYMENT, false, aspspConsentDataProvider)).thenReturn(spiResponse);
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(contextData, SPI_SINGLE_PAYMENT, authorisationResponse.getAuthorisationId(), aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);

        verify(xs2aUpdatePaymentAfterSpiService, times(1)).updatePaymentStatus(PAYMENT_ID, response.getTransactionStatus());
        verify(pisCheckAuthorisationConfirmationService, times(1)).checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider);
    }

    @Test
    void processAuthorisationConfirmation_failed_NoAuthorisation() {
        // given
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_404)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_AUTHORISATION))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());


        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<Authorisation>builder()
                                                                                                  .error(TECHNICAL_ERROR)
                                                                                                  .build());

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult.hasError()).isTrue();
        assertThat(actualResult.getErrorHolder()).isEqualToComparingFieldByField(expectedResult.getErrorHolder());
    }

    @Test
    void processAuthorisationConfirmation_failed_WrongScaStatus() {
        // given
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_INVALID))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());


        Authorisation authorisationResponse = buildGetPisAuthorisationResponse();
        authorisationResponse.setScaStatus(Xs2aScaStatus.FAILED);

        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<Authorisation>builder()
                                                                                                  .payload(authorisationResponse)
                                                                                                  .build());

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult.hasError()).isTrue();
        assertThat(actualResult.getErrorHolder()).isEqualToComparingFieldByField(expectedResult.getErrorHolder());
    }

    @Test
    void processAuthorisationConfirmation_failed_wrongCode() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        request.setConfirmationCode("wrong_code");

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_INVALID))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());

        Authorisation authorisationResponse = buildGetPisAuthorisationResponse();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<Authorisation>builder()
                                                                                                  .payload(authorisationResponse)
                                                                                                  .build());

        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID)).thenReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                                                                                               .payload(commonPaymentResponse)
                                                                                               .build());
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(SPI_SINGLE_PAYMENT);

        SpiContextData contextData = getSpiContextData();
        SpiPaymentConfirmationCodeValidationResponse response = new SpiPaymentConfirmationCodeValidationResponse(Xs2aScaStatus.FAILED, Xs2aTransactionStatus.RJCT);
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder().payload(response).build();
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)).thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(contextData);
        when(pisCheckAuthorisationConfirmationService.notifyConfirmationCodeValidation(contextData, false, SPI_SINGLE_PAYMENT, false, aspspConsentDataProvider)).thenReturn(spiResponse);
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(contextData, SPI_SINGLE_PAYMENT, authorisationResponse.getAuthorisationId(), aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult.hasError()).isTrue();
        assertThat(actualResult.getErrorHolder()).isEqualToComparingFieldByField(expectedResult.getErrorHolder());
        verify(xs2aUpdatePaymentAfterSpiService, times(1)).updatePaymentStatus(PAYMENT_ID, response.getTransactionStatus());
    }

    @Test
    void processAuthorisationConfirmation_checkOnSpi_spiError() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_INVALID))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());

        Authorisation authorisationResponse = buildGetPisAuthorisationResponse();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(pisCheckAuthorisationConfirmationService.checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider)).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<Authorisation>builder()
                                                                                                  .payload(authorisationResponse)
                                                                                                  .build());

        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID)).thenReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                                                                                               .payload(commonPaymentResponse)
                                                                                               .build());
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(SPI_SINGLE_PAYMENT);

        SpiContextData contextData = getSpiContextData();
        TppMessage spiErrorMessage = new TppMessage(MessageErrorCode.SCA_INVALID);
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder().error(spiErrorMessage).build();
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)).thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(contextData);
        when(pisCheckAuthorisationConfirmationService.notifyConfirmationCodeValidation(contextData, true, SPI_SINGLE_PAYMENT, false, aspspConsentDataProvider)).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS)).thenReturn(errorHolder);

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult.hasError()).isTrue();
        assertThat(actualResult.getErrorHolder()).isEqualToComparingFieldByField(expectedResult.getErrorHolder());
        verify(xs2aUpdatePaymentAfterSpiService, never()).updatePaymentStatus(any(), any());
        verify(pisCheckAuthorisationConfirmationService, times(1)).checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider);
    }

    @Test
    void processAuthorisationConfirmation_failed_errorOnSpi() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                                                                                    .error(new TppMessage(PSU_CREDENTIALS_INVALID))
                                                                                    .build();

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_INVALID))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());
        Authorisation authorisationResponse = buildGetPisAuthorisationResponse();

        SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest = new SpiCheckConfirmationCodeRequest(request.getConfirmationCode(), AUTHORISATION_ID);
        SpiContextData contextData = getSpiContextData();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(false);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<Authorisation>builder()
                                                                                                  .payload(authorisationResponse)
                                                                                                  .build());
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID)).thenReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                                                                                               .payload(commonPaymentResponse)
                                                                                               .build());
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(SPI_SINGLE_PAYMENT);

        when(spiContextDataProvider.provideWithPsuIdData(psuIdData))
            .thenReturn(contextData);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(pisCheckAuthorisationConfirmationService.checkConfirmationCode(contextData, spiCheckConfirmationCodeRequest, SPI_SINGLE_PAYMENT, aspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS)).thenReturn(errorHolder);

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private SpiContextData getSpiContextData() {
        return TestSpiDataProvider.defaultSpiContextData();
    }

    private Authorisation buildGetPisAuthorisationResponse() {
        Authorisation response = new Authorisation();
        response.setAuthorisationId(AUTHORISATION_ID);
        response.setParentId(PAYMENT_ID);
        response.setScaStatus(Xs2aScaStatus.UNCONFIRMED);
        response.setScaAuthenticationData(CONFIRMATION_CODE);
        response.setAuthorisationType(AuthorisationType.PIS_CREATION);
        response.setScaAuthenticationData(SCA_AUTHENTICATION_DATA);
        return response;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildUpdatePisCommonPaymentPsuDataRequest() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-update-pis-common-payment-psu-data-request.json",
                                         Xs2aUpdatePisCommonPaymentPsuDataRequest.class);

        request.setConfirmationCode(CONFIRMATION_CODE);
        request.setPaymentId(PAYMENT_ID);
        request.setAuthorisationId(AUTHORISATION_ID);
        request.setScaStatus(Xs2aScaStatus.UNCONFIRMED);
        request.setPsuData(buildPsuIdData());
        request.setPaymentProduct(PAYMENT_PRODUCT);
        request.setPaymentService(PaymentType.SINGLE);

        return request;
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    }

}
