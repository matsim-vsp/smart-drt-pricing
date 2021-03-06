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

import org.matsim.core.config.ReflectiveConfigGroup;

/**
 * @author : zmeng
 */
public class SmartDrtFareConfigGroup extends ReflectiveConfigGroup {
    public static final String GROUP_NAME = "smartDrtPricing";
    private static final String DRT_MODE = "drtMode";
    private static final String MAX_DRT_DISTANCE = "maxDrtDistance";
    private static final String BASE_DISTANCE_FARE = "baseDistanceFare";

    private static final String PENALTY_STRATEGY = "penaltyStrategy";
    private static final String PENALTY_FACTOR = "penaltyFactor";
    private static final String PENALTY_RATIO_THRESHOLD_CALCULATOR = "penaltyRatioThresholdCalculator";
    private static final String PENALTY_RATIO_THRESHOLD = "penaltyRatioThreshold";
    private static final String PENALTY_RATIO_THRESHOLD_FACTOR_A = "penaltyRatioThresholdFactorA";
    private static final String PENALTY_RATIO_THRESHOLD_FACTOR_B = "penaltyRatioThresholdFactorB";
    private static final String PENALTY_RATIO_THRESHOLD_FACTOR_C = "penaltyRatioThresholdFactorC";

    private static final String REWARD_STRATEGY = "rewardStrategy";
    private static final String REWARD_FACTOR = "rewardFactor";
    private static final String DISCOUNT_LIMITED_PCT = "discountLimitedPct";
    private static final String REWARD_RATIO_THRESHOLD_CALCULATOR = "rewardRatioThresholdCalculator";
    private static final String REWARD_RATIO_THRESHOLD = "rewardRatioThreshold";
    private static final String REWARD_RATIO_THRESHOLD_FACTOR_A = "rewardRatioThresholdFactorA";
    private static final String REWARD_RATIO_THRESHOLD_FACTOR_B = "rewardRatioThresholdFactorB";
    private static final String REWARD_RATIO_THRESHOLD_FACTOR_C = "rewardRatioThresholdFactorC";

    private static final String WRITE_FILE_INTERVAL = "writeFileInterval";

    public SmartDrtFareConfigGroup() {
        super(GROUP_NAME);
    }

    private String drtMode = "drt";
    private double maxDrtDistance = 40000;
    private double baseDistanceFare = 0.35/1000;

    private boolean penaltyStrategy = false;
    private double penaltyFactor = 1;
    private RatioCalculator penaltyRatioThresholdCalculator = RatioCalculator.exponents;
    private String penaltyRatioThreshold = "1,2";
    private String penaltyRatioThresholdFactorA = "1,2";
    private String penaltyRatioThresholdFactorB = "1,2";
    private String penaltyRatioThresholdFactorC = "1,2";

    private boolean rewardStrategy = false;
    private double rewardFactor = 0.4;
    private double discountLimitedPct = 0.5;
    private RatioCalculator rewardRatioThresholdCalculator = RatioCalculator.exponents;
    private String rewardRatioThreshold = "1,2";
    private String rewardRatioThresholdFactorA = "1,2";
    private String rewardRatioThresholdFactorB = "1,2";
    private String rewardRatioThresholdFactorC = "1,2";

    private int writeFileInterval = 1;

    public enum RatioCalculator{poly,exponents}

    @StringGetter(DISCOUNT_LIMITED_PCT)
    public double getDiscountLimitedPct() { return discountLimitedPct; }
    @StringSetter(DISCOUNT_LIMITED_PCT)
    public void setDiscountLimitedPct(double discountLimitedPct) { this.discountLimitedPct = discountLimitedPct; }
    @StringGetter(BASE_DISTANCE_FARE)
    public double getBaseDistanceFare() {
        return baseDistanceFare;
    }
    @StringSetter(BASE_DISTANCE_FARE)
    public void setBaseDistanceFare(double baseDistanceFare) {
        this.baseDistanceFare = baseDistanceFare;
    }

    @StringSetter(PENALTY_RATIO_THRESHOLD_CALCULATOR)
    public void setPenaltyRatioThresholdCalculator(RatioCalculator penaltyRatioThresholdCalculator) {
        this.penaltyRatioThresholdCalculator = penaltyRatioThresholdCalculator;
    }
    @StringGetter(PENALTY_RATIO_THRESHOLD_CALCULATOR)
    public RatioCalculator getPenaltyRatioThresholdCalculator() {
        return penaltyRatioThresholdCalculator;
    }
    @StringSetter(REWARD_RATIO_THRESHOLD_CALCULATOR)
    public void setRewardRatioThresholdCalculator(RatioCalculator rewardRatioThresholdCalculator) {
        this.rewardRatioThresholdCalculator = rewardRatioThresholdCalculator;
    }
    @StringGetter(REWARD_RATIO_THRESHOLD_CALCULATOR)
    public RatioCalculator getRewardRatioThresholdCalculator() {
        return rewardRatioThresholdCalculator;
    }

    @StringSetter(WRITE_FILE_INTERVAL)
    public void setWriteFileInterval(int writeFileInterval) { this.writeFileInterval = writeFileInterval; }
    @StringGetter(WRITE_FILE_INTERVAL)
    public int getWriteFileInterval() { return writeFileInterval; }
    @StringGetter(DRT_MODE)
    public String getDrtMode(){ return drtMode; }
    @StringSetter(DRT_MODE)
    public void setDrtMode(String drtMode) { this.drtMode = drtMode; }
    @StringGetter(MAX_DRT_DISTANCE)
    public double getMaxDrtDistance() { return maxDrtDistance; }
    @StringSetter(MAX_DRT_DISTANCE)
    public void setMaxDrtDistance(double maxDrtDistance) { this.maxDrtDistance = maxDrtDistance; }

    @StringGetter(PENALTY_STRATEGY)
    public boolean hasPenaltyStrategy() { return penaltyStrategy; }
    @StringSetter(PENALTY_STRATEGY)
    public void setPenaltyStrategy(boolean penaltyStrategy) { this.penaltyStrategy = penaltyStrategy; }
    @StringGetter(REWARD_STRATEGY)
    public boolean hasRewardStrategy() { return rewardStrategy; }
    @StringSetter(REWARD_STRATEGY)
    public void setRewardStrategy(boolean rewardStrategy) { this.rewardStrategy = rewardStrategy; }

    @StringGetter(PENALTY_FACTOR)
    public double getPenaltyFactor() { return penaltyFactor; }
    @StringSetter(PENALTY_FACTOR)
    public void setPenaltyFactor(double penaltyFactor) { this.penaltyFactor = penaltyFactor; }
    @StringGetter(PENALTY_RATIO_THRESHOLD)
    public String getPenaltyRatioThreshold() { return penaltyRatioThreshold; }
    @StringSetter(PENALTY_RATIO_THRESHOLD)
    public void setPenaltyRatioThreshold(String penaltyRatioThreshold) { this.penaltyRatioThreshold = penaltyRatioThreshold; }
    @StringGetter(PENALTY_RATIO_THRESHOLD_FACTOR_A)
    public String getPenaltyRatioThresholdFactorA() { return penaltyRatioThresholdFactorA; }
    @StringSetter(PENALTY_RATIO_THRESHOLD_FACTOR_A)
    public void setPenaltyRatioThresholdFactorA(String penaltyRatioThresholdFactorA) { this.penaltyRatioThresholdFactorA = penaltyRatioThresholdFactorA; }
    @StringGetter(PENALTY_RATIO_THRESHOLD_FACTOR_B)
    public String getPenaltyRatioThresholdFactorB() { return penaltyRatioThresholdFactorB; }
    @StringSetter(PENALTY_RATIO_THRESHOLD_FACTOR_B)
    public void setPenaltyRatioThresholdFactorB(String penaltyRatioThresholdFactorB) { this.penaltyRatioThresholdFactorB = penaltyRatioThresholdFactorB; }
    @StringGetter(PENALTY_RATIO_THRESHOLD_FACTOR_C)
    public String getPenaltyRatioThresholdFactorC() { return penaltyRatioThresholdFactorC; }
    @StringSetter(PENALTY_RATIO_THRESHOLD_FACTOR_C)
    public void setPenaltyRatioThresholdFactorC(String penaltyRatioThresholdFactorC) { this.penaltyRatioThresholdFactorC = penaltyRatioThresholdFactorC; }

    @StringGetter(REWARD_FACTOR)
    public double getRewardFactor() { return rewardFactor; }
    @StringSetter(REWARD_FACTOR)
    public void setRewardFactor(double rewardFactor) { this.rewardFactor = rewardFactor; }
    @StringGetter(REWARD_RATIO_THRESHOLD)
    public String getRewardRatioThreshold() { return rewardRatioThreshold; }
    @StringSetter(REWARD_RATIO_THRESHOLD)
    public void setRewardRatioThreshold(String rewardRatioThreshold) { this.rewardRatioThreshold = rewardRatioThreshold; }
    @StringGetter(REWARD_RATIO_THRESHOLD_FACTOR_A)
    public String getRewardRatioThresholdFactorA() { return rewardRatioThresholdFactorA; }
    @StringSetter(REWARD_RATIO_THRESHOLD_FACTOR_A)
    public void setRewardRatioThresholdFactorA(String rewardRatioThresholdFactorA) { this.rewardRatioThresholdFactorA = rewardRatioThresholdFactorA; }
    @StringGetter(REWARD_RATIO_THRESHOLD_FACTOR_B)
    public String getRewardRatioThresholdFactorB() { return rewardRatioThresholdFactorB; }
    @StringSetter(REWARD_RATIO_THRESHOLD_FACTOR_B)
    public void setRewardRatioThresholdFactorB(String rewardRatioThresholdFactorB) { this.rewardRatioThresholdFactorB = rewardRatioThresholdFactorB; }
    @StringGetter(REWARD_RATIO_THRESHOLD_FACTOR_C)
    public String getRewardRatioThresholdFactorC() { return rewardRatioThresholdFactorC; }
    @StringSetter(REWARD_RATIO_THRESHOLD_FACTOR_C)
    public void setRewardRatioThresholdFactorC(String rewardRatioThresholdFactorC) { this.rewardRatioThresholdFactorC = rewardRatioThresholdFactorC; }

}
