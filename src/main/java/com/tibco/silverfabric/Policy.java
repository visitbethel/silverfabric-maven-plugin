/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
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
