package org.matsim.smartDrtPricing.ratioThreshold;

/**
 * @author zmeng
 */
public interface RatioThresholdCalculator {
    double calculateRatioThreshold(double distance, double... parameters);
}
