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
import org.matsim.core.controler.AbstractModule;
import org.matsim.smartDrtPricing.ratioThreshold.*;

/**
 * @author : zmeng
 */
public class SmartDrtFareModule extends AbstractModule {
    @Inject
    SmartDrtFareConfigGroup smartDrtFareConfigGroup;
    @Override
    public void install() {
        this.bind(SmartDrtFareComputation.class).asEagerSingleton();

        if(smartDrtFareConfigGroup.getPenaltyRatioThresholdCalculator().equals(SmartDrtFareConfigGroup.RatioCalculator.poly)){
            this.bind(RatioThresholdCalculator.class).annotatedWith(Penalty.class).to(PolynomialRatioThresholdCalculator.class);
        } else if(smartDrtFareConfigGroup.getPenaltyRatioThresholdCalculator().equals(SmartDrtFareConfigGroup.RatioCalculator.exponents)){
            this.bind(RatioThresholdCalculator.class).annotatedWith(Penalty.class).to(NegativeExponentsRatioThresholdCalculator.class);
        }

        if(smartDrtFareConfigGroup.getRewardRatioThresholdCalculator().equals(SmartDrtFareConfigGroup.RatioCalculator.poly)){
            this.bind(RatioThresholdCalculator.class).annotatedWith(Reward.class).to(PolynomialRatioThresholdCalculator.class);
        } else if(smartDrtFareConfigGroup.getRewardRatioThresholdCalculator().equals(SmartDrtFareConfigGroup.RatioCalculator.exponents)){
            this.bind(RatioThresholdCalculator.class).annotatedWith(Reward.class).to(NegativeExponentsRatioThresholdCalculator.class);
        }

        addEventHandlerBinding().to(SmartDrtFareComputation.class);

        if(this.smartDrtFareConfigGroup.getSupportDrtSpeedUp()){
            this.bind(SmartTeleportDrtFareComputation.class).asEagerSingleton();
            addEventHandlerBinding().to(SmartTeleportDrtFareComputation.class);
            addControlerListenerBinding().to(SmartTeleportDrtFareControlerListener.class);
        } else {
            addControlerListenerBinding().to(SmartDrtFareControlerListener.class);
        }
    }
}
