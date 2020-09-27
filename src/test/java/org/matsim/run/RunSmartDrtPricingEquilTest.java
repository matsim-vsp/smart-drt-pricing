package org.matsim.run;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import org.junit.Rule;
import org.junit.Test;
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
import org.matsim.drtSpeedUp.DrtSpeedUpConfigGroup;
import org.matsim.drtSpeedUp.MultiModeDrtSpeedUpModule;
import org.matsim.smartDrtPricing.SmartDrtFareConfigGroup;
import org.matsim.smartDrtPricing.SmartDrtFareModule;
import org.matsim.testcases.MatsimTestUtils;

/**
 * @author : zmeng
 */
public class RunSmartDrtPricingEquilTest {
    @Rule
    public MatsimTestUtils utils = new MatsimTestUtils();

    @Test
    public final void runTest(){
        Config config = ConfigUtils.loadConfig("test/input/scenario/equil/config.xml", new MultiModeDrtConfigGroup(), new DvrpConfigGroup(), new DrtFaresConfigGroup());
        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = ConfigUtils.addOrGetModule(config, SwissRailRaptorConfigGroup.class);
        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(false);
        DrtConfigs.adjustMultiModeDrtConfig(ConfigUtils.addOrGetModule(config,MultiModeDrtConfigGroup.class), config.planCalcScore(), config.plansCalcRoute());

        Scenario scenario = DrtControlerCreator.createScenarioWithDrtRouteFactory(config);
        ScenarioUtils.loadScenario(scenario);

        config.controler().setRunId("test0");
        config.controler().setOutputDirectory(utils.getOutputDirectory());
        config.controler().setWriteEventsInterval(1);


        org.matsim.core.controler.Controler controler = new Controler(scenario);
        controler.addOverridingModule(new MultiModeDrtModule());
        controler.addOverridingModule(new DvrpModule());
        controler.configureQSimComponents(DvrpQSimComponents.activateAllModes(MultiModeDrtConfigGroup.get(controler.getConfig())));
        controler.addOverridingModule(new DrtFareModule());

        for( Person person : scenario.getPopulation().getPersons().values() ){
            person.getPlans().removeIf( (plan) -> plan!=person.getSelectedPlan() ) ;
        }

        ConfigUtils.addOrGetModule(config, SmartDrtFareConfigGroup.class);
        controler.addOverridingModule(new SmartDrtFareModule());

        controler.run();
    }

    @Test
    public final void drtSpeedUpTest(){
        Config config = ConfigUtils.loadConfig("test/input/scenario/equil/config.xml", new MultiModeDrtConfigGroup(), new DvrpConfigGroup(), new DrtFaresConfigGroup());
        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = ConfigUtils.addOrGetModule(config, SwissRailRaptorConfigGroup.class);
        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(false);
        DrtConfigs.adjustMultiModeDrtConfig(ConfigUtils.addOrGetModule(config,MultiModeDrtConfigGroup.class), config.planCalcScore(), config.plansCalcRoute());

        config.controler().setRunId("drtSpeedUpTest");
        config.controler().setOutputDirectory(utils.getOutputDirectory());
        config.controler().setWriteEventsInterval(1);

        Scenario scenario = DrtControlerCreator.createScenarioWithDrtRouteFactory(config);
        ScenarioUtils.loadScenario(scenario);


        org.matsim.core.controler.Controler controler = new Controler(scenario);
        controler.addOverridingModule(new MultiModeDrtModule());
        controler.addOverridingModule(new DvrpModule());
        controler.configureQSimComponents(DvrpQSimComponents.activateAllModes(MultiModeDrtConfigGroup.get(controler.getConfig())));
        controler.addOverridingModule(new DrtFareModule());

        for( Person person : scenario.getPopulation().getPersons().values() ){
            person.getPlans().removeIf( (plan) -> plan!=person.getSelectedPlan() ) ;
        }

        ConfigUtils.addOrGetModule(config, SmartDrtFareConfigGroup.class);
        controler.addOverridingModule(new SmartDrtFareModule());
        SmartDrtFareConfigGroup smartDrtFareConfigGroup = ConfigUtils.addOrGetModule(config,SmartDrtFareConfigGroup.class);

        ConfigUtils.addOrGetModule(config, DrtSpeedUpConfigGroup.class);
        controler.addOverridingModule(new MultiModeDrtSpeedUpModule());

        controler.run();
    }

    @Test
    public final void rewardComputeTest(){
        Config config = ConfigUtils.loadConfig("test/input/scenario/equil/config.xml", new MultiModeDrtConfigGroup(), new DvrpConfigGroup(), new DrtFaresConfigGroup());
        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = ConfigUtils.addOrGetModule(config, SwissRailRaptorConfigGroup.class);
        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(false);
        DrtConfigs.adjustMultiModeDrtConfig(ConfigUtils.addOrGetModule(config,MultiModeDrtConfigGroup.class), config.planCalcScore(), config.plansCalcRoute());

        config.controler().setRunId("rewardComputeTest");
        config.controler().setOutputDirectory(utils.getOutputDirectory());
        config.controler().setWriteEventsInterval(1);

        Scenario scenario = DrtControlerCreator.createScenarioWithDrtRouteFactory(config);
        ScenarioUtils.loadScenario(scenario);


        org.matsim.core.controler.Controler controler = new Controler(scenario);
        controler.addOverridingModule(new MultiModeDrtModule());
        controler.addOverridingModule(new DvrpModule());
        controler.configureQSimComponents(DvrpQSimComponents.activateAllModes(MultiModeDrtConfigGroup.get(controler.getConfig())));
        controler.addOverridingModule(new DrtFareModule());

        for( Person person : scenario.getPopulation().getPersons().values() ){
            person.getPlans().removeIf( (plan) -> plan!=person.getSelectedPlan() ) ;
        }

        ConfigUtils.addOrGetModule(config, SmartDrtFareConfigGroup.class);
        controler.addOverridingModule(new SmartDrtFareModule());
        SmartDrtFareConfigGroup smartDrtFareConfigGroup = ConfigUtils.addOrGetModule(config,SmartDrtFareConfigGroup.class);
        smartDrtFareConfigGroup.setDiscountLimitedPct(0.1);
        smartDrtFareConfigGroup.setRewardFactor(200);

        ConfigUtils.addOrGetModule(config, DrtSpeedUpConfigGroup.class);
        controler.addOverridingModule(new MultiModeDrtSpeedUpModule());

        controler.run();
    }
}
