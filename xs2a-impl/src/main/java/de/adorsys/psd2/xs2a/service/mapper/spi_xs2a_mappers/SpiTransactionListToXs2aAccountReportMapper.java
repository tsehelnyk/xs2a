/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpiTransactionListToXs2aAccountReportMapper {
    private static final Predicate<SpiTransaction> BOOKED_PREDICATE = SpiTransaction::isBookedTransaction;
    private static final Predicate<SpiTransaction> PENDING_PREDICATE = SpiTransaction::isPendingTransaction;
    private static final Predicate<SpiTransaction> INFORMATION_PREDICATE = SpiTransaction::isInformationTransaction;

    private final SpiToXs2aTransactionMapper toXs2aTransactionMapper;

    public Optional<Xs2aAccountReport> mapToXs2aAccountReport(BookingStatus bookingStatus, List<SpiTransaction> spiTransactions, byte[] rawTransactionsResponse) {
        if (ArrayUtils.isNotEmpty(rawTransactionsResponse)) {
            return Optional.of(new Xs2aAccountReport(null, null, null, rawTransactionsResponse));
        }
        if (CollectionUtils.isEmpty(spiTransactions)) {
            return Optional.empty();
        }

        List<Transactions> booked = Collections.emptyList();
        List<Transactions> pending = Collections.emptyList();
        List<Transactions> information = Collections.emptyList();

        switch (bookingStatus) {
            case INFORMATION:
                information = filterTransaction(spiTransactions, INFORMATION_PREDICATE);
                break;
            case BOOKED:
                booked = filterTransaction(spiTransactions, BOOKED_PREDICATE);
                break;
            case PENDING:
                pending = filterTransaction(spiTransactions, PENDING_PREDICATE);
                break;
            case BOTH:
                booked = filterTransaction(spiTransactions, BOOKED_PREDICATE);
                pending = filterTransaction(spiTransactions, PENDING_PREDICATE);
                break;
            case ALL:
                information = filterTransaction(spiTransactions, INFORMATION_PREDICATE);
                booked = filterTransaction(spiTransactions, BOOKED_PREDICATE);
                pending = filterTransaction(spiTransactions, PENDING_PREDICATE);
                break;
            default:
                throw new IllegalArgumentException("This Booking Status is not supported: " + bookingStatus);
        }
        return Optional.of(new Xs2aAccountReport(booked, pending, information, null));
    }

    @NotNull
    private List<Transactions> filterTransaction(List<SpiTransaction> spiTransactions, Predicate<SpiTransaction> predicate) {
        return spiTransactions
                   .stream()
                   .filter(predicate)
                   .map(toXs2aTransactionMapper::mapToXs2aTransaction)
                   .collect(Collectors.toList());
    }
}
