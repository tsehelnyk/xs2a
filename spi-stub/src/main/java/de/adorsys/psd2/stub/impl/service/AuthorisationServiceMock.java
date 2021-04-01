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

package de.adorsys.psd2.stub.impl.service;

import de.adorsys.psd2.xs2a.core.authorisation.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.core.consent.Xs2aConsentStatus;
import de.adorsys.psd2.xs2a.core.sca.Xs2aChallengeData;
import de.adorsys.psd2.xs2a.core.sca.Xs2aScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAvailableScaMethodsResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AuthorisationServiceMock {
    public SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods() {
        List<Xs2aAuthenticationObject> spiScaMethods = new ArrayList<>();
        Xs2aAuthenticationObject sms = new Xs2aAuthenticationObject();
        sms.setAuthenticationType("SMS_OTP");
        sms.setAuthenticationMethodId("sms");
        sms.setName("some-sms-name");
        spiScaMethods.add(sms);
        Xs2aAuthenticationObject push = new Xs2aAuthenticationObject();
        push.setAuthenticationType("PUSH_OTP");
        push.setAuthenticationMethodId("push");
        push.setDecoupled(true);
        spiScaMethods.add(push);

        return SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                   .payload(new SpiAvailableScaMethodsResponse(false, spiScaMethods))
                   .build();
    }

    public SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode() {
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
        Xs2aAuthenticationObject method = new Xs2aAuthenticationObject();
        method.setAuthenticationMethodId("sms");
        method.setAuthenticationType("SMS_OTP");
        spiAuthorizationCodeResult.setSelectedScaMethod(method);
        spiAuthorizationCodeResult.setChallengeData(new Xs2aChallengeData(null, Collections.singletonList("some data"), "some link", 100, null, "info"));

        return SpiResponse.<SpiAuthorizationCodeResult>builder()
                   .payload(spiAuthorizationCodeResult)
                   .build();
    }

    public SpiResponse<SpiConsentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(boolean confirmationCodeValidationResult) {
        Xs2aScaStatus scaStatus = confirmationCodeValidationResult ? Xs2aScaStatus.FINALISED : Xs2aScaStatus.FAILED;
        Xs2aConsentStatus consentStatus = confirmationCodeValidationResult ? Xs2aConsentStatus.VALID : Xs2aConsentStatus.REJECTED;

        SpiConsentConfirmationCodeValidationResponse response = new SpiConsentConfirmationCodeValidationResponse(scaStatus, consentStatus);

        return SpiResponse.<SpiConsentConfirmationCodeValidationResponse>builder()
                   .payload(response)
                   .build();
    }
}
