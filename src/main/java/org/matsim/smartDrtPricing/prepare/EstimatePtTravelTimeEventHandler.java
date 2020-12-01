package org.matsim.smartDrtPricing.prepare;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.router.TripRouter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author zmeng
 */
public class EstimatePtTravelTimeEventHandler implements PersonArrivalEventHandler, ActivityEndEventHandler {

    private Scenario scenario;
    private TripRouter tripRouter;
    private BufferedWriter bw = null;
    private Map<Id<Person>, DrtTripInfo> personId2drtTripInfoCollector = new HashMap<>();
    private Map<Id<Person>, Integer> personId2tripNum = new HashMap<>();
    private Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTrips = new HashMap<>();
    private Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTripsCurrentIt = new HashMap<>();

    public EstimatePtTravelTimeEventHandler(Scenario scenario, TripRouter tripRouter) {
        this.scenario = scenario;
        this.tripRouter = tripRouter;
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {

        if (this.personId2drtTripInfoCollector.containsKey(event.getPersonId())) {
            DrtTripInfo drtTrip = this.personId2drtTripInfoCollector.get(event.getPersonId());
            if (event.getLegMode().equals("drt")) {
                drtTrip.setFindDrtArrivalEvent(true);
                drtTrip.setDrtArrivalEvent(event);
                drtTrip.setDrtTrip(true);
            } else if (drtTrip.needLastArrivalEvent()) {
                EstimatePtTrip estimatePtTrip = this.computePtTravelTime(event,drtTrip,this.personId2estimatePtTrips,scenario,tripRouter);

                if (!this.personId2estimatePtTripsCurrentIt.containsKey(event.getPersonId())) {
                    this.personId2estimatePtTripsCurrentIt.put(event.getPersonId(), new LinkedList<>());
                }
                this.personId2estimatePtTripsCurrentIt.get(event.getPersonId()).add(estimatePtTrip);
                this.personId2drtTripInfoCollector.remove(event.getPersonId());
            }
        }

    }

    private EstimatePtTrip computePtTravelTime(PersonArrivalEvent event, DrtTripInfo drtTrip, Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTrips, Scenario scenario, TripRouter tripRouter) {
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
            double ptTravelTime = planElements.stream().filter(planElement -> (planElement instanceof Leg)).mapToDouble(planElement -> ((Leg) planElement).getTravelTime().seconds()).sum();
            estimatePtTrip.setPtTravelTime(ptTravelTime);
        }

        estimatePtTrip.setDrtTripInfo(drtTrip);
        estimatePtTrip.setRatio(estimatePtTrip.getPtTravelTime() / estimatePtTrip.getDrtTripInfo().getRealDrtTotalTripTime());

        return estimatePtTrip;
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

    public void writeFile(String outputFolder) throws IOException {
        if(this.personId2estimatePtTripsCurrentIt.size() > 0){
            String runOutputDirectory = outputFolder;
            if (!runOutputDirectory.endsWith("/")) runOutputDirectory = runOutputDirectory.concat("/");

            Path smartDrtPricingFolder = Paths.get(runOutputDirectory + "smartDrtPricing/"); //

            if(!smartDrtPricingFolder.toFile().mkdirs())
                Files.createDirectories(smartDrtPricingFolder);

            String fileName = runOutputDirectory + "smartDrtPricing/" + this.scenario.getConfig().controler().getRunId() + "." + "ini" + ".info_" + this.getClass().getName() + ".csv";
            File file = new File(fileName);

            try {
                bw = new BufferedWriter(new FileWriter(file));
                bw.write("it,personId,tripNum,departureLink,arrivalLink,departureTime,arrivalTime,drtTravelTime,EstimatePtTime,ratio");
                for (Id<Person> personId : this.personId2estimatePtTripsCurrentIt.keySet()) {
                    for(EstimatePtTrip estimatePtTrip : this.personId2estimatePtTripsCurrentIt.get(personId)){
                        bw.newLine();
                        bw.write("ini" + "," +
                                personId + "," +
                                estimatePtTrip.getDrtTripInfo().getTripNum() + "," +
                                estimatePtTrip.getDepartureLinkId() + "," +
                                estimatePtTrip.getArrivalLinkId() + "," +
                                estimatePtTrip.getDepartureTime() + "," +
                                estimatePtTrip.getDrtTripInfo().getLastArrivalEvent().getTime() + "," +
                                estimatePtTrip.getDrtTripInfo().getRealDrtTotalTripTime() + "," +
                                estimatePtTrip.getPtTravelTime() + "," +
                                estimatePtTrip.getRatio());
                    }
                }
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
