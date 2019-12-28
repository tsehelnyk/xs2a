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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.DayOfExecution;
import de.adorsys.psd2.model.ExecutionRule;
import de.adorsys.psd2.model.FrequencyCode;
import de.adorsys.psd2.model.PeriodicPaymentInitiationXmlPart2StandingorderTypeJson;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Xs2aObjectMapper.class})
public class PaymentModelMapperXs2aTest {

    private static final String CONTENT = "payment content";

    private PaymentModelMapperXs2a paymentModelMapper;
    @Autowired
    private Xs2aObjectMapper xs2aObjectMapper;

    private MockHttpServletRequest mockHttpServletRequest;
    private JsonReader jsonReader = new JsonReader();


    @Before
    public void setUp() {
        mockHttpServletRequest = new MockHttpServletRequest();
        paymentModelMapper = new PaymentModelMapperXs2a(mockHttpServletRequest, xs2aObjectMapper);
    }

    @Test
    public void mapToXs2aPayment() {
        mockHttpServletRequest.setContent(CONTENT.getBytes());

        byte[] actual = paymentModelMapper.mapToXs2aPayment();

        assertEquals(CONTENT, new String(actual));
    }

    @Test
    public void mapToXs2aRawPayment_notPeriodic() {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPaymentType(PaymentType.SINGLE);

        mockHttpServletRequest.setContent(CONTENT.getBytes());

        byte[] actual = paymentModelMapper.mapToXs2aRawPayment(requestParameters, "xml", null);

        assertEquals(CONTENT, new String(actual));
    }

    @Test
    public void mapToXs2aRawPayment_periodic() {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPaymentType(PaymentType.PERIODIC);

        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingOrderType = new PeriodicPaymentInitiationXmlPart2StandingorderTypeJson();
        jsonStandingOrderType.setDayOfExecution(DayOfExecution._5);
        jsonStandingOrderType.setExecutionRule(ExecutionRule.FOLLOWING);
        jsonStandingOrderType.setFrequency(FrequencyCode.MONTHLY);

        byte[] actual = paymentModelMapper.mapToXs2aRawPayment(requestParameters, "xml", jsonStandingOrderType);

        String expected = jsonReader.getStringFromFile("json/web/mapper/raw-periodic-payment.txt").trim();
        assertEquals(expected, new String(actual));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mapToXs2aRawPayment_exceptionWhenXmlPartIsNull() {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPaymentType(PaymentType.PERIODIC);

        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingOrderType = new PeriodicPaymentInitiationXmlPart2StandingorderTypeJson();
        jsonStandingOrderType.setDayOfExecution(DayOfExecution._5);
        jsonStandingOrderType.setExecutionRule(ExecutionRule.FOLLOWING);
        jsonStandingOrderType.setFrequency(FrequencyCode.MONTHLY);

       paymentModelMapper.mapToXs2aRawPayment(requestParameters, null, jsonStandingOrderType);
    }

}
