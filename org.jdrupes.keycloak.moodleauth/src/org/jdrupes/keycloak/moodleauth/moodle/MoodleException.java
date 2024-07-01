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
import java.util.Optional;
import org.jdrupes.keycloak.moodleauth.moodle.model.MoodleErrorValues;

/**
 * Represents a moodle exception
 */
@SuppressWarnings({ "serial" })
public class MoodleException extends IOException {

    private final MoodleErrorValues moodleError;

    /**
     * Instantiates a new moodle exception.
     *
     * @param error the error
     */
    public MoodleException(MoodleErrorValues error) {
        super(error.getErrorcode());
        this.moodleError = error;
    }

    /**
     * Returns the error reported by moodle.
     *
     * @return the string
     */
    public String error() {
        return moodleError.getError();
    }

    /**
     * Returns the error code reported by moodle.
     *
     * @return the string
     */
    public String errorCode() {
        return moodleError.getErrorcode();
    }

    /**
     * Returns the message provided by moodle.
     *
     * @return the string
     */
    public String message() {
        return moodleError.getMessage();
    }

    /**
     * Return the exception provided by moodle.
     *
     * @return the string
     */
    public String exception() {
        return moodleError.getException();
    }

    @Override
    public String toString() {
        return Optional.ofNullable(moodleError.getErrorcode())
            .map(s -> "Error code: " + s).orElse("")
            + Optional.ofNullable(moodleError.getError())
                .map(s -> ", error: " + s).orElse("")
            + Optional.ofNullable(moodleError.getMessage())
                .map(s -> ", message: " + s).orElse("")
            + Optional.ofNullable(moodleError.getException())
                .map(s -> ", exception: " + s).orElse("");

    }
}
