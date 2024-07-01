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

package org.jdrupes.keycloak.moodleauth.moodle;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import org.jdrupes.keycloak.moodleauth.moodle.model.MoodleSiteInfo;
import org.jdrupes.keycloak.moodleauth.moodle.model.MoodleUser;
import org.jdrupes.keycloak.moodleauth.moodle.service.MoodleClient;

/**
 * Represents an open connection to a moodle instance.
 */
public class MoodleClientConnection implements MoodleClient {

    private final RestClient restClient;
    private final MoodleUser moodleUser;
    private final MoodleSiteInfo siteInfo;

    /**
     * Instantiates a new moodle client connection.
     *
     * @param restClient the rest client
     */
    public MoodleClientConnection(URI siteUri, RestClient restClient,
            MoodleUser moodleUser, MoodleSiteInfo siteInfo) {
        this.restClient = restClient;
        this.moodleUser = moodleUser;
        this.siteInfo = siteInfo;
    }

    @Override
    public Object invoke(String wsfunction, Map<String, Object> params)
            throws IOException {
        return restClient.invoke(Object.class, Map.of("wsfunction", wsfunction),
            params);
    }

    @Override
    public MoodleUser moodleUser() {
        return moodleUser;
    }

    @Override
    public MoodleSiteInfo siteInfo() {
        return siteInfo;
    }

    @Override
    @SuppressWarnings({ "PMD.AvoidCatchingGenericException",
        "PMD.EmptyCatchBlock" })
    public void close() {
        try {
            restClient.close();
        } catch (Exception e) {
            // Only trying to be nice
        }
    }

}
