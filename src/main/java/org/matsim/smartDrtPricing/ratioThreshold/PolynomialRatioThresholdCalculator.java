package org.matsim.smartDrtPricing.ratioThreshold;

import com.google.inject.Inject;
import org.matsim.smartDrtPricing.SmartDrtFareConfigGroup;

/**
 * @author zmeng
 */
public class PolynomialRatioThresholdCalculator implements RatioThresholdCalculator {
    @Inject
    SmartDrtFareConfigGroup smartDrtFareConfigGroup;
    @Override
    public double calculateRatioThreshold(double distance, double... parameters) {

        double disKm;
        if(distance <= this.smartDrtFareConfigGroup.getMaxDrtDistance()){
            disKm = distance / 1000;
        } else {
            disKm = smartDrtFareConfigGroup.getMaxDrtDistance() / 1000;
        }

        return parameters[0] + parameters[1] * Math.pow(disKm, 3) + parameters[2] * Math.pow(disKm, 2) + parameters[3] * disKm;
    }
}
