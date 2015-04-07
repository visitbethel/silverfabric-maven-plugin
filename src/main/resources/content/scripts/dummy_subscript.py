from com.datasynapse.fabric.admin.info import AllocationInfo
from com.datasynapse.fabric.util import GridlibUtils, ContainerUtils
from com.datasynapse.fabric.common import RuntimeContextVariable, ActivationInfo
from subprocess import call, Popen
import os
import inspect

try: proxy
except NameError:    
    globals()['proxy'] = inspect.currentframe().f_back.f_globals['proxy']
    globals()['getVar_doInit'] = inspect.currentframe().f_back.f_globals['getVar_doInit']
else: pass


def prepareWorkDirectory():
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Beginning prepareWorkDirectory()")

    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Exiting prepareWorkDirectory()")

def doInit(additionalVariables):
    print "[dummy_subscript] Beginning doInit()"

    print "[dummy_subscript] Exiting doInit()"
    
def doStart():
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Beginning doStart")
            
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Exiting doStart")
    
def doShutdown():

    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Beginning doShutdown")
    
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Exiting doShutdown")
    
def getContainerRunningConditionPollPeriod():
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Performing getContainerRunningConditionPollPeriod")
    return 10000

def getComponentStartConditionPollPeriod():
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Performing getComponentStartConditionPollPeriod")
    return 10000

def isContainerRunning():

    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Beginning isContainerRunning")
    status = True
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Exiting isContainerRunning with status: " + str(status))
    return status

def isComponentRunning():
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Beginning isComponentRunning")
    status = True
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Exiting isComponentRunning with status: " + str(status))
    return status

def hasContainerStarted():
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Beginning hasContainerStarted")
    status = True
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Exiting hasContainerStarted with status: " + str(status))
    return status
    
def hasComponentStarted():
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Performing hasComponentStarted():")
    return isContainerRunning()
    
def getContainerRunningConditionPollPeriod():
    return 10000

def getContainerRunningConditionErrorMessage():
    return "Dummy Enabler not running on this machine"
    
def doInstall(info):
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Beginning doInstall")
    
    ContainerUtils.getLogger(proxy).info("[dummy_subscript] Exiting doInstall")
        
def doUninstall():
    print "[dummy_subscript] Beginning doUninstall"
    
    print "[dummy_subscript] Exiting doUninstall"
