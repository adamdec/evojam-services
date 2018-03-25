# Evojam Invitations Service

## Run locally

Run with embedded H2 database:
```bash
./bin/run
```

Run with custom `application.conf`:
```bash
./bin/run -Dconfig.file=application.conf
```

## Run in Docker

### Locally
DOCKER_REGISTRY=localhost
DOCKER_REGISTRY_PROJECT_NAME=evojam

Please note that the first step should be to create a 

Run with default configuration:
```bash
docker run -d DOCKER_REGISTRY/DOCKER_REGISTRY_PROJECT_NAME/evojam-invitation-service:1.0.0
```

Run with default configuration (interactive mode):
```bash
docker run -it DOCKER_REGISTRY/DOCKER_REGISTRY_PROJECT_NAME/evojam-invitation-service:1.0.0
```

Run with custom `application.conf`:
```bash
docker run -d \
  -v $(pwd)/application.conf:/application.conf \
  DOCKER_REGISTRY/DOCKER_REGISTRY_PROJECT_NAME/evojam-invitation-service:1.0.0 \
  -Dconfig.file=/application.conf
```

Override specific configuration parameters:
```bash
docker run -d \
  -e "API_HOST=HOST_NAME_TO_RUN_ON" \
  -e "API_PORT=9080" \
  -e "SSL_ENABLED=false" \
  DOCKER_REGISTRY/DOCKER_REGISTRY_PROJECT_NAME/evojam-invitation-service:1.0.0
```

Run with Oracle database:
```bash
docker run -d \
  -e "DB_JDBC_URL=jdbc:oracle:thin:@ORACLE_MACHINE_NAME:1521:invitation" \
  -e "DB_USERNAME=user" \
  -e "DB_PASSWORD=pass" \
  -e "DB_SCHEMA=schema" \
  DOCKER_REGISTRY/DOCKER_REGISTRY_PROJECT_NAME/evojam-invitation-service:1.0.0 \
  -Dconfig.resource=oracle.conf
```

## API Documentation

### Add new invitation
```bash
curl -i -H "Accept: application/json" -H "Content-Type:application/json" -X POST --data '{"invitee" : "Adam Dec", "email" : "adec@evojam.com"}' -u admin:admin http://localhost:8080/v1/invitation
```

### Get all invitations sorted by 'invitee' field

```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET -u admin:admin http://localhost:8080/v1/invitation
```
#### Docker
If application is to be run locally in Docker then one needs to replace the ip address (localhost) with the one which is assigned to the Docker container.
To check that please invoke:
```bash
docker ps
docker inspect <CONTAINER ID>
```
At the bottom, under "NetworkSettings", one can find "IPAddress".
If one uses cntlm as a proxy please not forget to exclude Docker container subnet in '/etc/cntlm.conf':
```text
NoProxy         localhost, 127.0.0.*, 172.17.0.*, 10.*.*.*
```

#### SSL
If SSL is enabled, one needs to add '--insecure' as a parameter to curl. This is only for testing purposes!

### Static information

The static information is available without authentication so that potential users of the service 
can review the docs at the start of any possible use of the service.

- http://localhost:8080/api-docs/swagger.json
- http://localhost:8080/api-docs/swagger.yaml

### Swagger UI

The ability to execute the service through Swagger is restricted so that it only works when the user is properly authenticated.

- http://localhost:8080/swagger

### Oracle database

To create a user and tablespace for the service, execute:
```sql
CREATE TABLESPACE invitations_tabspace
  DATAFILE 'invitations_tabspace.dat'
  SIZE 10M AUTOEXTEND ON;
 
CREATE TEMPORARY TABLESPACE invitations_tabspace_temp
  TEMPFILE 'invitations_tabspace_temp.dat'
  SIZE 5M AUTOEXTEND ON;
 
CREATE USER invitations
  IDENTIFIED BY invitations
  DEFAULT TABLESPACE invitations_tabspace
  TEMPORARY TABLESPACE invitations_tabspace_temp;
 
GRANT CREATE SESSION TO invitations;
GRANT CREATE TABLE TO invitations;
GRANT CREATE SEQUENCE TO invitations;
GRANT CREATE TRIGGER TO invitations;
GRANT UNLIMITED TABLESPACE TO invitations;
```

Then specify `invitations` as username and password when starting the service.