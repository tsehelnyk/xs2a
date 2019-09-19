/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.PaymentTypeAndProductValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationValidator;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_403;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePisCommonPaymentPsuDataValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final TransactionStatus REJECTED_TRANSACTION_STATUS = TransactionStatus.RJCT;
    private static final String AUTHORISATION_ID = "authorisation id";
    private static final String INVALID_AUTHORISATION_ID_FOR_ENDPOINT = "invalid authorisation id for endpoint";
    private static final String INVALID_AUTHORISATION_ID = "invalid authorisation id";

    private static final UUID X_REQUEST_ID = UUID.fromString("1af360bc-13cb-40ab-9aa0-cc0d6af4510c");
    private static final String CORRECT_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String WRONG_PAYMENT_PRODUCT = "sepa-credit-transfers111";

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED, "Invalid TPP"));
    private static final MessageError EXPIRED_PAYMENT_ERROR = new MessageError(PIS_403, of(RESOURCE_EXPIRED_403));
    private static final MessageError BLOCKED_ENDPOINT_ERROR = new MessageError(PIS_403, of(SERVICE_BLOCKED));
    private static final MessageError PAYMENT_PRODUCT_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_404, TppMessageInformation.of(PRODUCT_UNKNOWN));
    private static final MessageError INVALID_AUTHORISATION_ERROR = new MessageError(PIS_403, of(RESOURCE_UNKNOWN_403));
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psu-id", null, null, null);

    @Mock
    private PisTppInfoValidator pisTppInfoValidator;
    @Mock
    private PisEndpointAccessCheckerService pisEndpointAccessCheckerService;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    PaymentTypeAndProductValidator paymentProductAndTypeValidator;
    @Mock
    private PisAuthorisationValidator pisAuthorisationValidator;

    @InjectMocks
    private UpdatePisCommonPaymentPsuDataValidator updatePisCommonPaymentPsuDataValidator;

    @Before
    public void setUp() {
        // Inject pisTppInfoValidator via setter
        updatePisCommonPaymentPsuDataValidator.setPisValidators(pisTppInfoValidator, paymentProductAndTypeValidator);

        when(requestProviderService.getRequestId()).thenReturn(X_REQUEST_ID);

        when(pisTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(pisTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(true);
        when(pisEndpointAccessCheckerService.isEndpointAccessible(INVALID_AUTHORISATION_ID_FOR_ENDPOINT, PaymentAuthorisationType.CREATED))
            .thenReturn(false);
        when(paymentProductAndTypeValidator.validateTypeAndProduct(PaymentType.SINGLE, CORRECT_PAYMENT_PRODUCT))
            .thenReturn(ValidationResult.valid());
        when(paymentProductAndTypeValidator.validateTypeAndProduct(PaymentType.SINGLE, WRONG_PAYMENT_PRODUCT))
            .thenReturn(ValidationResult.invalid(PAYMENT_PRODUCT_VALIDATION_ERROR));
    }

    @Test
    public void validate_withValidPaymentObject_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse, PSU_ID_DATA))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, AUTHORISATION_ID));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidPaymentProduct_shouldReturnPaymentProductValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        commonPaymentResponse.setPaymentProduct(WRONG_PAYMENT_PRODUCT);
        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, AUTHORISATION_ID));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PAYMENT_PRODUCT_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, AUTHORISATION_ID));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidAuthorisationForEndpoint_shouldReturnValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, INVALID_AUTHORISATION_ID_FOR_ENDPOINT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(BLOCKED_ENDPOINT_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withExpiredPaymentObject_shouldReturnValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(REJECTED_TRANSACTION_STATUS, TPP_INFO);

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, AUTHORISATION_ID));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(EXPIRED_PAYMENT_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidTppAndExpiredPaymentAndInvalidAuthorisationForEndpoint_shouldReturnTppValidationErrorFirst() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(REJECTED_TRANSACTION_STATUS, INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, INVALID_AUTHORISATION_ID_FOR_ENDPOINT));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidAuthorisation_shouldReturnAuthorisationValidationError() {
        // Given
        when(pisEndpointAccessCheckerService.isEndpointAccessible(INVALID_AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(true);
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TRANSACTION_STATUS, TPP_INFO);
        when(pisAuthorisationValidator.validate(INVALID_AUTHORISATION_ID, commonPaymentResponse, PSU_ID_DATA))
            .thenReturn(ValidationResult.invalid(INVALID_AUTHORISATION_ERROR));

        // When
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, INVALID_AUTHORISATION_ID));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(INVALID_AUTHORISATION_ERROR, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TransactionStatus transactionStatus, TppInfo tppInfo) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTransactionStatus(transactionStatus);
        pisCommonPaymentResponse.setTppInfo(tppInfo);
        pisCommonPaymentResponse.setPaymentProduct(CORRECT_PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setPaymentType(PaymentType.SINGLE);
        return pisCommonPaymentResponse;
    }

    private UpdatePisCommonPaymentPsuDataPO buildUpdatePisCommonPaymentPsuDataPO(PisCommonPaymentResponse commonPaymentResponse, String authorisationId) {
        return new UpdatePisCommonPaymentPsuDataPO(commonPaymentResponse, authorisationId, PSU_ID_DATA);
    }
}
