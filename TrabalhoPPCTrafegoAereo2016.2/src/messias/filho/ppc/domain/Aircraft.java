package messias.filho.ppc.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import messias.filho.ppc.enums.Tipo;
import messias.filho.ppc.exceptions.AircraftCollidedException;
import messias.filho.ppc.exceptions.AircraftCrashedException;

/**
 * 
 * @author Messias
 * 
 */

public class Aircraft extends Thread {

	public Pista pista;

	public static final double LIMITE_COMBUSTIVEL = 30;
	public static final double TEMPO_LIMITE_COMBUSTIVEL_PRIORITARIO = 20;
	public static final double LIMITE_OPERACAO = 40;

	private int identificador; // Identificao do aviao
	private Tipo tipo; // ATERRISSAR ou DECOLAR
	private boolean firstAircraft; // Indica se eh o primeiro aviao criado
	private String horaNascimento, horaInicialAterDec, horaFinalAterDec; // Variaveis para o log final
	private long tempoInicioCriacao, tempoInicialAterDec, tempoFinalizacao; // Variaveis auxiliares de tempo
	private double tempoTotalOperacao, tempoDeEspera, tempoCombustivelAtual; // Variaveis de controle e tambem usadas no log final

	public Aircraft(int identificador, Tipo tipo) {
		this.identificador = identificador;
		this.tipo = tipo;
		this.pista = Pista.getInstance();
		this.firstAircraft = false;
		this.tempoInicioCriacao = System.currentTimeMillis();
		System.out.println("\nHORA DA CRIACAO DA AERONAVE (" + identificador + ") - " + tipo + " - [" + getHoraNascimento() + "]");
	}

	@Override
	public void run() {

		if (this.tipo.equals(Tipo.ATERRISSAR)) {
		
			this.pista.colocarAviaoNoAr(this);
			this.pista.printFilas();
			try {
				// Nesse momento, ela tenta aterrissar. Se a pista estiver ocupada ou alguma prioriodade maior for atendida, ela espera
				pista.aterrissar(this); // Lanca uma excecao caso a aeronave caia por falta de combustivel
			} catch (AircraftCrashedException e) {
				System.err.println(e.getMessage());				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		} else {
			
			try {
				this.pista.colocarAviaoNaFilaDeEspera(this); // Tenta colocar o aviao na fila de espera. Lanca uma excecao caso nao consiga				
				this.pista.printFilas();
				this.pista.decolar(this); // Nesse momento, ela tenta decolar. Espera, caso pista ocupada ou alguma prior. maior for atendida				
			} catch (AircraftCollidedException e1) {
				System.err.println(e1.getMessage());
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
				
		}

	}

	// Getter e Setters e Métodos Auxiliares de Controle
	
	public int getIdentificador() {
		return identificador;
	}

	public void setIdentificador(int identificador) {
		this.identificador = identificador;
	}
	
	public boolean isFirstAircraft() {
		return firstAircraft;
	}

	public void setFirstAircraft(boolean firstAircraft) {
		this.firstAircraft = firstAircraft;
	}
	
	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	/**
	 * 
	 * @return Calcula e retorna o tempo (valor) do combustivel atual
	 */
	public double getTempoCombustivelAtual() {
		this.tempoCombustivelAtual = (System.currentTimeMillis() - this.tempoInicioCriacao) / 1000;
		return tempoCombustivelAtual;
	}
	
	/**
	 * 
	 * @return String informando a data e hora de nascimento 
	 */
	public String getHoraNascimento() {
		this.horaNascimento = this.getDateTime();
		return horaNascimento;
	}

	/**
	 * 
	 * @return String informando a data e hora de inicio da aterrissagem ou decolagem
	 */
	public String getHoraInicialAterDec() {
		this.horaInicialAterDec = this.getDateTime();
		return horaInicialAterDec;
	}

	/**
	 * 
	 * @return String informando a data e hora de conclusao da aterrissagem ou decolagem
	 */
	public String getHoraFinalAterDec() {
		this.horaFinalAterDec = this.getDateTime();
		return horaFinalAterDec;
	}

	/**
	 * 
	 * @return Double que guarda o tempo total de espera da aeronave antes de decolar ou aterrissar
	 */
	public double getTempoDeEspera() {
		return tempoDeEspera;
	}

	/**
	 * 
	 * @return Calcula e retorna o tempo total da opracao da aeronave, ou seja, o tempo em que ela foi criada e o tempo que finalizou 
	 */
	public double calcularTempoTotalOperacao() {
		this.tempoFinalizacao = System.currentTimeMillis();
		this.tempoTotalOperacao = (this.tempoFinalizacao - this.tempoInicioCriacao) / 1000;
		return tempoTotalOperacao;
	}
	
	/**
	 * 
	 * @return Calcula e retorna o tempo total de espera da aeronave antes de decolar ou aterrissar
	 */
	public double calcularTempoDeEspera(){
		this.tempoInicialAterDec = System.currentTimeMillis();
		this.tempoDeEspera = (this.tempoInicialAterDec - this.tempoInicioCriacao) / 1000;
		return this.tempoDeEspera;
	}

	/**
	 * 
	 * @return Retorna true indicando que a aeronave caiu e false caso contrario
	 */
	public boolean crashed(){
		return this.tipo.equals(Tipo.ATERRISSAR) && this.tempoDeEspera > LIMITE_COMBUSTIVEL;
	}
	
	/**
	 * 
	 * @return Retorna true indicando que a aeronave colidiu na fila de espera e false caso contrario
	 */
	public boolean collided(){
		return this.tipo.equals(Tipo.DECOLAR) && this.horaInicialAterDec == null;
	}
	
	/**
	 * 
	 * @return String que informa a data e hora do sistema 
	 */
	public String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	public String toStringTipoAbreviado(){
		if(this.tipo.equals(Tipo.ATERRISSAR))
			return "A";
		return "D";
	}
	
	@Override
	public String toString() {
		String stout = 
				"> AERONAVE (" + this.identificador + "):\n"
				+ "\tTipo [" + this.tipo + "]\n"
				+ "\tHora de Nascimento [" + this.horaNascimento + "]\n";
		if(this.tipo.equals(Tipo.ATERRISSAR)){
			if(this.crashed()){
				stout += 
						"\tTempo de Espera [" + this.tempoDeEspera + "] > Combustivel Limitado [" + LIMITE_COMBUSTIVEL + "]\n"
						+ "\tAeronave caiu por falta de combustível\n";
			} else {
				stout += 
						"\tHora de Inicio de Aterrissagem [" + this.horaInicialAterDec + "]\n"
						+ "\tHora de Fim de Aterrissagem [" + this.horaFinalAterDec + "]\n"
						+ "\tTempo de Espera de Aterrisagem [" + this.tempoDeEspera + "]\n";				
			}
		}
		else{
			if(this.collided())
				stout += "\tAeronave colidiu na fila de decolagem\n";
			else 
				stout += 
					"\tHora de Inicio de Decolagem [" + this.horaInicialAterDec + "]\n"
					+ "\tHora de Fim de Decolagem [" + this.horaFinalAterDec + "]\n"
					+ "\tTempo de Espera de Decolagem [" + this.tempoDeEspera + "]\n";
		}
		return stout;
	}

}
