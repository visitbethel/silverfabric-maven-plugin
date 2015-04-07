"""
This script is loaded by default into all components.

When one of the SF API's implemented in this script is called, it will locate all python sub-scripts 
that match the pattern ${CONTAINER_WORK_DIR}/scripts/*.py which also implement that function and call 
them as well.  So if more than one script implements a given method, they will all be called.  This 
differs from SF's standard behavior which is to execute only the first function implementation it 
finds and then to ignore the others.  The order in which sub-scripts are executed is unpredicatble at this time.

Sub-scripts require two alterations from standard scripts:
1) The sub script must contain the following lines at the module level (before the first function definition):

    import inspect
    try: proxy
    except NameError:    
        globals()['proxy'] = inspect.currentframe().f_back.f_globals['proxy']
    else: pass

2) Functions should not call the the parent function (for example youscript.doStart() should not contain a 
call to proxy.doStart() ).  This will be done by this script after all sub-script's functions are complete.  

All sub-scripts should be added to the component as content files into t${CONTAINER_WORK_DIR}/scripts.  They 
should NOT be added scripts.  Doing so may cause them to be called before this scripts which would cause all 
other scripts to be bypassed.

Custom enablers written in python (or other scripting languages) are not currently supported by this script.

"""

from com.datasynapse.fabric.admin.info import AllocationInfo
from com.datasynapse.fabric.util import GridlibUtils, ContainerUtils
from com.datasynapse.fabric.common import RuntimeContextVariable, ActivationInfo

import os
import sys
import glob
import shutil
import datetime
import platform
import socket
import subprocess

from subprocess import Popen, PIPE, STDOUT, call
import shlex
from time import sleep, time
from random import randint
import threading
from com.datasynapse.fabric.admin import AdminManager

curscript = "sfs_component_script.py"
hostip = socket.gethostbyname(socket.gethostname()) 
truevalues = ["yes", "y", "true",  "t", "1"]
falsevalues = ["no",  "n", "false", "f", "0"]

#try: dataclean_activated
#except NameError:    
#    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] DEBUG dataclean_activated is not set yet.  Will set it to False.")
#    globals()['dataclean_activated'] = False
#else: pass

enablerscript_var = proxy.getContainer().getRuntimeContext().getVariable('sfs_ENABLER_SCRIPT_NAME')
if (enablerscript_var != None):
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Importing python enabler()")
    enablerscript = enablerscript_var.getValue()
else:
    enablerscript = None

def LoggerWithSilencer(message, silent=None):
    if silent == None:
        silent = False

    if silent == True:
        ContainerUtils.getLogger(proxy).finer(message)
    else:
        ContainerUtils.getLogger(proxy).info(message)

def callSubscript(curfunction, param1=None, silent=None):
    if silent == None:
        silent = False

    LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning callSubscript()", silent=silent)
        
    ret_val = None
    workdir = proxy.getContainer().getRuntimeContext().getVariable('CONTAINER_WORK_DIR').getValue()    
    scriptdir = os.path.join(workdir, "scripts")

    if os.path.isdir(scriptdir):

        if not (scriptdir in str(sys.path)):
            sys.path.append(scriptdir)
            ContainerUtils.getLogger(proxy).finer("sys.path modified: " + str(sys.path) )        

        #from ems import ems_doInit
        scriptlist = glob.glob(os.path.join(scriptdir,"*.py"))
        if curscript in scriptlist: 
            scriptlist.remove(curscript)

        #if enablerscript in scriptlist: 
        #    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Removing enabler script {" + enablerscript + "} from list of subscripts")
        #    scriptlist.remove(enablerscript)

        LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] The following sub-scripts will be inspected [" + str(scriptlist) + "].", silent=silent)
        #(os.listdir(scriptdir))")
      
        for script in scriptlist:
            
            scriptfiledir, scriptfile = os.path.split(script)
            modulename = scriptfile[:-3]            
            
            try: module = __import__(modulename)
            except ImportError, e:    
                exc_type, exc_value = sys.exc_info()[:2]
                ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Unable to import module [" + 
                                                        modulename + 
                                                        "].  Exception class: [" + str(exc_type) +  
                                                        "].  Exception Message[" + str(exc_value) +"].")
                raise e
            else: 
            
                try:
                    func = getattr(module, curfunction)
                except AttributeError:
                    LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Will not call " + curfunction + "() in sub-script script [" + script + "].  Method not found in sub-script.", silent=silent)
                else:
                    LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Calling " + curfunction + "() in script [" + script + "].", silent=silent)
    
                    if (param1 == None):
                        LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] calling function with no parameters.", silent=silent)
                        ret_val = func()
                    else:
                        LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] calling function with 1 parameter.", silent=silent)
                        ret_val = func(param1)
                        
                    #Immediately return if one of the monitoring functions returns a False
                    if (curfunction == "hasContainerStarted") or (curfunction == "hasComponentStarted") or (curfunction == "isContainerRunning") or (curfunction == "isComponentRunning"):                       
                       if (ret_val == False):
                          break

    else:
        LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] No subscripts found to call.", silent=silent)
        
    LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting callSubscript()", silent=silent)
    return ret_val
    
def callEnabler(curfunction, param1=None, silent=None):
    if silent == None:
        silent = False
        
    LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning callEnabler()", silent=silent)

    ret_val = None
    #workdir = proxy.getContainer().getRuntimeContext().getVariable('CONTAINER_WORK_DIR').getValue()    
    #scriptdir = os.path.join(workdir, "scripts")

    #default
    func = getattr(proxy, curfunction)
   
    if enablerscript != None:
        
        LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Enabler script configured", silent=silent)
        
        """ ------------------------ """
        enablerpath = proxy.getContainer().getScript(0).getFile().getAbsolutePath()
    
        containerdir = proxy.getContainer().getRuntimeContext().getVariable('CONTAINER_WORK_DIR').getValue()
        sfs_enabler_dir = os.path.join(containerdir, "sfs_scripts_enabler") 
        if not os.path.isdir(sfs_enabler_dir):
            os.makedirs(sfs_enabler_dir)
        
        
        sfs_enablerfile = os.path.join(sfs_enabler_dir, enablerscript)
        if not os.path.isfile(sfs_enablerfile):
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Copying [" + str(enablerpath) + "] to [" + str(sfs_enabler_dir) + "].")
    
            shutil.copy(enablerpath, sfs_enabler_dir)
            
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Appending logic to set proxy object to in [" + str(sfs_enabler_dir) + "].")
            sfs_enablerfile_fh = open(sfs_enablerfile,'a')
            sfs_enablerfile_fh.write("""
import inspect
try: proxy
except NameError:    
    globals()['proxy'] = inspect.currentframe().f_back.f_globals['proxy']
    globals()['runtimeContext'] = inspect.currentframe().f_back.f_globals['runtimeContext']
    globals()['features'] = inspect.currentframe().f_back.f_globals['features']
    globals()['logger'] = inspect.currentframe().f_back.f_globals['logger']
    print "Proxy object resolved."
else: pass
        """)
            sfs_enablerfile_fh.close()

        #shutil.copy(enablerpath, sfs_enablerfile)

        if not(sfs_enabler_dir in sys.path ):
            #sys.path.append(proxy.getContainer().getScript(0).getFile().getParentFile().getAbsolutePath())
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Adding [" + sfs_enabler_dir + "] to top of sys.path [" + str(sys.path) + "].")
            sys.path.insert(0,sfs_enabler_dir)

        """ ------------------------ """
        
        
        modulename = enablerscript[:-3]            
        
        #DEBUG
        #import __builtin__
        #import inspect
        #print "__import__ is: " + str(inspect.getsource(__builtin__.__import__))
        
        try: module = __import__(modulename)
        except ImportError, e:    
            exc_type, exc_value = sys.exc_info()[:2]
            ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Unable to import module [" + 
                                                    modulename + 
                                                    "].  Exception class: [" + str(exc_type) +  
                                                    "].  Exception Message[" + str(exc_value) +"].")
            #debug
            shutil.copy(sfs_enablerfile, "/tmp")
            raise e
        else: 

            #print dir(module)    #debug
            try:
                func = getattr(module, curfunction)
                LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Calling " + curfunction + "() in script [" + enablerscript + "].", silent=silent)
            except AttributeError:
                LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Will not call " + curfunction + "() in script [" + enablerscript + "].  Method not found in module.", silent=silent)
        
    if (param1 == None):
        LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] calling function with no parameters.", silent=silent)
        ret_val = func()
    else:
        LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] calling function with 1 parameter.", silent=silent)
        ret_val = func(param1)
        
    LoggerWithSilencer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting callEnabler()", silent=silent)
    return ret_val

def prepareWorkDirectory():
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning prepareWorkDirectory(). Running on: " + os.name + " " + platform.platform())

    """
    Integrity Checks 
    """    
    runtimeplat = platform.platform()
    
    if ContainerUtils.isWindows():
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Windows detected - ulimits not checked")
    else:
        """ File Descriptors """    
        commandline = "ulimit -Hn"
        output = runCommand(commandline, suppressOutput=True, shell=True)
        if (output[0] == 0):
            file_descriptor_limit = output[1].rstrip()
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] File Descriptor Hard Limit is [" + str(file_descriptor_limit) + "].")
    
            file_descriptor_limit_minimum = 1000000
            
            if (int(str(file_descriptor_limit)) < file_descriptor_limit_minimum):
                ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] File Descriptor Hard Limit [" + str(file_descriptor_limit) + 
                                                       "] is less than recommended minimum [" + str(file_descriptor_limit_minimum) + "].")
    
            #else:
            #    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] File Descriptor Limit is [" + str(file_descriptor_limit) + "].")
        else:            
            ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Unable to to verify if sufficient File Descriptors have been allocated.  " +
                                                   "Will attempt to continue with normal component startup.")
            
        commandline = "ulimit -Sn"
        output = runCommand(commandline, suppressOutput=True, shell=True)
        if (output[0] == 0):
            file_descriptor_soft_limit = output[1].rstrip()
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] File Descriptor Soft Limit is [" + str(file_descriptor_soft_limit) + "].")
    
            file_descriptor_soft_limit_minimum = 1000000
            
            if (int(str(file_descriptor_soft_limit)) < file_descriptor_soft_limit_minimum):
                ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] File Descriptor Soft Limit [" + str(file_descriptor_soft_limit) + 
                                                       "] is less than recommended minimum [" + str(file_descriptor_soft_limit_minimum) + "].")
    
            #else:
            #    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] File Descriptor Limit is [" + str(file_descriptor_soft_limit) + "].")
        else:            
            ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Unable to to verify if sufficient File Descriptors have been allocated.  " +
                                                   "Will attempt to continue with normal component startup.")

    
    localhostname = socket.gethostname()
    
    if ContainerUtils.isWindows():
        commandline = "ping " + localhostname + " -n 1"
        output = runCommand(commandline)
    else:
        commandline = "ping " + localhostname + " -c 1"
        output = runCommand(commandline, shell=True)
    
    if (output[0] != 0):
        ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Host is not properly configured.  Cannot resolve and ping its own hostname and ping it.  Component startup will not continue.")
        raise Exception("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Host is not properly configured.  Cannot resolve and ping its own hostname and ping it.  Component startup will not continue.")

    
    """ Persistent Storage Response Time """   
    persistentdata_dir = proxy.getContainer().getRuntimeContext().getVariable("sfs_PERSISTENTDATA_DIR").getValue()
    
    if os.path.exists(persistentdata_dir):
        if ContainerUtils.isWindows():
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Windows detected - Persistent data performance not checked")
        else:
            commandline = "time dd if=/dev/zero of=" + persistentdata_dir + "/testfile.txt bs=1000000 count=1000"
            output = runCommand(commandline, suppressOutput=False, shell=True)
            
            commandline = "rm " + persistentdata_dir + "/testfile.txt"
            output = runCommand(commandline, suppressOutput=False, shell=True)
    else:
        ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Persistent Data Directory " + persistentdata_dir +
                                                " does not exist yet.  Will create it.")        
        os.makedirs(persistentdata_dir)
        
    """ Available Entropy """  
    if ContainerUtils.isWindows(): 
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Windows detected - Entropy not checked")
    else:
        commandline = "cat /proc/sys/kernel/random/entropy_avail"
        output = runCommand(commandline, suppressOutput=True, shell=True)
        if (output[0] == 0):
            entropy_avail = output[1].rstrip()
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Available Entropy is [" + str(entropy_avail) + "].")
    
            entropy_avail_minimum = 1000
            
            if (int(str(entropy_avail)) <= entropy_avail_minimum):
                ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]  Available Entropy  [" + str(entropy_avail) + 
                                                       "] is less than recommended minimum [" + str(entropy_avail_minimum) + "].")
    
            #else:
            #    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] File Descriptor Limit is [" + str(entropy_avail) + "].")
        else:            
            ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Unable to to verify sufficient Available Entropy.  " +
                                                   "Will attempt to continue with normal component startup.")
    
    """
    Cold Start 
    """    
    coldstartEnabled = proxy.getContainer().getRuntimeContext().getVariable('sfs_COLDSTART_ENABLED').getValue()

    if (coldstartEnabled == "true"):
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] sfs_COLDSTART_ENABLED set to true.")

        deleteRuntimeDirOnShutdown = proxy.getContainer().getRuntimeContext().getVariable('DELETE_RUNTIME_DIR_ON_SHUTDOWN').getValue()
        if (deleteRuntimeDirOnShutdown == "false"):
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] DELETE_RUNTIME_DIR_ON_SHUTDOWN set to false.  Will not clean up work directory from previous deployment.")
        else:             
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Cleaning up directory from previous deployment.")
            proxy.cleanupContainer()
 
            enginedir = proxy.getContainer().getRuntimeContext().getVariable('ENGINE_WORK_DIR').getValue()
            
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Cleaning up any files or directories from previous deployment in " + enginedir + ".")
            
            if os.path.exists(enginedir):
                for f in os.listdir(enginedir):
                    callSubscript("cleanupContainer")
                    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Calling Enabler's cleanupContainer()")
                    proxy.cleanupContainer()

                    """
                    Some extra cleanup in case previous deployment was a different type of enabler
                    """

                    if (f == "log"):
                        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]     Preserving /log directory from previous deployment.")                     
                    else:
                        f_path = os.path.join(enginedir,f)
                        if os.path.isfile(f_path):
                            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]     Removing /" + f + " file from previous deployment.")
                            os.remove(f_path)
                        else:
                            shutil.rmtree(f_path) 
                            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]     Removing /" + f + " directory from previous deployment.")
            else:
                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]     [" + enginedir + "] does not exist.  Nothing to clean up.")

    callEnabler("prepareWorkDirectory")

    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting prepareWorkDirectory()")

def native_rmdir(path):
    ''' Removes directory recursively using native rmdir command
    '''

    # Get path to cmd
    try:
        cmd_path = native_rmdir._cmd_path
    except AttributeError:
        cmd_path = os.path.join(
            os.environ['SYSTEMROOT'] if 'SYSTEMROOT' in os.environ else r'C:\Windows', 'System32', 'cmd.exe')
        native_rmdir._cmd_path = cmd_path

    # /C - cmd will terminate after command is carried out
    # /S - recursively, 
    args = [cmd_path, '/C', 'rmdir', '/S', '/Q', path]
    subprocess.check_call(args, env={})

def doInit(additionalVariables):
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning doInit()")

    """
    Data Clean
    """
    #global dataclean_activated
    #globals()['dataclean_activated'] = False
    
    datacleanAllowed = proxy.getContainer().getRuntimeContext().getVariable('sfs_DATACLEAN_ALLOWED').getValue()
    if (datacleanAllowed == "true"):
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Data clean functionality allowed.  Will remove paths defined in sfs_DATACLEAN_PATHS if they are older than timestamp in sfs_DATACLEAN_TIMESTAMP.")
        datacleanTimestamp_var = proxy.getContainer().getRuntimeContext().getVariable('sfs_DATACLEAN_TIMESTAMP')
        if (datacleanTimestamp_var != None):
            datacleanTimestamp = datacleanTimestamp_var.getValue()
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]     sfs_DATACLEAN_TIMESTAMP = [" + datacleanTimestamp + "].")
            
            #s = "Apr 18 2014 17:19:42 -0600"
            datacleanTimestamp_datetime = datetime.datetime.strptime(datacleanTimestamp, "%b %d, %Y %H:%M:%S %z")
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]     sfs_DATACLEAN_TIMESTAMP (localized) = [" + str(datacleanTimestamp_datetime) + "].")

            datacleanPaths_var = proxy.getContainer().getRuntimeContext().getVariable('sfs_DATACLEAN_PATHS')
            if (datacleanPaths_var != None):
                
                datacleanPaths = datacleanPaths_var.getValue()
                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]     sfs_DATACLEAN_PATHS = [" + datacleanPaths + "].")
                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]     ----------------------------------------------------------------------")
                for path in datacleanPaths.split(","):
                    
                    if os.path.exists(path):
                        path_ctime = os.path.getctime(path)                        
                        fileCreation = datetime.datetime.fromtimestamp(path_ctime)
                        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]     Path [" + path + "] create time is [" + str(fileCreation) + "].")
                        if fileCreation < datacleanTimestamp_datetime:
                            """ Set Flag to notify any sub-scripts that do Data"""
                            additionalVariables.add(RuntimeContextVariable("sfs_script_DATACLEAN_ACTIVATED", "true", 
                                                   RuntimeContextVariable.STRING_TYPE, "Indicates Dataclean feature has been activated for this start", False, RuntimeContextVariable.NO_INCREMENT))
                            #globals()['dataclean_activated'] = True
                            if os.path.isfile(path):
                                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]          Removing file [" + path + "].")
                                os.remove(path)
                            elif os.path.isdir(path):
                                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]          Removing directory [" + path + "].")
                                if ContainerUtils.isWindows():
                                    delpath = path.replace("/", "\\")
                                    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]  On windows issuing: " + "rmdir /S /Q " + delpath)
                                    #runCommand('rmdir /S /Q '+ delpath)
                                    native_rmdir(delpath)
                                else:
                                    shutil.rmtree(path)
                            else:
                                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]          Cannot identify [" + path + "] as either a file or directory.  Will not delete.  Continuing.")
                        else:
                            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]          Keeping path [" + path + "].")
                    else:
                        ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]     Path [" + path + "] does not exist.  Nothing to delete. Continuing.")
            else:
                ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]     Data Clean allowed but the variable sfs_DATACLEAN_PATHS is not defined.  No files will be deleted. Continuing.")
        else:
            ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]     Data Clean allowed but the variable sfs_DATACLEAN_TIMESTAMP is not defined.  No files will be deleted. Continuing.")
    else:
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Data clean functionality not allowed.  Bypassing.")
                    
    #ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] DEBUG dataclean_activated is [" + str(globals()['dataclean_activated']) + "].")



    additionalVariables.add(RuntimeContextVariable("sfs_script_COMPONENT_NAME", proxy.getContainer().getCurrentDomain().getName(), 
                                                   RuntimeContextVariable.STRING_TYPE, "Current Component's name", False, RuntimeContextVariable.NO_INCREMENT))
    additionalVariables.add(RuntimeContextVariable("sfs_script_AMP_c", "&", 
                                                   RuntimeContextVariable.STRING_TYPE, "Ampersand character constant", False, RuntimeContextVariable.NO_INCREMENT))
    additionalVariables.add(RuntimeContextVariable("sfs_script_PAUSERESUME_STATISTICS_ON", "true", 
                                                   RuntimeContextVariable.STRING_TYPE, "Ampersand character constant", False, RuntimeContextVariable.NO_INCREMENT))

    """
    Call Sub-Module doInit's
    """
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] AdditonalVariable count is [" + str(len(additionalVariables)) + "].")

    #workdir = proxy.getContainer().getRuntimeContext().getVariable('CONTAINER_WORK_DIR').getValue()    
    #scriptdir = os.path.join(workdir, "scripts")
    #if os.path.isdir(scriptdir):
    #    sys.path.append(scriptdir)
    #    ContainerUtils.getLogger(proxy).info("sys.path modified: " + str(sys.path) )        
    callSubscript("doInit", additionalVariables)
        
    """
    Enabler doInit()
    """
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Calling Enabler's doInit() with AdditonalVariable count of [" + str(len(additionalVariables)) + "].")
    callEnabler("doInit", param1=additionalVariables)
    #proxy.doInit(additionalVariables)
    
    """
    PauseResume
    """
    setPauseResumeMode("run")
    setPauseResumeModeRequested("run")

    pauseresume_blockingprocess = proxy.getContainer().getRuntimeContext().getVariable('sfs_script_PAUSERESUME_BLOCKING_PROCESS').getValue()    
    clusterEvents_enabled = proxy.getContainer().getRuntimeContext().getVariable('sfs_CLUSTEREVENTS_ENABLED').getValue()    
    if pauseresume_blockingprocess == "true":
                
        """ Prepare PauseResume wrapper shell script  """
        containerdir = proxy.getContainer().getRuntimeContext().getVariable('CONTAINER_WORK_DIR').getValue()
        startwrapper_file = os.path.join(containerdir, "sfs_pauseresume_wrapper.sh")

        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Contents of proxy.container.unixCommand.startup [" +  proxy.container.unixCommand.startupCommand + "].")
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Contents of proxy.container.getStartCommand() [" +  proxy.container.getStartCommand() + "].")

        orignalStartCommand = proxy.container.getStartCommand()
        
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Inserting PauseResume wrapper shell script as start command [" + orignalStartCommand + "].")
        #proxy.container.setStartCommand(startwrapper_file)
        
        #TODO - Add windows support
        proxy.container.unixCommand.setStartupCommand(startwrapper_file)

        dos2unix(startwrapper_file)
        
        
        """ Set Component-specific Start Command via environment file """
        startwrapper_environment_file = os.path.join(containerdir, "sfs_pauseresume_wrapper_env.sh")
        
        startwrapper_environment_fh = open(startwrapper_environment_file, "w")
        startwrapper_environment_fh.write("SFS_PAUSERESUME_START_COMMAND=" + orignalStartCommand)
        startwrapper_environment_fh.close()

        dos2unix(startwrapper_environment_file)


    """
    ClusterEvents
    """
    if clusterEvents_enabled =="true":        

        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] ClusterEvents enabled.  Will perform setup activities.")

        restartAction_DeleteLock()
        
        runCommand("mkdir -p " + getClusterEvents_dir())        
        runCommand("mkdir -p " + getClusterEvents_Events_dir())        

        clusterEventsThread = createClusterEventThread()
        additionalVariables.add(RuntimeContextVariable("sfs_script_CLUSTEREVENTS_PROCESSING_THREAD", clusterEventsThread, 
                                                       RuntimeContextVariable.OBJECT_TYPE))
        additionalVariables.add(RuntimeContextVariable("sfs_script_CLUSTEREVENTS_PROCESSING_THREAD_RESTARTS", "0", 
                                                       RuntimeContextVariable.STRING_TYPE))        
        additionalVariables.add(RuntimeContextVariable("sfs_script_CLUSTEREVENTS_PROCESS_START_TIME", "False",
                                                       RuntimeContextVariable.STRING_TYPE))
        additionalVariables.add(RuntimeContextVariable("sfs_script_CLUSTEREVENTS_PROCESSING_THREAD_SHUTTING_DOWN", "False",
                                                       RuntimeContextVariable.STRING_TYPE))
        

    else:
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] ClusterEvents not enabled.  Bypassing setup activities.")

    
    #else:
    #    
    #    if clusterEvents_enabled =="true":        
    #        ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] ClusterEvents is enabled but PauseResume is not enabled.  This configuration is not supported.")
    #        raise Exception("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] ClusterEvents is enabled but PauseResume is not enabled.  This configuration is not supported.")


    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting doInit()")

def configureContainer(velocityContext):
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning configureContainer()")
    callSubscript("configureContainer", velocityContext)
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Calling Enabler's configureContainer()")
    callEnabler("configureContainer", param1=velocityContext)
	#proxy.configureContainer(velocityContext)
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting configureContainer()")
    
def doStart():
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning doStart()")
    callSubscript("doStart")
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Calling Enabler's doStart()")

    if rcvTrue('sfs_CLUSTEREVENTS_ENABLED'):

        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] ClusterEvents enabled.  Will pefore start activities.")
        
        clusterEvents_Addnode()    
        setProcessStartTime()
    
        clusterEventsThread = proxy.getContainer().getRuntimeContext().getVariable('sfs_script_CLUSTEREVENTS_PROCESSING_THREAD').getValue()
        clusterEventsThread.start()

    else:
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] ClusterEvents not enabled.  Bypassing start activities.")

    pauseresume_blockingprocess = proxy.getContainer().getRuntimeContext().getVariable('sfs_script_PAUSERESUME_BLOCKING_PROCESS').getValue()    
    if pauseresume_blockingprocess == "true":
        
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Contents of proxy.container.unixCommand.startup [" +  proxy.container.unixCommand.startupCommand + "].")
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Contents of proxy.container.getStartCommand() [" +  proxy.container.getStartCommand() + "].")

        createEnvironmentScript()

        #proxy.doStart()
        callEnabler("doStart")
    
    else:
        #proxy.doStart()
        callEnabler("doStart")

        createEnvironmentScript()

    
    """
    PauseResume for Blocking Processes
    """
    """ 
    pauseresumeMode = getPauseResumeMode()

    pauseresume_blockingprocess = proxy.getContainer().getRuntimeContext().getVariable('sfs_script_PAUSERESUME_BLOCKING_PROCESS').getValue()    

    if pauseresume_blockingprocess == "true":
        
        print "Current command is:" 
        #print proxy.container.unixCommand.startupCommand
        print proxy.container.getStartCommand()

        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Blocking Process has stopped. Entering PauseResume logic in doStart().")
        while True:        
            if (pauseresumeMode == "shutdown"):
                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Shutdown Requested on blocking process.  Will exit PauseResume logic in doStart().")
                break
            elif (pauseresumeMode == "run"):
                ## Call Resume Commannd  
                ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] sfs_script_PAUSERESUME_MODE set to 'run'.  Will call module 'sfs_pauseresume_restart.py' to resume processing.")
                callSubscript("sfs_pauseresume_restart")
            elif (pauseresumeMode == "pause"):
                ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Blocking process stopped while in Pause mode.  doStart() will sleep for 10 seconds.")
            else:
                ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] sfs_script_PAUSERESUME_MODE set to unknown value [" + pauseresumeMode + 
                                                       "].  Should be either 'run', 'pause' or 'shutdown'). Will ignore and continue with regular shutdown.")
                break
            sleep(10)
    else:
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Non-Blocking Process. Will bypass PauseResume logic in doStart().")
    """
    
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting doStart()")
    
def doInstall(info):
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning doInstall(info)")
    callSubscript("doInstall", info)
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Calling Enabler's doInstall()")
    #proxy.doInstall(info)
    callEnabler("doInstall", param1=info)
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting doInstall(info)")
    
#def activate():
#    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning activate()")
#    callSubscript("activate")
#    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting activate()")
    
#def deactivate():
#    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning deactivate()")
#    callSubscript("deactivate")
#    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting deactivate()")
    
def doUninstall():
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning doUninstall()")
    setPauseResumeMode("shutdown")
    callSubscript("doUninstall")
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Calling Enabler's doUninstall()")
    callEnabler("doUninstall")
    
    if rcvTrue('sfs_CLUSTEREVENTS_ENABLED'):
        
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Performing ClusterEvents Cleanup")
        proxy.getContainer().getRuntimeContext().getVariable('sfs_script_CLUSTEREVENTS_PROCESSING_THREAD_SHUTTING_DOWN').setValue("True")                
        clusterEventsThread = proxy.getContainer().getRuntimeContext().getVariable('sfs_script_CLUSTEREVENTS_PROCESSING_THREAD').getValue()
        if clusterEventsThread.isAlive():
            clusterEventsThread.join()
        else:
            ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] ClusterEvents thread terminated unexpectedly prior to cleanup.  Will continue with ClusterEvents cleanup.")

        restartAction_DeleteLock()    
        clusterEvents_Removenode()
    else:
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] ClusterEvents not enabled.  Bypassing Cleanup")   
    
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting doUninstall()")
    
def doShutDown():
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning doShutDown()")
    callSubscript("doShutDown")
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Calling Enabler's doShutDown()")
    callEnabler("doShutDown")
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting doShutDown()")
    
#TODO - Verify this function has a purpose.  Was intially included in Duetsche Bahn pfsave on 12/19/2014
def copyContainerEnvironment():
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning copyContainerEnvironment()")
    callSubscript("copyContainerEnvironment")
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Calling Enabler's copyContainerEnvironment()")
    callEnabler("copyContainerEnvironment")
    #proxy.copyContainerEnvironment()
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting copyContainerEnvironment()")

def cleanupContainer():
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning cleanupContainer()")
    callSubscript("cleanupContainer")
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Calling Enabler's cleanupContainer()")
    callEnabler("cleanupContainer")
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting cleanupContainer()")
    
def cleanupContainer():
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning cleanupContainer()")

    coldstartEnabled = proxy.getContainer().getRuntimeContext().getVariable('sfs_COLDSTART_ENABLED').getValue()

    if (coldstartEnabled == "true"):
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] sfs_COLDSTART_ENABLED set to true. Will bypass working directory cleanup.")        
    else:
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] sfs_COLDSTART_ENABLED not set to true. Calling enabler prepareWorkDirectory().")

        callEnabler("cleanupContainer")
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Calling Enabler's cleanupContainer()")
        proxy.cleanupContainer()
    

    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting cleanupContainer()")

def hasContainerStarted():
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning hasContainerStarted()")

    ret_val = callSubscript("hasContainerStarted")
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Sub-script hasContainerStarted() function(s) have returned [" + str(ret_val) + "].")
    if (ret_val == True) or (ret_val == None):

        hascontainerstarted_enabled = proxy.getContainer().getRuntimeContext().getVariable('sfs_ENABLER_HASCONTAINERSTARTED_ENABLED').getValue()
    
        if (hascontainerstarted_enabled == "true"):
        
            ret_val = callEnabler("hasContainerStarted")
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Enabler's hasContainerStarted() has returned [" + str(ret_val) + "]")
        else:
            ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Enabler's hasContainerStarted() has been disabled. Assuming response of 'True' and continuing.")
            ret_val = True
            
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting hasContainerStarted()")
    return ret_val

def hasComponentStarted():
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning hasComponentStarted()")

    ret_val = callSubscript("hasComponentStarted")
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Sub-script hasComponentStarted() function(s) have returned [" + str(ret_val) + "].")
    if (ret_val == True) or (ret_val == None):

        hascomponentstarted_enabled = proxy.getContainer().getRuntimeContext().getVariable('sfs_ENABLER_HASCOMPONENTSTARTED_ENABLED').getValue()
    
        if (hascomponentstarted_enabled == "true"):

            ret_val = callEnabler("hasComponentStarted")    
            ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Enabler's hasComponentStarted() has returned [" + str(ret_val) + "]")
        else:
            ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Enabler's hasComponentStarted() has been disabled. Assuming response of 'True' and continuing.")
            ret_val = True

    """
    Cold Start
    """
    """ backup work directory after successful start (in case of failure during next startup of component)"""
    if (ret_val == True):        

        coldstartEnabled = proxy.getContainer().getRuntimeContext().getVariable('sfs_COLDSTART_ENABLED').getValue()
    
        if (coldstartEnabled == "true"):
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] sfs_COLDSTART_ENABLED set to true. Will create backup in case of failure during next startup.")
    
            enginedir = proxy.getContainer().getRuntimeContext().getVariable('ENGINE_WORK_DIR').getValue()
            
            enginedir_bkup = enginedir + "_coldstart_bkup"      
            enginedir_bkup_tmp = enginedir_bkup + "_tmp"      
    
            try:
                if os.path.exists(enginedir_bkup_tmp):
                    shutil.rmtree(enginedir_bkup_tmp)
                          
                shutil.copytree(enginedir, enginedir_bkup_tmp)

                if os.path.exists(enginedir_bkup):
                    shutil.rmtree(enginedir_bkup)
    
                shutil.move(enginedir_bkup_tmp, enginedir_bkup)
                
            except:
                ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Error occurred while attempting to backup engine directory for potential cold start.  " + 
                                                        "If this problem can not be fixed quickly you may wish to disable coldstarts by setting sfs_COLDSTART_ENABLED to false.")
                raise

    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting hasComponentStarted()")
    
    sleep(10) # DEBUG: slow things down to debug PauseResuem logic 
    return ret_val


def getPauseResumeModeRequested():
    
    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning getPauseResumeModeRequested()")

    pauseresumeModeRequested = "[value not set]"

    pauseResumeModeRequested_file = getPauseResumeModeRequested_file()
        
    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Retrieving PauseResume Mode Requested()")
    for line in open(pauseResumeModeRequested_file):
        ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Processing file line: [" + line.rstrip() + "]")
        #if (not line.startswith("#")) and (not line.isspace()):
        pauseresumeModeRequested = line.rstrip()
        ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] pauseresumeModeRequested is : [" + pauseresumeModeRequested + "]")
        break
    
    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting getPauseResumeMode()")

    return pauseresumeModeRequested

def setPauseResumeModeRequested(mode):
    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning setPauseResumeModeRequested()")

    pauseResumeModeRequested_file = getPauseResumeModeRequested_file()
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Setting PauseResume Requested Mode in [" + str(pauseResumeModeRequested_file) + "].")
    
    text_file = open(pauseResumeModeRequested_file, "w")
    text_file.write(mode)
    text_file.close()

def getPauseResumeModeRequested_file():

    containerdir = proxy.getContainer().getRuntimeContext().getVariable('CONTAINER_WORK_DIR').getValue()
    return os.path.join(containerdir, "pauseresume_mode_requested")

def setPauseResumeMode(mode):
    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning setPauseResumeMode()")

    pauseResumeMode_file = getPauseResumeMode_file()
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Setting PauseResume Mode in [" + str(pauseResumeMode_file) + "].")
    
    text_file = open(pauseResumeMode_file, "w")
    text_file.write(mode)
    text_file.close()

    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting setPauseResumeMode()")

def getPauseResumeMode():

    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning getPauseResumeMode()")
    
    pauseresumeMode = "[value not set]"

    pauseResumeMode_file = getPauseResumeMode_file()
        
    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Retrieving PauseResume Mode")
    for line in open(pauseResumeMode_file):
        ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Processing file line: [" + line.rstrip() + "]")
        #if (not line.startswith("#")) and (not line.isspace()):
        pauseresumeMode = line.rstrip()
        ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] pauseresumeMode is : [" + pauseresumeMode + "]")
        break
    
    return pauseresumeMode

    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting getPauseResumeMode()")


def getPauseResumeMode_file():

    containerdir = proxy.getContainer().getRuntimeContext().getVariable('CONTAINER_WORK_DIR').getValue()
    return os.path.join(containerdir, ".sfs_pauseresume_mode")

def isContainerRunning():
    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning isContainerRunning()")

    pauseresumeMode = getPauseResumeMode()

    if (pauseresumeMode == "run") or (pauseresumeMode == "shutdown"):
        
        ret_val = callSubscript("isContainerRunning", silent=True)

        if (ret_val == True) or (ret_val == None):

            iscontainerrunning_enabled = proxy.getContainer().getRuntimeContext().getVariable('sfs_ENABLER_ISCONTAINERRUNNING_ENABLED').getValue()

            if (iscontainerrunning_enabled == "true"):
            
                if (proxy.getContainer().getGridLibName().startswith("TIBCO_BusinessEvents_container")):
                    ret_val = True
                else:
                    ret_val = callEnabler("isContainerRunning", silent=True)
                    
                if not (ret_val == True):
                    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Enabler's isContainerRunning() has returned [" + str(ret_val) + "]")
            else:
                #ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Enabler's isContainerRunning() has been disabled. Assuming response of 'True' and continuing.")
                ret_val = True

        else:
            ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] A subscript's isContainerRunning() function has returned [" + str(ret_val) + "]")

    elif (pauseresumeMode == "pause"):

        ret_val = True
    else:
        ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] PauseResume Mode set to unknown value [" + pauseresumeMode + 
                                               "].  Should be either 'run', 'pause' or 'shutdown'). Will ignore and continue.")
        
    
    """
    ClusterEvents
    """
    if rcvTrue('sfs_CLUSTEREVENTS_ENABLED'):
            
        clusterEventsThread = proxy.getContainer().getRuntimeContext().getVariable('sfs_script_CLUSTEREVENTS_PROCESSING_THREAD').getValue()
        if not clusterEventsThread.isAlive():
            ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] ClusterEventsThread are enabled but thread is no longer running.")
            
            clusterEventsThread_restarts = proxy.getContainer().getRuntimeContext().getVariable('sfs_script_CLUSTEREVENTS_PROCESSING_THREAD_RESTARTS').getValue()
            clusterEventsThread_restarts_int = int(clusterEventsThread_restarts)
            
            max_restarts = 3
            clusterEventsThread_restarts_int = clusterEventsThread_restarts_int + 1
            while True:
                if clusterEventsThread_restarts_int > max_restarts:
                    ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] " + 
                                                           "Maximum number of allowed restarts [" + str(max_restarts) + "] on ClusterEventsThread has been reached.")
                    break
                else:
                    ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] " + 
                                                           "Initiation Restart attempt number [" + str(clusterEventsThread_restarts_int) + "] on ClusterEventsThread.")
                    
                    clusterEventsThread = createClusterEventThread()
                    proxy.getContainer().getRuntimeContext().getVariable('sfs_script_CLUSTEREVENTS_PROCESSING_THREAD').setValue(clusterEventsThread)
                    clusterEventsThread.start()
                    
                    sleep(10)
                    if clusterEventsThread.isAlive():
                        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] " + 
                                                             "ClusterEventsThread has been sucessfully restarted.")
                        clusterEventsThread_restarts = proxy.getContainer().getRuntimeContext().getVariable('sfs_script_CLUSTEREVENTS_PROCESSING_THREAD_RESTARTS').setValue(str(clusterEventsThread_restarts_int))
                        break 
                    
            ret_val = False

    if not (ret_val == True):
        ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] isContainerRunning() return value is [" + ret_val + "]")
    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting isContainerRunning()")
    return ret_val

def isComponentRunning():
    
    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning isComponentRunning()")

    pauseresumeMode = getPauseResumeMode()
    pauseresumeModeRequested = getPauseResumeModeRequested()    
    
    if (pauseresumeModeRequested == "run"):
        if (pauseresumeMode == "run"):
            #ret_val = proxy.isComponentRunning()
            pass
        elif (pauseresumeMode == "pause"):
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] Restarting paused component. Calling restart sub-routines.")
            callSubscript("sfs_pauseresume_restart")
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] Restart sub-routines completed.")
                
            pauseresume_blockingprocess = proxy.getContainer().getRuntimeContext().getVariable('sfs_script_PAUSERESUME_BLOCKING_PROCESS').getValue()    
        
            if pauseresume_blockingprocess == "true":
                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] Blocking Process. Will set signal to indicate doStart() should restart the process.")

                #resetStatisticProviders()
                setPauseResumeMode("run")
                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] PauseResuem Mode set to 'run' for blocking process.")
                
            while True:
                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] Will wait for process to restart before continuing.")
                componentStarted = hasComponentStarted()
                if componentStarted:
                    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] Process has been restarted.  Resuming isCompoentRunning().")
                    break
                else:
                    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] Process has not restarted yet.  Sleeping 10 seconds.")                        
                    sleep(10)

            if pauseresume_blockingprocess != "true":
                #resetStatisticProviders()
                setPauseResumeMode("run")
                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] PauseResuem Mode set to 'run' for non-blocking process.")

        else:
            ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] PauseResume Mode set to unknown value [" + pauseresumeMode + 
                                                   "].  Should be either 'run', 'pause' or 'shutdown'). Will ignore and continue.")
        
        ret_val = callSubscript("isComponentRunning", silent=True)

        if (ret_val == True) or (ret_val == None):


            iscomponentrunning_enabled = proxy.getContainer().getRuntimeContext().getVariable('sfs_ENABLER_ISCOMPONENTRUNNING_ENABLED').getValue()

            if (iscomponentrunning_enabled == "true"):
            
                ret_val = callEnabler("isComponentRunning", silent=True)
                if not (ret_val == True):
                    ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] Enabler isComponentRunning() has returned [" + str(ret_val) + "]")
            else:
                #ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] Enabler's isComponentRunning() has been disabled. Assuming response of 'True' and continuing.")
                ret_val = True
            
        else:
            ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] A subscript's isComponentRunning() function has returned [" + str(ret_val) + "]")

    elif (pauseresumeModeRequested == "pause"):
        
        if (pauseresumeMode == "pause"):
            ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] Component is currently in pause mode.  Enabler is not actively monitoring this process.")

        elif (pauseresumeMode == "run"):
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] Pausing component")
            
            callSubscript("sfs_pauseresume_pause")
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] Restart sub-routines completed.")

            pauseresumeMode_var = proxy.getContainer().getRuntimeContext().getVariable('PauseResume Mode')
            setPauseResumeMode("pause")
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] PauseResume Mode set to 'pause'.")
            
            #removeStatisticProviders()

        else:
            ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] PauseResume Mode set to unknown value [" + pauseresumeMode + 
                                                   "].  Should be either 'run' or 'pause'. Will ignore and continue.")
        ret_val = True
    
    else:
        ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [isComponentRunning()] PauseResume Requested Mode contains unknown value [" + pauseresumeModeRequested + 
                                               "].  Should be either 'run' or 'pause'. Will ignore and continue.")
        
    if not (ret_val == True):
        ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] isComponentRunning() return value is [" + ret_val + "]")
    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting isComponentRunning()")
    return ret_val

def getStatistic(name):
    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning getStatistic(name)")
    
    #    #TODO Add suport for sub-moudules
    statistic = None

    pauseresumeMode = getPauseResumeMode()

    if (pauseresumeMode == "pause"):
        
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]    [getStatistic(name)] Statistic [" + name + "] requested while in Pause Mode.  Will return 0.")
        statistic = DefaultStatistic()
        statistic.setName(name)
        statistic.value(0)
        proxy.getContainer().getRuntimeContext().getVariable('sfs_script_PAUSERESUME_STATISTICS_ON').setValue("false")
    else:
        #statisticsEnabled = proxy.getContainer().getRuntimeContext().getVariable('sfs_script_PAUSERESUME_STATISTICS_ON').getValue()

        try:

            statistic = callEnabler("getStatistic", param1=name, silent=True)
            ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting getStatistic(name)")
            return statistic
        except Error, e:    
            
            pauseresumeMode = getPauseResumeMode()
    
            ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Unexpected exception returned from Enabler's isContainerRunning() function.  " +
                                                   "Statistic name requested was [" + str(name) + "]")
            raise e
    ContainerUtils.getLogger(proxy).finer("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting getStatistic(name)")
    return statistic

"""
Utility Functions
"""

def runCommand(commandline, stdin=None, stdout=None, expectedReturnCodes=None, suppressOutput=None, shell=None):

    if (expectedReturnCodes == None): expectedReturnCodes = [0]
    if (suppressOutput == None): suppressOutput = False
    if (shell == None): shell = False
    stderr = None
    if (suppressOutput):
        stdout=PIPE
        stderr=PIPE
    else: 
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Running command [" + commandline + "]")
            
    if shell:
        args = commandline
    else:
        args = shlex.split(commandline)

    os.unsetenv("LD_LIBRARY_PATH")
    os.unsetenv("LD_PRELOAD")

    if stdin == None:
        p = Popen(args, stdout=stdout, stdin=None, stderr=stderr, shell=shell)
        output = p.communicate()        
    else:
        p = Popen(args, stdout=stdout, stdin=PIPE, stderr=stderr, shell=shell)
        output = p.communicate(input=stdin)
    
    outputlist = [p.returncode]

    for item in output:
        outputlist.append(item)

    if (outputlist[0] in expectedReturnCodes ):
        if not (suppressOutput):
            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Command return code was [" + str(outputlist[0]) + "]")
            printStdoutPipe(stdout, outputlist)
    else:
        
        ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Return code " + str(outputlist[0]) + 
                                               " was not in list of expected return codes" + str(expectedReturnCodes))
        if (suppressOutput):
            ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Command was [" + commandline + "]")

        printStdoutPipe(stdout, outputlist)

    ContainerUtils.getLogger(proxy).fine("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] exiting runCommand(). Returning outputlist:" + (str(outputlist)))
    return outputlist

def printStdoutPipe(stdout, outputlist):

    if (stdout == PIPE):
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Command STDOUT:")
        print outputlist[1]

def dos2unix(f_path):
    
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning dos2unix()")

    if os.path.isfile(os.path.join(f_path)):

        f_tmp_path = f_path + ".tmp"
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Copying [" + str(f_path) + "] to [" + str(f_tmp_path) + "].")

        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]         Creating file \""+f_tmp_path+"\"...")
        tmpfile = open(f_tmp_path,'w')
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">]         Writing lines to [" + f_tmp_path + "] while replacing replacing ^M (carriage return) with \\n (newline) characters...")
        srcfile = open(f_path,'r')
        for line in srcfile:
            """ 
            Replace any Carriage Returns with Line Feeds
            """
            line = line.rstrip()
            tmpfile.write(line + '\n')
        tmpfile.close()
        srcfile.close()
        
        os.rename(f_path, f_path + '.bak')
        os.rename(f_tmp_path, f_path)

        call(["chmod", "-fR", "770", f_path])        
        
    else:
        ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] File not found [" + str(f_path) + "]")

    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting dos2unix()")
    
def getVar_doInit(varname, additionalVariables):
    
    value = None
    rcv = proxy.getContainer().getRuntimeContext().getVariable(varname)
    if (rcv == None):
        for newrcv in additionalVariables:
            if varname == newrcv.getName():
                value = newrcv.getValue()
                break
    else:
        value =  rcv.getValue()
    return value

def createEnvironmentScript():
    
    containerdir = proxy.getContainer().getRuntimeContext().getVariable('CONTAINER_WORK_DIR').getValue()

    if ContainerUtils.isWindows():
        environmentFilename = os.path.join(containerdir, "sfs_component_script_ENVIRONMENT.bat")
    else:
        environmentFilename = os.path.join(containerdir, "sfs_component_script_ENVIRONMENT.sh")
        
    environmentFile = open(environmentFilename, 'w')
    print>>environmentFile, "###################################################"
    print>>environmentFile, "# Generated by sfs_component_script.py"
    print>>environmentFile, "#     " + str(datetime.datetime.now())
    print>>environmentFile, "#"
    print>>environmentFile, "# This file sets all the ENVIRONMENT type runtimecontext"
    print>>environmentFile, "# variables defined by this enabler."
    print>>environmentFile, "#"
    print>>environmentFile, "###################################################"

    runtimeContext = proxy.getContainer().getRuntimeContext()
    for i in range (0, (runtimeContext.getVariableCount() - 1)):
          variable = runtimeContext.getVariable(i)
          ContainerUtils.getLogger(proxy).fine("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] " + str(variable.getName()) + " has type [" + str(variable.getTypeInt()) + "]")
          if  (variable.getTypeInt() == RuntimeContextVariable.ENVIRONMENT_TYPE):
              print>>environmentFile, variable.getName() + "=" + str(variable.getValue())   
    
    environmentFile.close()

##############################################################

def createClusterEventThread():
    """ Create New Thread object for processing requests and save it for later. """
    logger = ContainerUtils.getLogger(proxy)
    slstdout = StreamToLogger(logger,"STDOUT")
    slstderr = StreamToLogger(logger,"STDERR")
    clusterEventsThread = ClusterEventsThread(slstdout, slstderr)
    clusterEventsThread.setName("ClusterEventsThread")
    return clusterEventsThread

class StreamToLogger(object):
    """
    Fake file-like stream object that redirects writes to a logger instance.
    """
    def __init__(self, logger, streamtype):
        self.logger = logger
        self.streamtype = streamtype
 
    def write(self, buf):
        for line in buf.rstrip().splitlines():
            if (self.streamtype == "STDERR"):
                self.logger.warning(line.rstrip())
            else:
                self.logger.info(line.rstrip())

class ClusterEventsThread (threading.Thread):
    def __init__(self, slstdout, slstderr):
        threading.Thread.__init__(self)
        self.slstdout = slstdout
        self.slstderr = slstderr
        
    def run(self):
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Starting processClusterEvents.run()")

        """
        Redirect stdout and stderr for this thread. 
        """
        sys.stdout = self.slstdout
        sys.stderr = self.slstderr
 
        try:
            processClusterEvents()
        except:
            ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Unexpected error from processing thread")
            traceback.print_exc()

        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting processClusterEvents.run()")

def processClusterEvents():

    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning processClusterEvents()")

    while not rcvTrue('sfs_script_CLUSTEREVENTS_PROCESSING_THREAD_SHUTTING_DOWN'):
        pauseresumeMode = getPauseResumeMode()
        pauseresumeModeRequested = getPauseResumeModeRequested()
    
        if (pauseresumeMode == "run") and (pauseresumeModeRequested == "run"):
        
            #if isCurrentComponentShuttingDown() == False:
                
                newestEvent = getMostRecentModificationTime(getClusterEvents_Events_dir())
                processStartTime = getProcessStartTime()
                
                if newestEvent > processStartTime:
                    
                    ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] New Cluster Event found.")
                    while True:
                        
                        clusterevents_dir = getClusterEvents_dir()
                        lock_filematch = "*.restart.lck"

                        #Determine if another Cluster Member has already created a lock.
                        if numFileOfMatches(clusterevents_dir, lock_filematch) > 0:

                            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Restart lock already exists. Will wait 10 seconds.")
                            #Delete old locks after a user-defined expiration age.
                            expiration_age = proxy.getContainer().getRuntimeContext().getVariable('sfs_CLUSTEREVENTS_LOCK_EXPIRATION_AGE').getValue()
                            deleteExpiredFiles(expiration_age, getClusterEvents_dir(), lock_filematch)

                            #Another Cluster Member has already created a lock.  Therefore, wait.
                            sleep(10)
                        else:
                            ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Creating restart lock.")
                            restartAction_CreateLock()

                            # Wait to and see if multiple components wrote locks at the same time
                            sleep(5)
                            if (numFileOfMatches(clusterevents_dir, lock_filematch) > 1):
                                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Another cluster member generated a restart lock at the same time.  Will delete lock and wait.")
                                #Another Cluster Member has written a lock, Therefor, abort and wait
                                restartAction_DeleteLock()                            
                                wait(5 + randint(0,10))
                            else:
                    
                                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Starting restart sequence. Will pause component.")
                                #request pause
                                setPauseResumeModeRequested("pause")
                                
                                #wait for pauseresume mode change
                                while True:
                                    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Proceeding with restart sequence.")
                                    if getPauseResumeMode() == "pause":
                                        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Pause completed.  Will continiue with restart sequence.")
                                        break
                                    if rcvTrue('sfs_script_CLUSTEREVENTS_PROCESSING_THREAD_SHUTTING_DOWN'):
                                        break
                                    sleep(5)
                                
                                setProcessStartTime()
                
                                ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Continuing with restart sequence. Will resume component.")
                                setPauseResumeModeRequested("run")
                                
                                #wait for pauseresume mode change
                                while True:
                                    if getPauseResumeMode() == "run":
                                        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Compoent resumed.  Restart sequence complete.")
                                        restartAction_DeleteLock()
                                        break
                                    if rcvTrue('sfs_script_CLUSTEREVENTS_PROCESSING_THREAD_SHUTTING_DOWN'):
                                        break
                                    sleep(5)
                                
                                break
                        if rcvTrue('sfs_script_CLUSTEREVENTS_PROCESSING_THREAD_SHUTTING_DOWN'):
                            break
        if rcvTrue('sfs_script_CLUSTEREVENTS_PROCESSING_THREAD_SHUTTING_DOWN'):
            break

        sleep(10)
        
    ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting processClusterEvents()")

def getProcessStartTime():
    
    return proxy.getContainer().getRuntimeContext().getVariable('sfs_script_CLUSTEREVENTS_PROCESS_START_TIME').getValue()

def setProcessStartTime():

    return proxy.getContainer().getRuntimeContext().getVariable('sfs_script_CLUSTEREVENTS_PROCESS_START_TIME').setValue(time())
    
def getClusterEvents_dir():
    return proxy.getContainer().getRuntimeContext().getVariable('sfs_CLUSTEREVENTS_DIR').getValue()
def getClusterEvents_Events_dir():
    return os.path.join(getClusterEvents_dir(), "cluster" )
def getClusterEvents_Addnode_filename():
    return os.path.join(getClusterEvents_Events_dir(), hostip + ".added" )
def getclusterEvents_Removenode_filename():
    return os.path.join(getClusterEvents_Events_dir(), hostip + ".removed" )

def clusterEvents_Addnode():
    
    addnode_event_filename = getClusterEvents_Addnode_filename()
    addnode_event_file = open(addnode_event_filename, 'w')
    addnode_event_file.write(getCurComponentName())  
    addnode_event_file.close()
    
def clusterEvents_Removenode():
    
    removenode_event_filename = getclusterEvents_Removenode_filename()
    removenode_event_file = open(removenode_event_filename, 'w')
    removenode_event_file.write(getCurComponentName())  
    removenode_event_file.close()
    
def getMostRecentModificationTime(directory):
    """
    Returns the modified time of the most recent modified file.  Return value format is seconds since the epoch. 
    """
    import os
    import glob
    import time
    newest = max(glob.iglob(os.path.join(directory, '*')), key=os.path.getmtime)
    newest_mtime = os.path.getmtime(newest)
    
    ContainerUtils.getLogger(proxy).fine("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Most recent modification in [" + directory + "] is file [" + newest + "] at [" + time.ctime(newest_mtime) + "].")
        
    return newest_mtime

def getRestartAction_Lock_filename():
    return os.path.join(getClusterEvents_dir(), hostip + ".restart.lck" )

def restartAction_CreateLock():
    """
    Create a lock file so other cluster memebers do not try to restart at the same time
    """
    restartaction_lock_filename = getRestartAction_Lock_filename()
    restartaction_lock_file = open(restartaction_lock_filename, 'w')
    restartaction_lock_file.write(getCurComponentName())
    restartaction_lock_file.close()

def restartAction_DeleteLock():
    """
    Remove lock file so that another cluster memeber can restart is necessary
    """
    lock_filename = getRestartAction_Lock_filename()
    if os.path.isfile(lock_filename):
        os.remove(lock_filename)
    
def numFileOfMatches(directory, match):
    """
    Determine number of lock files in directory.
    """
    locks = glob.glob(os.path.join(directory, match))

    num_of_locks = len(locks)
    if num_of_locks > 0:    
        ContainerUtils.getLogger(proxy).info("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Lock files found [" + str(locks) + "].")
    
    return len(glob.glob(os.path.join(directory, "*.restart.lck")))
        
def deleteExpiredFiles(expiration_age, directory, match):
    """
    Delete files in [directory] that match [match] and are older than the number of seconds in [expiration_age].
    """
    filematches = glob.glob(os.path.join(directory, match))

    for filename in filematches:
        mtime = os.path.getmtime(filename)
        if (mtime + int(expiration_age) < time()):
            ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] [" + filename + "] has passed its expration time but is still on disk.  Will delete it and continue.")
            os.remove(filename)

def getCurComponentName():
    return proxy.getContainer().getCurrentDomain().getName()


def rcvTrue(rcv):
    
    ContainerUtils.getLogger(proxy).fine("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] checking runtimecontext variable [" + str(rcv) + "]")
    
    rcvvalue = proxy.getContainer().getRuntimeContext().getVariable(rcv).getValue()
    ContainerUtils.getLogger(proxy).finest("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] value is [" + str(rcvvalue) + "].")
    if (str(rcvvalue).lower() in ("yes", "y", "true",  "t", "1")): 
        result = True
    elif (str(rcvvalue).lower() in ("no",  "n", "false", "f", "0")): 
        result = False
    else:
        ContainerUtils.getLogger(proxy).finest("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] ERROR!!! trying to conver RuntimeContextVariable [" + rcv + 
                                               "] to boolean.  Value [" + str(rcvvalue) + 
                                               "] is invalid for conversion.  Valid values for true are [" + truevalues +
                                               "].  Valid values for false [" + falsevalues + "].")
        raise Exception("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] ERROR!!! trying to conver RuntimeContextVariable [" + rcv + "] to boolean.  Invalid value [" + str(rcvvalue) + "].")
    ContainerUtils.getLogger(proxy).fine("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting Checking enabler flag. Result is [" + str(result) + "]")
    return result
    

######################################################################################
# Following code not currently used

import traceback
def isCurrentComponentShuttingDown():

    ContainerUtils.getLogger(proxy).fine("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Beginning isCurrentComponentShuttingDown().")
    shutDownDetected = False
    try:

        admin = AdminManager.getStackAdmin()
        #throws Exception -IllegalStateException - If called on an Engine but the Engine is not yet allocated to run a Component
        componentAllocationMap  = admin.getComponentAllocationMap()
        #throws Exception - Exception - if effective policy is unavailable

        curcomponent = getCurComponentName()
        curnodeexpectedengines = getComponentExpectedEngineCount(curcomponent, componentAllocationMap)
        # Checking the current node engine count to be sure
        if (curnodeexpectedengines == 0):
            
            ContainerUtils.getLogger(proxy).warning("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Broker indicates this Component is shutting down (Expected Engine Count for this Component is 0).")
            shutDownDetected = True
                
    except:
       ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Unexpected error trying to get to determine the Expected Engine Count for this Component.  " +
                                              "Will ignore and continue.")
       traceback.print_exc()
                
    ContainerUtils.getLogger(proxy).fine("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Exiting isCurrentComponentShuttingDown().  Return is [" + str(shutDownDetected) + "]")
    return shutDownDetected 

def getComponentExpectedEngineCount(componentName, componentAllocationMap):

    try:
        componentAllocationEntryInfo = componentAllocationMap.getAllocationEntry(componentName)

        if (componentAllocationEntryInfo == None):
            expectedEngineCount = 0
        else:
            expectedEngineCount = int(componentAllocationEntryInfo.getExpectedEngineCount()) 
        
        ContainerUtils.getLogger(proxy).fine("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Expected Engine Count for component [ " + str(componentName) + " ] is [" + str(expectedEngineCount) + "]")
        
    except:
       ContainerUtils.getLogger(proxy).severe("[sfs_component_script.py <" + str(threading.currentThread().getName()) + ">] Unexpected error trying to get Expected Engine count for this component [" + str(componentName) + 
                                              "].  Will assume it is still in a running mode.")
       traceback.print_exc()
       
       expectedEngineCount = -1

    return expectedEngineCount
