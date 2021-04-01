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

package de.adorsys.psd2.xs2a.spi.domain.authorisation;

import de.adorsys.psd2.xs2a.core.authorisation.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.core.sca.Xs2aChallengeData;
import de.adorsys.psd2.xs2a.core.sca.Xs2aScaStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpiAuthorizationCodeResult extends SpiWithExemptionResponse {
    private Xs2aChallengeData challengeData;
    private Xs2aAuthenticationObject selectedScaMethod;
    private Xs2aScaStatus scaStatus;

    public SpiAuthorizationCodeResult(boolean scaExempted, Xs2aChallengeData challengeData,
                                      Xs2aAuthenticationObject selectedScaMethod, Xs2aScaStatus scaStatus) {
        super(scaExempted);
        this.challengeData = challengeData;
        this.selectedScaMethod = selectedScaMethod;
        this.scaStatus = scaStatus;
    }

    public boolean isEmpty() {
        return (challengeData == null || challengeData.isEmpty())
                   && selectedScaMethod == null;
    }
}
