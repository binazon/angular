#!/bin/sh

if [ "$#" -ne 1 ]; then
	echo "Usage : extractDataCollect.sh <db-name refreport>"
	exit 1
fi
PATH_FILES=/data/flf/stat/backup/arbrrt
rm ${PATH_FILES}/T_PROXY.sql 2>/dev/null
rm ${PATH_FILES}/T_SOURCE_CLASS.sql 2>/dev/null
rm ${PATH_FILES}/T_INPUT_SOURCE.sql 2>/dev/null
rm ${PATH_FILES}/T_SOURCE_PROXY.sql 2>/dev/null
DATABASE=$1
# Dump tables linked to collects
echo "-> mysqltool.sh tabledump ${DATABASE} T_PROXY ${PATH_FILES}/T_PROXY.sql"
mysqltool.sh tabledump ${DATABASE} T_PROXY ${PATH_FILES}/T_PROXY.sql
echo "-> mysqltool.sh tabledump ${DATABASE} T_SOURCE_CLASS ${PATH_FILES}/T_SOURCE_CLASS.sql"
mysqltool.sh tabledump ${DATABASE} T_SOURCE_CLASS ${PATH_FILES}/T_SOURCE_CLASS.sql
echo "-> mysqltool.sh tabledump ${DATABASE} T_INPUT_SOURCE ${PATH_FILES}/T_INPUT_SOURCE.sql"
mysqltool.sh tabledump ${DATABASE} T_INPUT_SOURCE ${PATH_FILES}/T_INPUT_SOURCE.sql
echo "-> mysqltool.sh tabledump ${DATABASE} T_SOURCE_PROXY ${PATH_FILES}/T_SOURCE_PROXY.sql"
mysqltool.sh tabledump ${DATABASE} T_SOURCE_PROXY ${PATH_FILES}/T_SOURCE_PROXY.sql

