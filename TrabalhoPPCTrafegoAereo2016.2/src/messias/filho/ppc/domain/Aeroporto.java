package messias.filho.ppc.domain;

import java.util.Random;

import messias.filho.ppc.enums.Tipo;
import messias.filho.ppc.util.Relatorio;

/**
 * 
 * @author Messias
 * 
 */

public class Aeroporto {
	
	// Variavel para controlar o tipo de teste
	public static final boolean TESTE_ALEATORIO = true;
	
	// Variaveis globais de entrada, possiveis de serem alteradas para a realizacao de testes aleatorios
	public static final int NUM_MAX_AVIOES_AR = 10;
	public static final int NUM_MAX_AVIOES_CHAO = 10;
	
	// Variavel para definir o tempo de criacao de aeronaves indepente do tipo do teste
	public static final int TEMPO_CRIACAO_AVIOES = 8;
	
	// Variavies para controlar o teste ordenado
	public static final Tipo A = Tipo.ATERRISSAR;
	public static final Tipo D = Tipo.DECOLAR;
	public static final Tipo[] ARRAY_ORDERED_CREATION_AIRCRAFT = {A,A,D,A,A,A,D,D,A,A,D,D,A,D,D,A,A,D,D,D};
	
	// Variaveis auxiliares para controlar o teste aleatorio 
	public static int contAvioesAr = 0;
	public static int contAvioesChao = 0;
	public static int auxId = 1;

	public static void main(String[] args) {

		Relatorio estatistica = new Relatorio();
		
		Pista pista = Pista.getInstance();

		if(TESTE_ALEATORIO) {
			
			Aircraft aircraftNovo = null;
			
			while (contAvioesAr < NUM_MAX_AVIOES_AR || contAvioesChao < NUM_MAX_AVIOES_CHAO) {

				aircraftNovo = geradorAleatorioDeAviao(auxId, pista);

				estatistica.insertAircraft(aircraftNovo);
				
				if (aircraftNovo != null)
					aircraftNovo.start();

				sleepTimeSeconds(TEMPO_CRIACAO_AVIOES);

			}
			
		} else
			testeOrdenadoCriacaoDeAviao(pista, estatistica);		
		
		estatistica.printAllStatistics();

	}
	
	private static Aircraft geradorAleatorioDeAviao(int id, Pista pista) {
		boolean isInTheAir = new Random().nextBoolean();
		Tipo tipoAviaoAux = null;

		if (contAvioesAr >= NUM_MAX_AVIOES_AR) {
			tipoAviaoAux = Tipo.DECOLAR;
			contAvioesChao++;
		} else if (contAvioesChao >= NUM_MAX_AVIOES_CHAO) {
			tipoAviaoAux = Tipo.ATERRISSAR;
			contAvioesAr++;
		} else {
			if (isInTheAir) {
				tipoAviaoAux = Tipo.ATERRISSAR;
				contAvioesAr++;
			} else {
				tipoAviaoAux = Tipo.DECOLAR;
				contAvioesChao++;
			}
		}

		Aircraft aircraft = new Aircraft(id, tipoAviaoAux);

		if(auxId == 1)
			aircraft.setFirstAircraft(true);
		
		auxId++;

		return aircraft;
	}
	
	private static void testeOrdenadoCriacaoDeAviao(Pista pista, Relatorio estatistica){
		
		for(int i = 0; i < ARRAY_ORDERED_CREATION_AIRCRAFT.length; i++){
			Aircraft aircraft = new Aircraft(i+1, ARRAY_ORDERED_CREATION_AIRCRAFT[i]);
			if(i+1 == 1)
				aircraft.setFirstAircraft(true);
			estatistica.insertAircraft(aircraft);
			aircraft.start();
			sleepTimeSeconds(TEMPO_CRIACAO_AVIOES);
		}
		
	}
	
	private static void sleepTimeSeconds(int tempoSegundos) {
		try {
			Thread.sleep(tempoSegundos * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
