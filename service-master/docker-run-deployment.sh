#!/bin/bash
set -e

cd /tmp/source

mvn clean install
cp target/HelloTomcat.war /usr/local/tomcat/webapps/HelloTomcat.war

/usr/local/tomcat/bin/catalina.sh run

# keep docker running when redeploying:
tail -f /dev/null
