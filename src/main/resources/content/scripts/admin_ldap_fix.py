import os
#import stat
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

def doInit(additionalVariables):

 	ContainerUtils.getLogger(proxy).info("[admin_ldap_fix.py] Beginning doStart()")
 	
	containerdir = proxy.getContainer().getRuntimeContext().getVariable('CONTAINER_WORK_DIR').getValue()
	
	ldap_config_file_bad = os.path.join(containerdir, "ldapconfig")
	if os.path.exists(ldap_config_file_bad):
		ldap_config_file_good = os.path.join(containerdir, "ldapConfig")
		ContainerUtils.getLogger(proxy).info("[admin_ldap_fix.py] The path [" + ldap_config_file_bad +  "] exists.  Moving to correct path [" + ldap_config_file_good + "].")
		shutil.move(ldap_config_file_bad, ldap_config_file_good)
	else:
		ContainerUtils.getLogger(proxy).info("[admin_ldap_fix.py] The path [" + ldap_config_file_bad +  "] does not exists.  Nothing to fix.  Continuing.")
				
 	ContainerUtils.getLogger(proxy).info("[admin_ldap_fix.py] Exiting doStart()")
