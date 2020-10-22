package org.matsim.smartDrtPricing;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.events.IterationEndsEvent;

import java.io.IOException;

/**
 * @author zmeng
 */
public class SmartTeleportDrtFareControlerListener  extends  SmartDrtFareControlerListener {
    @Inject
    Scenario scenario;
    @Inject
    SmartDrtFareComputation smartDrtFareComputation;
    @Inject
    SmartTeleportDrtFareComputation smartTeleportDrtFareComputation;
    @Inject
    SmartDrtFareConfigGroup smartDrtFareConfigGroup;

    @Override
    public void notifyIterationEnds(IterationEndsEvent iterationEndsEvent) {

        if(iterationEndsEvent.getIteration() == scenario.getConfig().controler().getLastIteration()){
            try {
                smartDrtFareComputation.writeFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                smartTeleportDrtFareComputation.writeFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (iterationEndsEvent.getIteration() % smartDrtFareConfigGroup.getWriteFileInterval() == 0) {
            try {
                smartDrtFareComputation.writeFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                smartTeleportDrtFareComputation.writeFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
