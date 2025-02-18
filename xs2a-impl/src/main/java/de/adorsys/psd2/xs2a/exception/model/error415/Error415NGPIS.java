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

package de.adorsys.psd2.xs2a.exception.model.error415;

import de.adorsys.psd2.xs2a.exception.model.AbstractErrorNGAIS;
import io.swagger.annotations.ApiModel;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * NextGenPSD2 specific definition of reporting error information in case of a HTTP error code 415.
 */
@ApiModel(description = "NextGenPSD2 specific definition of reporting error information in case of a HTTP error code 415. ")
@SuperBuilder
@NoArgsConstructor
public class Error415NGPIS extends AbstractErrorNGAIS<TppMessage415PIS> {
}

