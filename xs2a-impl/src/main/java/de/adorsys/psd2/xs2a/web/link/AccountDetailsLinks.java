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

import de.adorsys.psd2.core.data.Xs2aConsentAccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.profile.Xs2aAccountReference;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class AccountDetailsLinks extends AbstractLinks {

    public AccountDetailsLinks(String httpUrl, String accountId, AisConsent aisConsent) {
        super(httpUrl);
        AisConsentData consentData = aisConsent.getConsentData();
        Xs2aConsentAccountAccess accountAccess = aisConsent.getAccess();
        boolean isConsentGlobal = consentData.getAllPsd2() != null;
        List<Xs2aAccountReference> balances = accountAccess.getBalances();
        if (hasAccessToSource(balances) &&
                isValidAccountByAccess(accountId, balances, isConsentGlobal)) {

            setBalances(buildPath(UrlHolder.ACCOUNT_BALANCES_URL, accountId));
        }

        List<Xs2aAccountReference> transactions = accountAccess.getTransactions();

        if (hasAccessToSource(transactions)
                && isValidAccountByAccess(accountId, transactions, isConsentGlobal)) {
            setTransactions(buildPath(UrlHolder.ACCOUNT_TRANSACTIONS_URL, accountId));
        }
    }

    private boolean isValidAccountByAccess(String accountId, List<Xs2aAccountReference> allowedAccountData, boolean isConsentGlobal) {
        return isConsentGlobal ||
                   CollectionUtils.isNotEmpty(allowedAccountData)
                       && allowedAccountData.stream()
                              .anyMatch(a -> accountId.equals(a.getResourceId()));
    }

    private boolean hasAccessToSource(List<Xs2aAccountReference> references) {
        if (CollectionUtils.isEmpty(references)) {
            return true;
        }
        return !references.stream()
                    .allMatch(Xs2aAccountReference::isNotIbanAccount);
    }
}
