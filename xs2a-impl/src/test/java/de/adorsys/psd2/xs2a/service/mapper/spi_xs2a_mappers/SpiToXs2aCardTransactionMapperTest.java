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

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.xs2a.domain.Xs2aCardTransaction;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiCardTransaction;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aCardTransactionMapperImpl.class, SpiToXs2aAmountMapperImpl.class, Xs2aToSpiAddressMapper.class, SpiToXs2aExchangeRateMapperImpl.class})
class SpiToXs2aCardTransactionMapperTest {
    @Autowired
    private SpiToXs2aCardTransactionMapper spiToXs2aCardTransactionMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToXs2aCardTransaction() {
        //Given
        List<SpiCardTransaction> spiCardTransactions = jsonReader.getObjectFromFile("json/SpiCardTransactions.json", new TypeReference<List<SpiCardTransaction>>() {
        });
        SpiCardTransaction spiCardTransaction = spiCardTransactions.get(0);

        Xs2aCardTransaction expectedCardTransaction = jsonReader.getObjectFromFile("json/CardAccountTransaction.json", Xs2aCardTransaction.class);

        //When
        Xs2aCardTransaction actualCardTransaction = spiToXs2aCardTransactionMapper.mapToXs2aCardTransaction(spiCardTransaction);

        //Then
        assertEquals(expectedCardTransaction, actualCardTransaction);
    }

    @Test
    void mapToXs2aCardTransactionList() {
        //Given
        List<SpiCardTransaction> spiCardTransactions = jsonReader.getObjectFromFile("json/SpiCardTransactions.json", new TypeReference<List<SpiCardTransaction>>() {
        });

        List<Xs2aCardTransaction> expectedCardTransactions = jsonReader.getObjectFromFile("json/CardAccountTransactions.json",  new TypeReference<List<Xs2aCardTransaction>>() {
        });

        //When
        List<Xs2aCardTransaction>  actualCardTransactions = spiToXs2aCardTransactionMapper.mapToXs2aCardTransactionList(spiCardTransactions);

        //Then
        assertEquals(expectedCardTransactions, actualCardTransactions);
    }

    @Test
    void mapToXs2aCardTransaction_null() {
        //When
        Xs2aCardTransaction actualCardTransaction = spiToXs2aCardTransactionMapper.mapToXs2aCardTransaction(null);

        //Then
        assertNull(actualCardTransaction);
    }

    @Test
    void mapToXs2aCardTransactionList_null() {
        //When
        List<Xs2aCardTransaction>  actualCardTransactions = spiToXs2aCardTransactionMapper.mapToXs2aCardTransactionList(null);

        //Then
        assertNull(actualCardTransactions);
    }
}
