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

import org.junit.Rule;
import org.junit.Test;
import org.opennms.e2e.stacks.OpenNMSHelmOCEStack;
import org.opennms.gizmo.docker.GizmoDockerRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class End2EndStandaloneCorrelationTest extends CorrelationTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(End2EndStandaloneCorrelationTest.class);
    @Rule
    public final GizmoDockerRule gizmo = getGizmoRule();

    @Test
    public void canCorrelateAlarms() throws Exception {
        try {
            setup();

            // Trigger some alarms
            // TODO: This will be replaced with a call to switch sim to generate some alarms
            openNMSRestClient.triggerAlarmsForCorrelation();

            // OCE Should now correlate them, we need to wait here for the situation alarm to show up
            LOG.info("Waiting for a situation to be received by OpenNMS...");
            openNMSRestClient.waitForOutstandingSituation();

            // Login, navigate to dashboard, view alarm in table, verify the related alarms
            LOG.info("Situation received, verifying via Helm...");
            verifyGenericSituation(gizmo);
        } finally {
            cleanup();
        }
    }

    @Override
    OpenNMSHelmOCEStack getStack() {
        return OpenNMSHelmOCEStack.withStandaloneOCE();
    }
}
