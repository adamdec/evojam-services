# Invitations Service 1.0.0

Invitation service as a micro-service can be run:
- using run scripts, Linux & Windows versions are provided in a zip file: evojam-invitation-service-1.0.0.zip (please check 'bin' directory as per SBT Universal plugin convention).
- using Docker infrastructure (setup of the Docker must be done before it)

Service uses:
- Scala 2.12.x as a language of first choice
- Akka HTTP as a REST interface
- Swagger for endpoint documentation
- Slick as a persistence layer
- Flyway for DB schema migration (please take a look at evojam-invitation-service/src/main/resources/database directory)
	- DB schemas are being versioned using V00X_ convention, all changes are applied automatically during the application start-up)

Service supports:
- Oracle dialect & integration (for production environnement)
- HTTPS (for production environment)
	- Dummy 'ssl-certificate.p12' certificate is provided (do not use in production!)
- HTTP BASIC AUTH which is required for to access service endpoints
- Logging of incoming requests and outcoming responses
	- log file: invitation-service.log
	- console
- Endpoint contract versioning
	- all endpoints are prefixed with "/v1/" so one can easily determine which version of the API is invoked
	- minimizes the risk of "breaking changes" in already implemented endpoints

By default, application will:
- start on 0.0.0.0:8080
- setup H2 database with INVITATIONS table
- configures 2 users with different roles:
	- admin (all roles: "MANAGE_INVITATIONS", "SWAGGER")
	- service-user (one role: "MANAGE_INVITATIONS")

Service roles:
- MANAGE_INVITATIONS: one can persist and fetch invitations
- SWAGGER: one can see only endpoint documentation (for QA team)

## Invitations Service API
See documentation [here](evojam-invitation-service-api/README.md).

## Invitations Service
See documentation [here](evojam-invitation-service/README.md).

# Build

Project uses SBT 0.13.7 for SDLC (please note that 1.1.0 should also work).

Please check SBT resolvers, configures at: $WORKSPACE/evojam-services/project/build.sbt

Unfortunately due to my VPN restrictions I am not able to test if those are working :(

## Compile and package components

```bash
sbt clean compile
```

## Run unit tests:

```bash
sbt test
```

## Check code coverage (>90%):

```bash
sbt coverage test
sbt coverageReport
```

## Package a service

```bash
sbt universal:packageBin
```
Zip file will be generated: $WORKSPACE/evojam-services/evojam-invitation-service/target/universal/evojam-invitation-service-1.0.0.zip

## Build docker image

```bash
sbt docker:publishLocal
```
Image will be generated: localhost/evojam/evojam-invitation-service:1.0.0

# Service Road Plan

## 2.0.0
- switch to contract first approach (https://github.com/julienrf/endpoints)
	- not production ready, need to wait once HTTPS support will be added: https://github.com/julienrf/endpoints/issues/24
	- another approach: https://g00glen00b.be/exploring-contract-first-options-swagger/

## 1.1.0
- change configuration classes to circe-config (https://github.com/circe/circe-config)
	- remove boilerplate from the code
- improve security
	- Introduce and apply OWASP (https://www.owasp.org/index.php/REST_Security_Cheat_Sheet)
