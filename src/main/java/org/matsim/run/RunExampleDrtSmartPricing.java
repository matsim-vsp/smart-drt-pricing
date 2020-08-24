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

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFareModule;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFaresConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigs;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtModule;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.drtSmartPricing.SmartDRTFareModule;
import org.matsim.drtSmartPricing.SmartDrtFareConfigGroup;

/**
 * @author : zmeng
 */
public class RunExampleDrtSmartPricing {
    private static final Logger log = Logger.getLogger(RunExampleDrtSmartPricing.class);
    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("test/input/scenario/equil/config.xml", new MultiModeDrtConfigGroup(), new DvrpConfigGroup(), new DrtFaresConfigGroup());
        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = ConfigUtils.addOrGetModule(config, SwissRailRaptorConfigGroup.class);
        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(false);
        DrtConfigs.adjustMultiModeDrtConfig(ConfigUtils.addOrGetModule(config,MultiModeDrtConfigGroup.class), config.planCalcScore(), config.plansCalcRoute());

        Scenario scenario = DrtControlerCreator.createScenarioWithDrtRouteFactory(config);
        ScenarioUtils.loadScenario(scenario);


        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new MultiModeDrtModule());
        controler.addOverridingModule(new DvrpModule());
        controler.configureQSimComponents(DvrpQSimComponents.activateAllModes(MultiModeDrtConfigGroup.get(controler.getConfig())));
        controler.addOverridingModule(new DrtFareModule());

        for( Person person : scenario.getPopulation().getPersons().values() ){
            person.getPlans().removeIf( (plan) -> plan!=person.getSelectedPlan() ) ;
        }

        ConfigUtils.addOrGetModule(config, SmartDrtFareConfigGroup.class);
        controler.addOverridingModule(new SmartDRTFareModule());

        controler.run();
    }
}
