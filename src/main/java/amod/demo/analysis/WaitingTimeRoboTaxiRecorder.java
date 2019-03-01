package amod.demo.analysis;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisElement;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class WaitingTimeRoboTaxiRecorder implements AnalysisElement {

    private Map<Integer, Double> vehicleWaitTime = new HashMap<>();
    private Map<Integer, RoboTaxiStatus> prevStatus = new HashMap<>();
    private Map<Integer, Double> prevvehicleTime = new HashMap<>();
    private Map<Integer, Double> vehicleTime = new HashMap<>();

    @Override
    public void register(SimulationObject simulationObject) {
    	
    	double time = (double) simulationObject.now;

        /** initialize map when first simulationObject arrives */
        if (vehicleWaitTime.size() == 0) {
            simulationObject.vehicles.stream().forEach(vc -> vehicleWaitTime.put(vc.vehicleIndex, 0.0));
            simulationObject.vehicles.stream().forEach(vc -> prevStatus.put(vc.vehicleIndex, RoboTaxiStatus.STAY));
            simulationObject.vehicles.stream().forEach(vc -> prevvehicleTime.put(vc.vehicleIndex, 0.0));
            simulationObject.vehicles.stream().forEach(vc -> vehicleTime.put(vc.vehicleIndex, 0.0));
        }

        simulationObject.vehicles.stream().forEach(vc -> //
        {
            int vehicle = vc.vehicleIndex;
            if (vc.roboTaxiStatus.equals(RoboTaxiStatus.WAITING) //
                    && (!prevStatus.get(vehicle).equals(RoboTaxiStatus.WAITING))) {
            	prevvehicleTime.put(vehicle, time);
            }
            if ((!vc.roboTaxiStatus.equals(RoboTaxiStatus.WAITING)) //
                    && (prevStatus.get(vehicle).equals(RoboTaxiStatus.WAITING))) {
            	vehicleTime.put(vehicle, time);
            	Double waitTime = vehicleTime.get(vehicle) - prevvehicleTime.get(vehicle);
            	if(vehicleWaitTime.get(vehicle) < waitTime)
            		vehicleWaitTime.put(vehicle, waitTime);
            }
        });

        /** add previous status */
        simulationObject.vehicles.stream().forEach(vc -> //
        {
            prevStatus.put(vc.vehicleIndex, vc.roboTaxiStatus);

        });

    }

    /** @return {@link Tensor} containing the requests served for each {@link RoboTaxi} */
    public Tensor getWaitTimePerRoboTaxi() {
        Tensor waitTimes = Tensors.empty();
        vehicleWaitTime.values().stream().forEach(time -> waitTimes.append(RealScalar.of(time)));
        return waitTimes;
    }

}