/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 *
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.silverfabric;

import java.util.List;

public class ComponentAllocationInfoDetail {
    private String name;
    private String priority;
    private String max;
    private List<AllocationRule> allocationRules;
    private String min;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public List<AllocationRule> getAllocationRules() {
        return allocationRules;
    }

    public void setAllocationRules(List<AllocationRule> allocationRules) {
        this.allocationRules = allocationRules;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }
}
