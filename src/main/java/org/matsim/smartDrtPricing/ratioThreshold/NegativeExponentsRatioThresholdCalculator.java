package org.matsim.smartDrtPricing.ratioThreshold;

import static java.lang.Math.E;

/**
 * threshold = a * e^(-b * x) + c
 * @author zmeng
 */
public class NegativeExponentsRatioThresholdCalculator implements RatioThresholdCalculator{
    @Override
    public double calculateRatioThreshold(double distance, double... parameters) {
        return parameters[0] +
                parameters[1] * Math.pow(E,-parameters[2]*distance/1000);
    }
}
