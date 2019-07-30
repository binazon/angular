#!/bin/sh

if [ "$#" -ne 1 ]; then
	echo "Usage : injectDataCollect.sh <db-name refreport>"
	exit 1
fi
PATH_FILES=/data/flf/stat/backup/arbrrt
DATABASE=$1
# Check files
if ! [ -f "${PATH_FILES}/T_PROXY.sql" ]; then
	echo "${PATH_FILES}/T_PROXY.sql : File not found"
	exit 1
fi
if ! [ -f "${PATH_FILES}/T_SOURCE_CLASS.sql" ]; then
        echo "${PATH_FILES}/T_SOURCE_CLASS.sql : File not found"
        exit 1
fi
if ! [ -f "${PATH_FILES}/T_INPUT_SOURCE.sql" ]; then
        echo "${PATH_FILES}/T_INPUT_SOURCE.sql : File not found"
        exit 1
fi
if ! [ -f "${PATH_FILES}/T_SOURCE_PROXY.sql" ]; then
        echo "${PATH_FILES}/T_SOURCE_PROXY.sql : File not found"
        exit 1
fi
# Truncate tables linked to collects
echo "-> Delete contents table T_SOURCE_PROXY in ${DATABASE}"
mysqltool.sh query ${DATABASE} "delete from T_SOURCE_PROXY;"
echo "-> Delete contents T_INPUT_SOURCE in ${DATABASE}"
mysqltool.sh query ${DATABASE} "delete from T_INPUT_SOURCE;"
echo "-> Delete contents table T_SOURCE_CLASS in ${DATABASE}"
mysqltool.sh query ${DATABASE} "delete from T_SOURCE_CLASS;"
echo "-> Delete contents  T_PROXY in ${DATABASE}"
mysqltool.sh query ${DATABASE} "delete from T_PROXY;"

# Inject tables linked to collects
echo "-> mysqltool.sh inject ${DATABASE} ${PATH_FILES}/T_PROXY.sql"
mysqltool.sh inject ${DATABASE} ${PATH_FILES}/T_PROXY.sql
echo "-> mysqltool.sh inject ${DATABASE} ${PATH_FILES}/T_SOURCE_CLASS.sql"
mysqltool.sh inject ${DATABASE} ${PATH_FILES}/T_SOURCE_CLASS.sql
echo "-> mysqltool.sh inject ${DATABASE} ${PATH_FILES}/T_INPUT_SOURCE.sql"
mysqltool.sh inject ${DATABASE} ${PATH_FILES}/T_INPUT_SOURCE.sql
echo "-> mysqltool.sh inject ${DATABASE} ${PATH_FILES}/T_SOURCE_PROXY.sql"
mysqltool.sh inject ${DATABASE} ${PATH_FILES}/T_SOURCE_PROXY.sql

