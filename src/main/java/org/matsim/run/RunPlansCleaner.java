package org.matsim.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.Arrays;

/**
 * @author zmeng
 */
public class RunPlansCleaner {
    public static void main(String[] args) {

        if(args.length == 0){
            args = new String[]{"/Users/meng/IdeaProjects/smart-drt-pricing/test/input/scenario/equil/runTest_sdp.output_config.xml",
                    "/Users/meng/IdeaProjects/smart-drt-pricing/test/output/org/matsim/run/runSdpEquilTest/runTest_2/runTest_sdp.output_plans.xml.gz",
                    "/Users/meng/IdeaProjects/smart-drt-pricing/test/output/org/matsim/run/runSdpEquilTest/runTest_2/runTest_sdp.output_plans3.xml.gz"
            };
        }
        Config config = ConfigUtils.loadConfig(args[0]);
        config.plans().setInputFile(args[1]);

        Scenario scenario = ScenarioUtils.loadScenario(config);

        scenario.getPopulation().getPersons().forEach((id,person) -> {
            Plan plan = person.getSelectedPlan();
            person.getPlans().clear();
            plan.getPlanElements().forEach(planElement -> {
                if (planElement instanceof Leg) {
                    Leg leg = (Leg) planElement;
                    if (leg.getMode() == "drt") {
                        //String routeDescription = leg.getRoute().getRouteDescription();
                        leg.setRoute(null);
                        //leg.getRoute().setRouteDescription(replaceOptionalTime(routeDescription));
                    }
                }
            });
            person.addPlan(plan);
            person.setSelectedPlan(plan);
        });
        PopulationUtils.writePopulation(scenario.getPopulation(),args[2]);
    }

    public static String replaceOptionalTime(String str){
        for (String s : Arrays.asList("OptionalTime[","]")) {
            str = str.replace(s,"");
        }
        return str;
    }
}
