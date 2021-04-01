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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.core.data.Xs2aConsentAccountAccess;
import de.adorsys.psd2.xs2a.core.profile.Xs2aAccountReference;
import de.adorsys.psd2.xs2a.core.profile.Xs2aAdditionalInformationAccess;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAdditionalInformationAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Component
@RequiredArgsConstructor
public class SpiToXs2aAccountAccessMapper {
    private final SpiToXs2aAccountReferenceMapper spiToXs2aAccountReferenceMapper;

    public Optional<Xs2aConsentAccountAccess> mapToAccountAccess(SpiAccountAccess access) {
        return Optional.ofNullable(access)
                   .map(aa ->
                            new Xs2aConsentAccountAccess(
                                spiToXs2aAccountReferenceMapper.mapToXs2aAccountReferences(aa.getAccounts()),
                                spiToXs2aAccountReferenceMapper.mapToXs2aAccountReferences(aa.getBalances()),
                                spiToXs2aAccountReferenceMapper.mapToXs2aAccountReferences(aa.getTransactions()),
                                mapToAdditionalInformationAccess(aa.getSpiAdditionalInformationAccess())));
    }

    public Xs2aConsentAccountAccess getAccessForGlobalOrAllAvailableAccountsConsent(CreateConsentReq request) {
        return new Xs2aConsentAccountAccess(
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            modifyAdditionalInformationAccessOnGlobalOrAllAvailableAccountsConsent(request.getAccess().getAdditionalInformationAccess())
        );
    }

    private Xs2aAdditionalInformationAccess modifyAdditionalInformationAccessOnGlobalOrAllAvailableAccountsConsent(Xs2aAdditionalInformationAccess info) {
        if (info == null || info.noAdditionalInformationAccess()) {
            return null;
        }

        UnaryOperator<List<Xs2aAccountReference>> modifier = list -> list == null ? null : Collections.emptyList();

        return new Xs2aAdditionalInformationAccess(modifier.apply(info.getOwnerName()), modifier.apply(info.getTrustedBeneficiaries()));
    }

    private Xs2aAdditionalInformationAccess mapToAdditionalInformationAccess(SpiAdditionalInformationAccess spiAdditionalInformationAccess) {
        return Optional.ofNullable(spiAdditionalInformationAccess)
                   .map(info -> new Xs2aAdditionalInformationAccess(spiToXs2aAccountReferenceMapper.mapToXs2aAccountReferences(info.getOwnerName()),
                                                                spiToXs2aAccountReferenceMapper.mapToXs2aAccountReferences(info.getTrustedBeneficiaries())))
                   .orElse(null);
    }
}
