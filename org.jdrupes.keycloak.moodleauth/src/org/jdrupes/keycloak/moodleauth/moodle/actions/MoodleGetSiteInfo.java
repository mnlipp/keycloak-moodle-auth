/*
 * This file is part of the Keycloak Moodle authenticator
 * Copyright (C) 2024 Michael N. Lipp
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package org.jdrupes.keycloak.moodleauth.moodle.actions;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.jdrupes.keycloak.moodleauth.moodle.RestAction;
import org.jdrupes.keycloak.moodleauth.moodle.RestClient;
import org.jdrupes.keycloak.moodleauth.moodle.model.MoodleSiteInfo;

/**
 * Get the site info.
 */
public class MoodleGetSiteInfo extends RestAction {

    /**
     * Creates the action.
     *
     * @param client the client
     */
    public MoodleGetSiteInfo(RestClient client) {
        super(client);
    }

    /**
     * Invoke the action.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public MoodleSiteInfo invoke() throws IOException {
        return client.invoke(MoodleSiteInfo.class, Map.of(
            "wsfunction", "core_webservice_get_site_info"),
            Collections.emptyMap());
    }
}
