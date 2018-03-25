# Evojam Invitations Service API

This SBT module is being built in 3 configuration using cross-compilation:
- Scala 2.10
- Scala 2.11
- Scala 2.12

The purpose is to support old legacy systems that are using our API and have not switched yet to Scala 2.12.x.

Invitation service uses REST interface to receive and persist all initiations in the internal database (H2 by default).
Due to that fact there are some internal rules the client of the API is obliged to obey.
Invitation JSON object consists of two fields: invitee, email

Example:
```json
{
  "invitee": "Adam Dec",
  "email": "adec@evojam.com"
}
```

Invitee field must not:
- be empty

Email field must not:
- be empty
- be in wrong format (ex. local-part@domain, more here: https://en.wikipedia.org/wiki/Email_address)

Please note that Invitations JSOB object contains a list of invitation. Service is designed in such a way that this list will be never empty.
If there will be no invitations, service will return HTTP 404 NOT FOUND response.