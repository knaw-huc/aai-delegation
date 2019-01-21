# Test on AAI delegation for VRE

Spring security framework: 
The communication to oauth is handled by Spring security, it is currently running on a older version of the frame work and potential vulnerabilities can apply.
 
Spring security configuration:
```xml
<xml>
  <oauth:resource id="foodService" type="authorization_code"
                  client-id="service-user"
                  client-secret="service-pass"
                  user-authorization-uri="https://as/oauth2-as/oauth2-authz"
                  access-token-uri="https://as/oauth2/token"
                  scope="read" />  

  <authentication-manager>
    <authentication-provider>
      <user-service>
        <user name="user-username" password="user-password" authorities="authority" />
      </user-service>
    </authentication-provider>
  </authentication-manager>
</xml>
```
Replace the client-id with your username which identifies your service to AS server and its corresponding password. 
Replace the `user-authorization-url` and `access-token-uri` with your AS' corresponding urls. 

The steps above should be done on both client and service. 

After the changes, the demo setup can be started and stopped using the scripts below


How to start the test setup using docker containers:
```bash
./start.sh
```

How to stop the test setup, please note: the stop script will stop and remove ALL docker containers:
```bash
./stop.sh
```


