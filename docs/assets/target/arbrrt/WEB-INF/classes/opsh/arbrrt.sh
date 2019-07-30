#!/bin/bash 

###############################################################################
# 					A d m i n i s t r a t i o n  S c r i p t
#					   		R e f R e p o r t
#
# Usage :
#          ./arbrrt.sh [enable|disable|init|starttimer|stoptimer|graphdb|graphdbmemory]
#
#
# Requirements :
#          Be root or RefReport owner : "@username@"
#
#
# History :
#     Creation                   22/10/2010  TRT
#
###############################################################################
# BEGIN OF CONFIG_SECTION

# Command
RPM=`which rpm`

# Path/File
REFREPORT_ID="@id.platon@"
REFREPORT="RefReport ($REFREPORT_ID)"
REFREPORT_PACKAGE="@arbrrt.package@"
REFREPORT_VERSION="@arbrrt.version@"
REFREPORT_PATH="@arbrrtPath@"
REFREPORT_SCRIPT_PATH="@execPath@/opsh"

# Script informations
SCRIPT="arbrrt.sh"

# User used to launch the application
USER="@username@"
USER_UID="@userid@"

# END OF CONFIG_SECTION
#################################################################################

# Test for good user ID
be_good_user()
{
  if [ "$UID" -ne "0" -a "$UID" -ne "$USER_UID" ]
  then
    echo "Must be $USER or root to $1 $REFREPORT"
    exit 45
  else
      if [ "$UID" -eq "0" ]
      then
          su - $USER -c "cd $REFREPORT_SCRIPT_PATH; ./$SCRIPT $*"
          exit 0
      else
      	  cd $REFREPORT_SCRIPT_PATH;
      fi
  fi
}

# Secure application disabling on server
secure_disable()
{
	if [ -z "$1" ]; then
		glassfish.sh disable $REFREPORT_ID:$REFREPORT_VERSION
	else
		glassfish.sh disable $REFREPORT_ID:$1
	fi
}

# Secure application enabling on server
secure_enable()
{
	if [ -z "$1" ]; then
		glassfish.sh enable $REFREPORT_ID:$REFREPORT_VERSION
	else
		glassfish.sh enable $REFREPORT_ID:$1
	fi
	
}

# Echo status according to the given http_code
echoStatusWithHttpCode()
{
	HTTP_CODE=$1
	STATUS_OK=$2
	if [ "$HTTP_CODE" -eq "$STATUS_OK" ]; then
		echo -e "Status: \e[1;32mOK\e[0m"
	else
		echo -e "Status: \e[1;31mKO\e[0m"
		if [ "$HTTP_CODE" -eq "000" ]; then
			echo -e "Error: \e[0;31mCurl call no valid - Check glassfish is started\e[0m"
		else
			echo -e "Error: HTTP_CODE \e[0;31m$HTTP_CODE\e[0m"
		fi
	fi
}

# Force configuration provisioning from files
force_conf()
{
        echo -e "Force RefReport configuration from files..."
        HTTP_CODE=$(curl -X GET -w "%{http_code}" -s -S http://localhost:8080/$REFREPORT_ID/rs/provisioning/yellowPart/force 2>/dev/null)
        echoStatusWithHttpCode $HTTP_CODE "200"
}

# Stop configuration provisioning timer
stop_timer_conf()
{
		SILENT=$1
		
        echo -e "Stop RefReport configuration timer..."
        HTTP_CODE=$(curl -X GET -w "%{http_code}" -s -S http://localhost:8080/$REFREPORT_ID/rs/provisioning/yellowPart/timer/stop 2>/dev/null)
        if [ "$HTTP_CODE" -eq "200" ]; then
			echo -e "Status: \e[1;32mOK\e[0m"
		else
        	if [ "$SILENT" != "silent" ];then
				echo -e "Status: \e[1;31mKO\e[0m"
				if [ "$HTTP_CODE" -eq "000" ]; then
					echo -e "Error: \e[0;31mCurl call no valid - Check glassfish is started\e[0m"
				else
					echo -e "Error: HTTP_CODE \e[0;31m$HTTP_CODE\e[0m"
				fi
        	fi
		fi
}

# Start configuration provisioning timer
start_timer_conf()
{
        echo -e "Start RefReport configuration timer..."
        HTTP_CODE=$(curl -X GET -w "%{http_code}" -s -S http://localhost:8080/$REFREPORT_ID/rs/provisioning/yellowPart/timer/start 2>/dev/null)
        echoStatusWithHttpCode $HTTP_CODE "200"
}

# Load neo4j database 
load_neo4j_db()
{
        echo -e "Load RefReport neo4j database..."

		GRAPH_FOLDER="$1"
        echo "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><graphFolderParameter><graphFolder>$GRAPH_FOLDER</graphFolder></graphFolderParameter>" > /tmp/put_content.xml
        HTTP_CODE=$(curl -X PUT -d @/tmp/put_content.xml -H "Content-Type:application/xml" -w "%{http_code}" -s -S http://localhost:8080/$REFREPORT_ID/rs/inventoryGraph/administration/graph/active 2>/dev/null)
        rm -rf /tmp/put_content.xml
        
        echoStatusWithHttpCode $HTTP_CODE "201"
}

# Load neo4j database in memory
load_neo4j_inmemory()
{
        echo -e "Load current RefReport neo4j database in memory ..."

		GRAPH_FOLDER="D:\Data\stat\graphdatabase\$1"
        HTTP_CODE=$(curl -X PUT -H "Content-Type:application/xml" -w "%{http_code}" -s -S http://localhost:8080/$REFREPORT_ID/rs/inventoryGraph/administration/current/inmemory 2>/dev/null)
        
        echoStatusWithHttpCode $HTTP_CODE "200"
}

#
# (re)create the links to the administration shells into opsh 
#
setlink()
{
	FILES=`cd $REFREPORT_PATH/opsh; ls -c1` 
	for shl in $FILES
	do 
		rm -f $REFREPORT_SCRIPT_PATH/$shl
		ln -s $REFREPORT_PATH/opsh/$shl $REFREPORT_SCRIPT_PATH/$shl
	done
}

#Main
case "$1" in
        'enable')
        		be_good_user $*
        		echo "Enabling $REFREPORT $2 ..."
				secure_enable $2
                ;;
        'disable') 
        		be_good_user $*
                echo "Disabling $REFREPORT $2 ..."
				secure_disable $2
                ;;
        'init') 
        		be_good_user $*
                force_conf
                ;;
        'stoptimer') 
        		be_good_user $*
                stop_timer_conf
                ;;
        'stoptimersilently') 
        		be_good_user $*
                stop_timer_conf "silent"
                ;;
        'starttimer') 
        		be_good_user $*
                start_timer_conf
                ;;
        'graphdb') 
        		be_good_user $*
                load_neo4j_db $2
                ;;
        'graphdbmemory') 
        		be_good_user $*
                load_neo4j_inmemory
                ;;                
        'setlink')
        		be_good_user $*
                echo "Restore opsh script for $REFREPORT version $REFREPORT_VERSION ..."
				setlink
				;;
                *)
                echo -e "Usage: $SCRIPT <command> <arguments>\n"
                echo -e "Command description: "
                echo -e "  enable \e[1;31m[GOROCO]\e[0m\t\t: Enable $REFREPORT on Glassfish server \n\t\t\t\t  If GOROCO provided, previous active version is replaced by this one"
                echo -e "  disable \e[1;31m[GOROCO]\e[0m\t\t: Disable $REFREPORT on Glassfish server \n\t\t\t\t  If GOROCO provided, only GOROCO version is disabled. \n\t\t\t\t  (GOROCO can be 'ALL' to match every version of the application)"
				echo -e "  init\t\t\t\t: Initialize the RefReport configuration based on files \n\t\t\t\t  \e[1;31m(NB: All database configuration will be dropped)\e[0m"
                echo -e "  starttimer\t\t\t: Start RefReport provisioning configuration timer"
                echo -e "  stoptimer\t\t\t: Stop RefReport provisioning configuration timer"
 		        echo -e "  graphdb \e[1;33m<graph-db-directory>\e[0m\t: Load the neo4j graph database located in <graph-db-directory> as current"	        
 		        echo -e "  graphdbmemory\t\t\t: Load current neo4j graph database in memory"	        
 		        exit 1
		        ;;
esac
exit 0
