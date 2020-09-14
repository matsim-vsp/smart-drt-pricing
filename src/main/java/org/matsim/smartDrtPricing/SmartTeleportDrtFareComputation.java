package org.matsim.smartDrtPricing;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFaresConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.router.TripRouter;
import org.matsim.drtSpeedUp.DrtSpeedUp;
import org.matsim.smartDrtPricing.prepare.EstimatePtTrip;
import org.matsim.smartDrtPricing.prepare.TeleportDrtTripInfo;
import org.matsim.smartDrtPricing.ratioThreshold.Penalty;
import org.matsim.smartDrtPricing.ratioThreshold.RatioThresholdCalculator;
import org.matsim.smartDrtPricing.ratioThreshold.Reward;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author zmeng
 */
public class SmartTeleportDrtFareComputation implements ActivityEndEventHandler,PersonDepartureEventHandler,PersonArrivalEventHandler {
    @Inject
    Injector injector;
    @Inject
    SmartDrtFareConfigGroup smartDrtFareConfigGroup;
    @Inject
    Scenario scenario;
    @Inject
    TripRouter tripRouter;
    @Inject
    EventsManager events;
    @Inject
    private DrtFaresConfigGroup drtFaresConfigGroup;
    @Inject @Penalty
    private RatioThresholdCalculator penaltyRatioThresholdCalculator;
    @Inject @Reward
    private RatioThresholdCalculator rewardRatioThresholdCalculator;

    private Map<Integer,Double> it2BeelineFactorForDrtFare = new HashMap<>();
    private int currentIteration;
    private BufferedWriter bw = null;
    private Map<Id<Person>, TeleportDrtTripInfo> personId2drtTripInfoCollector = new HashMap<>();
    private Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTrips = new HashMap<>();
    private Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTripsCurrentIt = new HashMap<>();

    @Override
    public void reset(int iteration) {
        currentIteration = iteration;
        personId2drtTripInfoCollector.clear();
        personId2estimatePtTripsCurrentIt.clear();
        it2BeelineFactorForDrtFare.put(currentIteration,injector.getInstance(DvrpModes.key(DrtSpeedUp.class, "drt")).getCurrentBeelineFactorForDrtFare());
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        if(this.personId2drtTripInfoCollector.containsKey(event.getPersonId())){
            TeleportDrtTripInfo teleportDrtTrip = this.personId2drtTripInfoCollector.get(event.getPersonId());
            if(event.getLegMode().contains("teleportation") && event.getLegMode().contains(smartDrtFareConfigGroup.getDrtMode())){
                teleportDrtTrip.setFindDrtArrivalEvent(true);
                teleportDrtTrip.setDrtArrivalEvent(event);
                teleportDrtTrip.setDrtArrivalLinkIdAndDrtDistance(event.getLinkId());
            } else if (teleportDrtTrip.needLastArrivalEvent()) {
                EstimatePtTrip estimatePtTrip = SmartDrtFareComputation.computePtTravelTime(event, teleportDrtTrip, this.personId2estimatePtTrips, scenario,tripRouter,penaltyRatioThresholdCalculator,rewardRatioThresholdCalculator,smartDrtFareConfigGroup);
                double baseDistanceFare = this.drtFaresConfigGroup.getDrtFareConfigGroups().stream().filter(drtFareConfigGroup -> drtFareConfigGroup.getMode().equals(smartDrtFareConfigGroup.getDrtMode())).collect(Collectors.toList()).get(0).getDistanceFare_m();
                SmartDrtFareComputation.computeFareChange(event,estimatePtTrip, baseDistanceFare,smartDrtFareConfigGroup,events);
                if (!this.personId2estimatePtTripsCurrentIt.containsKey(event.getPersonId())) {
                    this.personId2estimatePtTripsCurrentIt.put(event.getPersonId(), new LinkedList<>());
                }
                this.personId2estimatePtTripsCurrentIt.get(event.getPersonId()).add(estimatePtTrip);
                this.personId2drtTripInfoCollector.remove(event.getPersonId());
            }
        }
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if(event.getLegMode().contains("teleportation") && event.getLegMode().contains(smartDrtFareConfigGroup.getDrtMode())){
            if(this.personId2drtTripInfoCollector.containsKey(event.getPersonId())){
                TeleportDrtTripInfo teleportDrtTripInfo = this.personId2drtTripInfoCollector.get(event.getPersonId());
                teleportDrtTripInfo.setDrtDepartureLinkId(event.getLinkId());
                teleportDrtTripInfo.setDrtTrip(true);
            } else
                throw new RuntimeException("this drt trip is unregistered: personId = " + event.getPersonId()
                    + " departureTime = " + event.getTime() + " legMode = " + event.getTime());
        }

    }

    @Override
    public void handleEvent(ActivityEndEvent event) {
        // this is a real person
        if (scenario.getPopulation().getPersons().containsKey(event.getPersonId())) {
            // store real activity_end time, regardless if this agent will use drt
            if (!event.getActType().contains("interaction")) {
                this.personId2drtTripInfoCollector.put(event.getPersonId(), new TeleportDrtTripInfo(event, this.it2BeelineFactorForDrtFare.get(this.currentIteration), scenario));
            }
        }
    }
    public void writeFile(){
        if(this.personId2estimatePtTripsCurrentIt.size() > 0) {
            String runOutputDirectory = this.scenario.getConfig().controler().getOutputDirectory();
            if (!runOutputDirectory.endsWith("/")) runOutputDirectory = runOutputDirectory.concat("/");

            String fileName = runOutputDirectory + "ITERS/it." + currentIteration + "/" + this.scenario.getConfig().controler().getRunId() + "." + currentIteration + ".info_" + this.getClass().getName() + ".csv";
            File file = new File(fileName);

            try {
                bw = new BufferedWriter(new FileWriter(file));
                bw.write("it,personId,departureLink,arrivalLink,departureTime,arrivalTime,drtTravelTime,unsharedDrtTime,unsharedDrtDistance,EstimatePtTime,ratio,penalty_meter,penalty,penaltyRatioThreshold,reward_meter,reward,rewardRatioThreshold");
                for (Id<Person> personId : this.personId2estimatePtTripsCurrentIt.keySet()) {
                    for(EstimatePtTrip estimatePtTrip : this.personId2estimatePtTripsCurrentIt.get(personId)){
                        bw.newLine();
                        bw.write(this.currentIteration + "," +
                                personId + "," +
                                estimatePtTrip.getDepartureLinkId() + "," +
                                estimatePtTrip.getArrivalLinkId() + "," +
                                estimatePtTrip.getDepartureTime() + "," +
                                estimatePtTrip.getDrtTripInfo().getLastArrivalEvent().getTime() + "," +
                                estimatePtTrip.getDrtTripInfo().getRealDrtTotalTripTime() + "," +
                                "-" + "," +
                                estimatePtTrip.getDrtTripInfo().getUnsharedRideDistance() + "," +
                                estimatePtTrip.getPtTravelTime() + "," +
                                estimatePtTrip.getRatio() + "," +
                                estimatePtTrip.getPenaltyPerMeter() + "," +
                                estimatePtTrip.getPenalty() + "," +
                                estimatePtTrip.getPenaltyRatioThreshold() + "," +
                                estimatePtTrip.getRewardPerMeter() + "," +
                                estimatePtTrip.getReward() + "," +
                                estimatePtTrip.getRewardRatioThreshold());
                    }
                }
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
