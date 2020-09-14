/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.smartDrtPricing.prepare;

import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEvent;

/**
 * @author : zmeng
 */
public class DrtTripInfo {
    private ActivityEndEvent realActivityEndEvent;
    private DrtRequestSubmittedEvent drtRequestSubmittedEvent;
    private PersonArrivalEvent drtArrivalEvent;
    private PersonArrivalEvent lastArrivalEvent;
    private boolean isDrtTrip = false;
    private boolean findDrtArrivalEvent = false;

    public DrtTripInfo(ActivityEndEvent realActivityEndEvent) {
        this.realActivityEndEvent = realActivityEndEvent;
    }

    public boolean needLastArrivalEvent() {
        return this.isDrtTrip() && this.findDrtArrivalEvent;
    }

    public ActivityEndEvent getRealActivityEndEvent() {
        return realActivityEndEvent;
    }

    public double getUnsharedRideDistance(){
        return this.getDrtRequestSubmittedEvent().getUnsharedRideDistance();
    }

    final public DrtRequestSubmittedEvent getDrtRequestSubmittedEvent() {
        return drtRequestSubmittedEvent;
    }

    final public void setDrtRequestSubmittedEvent(DrtRequestSubmittedEvent drtRequestSubmittedEvent) {
        this.drtRequestSubmittedEvent = drtRequestSubmittedEvent;
    }

    public PersonArrivalEvent getLastArrivalEvent() {
        return lastArrivalEvent;
    }

    public void setLastArrivalEvent(PersonArrivalEvent lastArrivalEvent) {
        this.lastArrivalEvent = lastArrivalEvent;
    }

    public boolean isDrtTrip() {
        return isDrtTrip;
    }

    public void setDrtTrip(boolean drtTrip) {
        isDrtTrip = drtTrip;
    }

    public void setFindDrtArrivalEvent(boolean findDrtArrivalEvent) {
        this.findDrtArrivalEvent = findDrtArrivalEvent;
    }

    public void setDrtArrivalEvent(PersonArrivalEvent drtArrivalEvent) {
        this.drtArrivalEvent = drtArrivalEvent;
    }

    final public double getWalkTime() {
        return (drtRequestSubmittedEvent.getTime() - realActivityEndEvent.getTime())
                + (lastArrivalEvent.getTime() - drtArrivalEvent.getTime());
    }

    final public double getTotalUnsharedTripTime() {
        return this.getWalkTime() + this.drtRequestSubmittedEvent.getUnsharedRideTime();
    }
    public double getRealDrtTotalTripTime(){
        return this.lastArrivalEvent.getTime() - realActivityEndEvent.getTime();
    }

}
