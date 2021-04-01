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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.authorisation.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.core.sca.Xs2aScaStatus;
import de.adorsys.psd2.xs2a.domain.Xs2aHrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

class AbstractLinks extends Links {

    private final String httpUrl;

    AbstractLinks(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    Xs2aHrefType buildPath(String path, Object... params) {
        UriComponentsBuilder uriComponentsBuilder = StringUtils.startsWith(httpUrl, "/")
                                                        ? fromPath(httpUrl)
                                                        : fromHttpUrl(httpUrl);
        return new Xs2aHrefType(uriComponentsBuilder
                                .path(path)
                                .buildAndExpand(params)
                                .toUriString());
    }

    protected boolean isScaStatusMethodSelected(Xs2aAuthenticationObject chosenScaMethod, Xs2aScaStatus scaStatus) {
        return chosenScaMethod != null
                   && scaStatus == Xs2aScaStatus.SCAMETHODSELECTED;
    }

    protected boolean isScaStatusMethodAuthenticated(Xs2aScaStatus scaStatus) {
        return scaStatus == Xs2aScaStatus.PSUAUTHENTICATED;
    }

    protected boolean isScaStatusMethodIdentified(Xs2aScaStatus scaStatus) {
        return scaStatus == Xs2aScaStatus.PSUIDENTIFIED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractLinks)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AbstractLinks that = (AbstractLinks) o;
        return httpUrl.equals(that.httpUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), httpUrl);
    }
}
