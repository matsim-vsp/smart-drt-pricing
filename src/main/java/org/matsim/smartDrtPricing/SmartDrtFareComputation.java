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

package org.matsim.smartDrtPricing;

import com.google.inject.Inject;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverService;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFaresConfigGroup;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEvent;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.router.TripRouter;
import org.matsim.smartDrtPricing.prepare.EstimatePtTrip;
import org.matsim.smartDrtPricing.prepare.DrtTripInfo;
import org.matsim.smartDrtPricing.prepare.Threshold;
import org.matsim.smartDrtPricing.ratioThreshold.Penalty;
import org.matsim.smartDrtPricing.ratioThreshold.Reward;
import org.matsim.smartDrtPricing.ratioThreshold.RatioThresholdCalculator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ikaddoura zmeng
 */
public class SmartDrtFareComputation implements DrtRequestSubmittedEventHandler, PersonArrivalEventHandler, ActivityEndEventHandler {

    private final Logger logger = Logger.getLogger(SmartDrtFareComputation.class);
    @Inject
    private EventsManager events;
    @Inject
    private Scenario scenario;
    @Inject
    private TripRouter tripRouter;
    @Inject
    private SmartDrtFareConfigGroup smartDrtFareConfigGroup;
    @Inject
    private DrtFaresConfigGroup drtFaresConfigGroup;
    @Inject @Penalty
    private RatioThresholdCalculator penaltyRatioThresholdCalculator;
    @Inject @Reward
    private RatioThresholdCalculator rewardRatioThresholdCalculator;

    private int currentIteration;
    private BufferedWriter bw = null;
    private Map<Id<Person>, DrtTripInfo> personId2drtTripInfoCollector = new HashMap<>();
    private Map<Id<Person>, Integer> personId2tripNum = new HashMap<>();
    private Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTrips = new HashMap<>();
    private Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTripsCurrentIt = new HashMap<>();

    @Override
    public void reset(int iteration) {
        this.currentIteration = iteration;
        personId2drtTripInfoCollector.clear();
        personId2estimatePtTripsCurrentIt.clear();
        personId2tripNum.clear();
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {

        if (this.personId2drtTripInfoCollector.containsKey(event.getPersonId())) {
            DrtTripInfo drtTrip = this.personId2drtTripInfoCollector.get(event.getPersonId());
            if (event.getLegMode().equals(smartDrtFareConfigGroup.getDrtMode())) {
                drtTrip.setFindDrtArrivalEvent(true);
                drtTrip.setDrtArrivalEvent(event);
            } else if (drtTrip.needLastArrivalEvent()) {
                EstimatePtTrip estimatePtTrip = SmartDrtFareComputation.computePtTravelTime(event, drtTrip, this.personId2estimatePtTrips, scenario,tripRouter,penaltyRatioThresholdCalculator,rewardRatioThresholdCalculator,smartDrtFareConfigGroup);
                double baseDistanceFare = this.drtFaresConfigGroup.getDrtFareConfigGroups().stream().filter(drtFareConfigGroup -> drtFareConfigGroup.getMode().equals(smartDrtFareConfigGroup.getDrtMode())).collect(Collectors.toList()).get(0).getDistanceFare_m();
                computeFareChange(event,estimatePtTrip, baseDistanceFare,smartDrtFareConfigGroup,events);
                if (!this.personId2estimatePtTripsCurrentIt.containsKey(event.getPersonId())) {
                    this.personId2estimatePtTripsCurrentIt.put(event.getPersonId(), new LinkedList<>());
                }
                this.personId2estimatePtTripsCurrentIt.get(event.getPersonId()).add(estimatePtTrip);
                this.personId2drtTripInfoCollector.remove(event.getPersonId());
            }
        }

    }

    public static void computeFareChange(PersonArrivalEvent event,EstimatePtTrip estimatePtTrip, double baseDistanceFare,SmartDrtFareConfigGroup smartDrtFareConfigGroup, EventsManager events) {
        estimatePtTrip.setPenalty(0);
        estimatePtTrip.setPenaltyPerMeter(0);
        estimatePtTrip.setReward(0);
        estimatePtTrip.setRewardPerMeter(0);
        if (smartDrtFareConfigGroup.hasPenaltyStrategy() && estimatePtTrip.getRatio() <= estimatePtTrip.getPenaltyRatioThreshold().getUpperLimit() && estimatePtTrip.getRatio() >= estimatePtTrip.getPenaltyRatioThreshold().getLowerLimit()) {
                double penaltyPerMeter = smartDrtFareConfigGroup.getPenaltyFactor() * baseDistanceFare * estimatePtTrip.getPenaltyRatioThreshold().getUpperLimit() / estimatePtTrip.getRatio() - baseDistanceFare;
                double penalty = estimatePtTrip.getDrtTripInfo().getUnsharedRideDistance() * penaltyPerMeter;
                events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), -penalty));
                estimatePtTrip.setPenaltyPerMeter(penaltyPerMeter);
                estimatePtTrip.setPenalty(penalty);
        }
        if (smartDrtFareConfigGroup.hasRewardStrategy() && estimatePtTrip.getRatio() >= estimatePtTrip.getRewardRatioThreshold().getLowerLimit() && estimatePtTrip.getRatio() <= estimatePtTrip.getRewardRatioThreshold().getUpperLimit()) {
                double rewardPerMeter = Math.min(smartDrtFareConfigGroup.getDiscountLimitedPct()* baseDistanceFare,
                        smartDrtFareConfigGroup.getRewardFactor() * baseDistanceFare * (estimatePtTrip.getRatio() / estimatePtTrip.getRewardRatioThreshold().getLowerLimit() - 1));
                double reward = estimatePtTrip.getDrtTripInfo().getUnsharedRideDistance() * rewardPerMeter;
                events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), reward));
                estimatePtTrip.setReward(reward);
                estimatePtTrip.setRewardPerMeter(rewardPerMeter);
        }
    }

    static EstimatePtTrip computePtTravelTime(PersonArrivalEvent event, DrtTripInfo drtTrip, Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTrips, Scenario scenario, TripRouter tripRouter, RatioThresholdCalculator penaltyRatioThresholdCalculator, RatioThresholdCalculator rewardRatioThresholdCalculator, SmartDrtFareConfigGroup smartDrtFareConfigGroup) {
        drtTrip.setLastArrivalEvent(event);

        if (!personId2estimatePtTrips.containsKey(event.getPersonId())) {
            personId2estimatePtTrips.put(event.getPersonId(), new LinkedList<>());
        }

        double departureTime = drtTrip.getRealActivityEndEvent().getTime();
        Id<Link> departureLinkID = drtTrip.getRealActivityEndEvent().getLinkId();
        Id<Link> arrivalLinkID = drtTrip.getLastArrivalEvent().getLinkId();

        List<EstimatePtTrip> ptTrips = personId2estimatePtTrips.get(event.getPersonId());

        EstimatePtTrip temEstimatePtTrip = new EstimatePtTrip(scenario, departureLinkID, arrivalLinkID, departureTime);
        EstimatePtTrip estimatePtTrip = temEstimatePtTrip.updatePtTrips(ptTrips);

        if (!estimatePtTrip.isHasPtTravelTime()) {
            estimatePtTrip.setHasPtTravelTime(true);
            List<? extends PlanElement> planElements = tripRouter.calcRoute(TransportMode.pt, estimatePtTrip.getDepartureFacility(), estimatePtTrip.getArrivalFacility(), estimatePtTrip.getDepartureTime(), scenario.getPopulation().getPersons().get(event.getPersonId()));
            double ptTravelTime = planElements.stream().filter(planElement -> (planElement instanceof Leg)).mapToDouble(planElement -> ((Leg) planElement).getTravelTime()).sum();
            estimatePtTrip.setPtTravelTime(ptTravelTime);
        }
        estimatePtTrip.setDrtTripInfo(drtTrip);
        computeRatio(drtTrip, estimatePtTrip,penaltyRatioThresholdCalculator,rewardRatioThresholdCalculator,smartDrtFareConfigGroup);

        return estimatePtTrip;
    }

    private static void computeRatio(DrtTripInfo drtTrip, EstimatePtTrip estimatePtTrip, RatioThresholdCalculator penaltyRatioThresholdCalculator, RatioThresholdCalculator rewardRatioThresholdCalculator, SmartDrtFareConfigGroup smartDrtFareConfigGroup) {

        double ratio = estimatePtTrip.getPtTravelTime() / drtTrip.getRealDrtTotalTripTime();
        estimatePtTrip.setRatio(ratio);
        double dis = estimatePtTrip.getDrtTripInfo().getUnsharedRideDistance();
        Threshold.Builder penaltyThresholdBuilder = new Threshold.Builder();
        Threshold.Builder rewardThresholdBuilder = new Threshold.Builder();

        if(smartDrtFareConfigGroup.getPenaltyRatioThreshold().contains(",")){
            double[] penaltyThreshold = Arrays.stream(smartDrtFareConfigGroup.getPenaltyRatioThreshold().split(",")).mapToDouble(Double::parseDouble).toArray();
            double[] a =  Arrays.stream(smartDrtFareConfigGroup.getPenaltyRatioThresholdFactorA().split(",")).mapToDouble(Double::parseDouble).toArray();
            double[] b =  Arrays.stream(smartDrtFareConfigGroup.getPenaltyRatioThresholdFactorB().split(",")).mapToDouble(Double::parseDouble).toArray();
            double[] c =  Arrays.stream(smartDrtFareConfigGroup.getPenaltyRatioThresholdFactorC().split(",")).mapToDouble(Double::parseDouble).toArray();

            penaltyThresholdBuilder.setLowerLimit(penaltyRatioThresholdCalculator.calculateRatioThreshold(dis,penaltyThreshold[0],a[0],b[0],c[0])).
            setUpperLimit(penaltyRatioThresholdCalculator.calculateRatioThreshold(dis,penaltyThreshold[1],a[1],b[1],c[1]));
        } else {
            double penaltyThreshold = Double.parseDouble(smartDrtFareConfigGroup.getPenaltyRatioThreshold());
            double a = Double.parseDouble(smartDrtFareConfigGroup.getPenaltyRatioThresholdFactorA());
            double b = Double.parseDouble(smartDrtFareConfigGroup.getPenaltyRatioThresholdFactorB());
            double c = Double.parseDouble(smartDrtFareConfigGroup.getPenaltyRatioThresholdFactorC());
            penaltyThresholdBuilder.setUpperLimit(penaltyRatioThresholdCalculator.calculateRatioThreshold(dis,penaltyThreshold,a,b,c));
        }

        if(smartDrtFareConfigGroup.getRewardRatioThreshold().contains(",")){
            double[] rewardThreshold = Arrays.stream(smartDrtFareConfigGroup.getRewardRatioThreshold().split(",")).mapToDouble(Double::parseDouble).toArray();
            double[] a =  Arrays.stream(smartDrtFareConfigGroup.getRewardRatioThresholdFactorA().split(",")).mapToDouble(Double::parseDouble).toArray();
            double[] b =  Arrays.stream(smartDrtFareConfigGroup.getRewardRatioThresholdFactorB().split(",")).mapToDouble(Double::parseDouble).toArray();
            double[] c =  Arrays.stream(smartDrtFareConfigGroup.getRewardRatioThresholdFactorC().split(",")).mapToDouble(Double::parseDouble).toArray();

            rewardThresholdBuilder.setLowerLimit(rewardRatioThresholdCalculator.calculateRatioThreshold(dis,rewardThreshold[0],a[0],b[0],c[0])).
                    setUpperLimit(rewardRatioThresholdCalculator.calculateRatioThreshold(dis,rewardThreshold[1],a[1],b[1],c[1]));
        } else {
            double rewardThreshold = Double.parseDouble(smartDrtFareConfigGroup.getRewardRatioThreshold());
            double a = Double.parseDouble(smartDrtFareConfigGroup.getRewardRatioThresholdFactorA());
            double b = Double.parseDouble(smartDrtFareConfigGroup.getRewardRatioThresholdFactorB());
            double c = Double.parseDouble(smartDrtFareConfigGroup.getRewardRatioThresholdFactorC());
            rewardThresholdBuilder.setLowerLimit(rewardRatioThresholdCalculator.calculateRatioThreshold(dis,rewardThreshold,a,b,c));
        }

        estimatePtTrip.setPenaltyRatioThreshold(penaltyThresholdBuilder.build());
        estimatePtTrip.setRewardRatioThreshold(rewardThresholdBuilder.build());
    }

    @Override
    public void handleEvent(DrtRequestSubmittedEvent event) {
        // store agent Id who really used drt
        if (this.smartDrtFareConfigGroup.getDrtMode().equals(event.getMode()) && this.personId2drtTripInfoCollector.containsKey(event.getPersonId())) {
            DrtTripInfo drtTripInfo = this.personId2drtTripInfoCollector.get(event.getPersonId());
            drtTripInfo.setDrtRequestSubmittedEvent(event);
            drtTripInfo.setDrtTrip(true);
        }
    }

    @Override
    public void handleEvent(ActivityEndEvent event) {
        // this is a real person
        Id<Person> personId = event.getPersonId();

        if (scenario.getPopulation().getPersons().containsKey(personId)) {
            // store real activity_end time, regardless if this agent will use drt
            if (!event.getActType().contains("interaction")) {
                if (this.personId2tripNum.containsKey(personId))
                    this.personId2tripNum.put(personId,this.personId2tripNum.get(personId) + 1);
                else
                    this.personId2tripNum.put(personId, 1);
                this.personId2drtTripInfoCollector.put(personId, new DrtTripInfo(event,this.personId2tripNum.get(personId)));
            }

        }
    }

    public void writeFile() throws IOException {
        if(this.personId2estimatePtTripsCurrentIt.size() > 0){
            String runOutputDirectory = this.scenario.getConfig().controler().getOutputDirectory();
            if (!runOutputDirectory.endsWith("/")) runOutputDirectory = runOutputDirectory.concat("/");

            Path smartDrtPricingFolder = Paths.get(runOutputDirectory + "smartDrtPricing/");

            if(!smartDrtPricingFolder.toFile().mkdirs())
                Files.createDirectories(smartDrtPricingFolder);

            String fileName = runOutputDirectory + "smartDrtPricing/" + this.scenario.getConfig().controler().getRunId() + "." + currentIteration + ".info_" + this.getClass().getName() + ".csv";
            File file = new File(fileName);

            try {
                bw = new BufferedWriter(new FileWriter(file));
                bw.write("it,personId,tripNum,departureLink,arrivalLink,departureTime,arrivalTime,drtTravelTime,unsharedDrtTime,unsharedDrtDistance,EstimatePtTime,ratio,penalty_meter,penalty,penaltyRatioThreshold,reward_meter,reward,rewardRatioThreshold");
                for (Id<Person> personId : this.personId2estimatePtTripsCurrentIt.keySet()) {
                    for(EstimatePtTrip estimatePtTrip : this.personId2estimatePtTripsCurrentIt.get(personId)){
                        bw.newLine();
                        bw.write(this.currentIteration + "," +
                                personId + "," +
                                estimatePtTrip.getDrtTripInfo().getTripNum() + "," +
                                estimatePtTrip.getDepartureLinkId() + "," +
                                estimatePtTrip.getArrivalLinkId() + "," +
                                estimatePtTrip.getDepartureTime() + "," +
                                estimatePtTrip.getDrtTripInfo().getLastArrivalEvent().getTime() + "," +
                                estimatePtTrip.getDrtTripInfo().getRealDrtTotalTripTime() + "," +
                                estimatePtTrip.getDrtTripInfo().getTotalUnsharedTripTime() + "," +
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

