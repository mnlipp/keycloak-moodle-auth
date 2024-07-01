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
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;
import org.jdrupes.keycloak.moodleauth.moodle.actions.MoodleGetSiteInfo;
import org.jdrupes.keycloak.moodleauth.moodle.actions.MoodleUserByName;
import org.jdrupes.keycloak.moodleauth.moodle.model.MoodleSiteInfo;
import org.jdrupes.keycloak.moodleauth.moodle.model.MoodleTokens;
import org.jdrupes.keycloak.moodleauth.moodle.model.MoodleUser;
import org.jdrupes.keycloak.moodleauth.moodle.service.MoodleAuthFailedException;
import org.jdrupes.keycloak.moodleauth.moodle.service.MoodleClient;
import org.jdrupes.keycloak.moodleauth.moodle.service.MoodleService;
import org.jdrupes.keycloak.moodleauth.moodle.service.Password;

/**
 * Represents an open connection to a moodle instance.
 */
public class MoodleServiceProvider implements MoodleService {

    @SuppressWarnings({ "PMD.FieldNamingConventions",
        "PMD.UnusedPrivateField", "unused" })
    private static final Logger logger
        = Logger.getLogger(MoodleServiceProvider.class.getName());

    @Override
    @SuppressWarnings({ "PMD.AvoidCatchingGenericException",
        "PMD.EmptyCatchBlock" })
    public MoodleClient connect(String website, String username,
            Password password) throws IOException, MoodleAuthFailedException {
        // Request token
        try {
            String site = website;
            if (!site.contains("://")) {
                site = "https://" + site;
            }
            URI siteUri = new URI("https", "localhost", null, null, null)
                .resolve(site);
            if ("".equals(siteUri.getPath())) {
                siteUri = siteUri.resolve(new URI(null, null, "/", null, null));
            }
            URI tokenUri = siteUri
                .resolve(new URI(null, null, "login/token.php", null, null));
            var restClient = new RestClient(tokenUri);
            var tokens = restClient.invoke(MoodleTokens.class,
                Map.of("username", username,
                    "password", new String(password.password()),
                    "service", "moodle_mobile_app"),
                Collections.emptyMap());
            if (tokens.getErrorcode() != null) {
                try {
                    restClient.close();
                } catch (Exception e) {
                    // Was just trying to be nice
                }
                throw new MoodleAuthFailedException(tokens.getError());
            }
            URI serviceUri = siteUri.resolve(
                new URI(null, null, "webservice/rest/server.php", null, null));
            restClient.setUri(serviceUri);
            restClient.setDefaultParams(Map.of("wstoken", tokens.getToken(),
                "moodlewsrestformat", "json"));
            MoodleUser muser
                = new MoodleUserByName(restClient).invoke(username);
            MoodleSiteInfo siteInfo
                = new MoodleGetSiteInfo(restClient).invoke();
            return new MoodleClientConnection(siteUri, restClient, muser,
                siteInfo);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
