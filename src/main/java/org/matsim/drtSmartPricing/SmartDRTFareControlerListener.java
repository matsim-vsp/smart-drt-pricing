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

package org.matsim.drtSmartPricing;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

/**
 * @author : zmeng
 */
public class SmartDRTFareControlerListener implements IterationEndsListener {

    @Inject
    private SmartDrtFareConfigGroup smartDrtFareConfigGroup;
    @Inject
    private SmartDRTFareComputation smartDRTFareComputation;
    @Inject
    private EventsManager eventsManager;
    @Inject
    private Scenario scenario;

    @Override
    public void notifyIterationEnds(IterationEndsEvent iterationEndsEvent) {
        smartDRTFareComputation.writeLog();
        if(iterationEndsEvent.getIteration() == scenario.getConfig().controler().getLastIteration()){
            smartDRTFareComputation.writeFile();
        } else if (iterationEndsEvent.getIteration() % smartDrtFareConfigGroup.getWriteFileInterval() == 0) {
            smartDRTFareComputation.writeFile();
        }
    }
}