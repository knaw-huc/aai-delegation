#!/bin/bash
set -e

cd /tmp
wget https://www.python.org/ftp/python/2.6.8/Python-2.6.8.tar.bz2
tar jxf Python-2.6.8.tar.bz2
cd Python-2.6.8
./configure --prefix=/usr/local --with-zlib
make
make install

virtualenv --python=/usr/local/bin/python2.6
cd /tmp/source

if [ -d "ndg_oauth" ]; then
    rm -fr ndg_oauth 
fi
git clone https://github.com/wvengen/ndg_oauth.git ndg_oauth 
cd ndg_oauth/ndg_oauth_server 
python setup.py install 

# paster serve bearer_tok_server_app.ini
# keep docker running when redeploying:
tail -f /dev/null
