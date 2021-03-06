package org.matsim.run;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.speedup.DrtSpeedUpParams;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.run.drt.RunDrtOpenBerlinScenario;
import org.matsim.smartDrtPricing.SmartDrtFareConfigGroup;
import org.matsim.smartDrtPricing.SmartDrtFareModule;
import org.matsim.testcases.MatsimTestUtils;

import java.util.Arrays;

/**
 * @author zmeng
 */
public class runSdpEquilTest {
    @Rule
    public MatsimTestUtils utils = new MatsimTestUtils();

    @Test
    // make sure drt and drt speed up works fine
    public void runTest_1(){

        String[] args = new String[]{"test/input/scenario/equil/config.xml"};

        Config config = RunDrtOpenBerlinScenario.prepareConfig(args);

        for (DrtConfigGroup drtCfg : MultiModeDrtConfigGroup.get(config).getModalElements()) {
            if (drtCfg.getDrtSpeedUpParams().isEmpty()) {
                drtCfg.addParameterSet(new DrtSpeedUpParams());
            }
        }

        // config settings for test
        config.controler().setRunId("runTest_1");
        config.controler().setOutputDirectory(utils.getOutputDirectory());
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setLastIteration(20);
        config.controler().setWriteEventsInterval(1);

        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = ConfigUtils.addOrGetModule(config, SwissRailRaptorConfigGroup.class);
        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(false);


        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);
        for (Person person : scenario.getPopulation().getPersons().values()) {
            person.getPlans().removeIf((plan) -> plan != person.getSelectedPlan());
        }

        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);

        controler.run() ;
    }
    @Test
    public void runTest_2(){

        String[] args = new String[]{"test/input/scenario/equil/config.xml"};

        Config config = RunDrtOpenBerlinScenario.prepareConfig(args, new SmartDrtFareConfigGroup());

        for (DrtConfigGroup drtCfg : MultiModeDrtConfigGroup.get(config).getModalElements()) {
            if (drtCfg.getDrtSpeedUpParams().isEmpty()) {
                drtCfg.addParameterSet(new DrtSpeedUpParams());
            }
        }

        // config settings for test
        config.controler().setRunId("runTest_sdp");
        config.controler().setOutputDirectory(utils.getOutputDirectory());
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setLastIteration(20);
        config.controler().setWriteEventsInterval(1);

        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = ConfigUtils.addOrGetModule(config, SwissRailRaptorConfigGroup.class);
        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(false);


        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);
        for (Person person : scenario.getPopulation().getPersons().values()) {
            person.getPlans().removeIf((plan) -> plan != person.getSelectedPlan());
        }

        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);
        controler.addOverridingModule(new SmartDrtFareModule());

        controler.run() ;
    }

    @Test
    public void runTest_3(){

        String[] args = new String[]{"test/input/scenario/equil/config.xml",
                "--config:controler.lastIteration","20",
                "--config:controler.overwriteFiles","deleteDirectoryIfExists",
                "--config:smartDrtPricing.baseDistanceFare","3.5E-4",
                "--config:smartDrtPricing.discountLimitedPct","0.5",
                "--config:smartDrtPricing.maxDrtDistance","40000.0",
                "--config:smartDrtPricing.penaltyFactor","1.0",
                "--config:smartDrtPricing.penaltyRatioThreshold","0.97754592",
                "--config:smartDrtPricing.penaltyRatioThresholdCalculator","exponents",
                "--config:smartDrtPricing.penaltyRatioThresholdFactorA","0.55197643",
                "--config:smartDrtPricing.penaltyRatioThresholdFactorB","0.45114302",
                "--config:smartDrtPricing.penaltyRatioThresholdFactorC","0.0",
                "--config:smartDrtPricing.penaltyStrategy","true",
                "--config:smartDrtPricing.rewardFactor","0.5",
                "--config:smartDrtPricing.rewardRatioThreshold","1.43676465",
                "--config:smartDrtPricing.rewardRatioThresholdCalculator","exponents",
                "--config:smartDrtPricing.rewardRatioThresholdFactorA","0.34630026",
                "--config:smartDrtPricing.rewardRatioThresholdFactorB","1.03209483",
                "--config:smartDrtPricing.rewardRatioThresholdFactorC","0",
                "--config:smartDrtPricing.rewardStrategy","true"};

        Config config = RunDrtOpenBerlinScenario.prepareConfig(args, new SmartDrtFareConfigGroup());

        for (DrtConfigGroup drtCfg : MultiModeDrtConfigGroup.get(config).getModalElements()) {
            if (drtCfg.getDrtSpeedUpParams().isEmpty()) {
                drtCfg.addParameterSet(new DrtSpeedUpParams());
            }
        }
        // config settings for test
        config.controler().setRunId("runTest_sdp");
        config.controler().setOutputDirectory(utils.getOutputDirectory());
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.controler().setLastIteration(20);
        config.controler().setWriteEventsInterval(1);
        config.plans().setInputFile("/Users/meng/IdeaProjects/smart-drt-pricing/test/input/scenario/equil/plans-drt.xml");

        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = ConfigUtils.addOrGetModule(config, SwissRailRaptorConfigGroup.class);
        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(false);

        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);

        for (Person person : scenario.getPopulation().getPersons().values()) {
            person.getPlans().removeIf((plan) -> plan != person.getSelectedPlan());
        }

        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);
        controler.addOverridingModule(new SmartDrtFareModule());

        controler.run() ;
    }

    @Test
    public void plansReader() {
        Config config = ConfigUtils.loadConfig("/Users/meng/work/smart-drt-pricing-paper/open-berlin-scenario/run_sdp_0/output/sdp-0.output_config_reduced.xml");
        config.plans().setInputFile("/Users/meng/work/smart-drt-pricing-paper/open-berlin-scenario/run_sdp_0/output/sdp-0.output_plans.xml.gz");

        Scenario scenario = ScenarioUtils.loadScenario(config);

        scenario.getPopulation().getPersons().forEach((id,person) -> {
            Plan plan = person.getSelectedPlan();
            person.getPlans().clear();
            plan.getPlanElements().forEach(planElement -> {
                if (planElement instanceof Leg) {
                    Leg leg = (Leg) planElement;
                    if (leg.getMode() == "drt") {
                        String routeDescription = leg.getRoute().getRouteDescription();
                        leg.getRoute().setRouteDescription(replaceOptionalTime(routeDescription));
                    }
                }
            });
            person.addPlan(plan);
            person.setSelectedPlan(plan);
        });
        PopulationUtils.writePopulation(scenario.getPopulation(),"/Users/meng/work/smart-drt-pricing-paper/open-berlin-scenario/run_sdp_0/output/sdp-0.output_plans-1.xml.gz");
    }
    public String replaceOptionalTime(String str){
        for (String s : Arrays.asList("OptionalTime[","]")) {
            str = str.replace(s,"");
        }
        return str;
    }
}
