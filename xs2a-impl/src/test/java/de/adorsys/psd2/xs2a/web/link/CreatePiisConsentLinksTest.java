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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aConfirmationOfFundsResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePiisConsentLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String CONSENT_ID = "9mp1PaotpXSToNCi";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private static final String REDIRECT_LINK = "built_redirect_link";
    private static final String CONFIRMATION_LINK = "confirmation_link";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    private static final HrefType SELF = new HrefType("http://url/v2/consents/confirmation-of-funds/9mp1PaotpXSToNCi");
    private static final HrefType STATUS = new HrefType("http://url/v2/consents/confirmation-of-funds/9mp1PaotpXSToNCi/status");
    private static final HrefType START_AUTHORISATION_WITH_PSU_AUTHENTICATION = new HrefType("http://url/v2/consents/confirmation-of-funds/9mp1PaotpXSToNCi/authorisations");
    private static final HrefType SCA_STATUS = new HrefType("http://url/v2/consents/confirmation-of-funds/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;
    @Mock
    private RedirectIdService redirectIdService;

    private CreatePiisConsentLinks links;
    private Xs2aConfirmationOfFundsResponse response;

    private Links expectedLinks;

    @BeforeEach
    void setUp() {
        expectedLinks = new AbstractLinks(HTTP_URL);

        response = new Xs2aConfirmationOfFundsResponse(ConsentStatus.RECEIVED.getValue(), CONSENT_ID, false, INTERNAL_REQUEST_ID);
        response.setAuthorizationId(AUTHORISATION_ID);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndExplicitMethodAndPsuDataIsEmptyAndMultiLevelRequired() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);
        expectedLinks.setStartAuthorisationWithPsuAuthentication(START_AUTHORISATION_WITH_PSU_AUTHENTICATION);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndExplicitMethodAndPsuDataIsNotEmptyAndMultiLevelRequired() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);
        expectedLinks.setStartAuthorisationWithPsuAuthentication(START_AUTHORISATION_WITH_PSU_AUTHENTICATION);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndExplicitMethodAndMultiLevelNotRequired() {
        // Given
        response.setAuthorizationId(AUTHORISATION_ID);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);
        expectedLinks.setStartAuthorisationWithPsuAuthentication(START_AUTHORISATION_WITH_PSU_AUTHENTICATION);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_Embedded_Explicit_MultiLevelScaNotRequired_SigningBasketModeActive() {
        // Given
        response.setAuthorizationId(AUTHORISATION_ID);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(true)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);
        expectedLinks.setStartAuthorisation(START_AUTHORISATION_WITH_PSU_AUTHENTICATION);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndImplicitMethodAndPsuDataIsEmpty() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);
        expectedLinks.setScaStatus(SCA_STATUS);
        expectedLinks.setUpdatePsuAuthentication(SCA_STATUS);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndImplicitMethodAndPsuDataIsNotEmpty() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);
        expectedLinks.setScaStatus(SCA_STATUS);
        expectedLinks.setUpdatePsuAuthentication(SCA_STATUS);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndDecoupledScaApproachAndExplicitMethodAndPsuDataIsEmptyAndMultiLevelRequired() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);
        expectedLinks.setStartAuthorisation(START_AUTHORISATION_WITH_PSU_AUTHENTICATION);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_Decoupled_Explicit() {
        // Given
        response.setAuthorizationId(AUTHORISATION_ID);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(true)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);
        expectedLinks.setStartAuthorisation(START_AUTHORISATION_WITH_PSU_AUTHENTICATION);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_Decoupled_implicit() {
        // Given
        response.setAuthorizationId(AUTHORISATION_ID);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(true)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);
        expectedLinks.setScaStatus(SCA_STATUS);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndExplicitMethod() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);
        expectedLinks.setStartAuthorisation(START_AUTHORISATION_WITH_PSU_AUTHENTICATION);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndOauthFlow() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, ScaRedirectFlow.OAUTH);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);
        expectedLinks.setScaRedirect(new HrefType(null));
        expectedLinks.setScaStatus(SCA_STATUS);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproach_isNull(){
        // Given
        response.setAuthorizationId(null);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
                                            .httpUrl(HTTP_URL)
                                            .isExplicitMethod(false)
                                            .isSigningBasketModeActive(false)
                                            .isAuthorisationConfirmationRequestMandated(false)
                                            .instanceId("")
                                            .build();
        links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, ScaRedirectFlow.OAUTH);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndImplicitMethod() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildConsentScaRedirectLink(CONSENT_ID, AUTHORISATION_ID, INTERNAL_REQUEST_ID, "", ConsentType.PIIS_TPP)).thenReturn(REDIRECT_LINK);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setScaStatus(SCA_STATUS);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndImplicitMethod_confirmation() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildConsentScaRedirectLink(CONSENT_ID, AUTHORISATION_ID, INTERNAL_REQUEST_ID, "", ConsentType.PIIS_TPP)).thenReturn(REDIRECT_LINK);
        when(redirectLinkBuilder.buildConfirmationLink(CONSENT_ID, AUTHORISATION_ID, ConsentType.PIIS_TPP)).thenReturn(CONFIRMATION_LINK);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(true)
            .instanceId("")
            .build();
        links = new CreatePiisConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(SELF);
        expectedLinks.setStatus(STATUS);
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setScaStatus(SCA_STATUS);
        expectedLinks.setConfirmation(new HrefType("http://url/confirmation_link"));

        // Then
        assertEquals(expectedLinks, links);
    }
}
