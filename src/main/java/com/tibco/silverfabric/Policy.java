package com.tibco.silverfabric;

import java.util.List;

/**
 * User: franck
 * Date: 1/31/13
 */
public class Policy {
    private String scheduleName="";
    private List<componentAllocationInfoDetail> componentAllocationInfo;
    private boolean scheduled;

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public List<componentAllocationInfoDetail> getComponentAllocationInfo() {
        return componentAllocationInfo;
    }

    public void setComponentAllocationInfo(List<componentAllocationInfoDetail> componentAllocationInfo) {
        this.componentAllocationInfo = componentAllocationInfo;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }
}
