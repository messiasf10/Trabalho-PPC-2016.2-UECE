package messias.filho.ppc.domain;

import java.util.ArrayList;

import messias.filho.ppc.exceptions.AircraftCollidedException;
import messias.filho.ppc.exceptions.AircraftCrashedException;

/**
 * 
 * @author Messias
 *
 */

/* Padrão de Projeto Singleton
*  Garantir que a classe Pista tenha apenas uma instância de si mesma 
*  e que forneça um ponto global de acesso a ela */
public class Pista {

	private static Pista uniqueInstance = new Pista();
	
	public static final int TAMANHO_FILA_ESPERA = 3;

	private ArrayList<Aircraft> filaEspera;	
	private ArrayList<Aircraft> avioesNoAr;
	private boolean pistaOcupada;

	private Pista() {
		this.pistaOcupada = false;
		this.filaEspera = new ArrayList<>();		
		this.avioesNoAr = new ArrayList<>();
	}
	
	/**
	 *  
	 * @return Retorna uma única instância da classe Pista
	 */
	public static Pista getInstance() {
        return uniqueInstance;
    }
	
	public synchronized void aterrissar(Aircraft aircraft) throws InterruptedException, AircraftCrashedException {		
		while(!aircraft.equals(nextAircraftUseTrack())){
			wait();
		}
		
		aircraft.calcularTempoDeEspera();
		
		this.retirarAviaoDoAr(aircraft);
		
		if(aircraft.crashed()){
			notifyAll();
			throw new AircraftCrashedException("\n# AERONAVE (" + aircraft.getIdentificador() + ") CAIU "
					+ "- TEMPO DE ESPERA = " + aircraft.getTempoDeEspera() + "\n");
		}
		
		System.out.println("> AERONAVE (" + aircraft.getIdentificador() + ") COMECOU ATERRISSAGEM [" + aircraft.getHoraInicialAterDec() + "]");
		
		this.pistaOcupada = true;
		Thread.sleep(10000);
		this.pistaOcupada = false;
		
		System.out.println("< AERONAVE (" + aircraft.getIdentificador() + ") ATERRISSOU [" + aircraft.getHoraFinalAterDec() + "]");
		
		System.out.println("+ TEMPO TOTAL DE OPERACAO DO AVIAO (" + aircraft.getIdentificador()
				+ ") [" + aircraft.getTipo() + "] = " + aircraft.calcularTempoTotalOperacao());
		
		this.printFilas();
		
		notifyAll();
	}
	
	public synchronized void decolar(Aircraft aircraft) throws InterruptedException {
		while(!aircraft.equals(nextAircraftUseTrack())){		// prioridade do proximo a pousar	
			wait();
		}
		
		this.pistaOcupada = true;
		if(aircraft.isFirstAircraft()) {
			this.retirarAviaoDaFilaDeEspera(aircraft);
			System.out.println("> AERONAVE (" + aircraft.getIdentificador() + ") COMECOU DECOLAGEM" + "[" + aircraft.getHoraInicialAterDec() + "]");
			aircraft.calcularTempoDeEspera(); // Retirando a aviao da fila de espera quando vai decolar
			Thread.sleep(5000);
		}
		else{
			this.retirarAviaoDaFilaDeEspera(aircraft); // Retirando o aviao da fila de espera pois ele vai decolar daqui a pouco
			System.out.println("> AERONAVE (" + aircraft.getIdentificador() + ") PRONTA PARA DECOLAR. ESPERANDO 5s POR SEGURANCA " + "[" + aircraft.getHoraInicialAterDec() + "]");
			Thread.sleep(5000);
			// this.retirarAviaoDaFilaDeEspera(aircraft); // Retirando o aviao da fila de espera depois dos 5s de espera de seguranca
			System.out.println("> AERONAVE (" + aircraft.getIdentificador() + ") COMECOU DECOLAGEM " + "[" + aircraft.getHoraInicialAterDec() + "]");
			aircraft.calcularTempoDeEspera();
			Thread.sleep(5000);
		}
		this.pistaOcupada = false;
		
		System.out.println("< AERONAVE (" + aircraft.getIdentificador() + ") DECOLOU [" + aircraft.getHoraFinalAterDec() + "]");
		
		System.out.println("+ TEMPO TOTAL DE OPERACAO DO AVIAO (" + aircraft.getIdentificador()
				+ ") [" + aircraft.getTipo() + "] = " + aircraft.calcularTempoTotalOperacao());
		
		this.printFilas();
		
		notifyAll();
	}

	/**
	 * @return Colocar aeronave na fila de espera. Lanca uma excecao caso nao consiga pois a fila esta cheia
	 * @param aircraft
	 * @throws AircraftCollidedException
	 */
	public void colocarAviaoNaFilaDeEspera(Aircraft aircraft) throws AircraftCollidedException {
		if (this.filaEspera.size() >= TAMANHO_FILA_ESPERA)
			throw new AircraftCollidedException("\nHOUVE COLISÃO NA FILA DE ESPERA DE DECOLAGEM - AVIAO (" + aircraft.getIdentificador() + ")\n");
		this.filaEspera.add(aircraft);		
	}
	
	/**
	 * 
	 * @param aircraft
	 * @return Retira aeronaes da fila de espera e retorna true se conseguir false caso contrario
	 */
	public boolean retirarAviaoDaFilaDeEspera(Aircraft aircraft){
		return this.filaEspera.remove(aircraft);
	}

	/**
	 * @return Coloca avioes no ar
	 * @param aircraft
	 */
	public void colocarAviaoNoAr(Aircraft aircraft) {
		this.avioesNoAr.add(aircraft);		
	}
	
	/**
	 * 
	 * @param aircraft
	 * @return Retira avioes do ar e retorna o successo da operacao
	 */
	public boolean retirarAviaoDoAr(Aircraft aircraft){
		return this.avioesNoAr.remove(aircraft);
	}
	
	/**
	 * 
	 * @return Retorna o aviao mais antigo que esta no ar
	 */
	private Aircraft getProximoAPousar() {
		Aircraft aircraftAux = null;
		if (this.avioesNoAr.size() != 0){
			aircraftAux = this.avioesNoAr.get(0);			
		}	
		return aircraftAux;
	}

	/**
	 * 
	 * @return Retorna o aviao mais antigo da fila de espera de decolagem
	 */
	private Aircraft getProximoADecolar() {
		Aircraft aircraftAux = null;
		if (this.filaEspera.size() != 0){
			aircraftAux = this.filaEspera.get(0);
		}
		return aircraftAux;
	}

	private boolean priorityAircraftOnTheGround(){
		if(filaEspera.size() >= 3){
			if(avioesNoAr.size() != 0){
				Aircraft olderAircraftAir = this.getProximoAPousar();
				System.out.println("COMBUSTIVEL AERONAVE (" + olderAircraftAir.getIdentificador() + ") = " + olderAircraftAir.getTempoCombustivelAtual());
				if(olderAircraftAir.getTempoCombustivelAtual() < Aircraft.TEMPO_LIMITE_COMBUSTIVEL_PRIORITARIO)
					return true;
				else {
					System.out.println("PRIORIDADE PRA QUEM VAI POUSAR POR CAUSA DO COMBUSTIVEL LIMITADO"); // do combustivel, prior. para ele
					return false;
				}						
			} else
				return true;
		} else {
			if(avioesNoAr.size() >= filaEspera.size())
				return false;
			else {
				if(avioesNoAr.size() != 0){ // Se nao tiver nenhum aviao em cima, prioridade para os de baixo
					Aircraft olderAircraftAir = this.getProximoAPousar();
					System.out.println("COMBUSTIVEL AERONAVE (" + olderAircraftAir.getIdentificador() + ") = " + olderAircraftAir.getTempoCombustivelAtual());
					if(olderAircraftAir.getTempoCombustivelAtual() < Aircraft.TEMPO_LIMITE_COMBUSTIVEL_PRIORITARIO)
						return true;
					else {
						System.out.println("PRIORIDADE PRA QUEM VAI POUSAR POR CAUSA DO COMBUSTIVEL LIMITADO"); // do combustivel, prior. para ele
						return false;
					}						
				} else 
					return true;								
			}			
		}
	}
	
	private Aircraft nextAircraftUseTrack(){
		Aircraft olderAircraftGround = this.getProximoADecolar();
		if(avioesNoAr.size() != 0){
			Aircraft olderAircraftAir = this.getProximoAPousar();
			if(filaEspera.size() == 2 || filaEspera.size() == 3 ){
				System.out.println("COMBUSTIVEL AERONAVE (" + olderAircraftAir.getIdentificador() + ") = " + olderAircraftAir.getTempoCombustivelAtual());
				if(olderAircraftAir.getTempoCombustivelAtual() < Aircraft.TEMPO_LIMITE_COMBUSTIVEL_PRIORITARIO)
					return olderAircraftGround;
				else {
					System.out.println("PRIORIDADE PRA QUEM VAI POUSAR POR CAUSA DO COMBUSTIVEL LIMITADO"); // do combustivel, prior. para ele
					return olderAircraftAir;
				}				
			} else {				
				if(avioesNoAr.size() > filaEspera.size()) // ou >=
					return olderAircraftAir;
				else {					
					System.out.println("COMBUSTIVEL AERONAVE (" + olderAircraftAir.getIdentificador() + ") = " + olderAircraftAir.getTempoCombustivelAtual());
					if(olderAircraftAir.getTempoCombustivelAtual() < Aircraft.TEMPO_LIMITE_COMBUSTIVEL_PRIORITARIO)
						return olderAircraftGround;
					else {
						System.out.println("PRIORIDADE PRA QUEM VAI POUSAR POR CAUSA DO COMBUSTIVEL LIMITADO"); // do combustivel, prior. para ele
						return olderAircraftAir;
					}
				}
			}			
		} else 
			return olderAircraftGround;				
	}
	
	public void printFilas(){
		System.out.println("\n----------");
		this.printPistaAr();
		this.printPistaChao();
		System.out.println("----------\n");
	}
	
	private void printPistaChao(){
		StringBuffer stout = new StringBuffer();
		stout.append("FILA DE ESPERA = [");
		for(Aircraft a : filaEspera){
			stout.append(" (" + a.getIdentificador() + ") "); 
		}
		stout.append("]");
		System.out.println(stout.toString());
	}
	
	private void printPistaAr(){
		StringBuffer stout = new StringBuffer();
		stout.append("FILA NO AR = [");
		for(Aircraft a : avioesNoAr){
			stout.append(" (" + a.getIdentificador() + ") "); 
		}
		stout.append("]");
		System.out.println(stout.toString());
	}
	

}
