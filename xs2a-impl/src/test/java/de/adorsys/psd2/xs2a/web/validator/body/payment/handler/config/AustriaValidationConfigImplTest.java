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

package de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config;

import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AustriaValidationConfigImplTest {
    private final JsonReader jsonReader = new JsonReader();
    private AustriaValidationConfigImpl actual;

    @BeforeEach
    void setUp() {
        actual = new AustriaValidationConfigImpl();
    }

    @Test
    void checkConfiguration() {
        AustriaValidationConfigImpl expected = jsonReader.getObjectFromFile("json/validation/austria-payment-validation-config.json",
                                                                            AustriaValidationConfigImpl.class);
        assertEquals(expected, actual);
    }
}
