import os
import stat
import shutil

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
	# Create BW external directories
 	ContainerUtils.getLogger(proxy).info("[bw.py] Beginning doStart()")
	
	tibco_dir = proxy.getContainer().getRuntimeContext().getVariable("sfs_TIBCO_DIR").getValue()
	engdir = proxy.getContainer().getRuntimeContext().getVariable('CONTAINER_WORK_DIR').getValue() 
	tibcohome = proxy.getContainer().getRuntimeContext().getVariable('TIBCO_HOME').getValue()

	jardir = os.path.join(tibco_dir, "extResources", "jars")
	certdir = os.path.join(tibco_dir, "extResources", "certificates")
	confdir = os.path.join(tibco_dir, "extResources", "config")
	otherdir = os.path.join(tibco_dir, "extResources", "other")

	if not os.path.exists(jardir):
		os.makedirs(jardir)

	if not os.path.exists(certdir):
		os.makedirs(certdir)

	if not os.path.exists(confdir):
		os.makedirs(confdir)

	if not os.path.exists(otherdir):
		os.makedirs(otherdir)
		
	# move tibco supplied drivers to tpcl jdbc directory
	tpcljdbcdir = os.path.join(tibcohome, 'tpcl', '5.7', 'jdbc')
	jdbcsrcdir = os.path.join(engdir, 'jdbc')
	
	ContainerUtils.getLogger(proxy).info("[bw.py] TPCL JDBC Dir: " + tpcljdbcdir)
	ContainerUtils.getLogger(proxy).info("[bw.py] JDBC Src Dir: " + jdbcsrcdir)
	
	if not os.path.exists(tpcljdbcdir):
		os.makedirs(tpcljdbcdir)
	
	sourceFileList = os.listdir(jdbcsrcdir)
	for files in sourceFileList:
		if files.endswith(".jar"):
			ContainerUtils.getLogger(proxy).info("[bw.py] Copying: " + jdbcsrcdir + files )
			shutil.copy(jdbcsrcdir + "/" + files, tpcljdbcdir)
