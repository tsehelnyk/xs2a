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

package de.adorsys.psd2.xs2a.domain.pis;

import de.adorsys.psd2.xs2a.core.profile.Xs2aAccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BulkPaymentTest {
    private static final Xs2aAccountReference DEBTOR_ACCOUNT_1 = new Xs2aAccountReference(AccountReferenceType.IBAN,
                                                                                  "debtor iban 1",
                                                                                  Currency.getInstance("EUR"));
    private static final Xs2aAccountReference DEBTOR_ACCOUNT_2 = new Xs2aAccountReference(AccountReferenceType.IBAN,
                                                                                  "debtor iban 2",
                                                                                  Currency.getInstance("EUR"));
    private static final Xs2aAccountReference CREDITOR_ACCOUNT = new Xs2aAccountReference(AccountReferenceType.IBAN,
                                                                                  "creditor iban",
                                                                                  Currency.getInstance("EUR"));

    @Test
    void getAccountReferences_shouldReturnAllReferences() {
        // Given
        BulkPayment bulkPayment = buildBulkPayment(Collections.singletonList(buildSinglePayment()));

        // When
        Set<Xs2aAccountReference> actualAccountReferences = bulkPayment.getAccountReferences();

        // Then
        Set<Xs2aAccountReference> expectedAccountReferences = new HashSet<>(Arrays.asList(DEBTOR_ACCOUNT_1, DEBTOR_ACCOUNT_2, CREDITOR_ACCOUNT));
        assertEquals(expectedAccountReferences, actualAccountReferences);
    }

    private BulkPayment buildBulkPayment(List<SinglePayment> payments) {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setDebtorAccount(DEBTOR_ACCOUNT_1);
        bulkPayment.setPayments(payments);
        return bulkPayment;
    }

    private SinglePayment buildSinglePayment() {
        SinglePayment singlePayment = new SinglePayment();
        singlePayment.setDebtorAccount(DEBTOR_ACCOUNT_2);
        singlePayment.setCreditorAccount(CREDITOR_ACCOUNT);
        return singlePayment;
    }
}
