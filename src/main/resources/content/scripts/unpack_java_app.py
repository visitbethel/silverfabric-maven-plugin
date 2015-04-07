
import os
import commands

from com.datasynapse.fabric.common import RuntimeContextVariable
from com.datasynapse.fabric.util import ContainerUtils

"""
	The Component is a sub-script of sfs_component_script.py.  
"""
import inspect
try: proxy
except NameError:    
    globals()['proxy'] = inspect.currentframe().f_back.f_globals['proxy']
else: pass

def doStart():
	#
	# Unpack the zip (application) that was attached to the component as a conent file
	# ${CONTAINER_WORK_DIR}/${UNPACK_JAVA_APP_DIR} is expected to be the location with zip file(s)
	#
 	ContainerUtils.getLogger(proxy).info("[unpack_java_app.py] Beginning doStart()")
	
	containerdir = proxy.getContainer().getRuntimeContext().getVariable('CONTAINER_WORK_DIR').getValue() 
 	ContainerUtils.getLogger(proxy).info("[unpack_java_app.py] CONTAINER_WORK_DIR="+containerdir)

	unpackjavaappdir = proxy.getContainer().getRuntimeContext().getVariable("UNPACK_JAVA_APP_DIR").getValue()
 	ContainerUtils.getLogger(proxy).info("[unpack_java_app.py] UNPACK_JAVA_APP_DIR="+unpackjavaappdir)

	zipdir = os.path.join(containerdir, unpackjavaappdir)

	saveCwd = os.getcwd()
	
	os.chdir(zipdir)
	sourceFileList = os.listdir(zipdir)
	for file in sourceFileList:
		if file.endswith(".zip"):
			zipfilepath=os.path.join(zipdir, file)
			zipfilepath_todir = zipfilepath + "_Files"
			ContainerUtils.getLogger(proxy).info("[unpack_java_app.py] Found: " + zipfilepath )
			ContainerUtils.getLogger(proxy).info("[unpack_java_app.py] Extract to: " + zipfilepath_todir )
			os.makedirs(zipfilepath_todir)
			os.chdir(zipfilepath_todir)
			unzipresult = commands.getoutput( "/usr/bin/unzip "+zipfilepath ); 
			ContainerUtils.getLogger(proxy).info("[unpack_java_app.py] /usr/bin/unzip returned: " + unzipresult )

	os.chdir(saveCwd)

