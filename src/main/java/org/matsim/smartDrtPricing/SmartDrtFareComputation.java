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
import org.matsim.smartDrtPricing.ratioThreshold.Penalty;
import org.matsim.smartDrtPricing.ratioThreshold.Reward;
import org.matsim.smartDrtPricing.ratioThreshold.RatioThresholdCalculator;

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
    private Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTrips = new HashMap<>();
    private Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTripsCurrentIt = new HashMap<>();
    private int newCalculatedNumOfPtTrips= 0;
    private int numOfHasPenaltyDrtUsers = 0;
    private int numOfHasRewardDrtUsers = 0;
    private int totalDrtUsers = 0;

    @Override
    public void reset(int iteration) {
        this.currentIteration = iteration;
        personId2drtTripInfoCollector.clear();
        personId2estimatePtTripsCurrentIt.clear();
        newCalculatedNumOfPtTrips = 0;
        numOfHasPenaltyDrtUsers = 0;
        numOfHasRewardDrtUsers = 0;
        totalDrtUsers = 0;
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {

        if (this.personId2drtTripInfoCollector.containsKey(event.getPersonId())) {
            DrtTripInfo drtTrip = this.personId2drtTripInfoCollector.get(event.getPersonId());
            if (event.getLegMode().equals(smartDrtFareConfigGroup.getDrtMode())) {
                drtTrip.setFindDrtArrivalEvent(true);
                drtTrip.setDrtArrivalEvent(event);
            } else if (drtTrip.needLastArrivalEvent()) {
                totalDrtUsers++;
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
        if (smartDrtFareConfigGroup.hasPenaltyStrategy() && estimatePtTrip.getRatio() <= estimatePtTrip.getPenaltyRatioThreshold()) {
                double penaltyPerMeter = smartDrtFareConfigGroup.getPenaltyFactor() * baseDistanceFare * estimatePtTrip.getPenaltyRatioThreshold() / estimatePtTrip.getRatio() - baseDistanceFare;
                double penalty = estimatePtTrip.getDrtTripInfo().getUnsharedRideDistance() * penaltyPerMeter;
                events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), -penalty));
                estimatePtTrip.setPenaltyPerMeter(penaltyPerMeter);
                estimatePtTrip.setPenalty(penalty);
        }
        if (smartDrtFareConfigGroup.hasRewardStrategy() && estimatePtTrip.getRatio() >= estimatePtTrip.getRewardRatioThreshold()) {
                double rewardPerMeter = Math.min(smartDrtFareConfigGroup.getDiscountLimitedPct()* baseDistanceFare,
                        smartDrtFareConfigGroup.getRewardFactor() * baseDistanceFare * (estimatePtTrip.getRatio() / estimatePtTrip.getRewardRatioThreshold() - 1));
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

        double penaltyRatioThreshold = penaltyRatioThresholdCalculator.calculateRatioThreshold(estimatePtTrip.getDrtTripInfo().getUnsharedRideDistance(),
                smartDrtFareConfigGroup.getPenaltyRatioThreshold(),
                smartDrtFareConfigGroup.getPenaltyRatioThresholdFactorA(),
                smartDrtFareConfigGroup.getPenaltyRatioThresholdFactorB(),
                smartDrtFareConfigGroup.getPenaltyRatioThresholdFactorC());

        double rewardRatioThreshold = rewardRatioThresholdCalculator.calculateRatioThreshold(estimatePtTrip.getDrtTripInfo().getUnsharedRideDistance(),
                smartDrtFareConfigGroup.getRewardRatioThreshold(),
                smartDrtFareConfigGroup.getRewardRatioThresholdFactorA(),
                smartDrtFareConfigGroup.getRewardRatioThresholdFactorB(),
                smartDrtFareConfigGroup.getRewardRatioThresholdFactorC());

        estimatePtTrip.setPenaltyRatioThreshold(penaltyRatioThreshold);
        estimatePtTrip.setRewardRatioThreshold(rewardRatioThreshold);
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
        if (scenario.getPopulation().getPersons().containsKey(event.getPersonId())) {
            // store real activity_end time, regardless if this agent will use drt
            if (!event.getActType().contains("interaction")) {
                this.personId2drtTripInfoCollector.put(event.getPersonId(), new DrtTripInfo(event));
            }
        }
    }

    public void writeFile(){
        if(this.personId2estimatePtTripsCurrentIt.size() > 0){
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

