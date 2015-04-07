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
	# Start of process
 	ContainerUtils.getLogger(proxy).info("[admin.py] Beginning doStart()")
 	
 	# Get required context vars
	engdir = proxy.getContainer().getRuntimeContext().getVariable('ENGINE_WORK_DIR').getValue() 
	domaindir = proxy.getContainer().getRuntimeContext().getVariable('TIBCO_DOMAIN_NAME').getValue()
	tibcohome = proxy.getContainer().getRuntimeContext().getVariable('TIBCO_HOME').getValue()
	
	# 
	tmpdir = os.path.join(engdir, "domaindata", "tra", domaindir, "tmp")

	ContainerUtils.getLogger(proxy).info("[admin.py] Temp Dir Name:" + tmpdir )
	ContainerUtils.getLogger(proxy).info("[admin.py] TIBCO Home:" + tibcohome )
	
	#certdir = engdir + "/fabric/https/serverCert"
	#ldapcertdir = engdir + "/fabric/ldapCert"

	#ContainerUtils.getLogger(proxy).info("[admin.py] serverCert Dir Name:" + certdir )
	#ContainerUtils.getLogger(proxy).info("[admin.py] ldapCert Dir Name:" + ldapcertdir )
	
	#if not os.path.exists(certdir):
	#	os.makedirs(certdir)
	#if not os.path.exists(ldapcertdir):
	#	os.makedirs(ldapcertdir)
		
	#shutil.copy(engdir+"/fabric/https/servercert/SilverBWAdmin.europe.intranet.pem", certdir)
	#shutil.copy(engdir+"/fabric/ldapcert/rootca.pem", ldapcertdir)
	
	#proxy.doStart()

	if not os.path.exists(tmpdir):
		os.makedirs(tmpdir)
		
 	ContainerUtils.getLogger(proxy).info("[admin.py] Exiting doStart()")