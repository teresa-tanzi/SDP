package casa;

import casa.simulation_src_2019.SmartMeterSimulator;

public class BoostThread extends Thread {
	private SmartMeterSimulator simulator;

	public BoostThread(SmartMeterSimulator simulator) {
		this.simulator = simulator;
	}

	public void run() {
		CasaInfo.getInstance().useBoost(simulator);
	}
}