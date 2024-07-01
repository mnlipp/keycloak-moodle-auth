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

package org.jdrupes.keycloak.moodleauth.moodle.service;

import java.io.IOException;
import java.util.Map;
import org.jdrupes.keycloak.moodleauth.moodle.model.MoodleSiteInfo;
import org.jdrupes.keycloak.moodleauth.moodle.model.MoodleUser;

/**
 * Represents the client side of a connection to a moodle server.
 */
public interface MoodleClient extends AutoCloseable {

    @Override
    void close();

    /**
     * Invoke some function. Allows the invocation of functions that
     * haven't been typed yet.
     *
     * @param wsfunction the function
     * @param params the parameters
     * @return the result
     */
    Object invoke(String wsfunction, Map<String, Object> params)
            throws IOException;

    /**
     * The authenticated moodle user.
     *
     * @return the moodle user
     */
    MoodleUser moodleUser();

    /**
     * The site info (includes information about the user)
     *
     * @return the moodle site info
     */
    MoodleSiteInfo siteInfo();

}
