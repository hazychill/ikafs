#!/bin/sh

ORIG_FILE=war/WEB-INF/appengine-web.xml
OLD_FILE="${ORIG_FILE}.old"
NEW_FILE="${ORIG_FILE}.new"

cat "${ORIG_FILE}" | sed "s|<version>.*</version>|<version>ika-$(date +%Y%m%d%H%M)-$(git rev-parse --short=10 HEAD)</version>|" > "${NEW_FILE}" &&
mv "${ORIG_FILE}" "${OLD_FILE}" &&
mv "${NEW_FILE}" "${ORIG_FILE}"
ret=$?

if [ ${ret} -eq 0 ]
then
	diff -u "${OLD_FILE}" "${ORIG_FILE}"
else
	echo "error(${ret})"
	exit 1
fi
