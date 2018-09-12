/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.e2e.oce;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.opennms.e2e.core.EndToEndTestRule;
import org.opennms.e2e.grafana.Grafana44SeleniumDriver;
import org.opennms.e2e.grafana.GrafanaRestClient;
import org.opennms.e2e.opennms.OpenNMSRestClient;
import org.opennms.e2e.stacks.OpenNMSHelmOCEStack;
import org.opennms.gizmo.docker.GizmoDockerRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class End2EndCorrelationTestBase extends CorrelationTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(End2EndStandaloneCorrelationTest.class);
    final OpenNMSHelmOCEStack stack;
    GrafanaRestClient grafanaRestClient;
    OpenNMSRestClient openNMSRestClient;

    @Rule
    public final EndToEndTestRule e2e;

    public End2EndCorrelationTestBase(boolean redundant) {
        if (redundant) {
            stack = OpenNMSHelmOCEStack.withRedundantOCE();
        } else {
            stack = OpenNMSHelmOCEStack.withStandaloneOCE();
        }

        e2e = EndToEndTestRule.builder()
                .withGizmoRule(GizmoDockerRule.builder()
                        .withStack(stack)
                        .build())
                .withWebDriverType(EndToEndTestRule.WebDriverType.LOCAL_CHROME)
                // TODO: Will use sauce labs when this gets merged
//            .withWebDriverType(EndToEndTestRule.WebDriverType.SAUCELABS)
                .build();
    }

    @Test
    public void canCorrelateAlarms() throws InterruptedException, IOException {
        try {
            setup();

            // Trigger some alarms
            // TODO: This will be replaced with a call to switch sim to generate some alarms
            openNMSRestClient.triggerAlarmsForCorrelation();

            // OCE Should now correlate them, we need to wait here for the situation alarm to show up
            LOG.info("Waiting for a situation to be received by OpenNMS...");
            openNMSRestClient.waitForOutstandingSituation();
            LOG.info("Situation received, verifying via Helm...");

            // Login, navigate to dashboard, view alarm in table, verify the related alarms
            verifyGenericSituation(new Grafana44SeleniumDriver(e2e.getDriver(), stack.getHelmUrl()));
        } finally {
            cleanup();
        }
    }

    void setup() throws IOException {
        LOG.info("Setting up...");
        grafanaRestClient = new GrafanaRestClient(stack.getHelmUrl());
        openNMSRestClient = new OpenNMSRestClient(stack.getOpenNMSUrl());
        setupHelm(grafanaRestClient);
        // No alarms/situations
        assertThat(openNMSRestClient.getAlarms(), hasSize(0));
    }

    void cleanup() {
        LOG.info("Cleaning up...");
        openNMSRestClient.clearAllAlarms();
        cleanupHelm(grafanaRestClient);
    }
}
