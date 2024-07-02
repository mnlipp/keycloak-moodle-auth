---
title: Keycloak-Moodle-Auth by mnlipp
description: A Keycloak extension that uses a Moodle installation for authentication
layout: keycloak-moodle-auth
---

# Welcome to Keycloak-Moodle-Auth

*This project is not about configuring OIDC in Moodle!*

This project provides a Keycloak extension that uses a 
[Moodle](https://moodle.org/) instance for authentication
(and registration).

Note that this extension probably shouldn't exist. You need it only
if you require an OIDC provider for some application (OIDC being
supported by most systems nowadays) and one of the following reasons
apply:

  * Moodle is really the primary identity provider in your institution.
    
  * The primary identity provider in your institution uses a protocol
    that is not supported by Keycloak. (Unlikely, unless someone has
    "hacked" Moodle to support this exotic protocol.)
    
  * You simply don't have access to the primary identity provider
    used by the Moodle instance.

## How it works

The authenticator requires the REST web services to be
[enabled](https://docs.moodle.org/404/en/Using_web_services) in
the Moodle installation. When a user enters his username and
password in the form, these credentials are used to authenticate
against the REST API. If the authentication is successful, the
user is authenticated with Keycloak. If the user didn't exist
in the realm yet, an account is created automatically, using
the information obtained from Moodle for email and first and
last name. Note that the password is never stored in Keycloak.

## Realm configuration

Create an authentication flow that uses the "Moodle Username Password
Form".

To ensure a consistent user experience, the following realm settings are recommended:

  * On tab "Login" turn off "Login with email". This makes sure that
    the prompt on the login screen does not mention email as an 
    alternative to entering the username.
    
  * On tab "User registration" remove roles "view-profile" and
    "manage-account".
    
  * If, for some reason, you want to allow users to view their profile
    and manage their account (e.g. for enabling two-factor authentication),
    make sure to adjust the permissions for the attributes on tab
    "User profile". Attributes "username", "email", "firstName" and
    "lastName" must not be editable by the user. Attribute "username"
    is used as key for synchronising with Moodle, and the other attributes
    are overwritten on each login because they can change in Moodle.

     In addition, on tab "Required actions" of the realm's authentication
     configuration, disable "Update Password". This disables the section
     related to setting up a password on the "Account security"/"Signing in"
     page.
     