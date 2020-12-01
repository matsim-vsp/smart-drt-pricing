package org.matsim.run;


import com.google.inject.Injector;
import com.google.inject.Module;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.ControlerDefaultsModule;
import org.matsim.core.controler.NewControlerModule;
import org.matsim.core.controler.corelisteners.ControlerDefaultCoreListenersModule;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.router.TripRouter;
import org.matsim.core.scenario.ScenarioByInstanceModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.run.drt.RunDrtOpenBerlinScenario;
import org.matsim.smartDrtPricing.SmartDrtFareConfigGroup;
import org.matsim.smartDrtPricing.prepare.EstimatePtTravelTimeEventHandler;

import java.io.IOException;

/**
 * this is a post-processing tool for calculation of estimated public transit travel time after a MATSim simulation run.
 * args[0] is the config file which is used to configure the simulation
 * args[1] output_events file obtained after the simulation
 * args[2] output directory used to store the result: estimated pt travel time for each drt trip.
 * @author zmeng
 */
public class RunPtTravelTimeCalculator {
    public static void main(String[] args) throws IOException {
        args = new String[]{
          "test/input/scenario/equil/runTest_sdp.output_config.xml", //config
          "/Users/meng/IdeaProjects/smart-drt-pricing/test/output/org/matsim/run/runSdpEquilTest/runTest_2/runTest_sdp.output_events.xml.gz",
          "test/output/org/matsim/run/runSdpEquilTest/runTest_2/sdp"

        };

        Config config = RunDrtOpenBerlinScenario.prepareConfig(new String[]{args[0]}, new SmartDrtFareConfigGroup());

        config.controler().setOutputDirectory(args[2]);

        Scenario scenario = ScenarioUtils.loadScenario(config);

        Module module = new AbstractModule(){
            @Override
            public void install() {
                install(new NewControlerModule());
                install(new ControlerDefaultCoreListenersModule());
                install(new ControlerDefaultsModule());
                install(new ScenarioByInstanceModule(scenario));
            }
        };


        Injector injector = org.matsim.core.controler.Injector.createInjector(config,module);
        TripRouter tripRouter = injector.getInstance(TripRouter.class);

        EventsManager eventManager = EventsUtils.createEventsManager();
        EstimatePtTravelTimeEventHandler estimatePtTravelTimeEventHandler = new EstimatePtTravelTimeEventHandler(scenario, tripRouter);
        eventManager.addHandler(estimatePtTravelTimeEventHandler);
        eventManager.initProcessing();
        (new MatsimEventsReader(eventManager)).readFile(args[1]);
        eventManager.finishProcessing();

        estimatePtTravelTimeEventHandler.writeFile(args[2]);

    }
}
