package org.matsim.smartDrtPricing.prepare;


/**
 * @author zmeng
 */
public class Threshold {

    public static class Builder {
        private double upperLimit = Double.POSITIVE_INFINITY;
        private double lowerLimit = Double.NEGATIVE_INFINITY;

        public Builder setUpperLimit(double upperLimit){
            this.upperLimit = upperLimit;
            return this;
        }

        public Builder setLowerLimit(double lowerLimit){
            this.lowerLimit = lowerLimit;
            return this;
        }

        public Threshold build(){
            Threshold threshold = new Threshold();
            threshold.lowerLimit = this.lowerLimit;
            threshold.upperLimit = this.upperLimit;
            return threshold;
        }
    }
    private double upperLimit;
    private double lowerLimit;

    public double getLowerLimit() {
        return lowerLimit;
    }

    public double getUpperLimit() {
        return upperLimit;
    }

    @Override
    public String toString() {
        return lowerLimit + "|" + upperLimit;
    }
}
