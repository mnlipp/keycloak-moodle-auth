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

/**
 * Provides the connection to Moodle. If you think that this is
 * over engineered, you are right. It is a reduced version of a much
 * more powerful library from project 
 * [moodle-tools-console](https://github.com/mnlipp/moodle-tools-console)
 * that cannot be packed into a keycloak extension easily. 
 */
package org.jdrupes.keycloak.moodleauth.moodle;