/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.e2e.core;

import cucumber.api.Scenario;
import org.opennms.e2e.selenium.LocalChromeWebDriverStrategy;
import org.opennms.e2e.selenium.NoOpWebDriverStrategy;
import org.opennms.e2e.selenium.SauceLabsWebDriverStrategy;
import org.opennms.gizmo.GizmoRule;
import org.openqa.selenium.WebDriver;

import java.util.Objects;

public class EndToEndTestRule {

    public enum WebDriverType {
        LOCAL_CHROME,
        SAUCELABS,
        NONE
    }

    public static class EndToEndTestRuleBuilder {
        private GizmoRule<?,?> gizmo;
        private WebDriverType webDriverType = WebDriverType.NONE;

        public EndToEndTestRuleBuilder withGizmoRule(GizmoRule<?,?> gizmo) {
            this.gizmo = gizmo;
            return this;
        }

        public EndToEndTestRuleBuilder withWebDriverType(WebDriverType type) {
            this.webDriverType = Objects.requireNonNull(type);
            return this;
        }

        public EndToEndTestRule build() {
            return new EndToEndTestRule(this);
        }
    }

    public static EndToEndTestRuleBuilder builder() {
        return new EndToEndTestRuleBuilder();
    }

    private final GizmoRule<?,?> gizmo;
    private final WebDriverStrategy webDriverStrategy;

    private EndToEndTestRule(EndToEndTestRuleBuilder builder) {
        gizmo = builder.gizmo;
        switch(builder.webDriverType) {
            case LOCAL_CHROME:
                webDriverStrategy = new LocalChromeWebDriverStrategy();
                break;
            case SAUCELABS:
                webDriverStrategy = new SauceLabsWebDriverStrategy();
                break;
            case NONE:
                webDriverStrategy = new NoOpWebDriverStrategy();
                break;
            default:
                throw new IllegalArgumentException("Unsupported web driver type: " + builder.webDriverType);
        }
    }

    public void setUp(Scenario scenario) throws Exception {
        // Ensure the environment is created before starting the WebDriver
        if (gizmo != null) {
            gizmo.before();
        }
        webDriverStrategy.setUp(scenario);
    }

    public WebDriver getDriver() {
       return webDriverStrategy.getDriver();
    }

    public void tearDown(Scenario scenario) throws Exception {
        webDriverStrategy.tearDown(scenario);
        if (gizmo != null) {
            gizmo.after(scenario.isFailed());
        }
    }

}