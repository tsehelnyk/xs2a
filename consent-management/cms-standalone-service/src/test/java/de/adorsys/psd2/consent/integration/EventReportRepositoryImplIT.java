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

package de.adorsys.psd2.consent.integration;

import com.google.common.collect.Sets;
import de.adorsys.psd2.consent.integration.config.IntegrationTestConfiguration;
import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.persist.EventReportRepository;
import de.adorsys.psd2.event.persist.EventRepository;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integration-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@DataJpaTest
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
class EventReportRepositoryImplIT {
    private static final String INSTANCE_ID = "3de76f19-1df7-44d8-b760-ca972d2f945c";
    private static final String CONSENT_ID = "fa6e687b-1ac9-4b1a-9c74-357c35c82ba1";
    private static final String PAYMENT_ID = "j-t4XyLJTzQkonfSTnyxIMc";
    private static final OffsetDateTime START = OffsetDateTime.parse("2019-07-09T12:29:50.042136Z");
    private static final OffsetDateTime END = OffsetDateTime.parse("2019-07-09T14:29:50.042136Z");

    @Autowired
    private EventReportRepository repository;

    @Autowired
    private EventRepository eventRepository;

    private ReportEvent expectedEvent;
    private final JsonReader jsonReader = new JsonReader();

    @BeforeAll
    void setUp() {
        EventPO eventPO = jsonReader.getObjectFromFile("json/event.json", EventPO.class);
        expectedEvent = buildReportEvent(eventPO);
        eventRepository.save(eventPO);
    }

    @Test
    void getEventsForPeriod() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriod(START, END, INSTANCE_ID, 0, 20);

        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(expectedEvent, updateToUTC(eventsForPeriod.get(0)));
    }

    @Test
    void getEventsForPeriodAndConsentId() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndConsentId(START, END, CONSENT_ID, INSTANCE_ID, 0, 20);

        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(expectedEvent, updateToUTC(eventsForPeriod.get(0)));
    }

    @Test
    void getEventsForPeriodAndPaymentId() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndPaymentId(START, END, PAYMENT_ID, INSTANCE_ID, 0, 20);

        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(expectedEvent, updateToUTC(eventsForPeriod.get(0)));
    }

    @Test
    void getEventsForPeriodAndEventOrigin() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndEventOrigin(START, END, EventOrigin.TPP, INSTANCE_ID, 0, 20);

        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(expectedEvent, updateToUTC(eventsForPeriod.get(0)));
    }

    @Test
    void getEventsForPeriodAndEventType() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndEventType(START, END, EventType.PAYMENT_INITIATION_REQUEST_RECEIVED, INSTANCE_ID, 0, 20);

        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEquals(expectedEvent, updateToUTC(eventsForPeriod.get(0)));
    }

    private ReportEvent updateToUTC(ReportEvent reportEvent) {
        reportEvent.setTimestamp(reportEvent.getTimestamp().withOffsetSameInstant(ZoneOffset.UTC));
        return reportEvent;
    }

    private ReportEvent buildReportEvent(EventPO eventPO) {
        ReportEvent reportEvent = new ReportEvent();
        reportEvent.setId(1L);
        reportEvent.setTimestamp(eventPO.getTimestamp());
        reportEvent.setConsentId(eventPO.getConsentId());
        reportEvent.setPaymentId(eventPO.getPaymentId());
        reportEvent.setPayload(eventPO.getPayload());
        reportEvent.setEventOrigin(eventPO.getEventOrigin());
        reportEvent.setEventType(eventPO.getEventType());
        reportEvent.setInstanceId(eventPO.getInstanceId());
        reportEvent.setTppAuthorisationNumber(eventPO.getTppAuthorisationNumber());
        reportEvent.setXRequestId(eventPO.getXRequestId());
        reportEvent.setPsuIdData(Sets.newHashSet(eventPO.getPsuIdData()));
        reportEvent.setInternalRequestId(eventPO.getInternalRequestId());
        return reportEvent;
    }
}
