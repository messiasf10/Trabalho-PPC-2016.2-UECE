package messias.filho.ppc.util;

import java.util.ArrayList;

import messias.filho.ppc.domain.Aircraft;

/**
 * 
 * @author Messias
 *
 */

public class Relatorio {

	private ArrayList<Aircraft> aircraftsList;

	public Relatorio() {
		this.aircraftsList = new ArrayList<>();
	}

	public void insertAircraft(Aircraft aircraft) {
		this.aircraftsList.add(aircraft);
	}

	public void printAllStatistics() {
		joinAll();
		String sequencia = "{";
		Aircraft aircraft = null;
		for (int i = 0; i < aircraftsList.size(); i++) {
			aircraft = aircraftsList.get(i);
			if((i+1) != aircraftsList.size())
				sequencia += aircraft.toStringTipoAbreviado() + ",";
			else
				sequencia += aircraft.toStringTipoAbreviado() + "}";
			System.out.println(aircraft.toString());
		}
		System.out.println("> SAÍDA GERADA AUTOMATICAMENTE PARA TESTES ORDENADOS:\n"
				+ "\t" + sequencia);
	}

	private void joinAll() {
		try {
			for (Aircraft aircraft : aircraftsList) {
				aircraft.join();
			}
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
	}

}
