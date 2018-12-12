#!/bin/bash
set -e

cd /tmp/source

mvn clean install
cp target/PlateClient.war /usr/local/tomcat/webapps/PlateClient.war

/usr/local/tomcat/bin/catalina.sh run

rm -fr /usr/local/tomcat/webapps/ROOT
cp target/PlateClient.war /usr/local/tomcat/webapps/ROOT.war

# keep docker running when redeploying:
tail -f /dev/null
