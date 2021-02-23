package org.matsim.run;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.speedup.DrtSpeedUpParams;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.run.drt.RunDrtOpenBerlinScenario;
import org.matsim.smartDrtPricing.SmartDrtFareConfigGroup;
import org.matsim.smartDrtPricing.SmartDrtFareModule;

/**
 * @author zmeng
 */
public class RunOpenBerlinScenarioWithSdp {
    private static final Logger log = Logger.getLogger(RunOpenBerlinScenarioWithSdp.class);

    public static void main(String[] args) {
        for (String arg : args) {
            log.info(arg);
        }

        if (args.length == 0) {
            args = new String[] { "scenarios/berlin-v5.5-1pct/input/drt/berlin-drt-v5.5-1pct.config.xml" };
        }

        //add sdp configGroup
        Config config = RunDrtOpenBerlinScenario.prepareConfig(args, new SmartDrtFareConfigGroup());

        for (DrtConfigGroup drtCfg : MultiModeDrtConfigGroup.get(config).getModalElements()) {
            if (drtCfg.getDrtSpeedUpParams().isEmpty()) {
                drtCfg.addParameterSet(new DrtSpeedUpParams());
            }
        }

        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = ConfigUtils.addOrGetModule(config, SwissRailRaptorConfigGroup.class);
        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(false);

        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);
        for (Person person : scenario.getPopulation().getPersons().values()) {
            person.getPlans().removeIf((plan) -> plan != person.getSelectedPlan());
        }

        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);

        //add sdp module
        controler.addOverridingModule(new SmartDrtFareModule());

        controler.run();
    }
}
