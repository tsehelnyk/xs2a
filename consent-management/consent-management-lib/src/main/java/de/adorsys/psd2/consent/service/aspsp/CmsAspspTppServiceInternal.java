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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.aspsp.api.tpp.CmsAspspTppService;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.TppStopListEntity;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.repository.TppStopListRepository;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.consent.service.mapper.TppStopListMapper;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmsAspspTppServiceInternal implements CmsAspspTppService {
    private final TppStopListRepository stopListRepository;
    private final TppStopListMapper tppStopListMapper;
    private final TppInfoRepository tppInfoRepository;
    private final TppInfoMapper tppInfoMapper;

    @NotNull
    @Override
    public Optional<TppStopListRecord> getTppStopListRecord(@NotNull String tppAuthorisationNumber, @NotNull String instanceId) {
        Optional<TppStopListEntity> stopListEntityOptional = stopListRepository.findByTppAuthorisationNumberAndInstanceId(tppAuthorisationNumber, instanceId);
        return stopListEntityOptional.map(tppStopListMapper::mapToTppStopListRecord);
    }

    @Transactional
    @Override
    public boolean blockTpp(@NotNull String tppAuthorisationNumber, @NotNull String instanceId, @Nullable Duration lockPeriod) {
        Optional<TppStopListEntity> stopListEntityOptional = stopListRepository.findByTppAuthorisationNumberAndInstanceId(tppAuthorisationNumber, instanceId);

        TppStopListEntity entityToBeBlocked = stopListEntityOptional
                                                  .orElseGet(() -> {
                                                      TppStopListEntity entity = new TppStopListEntity();
                                                      entity.setTppAuthorisationNumber(tppAuthorisationNumber);
                                                      entity.setInstanceId(instanceId);
                                                      return entity;
                                                  });
        entityToBeBlocked.block(lockPeriod);
        if (stopListEntityOptional.isEmpty()) {
            stopListRepository.save(entityToBeBlocked);
        }
        return true;
    }

    @Transactional
    @Override
    public boolean unblockTpp(@NotNull String tppAuthorisationNumber, @NotNull String instanceId) {
        Optional<TppStopListEntity> stopListEntityOptional = stopListRepository.findByTppAuthorisationNumberAndInstanceId(tppAuthorisationNumber, instanceId);

        if (stopListEntityOptional.isPresent()) {
            TppStopListEntity entityToBeUnblocked = stopListEntityOptional.get();
            entityToBeUnblocked.unblock();
        }
        return true;
    }

    @NotNull
    @Override
    public Optional<TppInfo> getTppInfo(@NotNull String tppAuthorisationNumber, @NotNull String instanceId) {
        Optional<TppInfoEntity> tppInfoEntityOptional = tppInfoRepository.findFirstByAuthorisationNumberAndInstanceId(tppAuthorisationNumber, instanceId);
        return tppInfoEntityOptional.map(tppInfoMapper::mapToTppInfo);
    }
}
