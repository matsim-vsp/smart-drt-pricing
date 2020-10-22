package org.matsim.smartDrtPricing.prepare;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.network.NetworkUtils;

/**
 * @author zmeng
 */
public class TeleportDrtTripInfo extends DrtTripInfo{

    Scenario scenario;
    private Id<Link> drtDepartureLinkId;
    private Id<Link> drtArrivalLinkId;
    private double beelineDrtDistanceWithFactor;
    private double unsharedRideDistance;
    private final double beelineDrtDistanceFactor;

    public TeleportDrtTripInfo(ActivityEndEvent realActivityEndEvent, int tripNum, double beelineDrtDistanceFactor, Scenario scenario) {
        super(realActivityEndEvent,tripNum);
        this.beelineDrtDistanceFactor = beelineDrtDistanceFactor;
        this.scenario = scenario;
    }

    public Id<Link> getDrtDepartureLinkId() {
        return drtDepartureLinkId;
    }

    public void setDrtDepartureLinkId(Id<Link> drtDepartureLinkId) {
        this.drtDepartureLinkId = drtDepartureLinkId;
    }

    public Id<Link> getDrtArrivalLinkId() {
        return drtArrivalLinkId;
    }

    public void setDrtArrivalLinkIdAndDrtDistance(Id<Link> drtArrivalLinkId) {
        this.drtArrivalLinkId = drtArrivalLinkId;
        this.unsharedRideDistance = NetworkUtils.getEuclideanDistance(scenario.getNetwork().getLinks().get(this.drtDepartureLinkId).getCoord(),
                scenario.getNetwork().getLinks().get(drtArrivalLinkId).getCoord());
    }

    private double getBeelineDrtDistanceWithFactor() {
        return beelineDrtDistanceWithFactor;
    }

    public void setBeelineDrtDistanceWithFactor(double beelineDrtDistanceWithFactor) {
        this.beelineDrtDistanceWithFactor = beelineDrtDistanceWithFactor;
    }

    @Override
    public double getUnsharedRideDistance() {
        return this.unsharedRideDistance;
    }

}
