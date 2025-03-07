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

/**
 * Base class for rest actions.
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class RestAction {

    protected RestClient client;

    /**
     * Instantiates a new rest action.
     *
     * @param client the client
     */
    public RestAction(RestClient client) {
        this.client = client;
    }

}
