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

package de.adorsys.psd2.consent.repository.specification;

import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.integration.test.BaseTest;
import de.adorsys.psd2.integration.test.TestDBConfiguration;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = TestDBConfiguration.class,
    initializers = {PiisConsentEntitySpecificationIT.Initializer.class})
class PiisConsentEntitySpecificationIT extends BaseTest {

    private static final String TPP_AUTHORISATION_NUMBER = "12345987";
    private static final String INSTANCE_ID = "UNDEFINED";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("987654321", "", "", "", "");

    @Autowired
    private PiisConsentEntitySpecification piisConsentEntitySpecification;

    @Autowired
    private ConsentJpaRepository consentJpaRepository;
    @Autowired
    private TppInfoRepository tppInfoRepository;

    private AccountReference accountReference;

    @BeforeEach
    void setUp() {
        clearData();

        TppInfoEntity tppInfo = tppInfoRepository.save(
            jsonReader.getObjectFromFile("json/specification/tpp-info-entity.json", TppInfoEntity.class));

        ConsentEntity consentEntity = jsonReader.getObjectFromFile("json/specification/consent-entity.json", ConsentEntity.class);
        consentEntity.setConsentType(ConsentType.PIIS_ASPSP.getName());
        consentEntity.getTppInformation().setTppInfo(tppInfo);
        consentEntity.getAspspAccountAccesses().get(0).setConsent(consentEntity);
        consentJpaRepository.save(consentEntity);

        accountReference = new AccountReference();
        accountReference.setIban("DE15500105172295759744");
        accountReference.setCurrency(Currency.getInstance("EUR"));
    }

    @Test
    @Transactional
    void byPsuDataAndInstanceId() {
        List<ConsentEntity> actual = consentJpaRepository.findAll(
            piisConsentEntitySpecification.byPsuDataAndInstanceId(
                PSU_ID_DATA,
                INSTANCE_ID
            ));

        assertTrue(CollectionUtils.isNotEmpty(actual));
        assertEquals(1, actual.size());
        ConsentEntity actualConsent = actual.get(0);
        assertEquals(PSU_ID_DATA.getPsuId(), actualConsent.getPsuDataList().get(0).getPsuId());
        assertEquals(INSTANCE_ID, actual.get(0).getInstanceId());
    }

    @Test
    @Transactional
    void byPsuIdDataAndAuthorisationNumberAndAccountReference() {
        List<ConsentEntity> actual = consentJpaRepository.findAll(
            piisConsentEntitySpecification.byPsuIdDataAndAuthorisationNumberAndAccountReferenceAndInstanceId(
                PSU_ID_DATA,
                TPP_AUTHORISATION_NUMBER,
                accountReference,
                INSTANCE_ID
            ));

        assertTrue(CollectionUtils.isNotEmpty(actual));
        assertEquals(1, actual.size());
        ConsentEntity actualConsent = actual.get(0);
        assertEquals(PSU_ID_DATA.getPsuId(), actualConsent.getPsuDataList().get(0).getPsuId());
        assertEquals(TPP_AUTHORISATION_NUMBER, actualConsent.getTppInformation().getTppInfo().getAuthorisationNumber());
        assertEquals(accountReference.getIban(), actualConsent.getAspspAccountAccesses().get(0).getAccountIdentifier());
        assertEquals(AccountReferenceType.IBAN, actualConsent.getAspspAccountAccesses().get(0).getAccountReferenceType());
        assertEquals(accountReference.getCurrency(), actualConsent.getAspspAccountAccesses().get(0).getCurrency());
        assertEquals(INSTANCE_ID, actualConsent.getInstanceId());
    }

    @Test
    @Transactional
    void byCurrencyAndAccountReferenceSelector() {
        List<ConsentEntity> actual = consentJpaRepository.findAll(
            piisConsentEntitySpecification.byCurrencyAndAccountReferenceSelector(
                accountReference.getCurrency(),
                accountReference.getUsedAccountReferenceSelector()
            ));

        assertTrue(CollectionUtils.isNotEmpty(actual));
        assertEquals(1, actual.size());
        ConsentEntity actualConsent = actual.get(0);
        assertEquals(accountReference.getIban(), actualConsent.getAspspAccountAccesses().get(0).getAccountIdentifier());
        assertEquals(AccountReferenceType.IBAN, actualConsent.getAspspAccountAccesses().get(0).getAccountReferenceType());
        assertEquals(accountReference.getCurrency(), actualConsent.getAspspAccountAccesses().get(0).getCurrency());
    }

    @Test
    @Transactional
    void byAccountReferenceSelector() {
        List<ConsentEntity> actual = consentJpaRepository.findAll(
            piisConsentEntitySpecification.byAccountReferenceSelector(
                accountReference.getUsedAccountReferenceSelector()
            ));

        assertTrue(CollectionUtils.isNotEmpty(actual));
        assertEquals(1, actual.size());
        ConsentEntity actualConsent = actual.get(0);
        assertEquals(accountReference.getIban(), actualConsent.getAspspAccountAccesses().get(0).getAccountIdentifier());
        assertEquals(AccountReferenceType.IBAN, actualConsent.getAspspAccountAccesses().get(0).getAccountReferenceType());
    }
}
