/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.espressoserver.lib.helpers

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.*
import io.appium.espressoserver.lib.handlers.exceptions.InvalidSelectorException
import io.appium.espressoserver.lib.model.Strategy
import io.appium.espressoserver.EspressoServerRunnerTest
import io.appium.espressoserver.lib.handlers.exceptions.AppiumException
import io.appium.espressoserver.lib.handlers.exceptions.InvalidArgumentException
import io.appium.espressoserver.lib.handlers.exceptions.StaleElementException
import io.appium.espressoserver.lib.model.Locator
import java.util.NoSuchElementException

/**
 * Retrieve cached node and return the SemanticsNodeInteraction
 */
fun getNodeInteractionById(elementId: String?): SemanticsNodeInteraction =
    elementId?.let { ComposeViewCache.get(it) ?: throw StaleElementException(it) }
        ?: throw InvalidArgumentException("Cannot find 'null' element")


fun SemanticsNodeInteraction.findDescendantNodeInteractions(locator: Locator): SemanticsNodeInteractionCollection =
    this.onChildren().filter(semanticsMatcherForLocator(locator))

fun semanticsMatcherForLocator(locator: Locator): SemanticsMatcher =
    when (locator.using) {
        Strategy.VIEW_TAG -> hasTestTag(locator.value!!)
        Strategy.TEXT -> hasText(locator.value!!)
        Strategy.LINK_TEXT -> hasText(locator.value!!)
        Strategy.ACCESSIBILITY_ID -> hasContentDescription(locator.value!!)
        else -> throw InvalidSelectorException(
            "Can't use non-Compose selectors. " +
                    "Only ${Strategy.VIEW_TAG}, ${Strategy.TEXT}, ${Strategy.LINK_TEXT} and " +
                    "${Strategy.ACCESSIBILITY_ID} are supported"
        )
    }

fun getSemanticsNode(elementId: String): SemanticsNode =
    try {
        getNodeInteractionById(elementId).fetchSemanticsNode()
    } catch (e: AssertionError) {
        throw StaleElementException(elementId)
    }