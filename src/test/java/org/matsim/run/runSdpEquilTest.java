//package org.matsim.run;
//
//import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
//import org.junit.Rule;
//import org.junit.Test;
//import org.matsim.api.core.v01.Scenario;
//import org.matsim.api.core.v01.population.Person;
//import org.matsim.contrib.drt.run.DrtConfigGroup;
//import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
//import org.matsim.contrib.drt.speedup.DrtSpeedUpParams;
//import org.matsim.core.config.Config;
//import org.matsim.core.config.ConfigUtils;
//import org.matsim.core.controler.Controler;
//import org.matsim.core.controler.OutputDirectoryHierarchy;
//import org.matsim.run.drt.RunDrtOpenBerlinScenario;
//import org.matsim.smartDrtPricing.SmartDrtFareConfigGroup;
//import org.matsim.smartDrtPricing.SmartDrtFareModule;
//import org.matsim.testcases.MatsimTestUtils;
//
///**
// * @author zmeng
// */
//public class runSdpEquilTest {
//    @Rule
//    public MatsimTestUtils utils = new MatsimTestUtils();
//
//    @Test
//    // make sure drt and drt speed up works fine
//    public void runTest_1(){
//
//        String[] args = new String[]{"test/input/scenario/equil/config.xml"};
//
//        Config config = RunDrtOpenBerlinScenario.prepareConfig(args);
//
//        for (DrtConfigGroup drtCfg : MultiModeDrtConfigGroup.get(config).getModalElements()) {
//            if (drtCfg.getDrtSpeedUpParams().isEmpty()) {
//                drtCfg.addParameterSet(new DrtSpeedUpParams());
//            }
//        }
//
//        // config settings for test
//        config.controler().setRunId("runTest_1");
//        config.controler().setOutputDirectory(utils.getOutputDirectory());
//        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
//        config.controler().setLastIteration(20);
//        config.controler().setWriteEventsInterval(1);
//
//        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = ConfigUtils.addOrGetModule(config, SwissRailRaptorConfigGroup.class);
//        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(false);
//
//
//        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);
//        for (Person person : scenario.getPopulation().getPersons().values()) {
//            person.getPlans().removeIf((plan) -> plan != person.getSelectedPlan());
//        }
//
//        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);
//
//        controler.run() ;
//    }
//    @Test
//    public void runTest_2(){
//
//        String[] args = new String[]{"test/input/scenario/equil/config.xml"};
//
//        Config config = RunDrtOpenBerlinScenario.prepareConfig(args, new SmartDrtFareConfigGroup());
//
//        for (DrtConfigGroup drtCfg : MultiModeDrtConfigGroup.get(config).getModalElements()) {
//            if (drtCfg.getDrtSpeedUpParams().isEmpty()) {
//                drtCfg.addParameterSet(new DrtSpeedUpParams());
//            }
//        }
//
//        // config settings for test
//        config.controler().setRunId("runTest_sdp");
//        config.controler().setOutputDirectory(utils.getOutputDirectory());
//        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
//        config.controler().setLastIteration(20);
//        config.controler().setWriteEventsInterval(1);
//
//        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = ConfigUtils.addOrGetModule(config, SwissRailRaptorConfigGroup.class);
//        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(false);
//
//
//        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);
//        for (Person person : scenario.getPopulation().getPersons().values()) {
//            person.getPlans().removeIf((plan) -> plan != person.getSelectedPlan());
//        }
//
//        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);
//        controler.addOverridingModule(new SmartDrtFareModule());
//
//        controler.run() ;
//    }
//}
