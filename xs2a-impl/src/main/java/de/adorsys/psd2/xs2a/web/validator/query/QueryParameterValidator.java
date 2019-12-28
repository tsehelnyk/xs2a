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

package de.adorsys.psd2.xs2a.web.validator.query;

import de.adorsys.psd2.xs2a.core.error.MessageError;

import java.util.List;
import java.util.Map;

public interface QueryParameterValidator {
    /**
     * Validates query parameters from the request and populates given error with error text if parameters are invalid
     *
     * @param queryParameterMap query parameters from the request, with query parameter names acting as keys
     * @param messageError      error to be populated
     * @return {@link MessageError} object, enriched or not.
     */
    MessageError validate(Map<String, List<String>> queryParameterMap, MessageError messageError);
}
