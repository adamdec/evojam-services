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

If application is to be run in Docker (still locally) then one needs to replace the ip address (localhost) with the one which is assigned currently to the Docker container.
To check that, please invoke the commands:
```bash
docker ps
docker inspect <CONTAINER ID>
```
At the bottom, under "NetworkSettings", one can find "IPAddress".

If one uses cntlm (or any) as a proxy please not forget to exclude Docker container subnet in '/etc/cntlm.conf':
```text
NoProxy         localhost, 127.0.0.*, 172.17.0.*, 10.*.*.*
```
### Docker running locally

Please replace:
- DOCKER_REGISTRY=localhost
- DOCKER_REGISTRY_PROJECT_NAME=evojam

Please note that the first step should be to create a Docker image.
To check if one is there in local repository, please invoke the command:
```bash
docker images
```

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

Service REST endpoints are documented using Swagger (https://swagger.io/). Below you will find example queries that can be executed using curl.

### Add new invitation

HTTP
```bash
curl -i -H "Accept: application/json" -H "Content-Type:application/json" -X POST --data '{"invitee" : "Adam Dec", "email" : "adec@evojam.com"}' -u admin:admin http://localhost:8080/v1/invitation
```
HTTPS
```bash
curl -i -H "Accept: application/json" -H "Content-Type:application/json" -X POST --data '{"invitee" : "Adam Dec", "email" : "adec@evojam.com"}' -u admin:admin https://localhost:9080/v1/invitation --insecure
```
#### Example response when invitation is persisted

```text
HTTP/1.1 200 Connection established

HTTP/1.1 201 Created
Server: akka-http/10.1.0
Date: Sun, 25 Mar 2018 09:10:44 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 76
```

### Get all invitations sorted by 'invitee' field

HTTP
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET -u admin:admin http://localhost:8080/v1/invitation
```
HTTPS
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET -u admin:admin https://localhost:9080/v1/invitation --insecure
```

#### Example response when invitations are not found

```text
HTTP/1.1 200 Connection established

HTTP/1.1 404 Not Found
Server: akka-http/10.1.0
Date: Sun, 25 Mar 2018 09:08:30 GMT
Content-Type: application/json
Content-Length: 104

{"message":"No invitations were found in DB","uri":"https://172.17.0.2:9080/v1/invitation","cause":null}
```
#### Example response when invitations are found

```text
HTTP/1.1 200 Connection established

HTTP/1.1 200 OK
Server: akka-http/10.1.0
Date: Sun, 25 Mar 2018 09:11:43 GMT
Content-Type: application/json
Content-Length: 66

{"invitations":[{"invitee":"Adam Dec","email":"adec@evojam.com"}]}
```
### Application healthcheck

- Check if application is alive
- Check if DB connection is healthy
- Return service up-time

HTTP
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET -u admin:admin http://localhost:8080/healthcheck
```
HTTPS
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET -u admin:admin https://localhost:9080/healthcheck --insecure
```

#### Response
```text
HTTP/1.1 200 Connection established

HTTP/1.1 200 OK
Server: akka-http/10.1.0
Date: Sun, 25 Mar 2018 09:46:38 GMT
Content-Type: application/json
Content-Length: 417
```
```json
{
   "build": {
      "licenses": "List()",
      "name": "evojam-invitation-service",
      "teamPage": "https://wiki.evojam/evojam-invitation-service",
      "projectUrl": "https://bitbucket.evojam/evojam-services/evojam-invitation-service",
      "scalaVersion": "2.12.5",
      "version": "1.0.0",
      "teamEmail": "dl-EVOJAM-TEAMevojam.com",
      "sbtVersion": "0.13.17",
      "team": "EVOJAM TEAM"
   },
   "database": {
      "connectivity": "OK",
      "version": "001"
   },
   "metrics": {
      "uptime": "00:38:18.393"
   }
}
```

### Application simple healthcheck

- The purpose of the simple health check is only to indicate if the application is up & running. DB connection is not checked. 
This can be freely used for example by Kubernetes (OpenShift) to check the service health. If the service is down Kubernetes (OpenShift) can reconfigure its's load balancer and spawn additional services if needed.

HTTP
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET -u admin:admin http://localhost:8080/
```
HTTPS
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET -u admin:admin https://localhost:9080/ --insecure
```

#### Response
```text
HTTP/1.1 200 Connection established

HTTP/1.1 200 OK
Server: akka-http/10.1.0
Date: Sun, 25 Mar 2018 09:49:41 GMT
Content-Type: application/json
Content-Length: 322
```
```json
{
   "licenses": "List()",
   "name": "evojam-invitation-service",
   "teamPage": "https://wiki.evojam/evojam-invitation-service",
   "projectUrl": "https://bitbucket.evojam/evojam-services/evojam-invitation-service",
   "scalaVersion": "2.12.5",
   "version": "1.0.0",
   "teamEmail": "dl-EVOJAM-TEAMevojam.com",
   "sbtVersion": "0.13.17",
   "team": "EVOJAM TEAM"
}
```


Please note that:
- charset=UTF-8 does not to be included in the Content-Type HTTP header (https://www.iana.org/assignments/media-types/application/json)

### SSL
If SSL is enabled, one needs to add '--insecure' as a parameter to curl. This is only for testing purposes!

### Static information

The static information is available without authentication so that potential users of the service can review the docs at the start of any possible use of the service.

- http://localhost:8080/api-docs/swagger.json
- http://localhost:8080/api-docs/swagger.yaml

### Swagger UI

The ability to execute the service through the Swagger is restricted so that it only works when the user is properly authenticated.

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

