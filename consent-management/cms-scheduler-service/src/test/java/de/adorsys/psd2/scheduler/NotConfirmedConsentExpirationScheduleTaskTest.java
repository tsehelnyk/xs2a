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

package de.adorsys.psd2.scheduler;

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.service.AisConsentConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotConfirmedConsentExpirationScheduleTaskTest {

    @InjectMocks
    private NotConfirmedConsentExpirationScheduleTask scheduleTask;

    @Mock
    private AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    @Mock
    private ConsentJpaRepository consentJpaRepository;

    @Captor
    private ArgumentCaptor<ArrayList<String>> consentsCaptor;

    @Test
    void obsoleteNotConfirmedConsentIfExpired() {
        // Given
        when(consentJpaRepository.findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED)))
            .thenReturn(getTestConsentEntityList());
        when(aisConsentConfirmationExpirationService.isConfirmationExpired(any(ConsentEntity.class)))
            .thenReturn(true, false, true);

        // When
        scheduleTask.obsoleteNotConfirmedConsentIfExpired();

        // Then
        verify(consentJpaRepository, times(1))
            .findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED));
        verify(aisConsentConfirmationExpirationService, times(2)).isConfirmationExpired(any(ConsentEntity.class));
        verify(aisConsentConfirmationExpirationService, times(1)).updateConsentListOnConfirmationExpirationByExternalIds(consentsCaptor.capture());

        assertEquals(1, consentsCaptor.getValue().size());
        assertEquals("first id", consentsCaptor.getValue().get(0));
    }

    @Test
    void obsoleteNotConfirmedConsentIfExpired_emptyList() {
        // Given
        when(consentJpaRepository.findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED)))
            .thenReturn(Collections.emptyList());

        // When
        scheduleTask.obsoleteNotConfirmedConsentIfExpired();

        // Then
        verify(consentJpaRepository, times(1))
            .findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED));
        verify(aisConsentConfirmationExpirationService, never()).isConfirmationExpired(any(ConsentEntity.class));
        verify(aisConsentConfirmationExpirationService, never()).updateConsentListOnConfirmationExpirationByExternalIds(anyList());
    }

    private List<ConsentEntity> getTestConsentEntityList() {
        ConsentEntity firstEntity = new ConsentEntity();
        firstEntity.setExternalId("first id");
        firstEntity.setSigningBasketBlocked(false);
        ConsentEntity secondEntity = new ConsentEntity();
        secondEntity.setExternalId("second id");
        secondEntity.setSigningBasketBlocked(false);
        ConsentEntity thirdEntity = new ConsentEntity();
        thirdEntity.setExternalId("third id");
        thirdEntity.setSigningBasketBlocked(true);
        return List.of(firstEntity, secondEntity, thirdEntity);
    }
}
