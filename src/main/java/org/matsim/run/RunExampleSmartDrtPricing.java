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

package org.matsim.run;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.smartDrtPricing.SmartDrtFareModule;
import org.matsim.smartDrtPricing.SmartDrtFareConfigGroup;

/**
 * @author : zmeng
 */
public class RunExampleSmartDrtPricing {
    private static final Logger log = Logger.getLogger(RunExampleSmartDrtPricing.class);
    public static void main(String[] args) {

        // config
        Config config = ConfigUtils.createConfig();
        ConfigUtils.addOrGetModule(config, SmartDrtFareConfigGroup.class);
        // scenario
        Scenario scenario = ScenarioUtils.loadScenario(config);
        // controler
        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new SmartDrtFareModule());
        // run
        controler.run();
    }
}
