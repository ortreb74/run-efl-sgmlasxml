#!/usr/bin/env bash

echo java -cp "c:/app/sgmlAsXml/lib/*" program.Sgml2Xml "$1"

java -cp "c:/app/sgmlAsXml/lib/*" program.Sgml2Xml "$1"

if [ $? -ne 0 ] 
then
read
exit $?
fi
