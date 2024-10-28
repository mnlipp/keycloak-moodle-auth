# Keycloak Moodle Authentication

See the [project's home page](https://jdrupes.org/keycloak-moodle-auth/) for a
detailed description.

## Building

  * `./gradlew build` builds
  * `./gradlew deploy` additionally copies the extension into `target/`

## Testing

The extension can easily be tested with `podman-compose up` which mounts
`target/` on `/opt/keycloak/providers` in the container. For repeated testing,
do a partial export of your test realm and save it to `moodle-realm.json`.
