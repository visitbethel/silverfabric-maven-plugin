#!/bin/sh
##############################################################################
# sfs_pauseresume_wrapper.sh
#
# This supports the PatternFactory's PauseResume feature on Block Processes.
# It will keep the blocking thread from exiting while in 'pause' mode, which would
# cause the component to shutdown and restart.
#
##############################################################################

#--------------------------------------------------------------------
# Get start command
#  Note: extra work required because we can't be sure what the current work directory
#        will be when this script is called.
#--------------------------------------------------------------------
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
. ${DIR}/sfs_pauseresume_wrapper_env.sh

echo "[sfs_pauseresume_wrapper.sh] Start command is [${SFS_PAUSERESUME_START_COMMAND}]."

#--------------------------------------------------------------------
# If blocking process returns but pause has been requested, keep thread alive 
#--------------------------------------------------------------------

SFS_PAUSERESUME_WRAPPER_ACTIVE="yes"

while [ "$SFS_PAUSERESUME_WRAPPER_ACTIVE" == "yes" ]
do 

	SFS_PAUSERESUME_WRAPPER_MODE=`cat $CONTAINER_WORK_DIR/.sfs_pauseresume_mode`
	echo "[sfs_pauseresume_wrapper.sh] PauseResume mode is [${SFS_PAUSERESUME_WRAPPER_MODE}]"
	
	if [ "${SFS_PAUSERESUME_WRAPPER_MODE}" == "shutdown" ]
	then
		echo "[sfs_pauseresume_wrapper.sh] Exiting."
		SFS_PAUSERESUME_WRAPPER_ACTIVE="no"
	elif [ "${SFS_PAUSERESUME_WRAPPER_MODE}" == "run" ]
	then

		echo "[sfs_pauseresume_wrapper.sh] SFS_PAUSERESUME_WRAPPER_MODE set to 'run'.  Will call [${SFS_PAUSERESUME_START_COMMAND}]"

		${SFS_PAUSERESUME_START_COMMAND}

		echo "[sfs_pauseresume_wrapper.sh] Blocking Process has stopped."
		sleep 10

	elif [ "${SFS_PAUSERESUME_WRAPPER_MODE}" == "pause" ]
	then
		echo "[sfs_pauseresume_wrapper.sh] Will sleep for 10 seconds."
		sleep 10
	else
		echo "[sfs_pauseresume_wrapper.sh] PauseResume mode is invalid.  Should be either 'run', 'pause' or 'shutdown'. Will assume 'pause' and sleep for 10 second."
		sleep 10
	fi

done



