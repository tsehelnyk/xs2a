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

package de.adorsys.psd2.consent.service.account;

import de.adorsys.psd2.core.data.Xs2aConsentAccountAccess;
import de.adorsys.psd2.xs2a.core.profile.Xs2aAccountReference;
import de.adorsys.psd2.xs2a.core.profile.Xs2aAdditionalInformationAccess;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AccountAccessUpdater {
    @NotNull
    public Xs2aConsentAccountAccess updateAccountReferencesInAccess(@NotNull Xs2aConsentAccountAccess existingAccess,
                                                                    @NotNull Xs2aConsentAccountAccess newAccess) {
        if (hasNoAccountReferences(existingAccess)) {
            return new Xs2aConsentAccountAccess(newAccess.getAccounts(), newAccess.getBalances(), newAccess.getTransactions(), newAccess.getAdditionalInformationAccess());
        }

        List<Xs2aAccountReference> updatedAccounts = existingAccess.getAccounts().stream()
                                                     .map(ref -> updateAccountReference(ref, newAccess.getAccounts()))
                                                     .collect(Collectors.toList());
        List<Xs2aAccountReference> updatedBalances = existingAccess.getBalances().stream()
                                                     .map(ref -> updateAccountReference(ref, newAccess.getBalances()))
                                                     .collect(Collectors.toList());
        List<Xs2aAccountReference> updatedTransactions = existingAccess.getTransactions().stream()
                                                         .map(ref -> updateAccountReference(ref, newAccess.getTransactions()))
                                                         .collect(Collectors.toList());

        Xs2aAdditionalInformationAccess updatedAdditionalInformation = updateAccountReferencesInAdditionalInformation(existingAccess.getAdditionalInformationAccess(),
                                                                                                                  newAccess.getAdditionalInformationAccess());

        return new Xs2aConsentAccountAccess(updatedAccounts, updatedBalances, updatedTransactions, updatedAdditionalInformation);
    }

    private boolean hasNoAccountReferences(Xs2aConsentAccountAccess accountAccess) {
        Xs2aAdditionalInformationAccess additionalInformationAccess = accountAccess.getAdditionalInformationAccess();
        boolean hasNoAdditionalInformationReferences = additionalInformationAccess == null
                                                           || CollectionUtils.isEmpty(additionalInformationAccess.getOwnerName());

        return CollectionUtils.isEmpty(accountAccess.getAccounts())
                   && CollectionUtils.isEmpty(accountAccess.getBalances())
                   && CollectionUtils.isEmpty(accountAccess.getTransactions())
                   && hasNoAdditionalInformationReferences;
    }

    private Xs2aAdditionalInformationAccess updateAccountReferencesInAdditionalInformation(Xs2aAdditionalInformationAccess existingAccess,
                                                                                           Xs2aAdditionalInformationAccess requestedAccess) {
        if (isAdditionalInformationAbsent(existingAccess) || isAdditionalInformationAbsent(requestedAccess)) {
            return existingAccess;
        }
        return new Xs2aAdditionalInformationAccess(getAccountReferences(existingAccess.getOwnerName(), requestedAccess.getOwnerName()),
                                               getAccountReferences(existingAccess.getTrustedBeneficiaries(), requestedAccess.getTrustedBeneficiaries()));
    }

    private List<Xs2aAccountReference> getAccountReferences(List<Xs2aAccountReference> existing, List<Xs2aAccountReference> requested){
        if (existing != null && requested != null) {
            return existing.stream()
                       .map(ref -> updateAccountReference(ref, requested))
                       .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private boolean isAdditionalInformationAbsent(Xs2aAdditionalInformationAccess additionalInformationAccess) {
        return additionalInformationAccess == null || isAdditionalInformationEmpty(additionalInformationAccess);
    }

    private boolean isAdditionalInformationEmpty(Xs2aAdditionalInformationAccess additionalInformationAccess) {
        return isOwnerNameAbsent(additionalInformationAccess) && isTrustedBeneficiariesAbsent(additionalInformationAccess);
    }

    private boolean isOwnerNameAbsent(Xs2aAdditionalInformationAccess additionalInformationAccess) {
        return additionalInformationAccess.getOwnerName() == null;
    }

    private boolean isTrustedBeneficiariesAbsent(Xs2aAdditionalInformationAccess additionalInformationAccess) {
        return additionalInformationAccess.getTrustedBeneficiaries() == null;
    }

    private Xs2aAccountReference updateAccountReference(Xs2aAccountReference existingReference, List<Xs2aAccountReference> requestedAspspReferences) {
        return requestedAspspReferences.stream()
                   .filter(aspsp -> aspsp.getUsedAccountReferenceSelector().equals(existingReference.getUsedAccountReferenceSelector()))
                   .filter(aspsp -> Objects.equals(aspsp.getCurrency(), existingReference.getCurrency()))
                   .findFirst()
                   .orElse(existingReference);
    }
}
