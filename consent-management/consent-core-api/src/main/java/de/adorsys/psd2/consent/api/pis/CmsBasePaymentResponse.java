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

package de.adorsys.psd2.consent.api.pis;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class CmsBasePaymentResponse implements CmsPayment {

    private String paymentId;
    private PaymentType paymentType;
    private String paymentProduct;
    private List<PsuIdData> psuIdDatas;
    private TppInfo tppInfo;
    private OffsetDateTime creationTimestamp;
    private OffsetDateTime statusChangeTimestamp;


    @Override
    public String getPaymentId() {
        return paymentId;
    }

    @Override
    public PaymentType getPaymentType() {
        return paymentType;
    }

    @Override
    public String getPaymentProduct() {
        return paymentProduct;
    }

    @Override
    public List<PsuIdData> getPsuIdDatas() {
        return psuIdDatas;
    }

    @Override
    public TppInfo getTppInfo() {
        return tppInfo;
    }

    @Override
    public OffsetDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    @Override
    public OffsetDateTime getStatusChangeTimestamp() {
        return getStatusChangeTimestamp();
    }
}
