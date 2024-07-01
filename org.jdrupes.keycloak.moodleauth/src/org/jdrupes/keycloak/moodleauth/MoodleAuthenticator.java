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

package org.jdrupes.keycloak.moodleauth;

import jakarta.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Optional;
import org.jdrupes.keycloak.moodleauth.moodle.MoodleServiceProvider;
import org.jdrupes.keycloak.moodleauth.moodle.model.MoodleUser;
import org.jdrupes.keycloak.moodleauth.moodle.service.MoodleAuthFailedException;
import org.jdrupes.keycloak.moodleauth.moodle.service.MoodleClient;
import org.jdrupes.keycloak.moodleauth.moodle.service.Password;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.services.ServicesLogger;

/**
 * The Class MoodleAuthenticator.
 */
public class MoodleAuthenticator implements Authenticator {

    private static ServicesLogger log = ServicesLogger.LOGGER;

    /**
     * User does not have to been identified, because this is
     * a combined login/auto registration form. So return false;
     *
     * @return false
     */
    @Override
    public boolean requiresUser() {
        return false;
    }

    /**
     * Is this authenticator configured for this user.
     *
     * @param session the session
     * @param realm the realm
     * @param user the user
     * @return true, if successful
     */
    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm,
            UserModel user) {
        return true;
    }

    /**
     * Never called because
     * {@link MoodleAuthenticatorFactory#isUserSetupAllowed()} returns
     * false.
     *
     * @param session the session
     * @param realm the realm
     * @param user the user
     */
    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm,
            UserModel user) {
    }

    /**
     * Not documented by keycloak. Probably supposed to release resources.
     * Does nothing.
     */
    @Override
    public void close() {
    }

    /**
     * Start the authentication by creating the form. 
     *
     * @param context the context
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        var challenge = formsProvider(context).createForm("moodle-login.ftl");
        context.challenge(challenge);
    }

    /**
     * Called when the form has been submitted. Checks if the
     * user/password combination is valid by trying to access the Moodle
     * instance. If successful, creates the user if it doesn't exist yet.
     *
     * @param context the context
     */
    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData
            = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }

        // Get Moodle Service
        String moodleUrl = Optional.ofNullable(context.getAuthenticatorConfig())
            .map(AuthenticatorConfigModel::getConfig)
            .map(m -> m.get(MoodleAuthenticatorFactory.MOODLE_URL)).orElse("");
        if (moodleUrl.isEmpty()) {
            var challenge = formsProvider(context).setError("missingMoodleUrl")
                .createForm("moodle-login.ftl");
            context.failureChallenge(
                AuthenticationFlowError.IDENTITY_PROVIDER_ERROR, challenge);
            log.error("Moodle URL not configured.");
            return;
        }
        var moodleServiceProvider = new MoodleServiceProvider();
        var username = formData.getFirst("username");
        try (var moodleClient = moodleServiceProvider.connect(moodleUrl,
            username,
            new Password(formData.getFirst("password").toCharArray()))) {

            // Valid user, check if exists
            var userProvider = context.getSession().users();
            var kcUser = Optional.ofNullable(userProvider
                .getUserByUsername(context.getRealm(), username))
                .orElseGet(() -> createUser(context, userProvider, username,
                    moodleClient));
            context.setUser(kcUser);
            context.success();
        } catch (IOException e) {
            var challenge
                = formsProvider(context).setError("temoraryMoodleFailure")
                    .createForm("moodle-login.ftl");
            context.failureChallenge(
                AuthenticationFlowError.IDENTITY_PROVIDER_ERROR, challenge);
            return;
        } catch (MoodleAuthFailedException e) {
            var challenge = formsProvider(context)
                .setError("invalidUserMessage").createForm("moodle-login.ftl");
            context.failureChallenge(
                AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }
    }

    private UserModel createUser(AuthenticationFlowContext context,
            UserProvider userProvider, String username,
            MoodleClient moodleClient) {
        MoodleUser moodleUser = moodleClient.moodleUser();
        var kcUser = userProvider.addUser(context.getRealm(), username);
        kcUser.setEnabled(true);
        kcUser.setEmail(moodleUser.getEmail());
        kcUser.setEmailVerified(true);
        var siteInfo = moodleClient.siteInfo();
        if (moodleUser.getFirstname() != null
            && !moodleUser.getFirstname().isBlank()) {
            kcUser.setFirstName(moodleUser.getFirstname());
        } else {
            kcUser.setFirstName(siteInfo.getFirstname());
        }
        if (moodleUser.getLastname() != null
            && !moodleUser.getLastname().isBlank()) {
            kcUser.setLastName(moodleUser.getLastname());
        } else {
            kcUser.setLastName(siteInfo.getLastname());
        }
        return kcUser;
    }

    private LoginFormsProvider
            formsProvider(AuthenticationFlowContext context) {
        LoginFormsProvider form = context.form();
        var moodleUrl = context.getAuthenticatorConfig().getConfig()
            .get(MoodleAuthenticatorFactory.MOODLE_URL);
        form.setAttribute(MoodleAuthenticatorFactory.MOODLE_URL, moodleUrl);
        return form;
    }

}
