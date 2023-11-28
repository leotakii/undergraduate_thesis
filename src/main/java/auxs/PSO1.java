package auxs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

import org.apache.commons.collections.list.SetUniqueList;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import taxi.od.solver.Mapping;
import taxi.od.solver.ODMatrix;
//import taxi.od.solver.TaxiODSolver;
//import static taxi.od.solver.TaxiODSolver.horaAtual;
import taxi.od.solver.VirtualSensors;

/**
 * * @author luciano
 * @version 5.6
 */
public class PSO1 {

	//ALERTA: VERIFICAR "solucao alternativa", caso função de custo esteja muito lenta
	boolean debug = true;
	private String selectionGA,crossoverGA,mutationGA;
	private String generationReplacement; //"Generational" or "SteadyState"

	Random random;
	String nomeTeste;
	private double elitistRateGA;
	static String VirtualSensorsFile;
	private boolean salvandoBackup = false;
	
	//TODO Fazer configuracoes no iRace com estes booleanos
	private boolean sensoresFluxoNotRotas = false;
	private boolean sensoresMaisRotas = true;
	private boolean sensoresCorrelacao = false;
	////////////////////////////////////////////////
	
	private String funcaoFitness = "geh"; // geh, reg ou folga  (25,
	
	private Mapping mape;
	
	boolean probArestaODdefinidas = true;
	boolean warmStartup = false;
	private boolean testeRedeFechada = false;
	boolean juncaoProbArestas = false;
	
	String functionProgressStatistics = "";
	String metricProgressStatisticsGEH = "";
	String metricProgressStatisticspGEH5 = "";
	String metricProgressStatisticsR2Links = "";

	double wIn = 0.43;
	double w;
	double wF = 0.12;
	double c1 = 0.59; //local
	double c2 = 1.39; //global
	int reset = 26;
	int contReset = 0;
	double resetPerct = 0.99;
	
	
	int POPULATION_SIZE;
	int MAX_EVALUATIONS;
	
	double CROSSOVER_PROBABILITY ; //0.44
	double MUTATION_PROBABILITY ;//0.34;
	private double varMutacao = 0.57;
	
	double DIFFERENTIAL_WEIGHT;

	private int max, min;
	static double[][] posMutantPart;
	
	static double[][] posPart;       // posições atuais das particulas
	static double[][] velPart;    // velocidades das particulas
	static double[][] localBest;     // melhor posição já alcançada pela particula
	public static int globalBest;         // Qual particula já alcançou a melhor posição
	static int N;                  // Tamanho do problema
	static double[] menorCustoLocal; // custo da melhor posição já encontrada pela particula, auxilia calculos
	private ODMatrix ODmatrix;
	// private Mapping map;
	private VirtualSensors virtualSensors;
	private int batchPriori = 0;
	private int tempoPriori;
	private int tempoProblema;
	private int batchProblema = 0;
	private int arestasSensor = -1;
	//private int[] sensorNodeFrom;
	//private int[] sensorNodeTo;
	private int[] sensorAresta;
	private int clusters;

	private Double[] mae;
	private Double[] rmse;
	private Double[] fitness;
	private Double[] maeIn;
	private Double[] rmseIn;
	private Double[] fitnessIn;
	private Double[] gehIn;
	private Double[] geh;
	private Double[] percGeh;
	private Double[] r2links;
	private Double[] r2odm;

	private double[] bestMOD;
	private double fitnessBestMod;
	private boolean[] parODcoberto;

	private ArrayList<Integer>[][] ODparIndArestaMaisMov;
	private String OSMFileLocation;

	private boolean useMatrixPriori;
	private boolean useVariance;
	private ArrayList<Integer>[][] ODparIndArestaMaisMovENCONTRADOS;

	private boolean testeParametros = false;
	private int discretTemporal;

	private ArrayList<Integer> doParOD = new ArrayList<>();
	private ArrayList<Integer> daAresta = new ArrayList<>();
	private ArrayList<Integer> doIndiceSensor = new ArrayList<>();
	private ArrayList<Double> prob_od_a = new ArrayList<>();
	private int[] doParODV;
	private int[] daArestaV;
	private int[] doIndiceSensorV;
	private double[] prob_od_aV;

	private double maiorProbabilidade;

	ArrayList<String> kinds = new ArrayList<>();
	ArrayList<Double> minKind = new ArrayList<>();
	ArrayList<Double> maxKind = new ArrayList<>();
	private ArrayList<Integer>[][] ODparIndArestaMaisMov2;
	private ArrayList<Integer>[][] ODparIndArestaMaisMovENCONTRADOS2;
	private int tempoPriori2;
	private ArrayList[][] ODparIndArestaMaisMovENCONTRADOSindiceAresta;
	private ArrayList[][] ODparIndArestaMaisMovENCONTRADOS2indiceAresta;

	public long getTempoExec() {
		return tempo;
	}
	long tempo;
	

	
	public double runParametersOnPSO(String funcaoFitness, int runs, ODMatrix odm, VirtualSensors vts,
			int populationSize, int maxEvaluations, double c1, double c2, double wIn, double wF, int itReset, double pReset, int tprior, int numSensores, Random random) {

		ArrayList<Double> results = new ArrayList<>();
		ODmatrix = odm;
		tempoPriori = tprior;
		tempoProblema = tprior;
		virtualSensors = vts;
		arestasSensor = numSensores;
		long t = System.nanoTime();
		//setar parâmetros
		
		POPULATION_SIZE = populationSize;
		MAX_EVALUATIONS = maxEvaluations;
		this.wIn = wIn;
		this.wF = wF;
		this.c1 = c1;
		this.c2 = c2;
		this.reset = itReset;
		this.resetPerct = pReset;
		this.funcaoFitness = funcaoFitness;
		this.random = random;
		double result;
		result = runPSO(ODmatrix, virtualSensors, odm.getNumeroClusters(), tempoPriori, tempoProblema, arestasSensor, runs); //(ODmatrix, virtualSensors, clusters, tempoPriori, tempoProblema, arestasSensor, min)

	/*	for (int r = 0; r < runs; r++) {
			results.add(geh[r]);
		}
*/
		tempo = System.nanoTime() - t;
		return result;
	}
	
	public double runParametersOnDE(String funcaoFitness, int runs, ODMatrix odm, VirtualSensors vts,
			int populationSize, int maxEvaluations, double differentialWeight, double crossoverRate, int numSensores, int tempoPriorix, int tempoProbl, Random random) {
		//rnGA(ODMatrix ODmatrix1, VirtualSensors virtualSensors1,
		//int clusters, int tempoPriori, int batchPriori, int tempoProblema, int batchProblema, int arestasSensor, int runs) 
		ODmatrix = odm;
		virtualSensors = vts;
		arestasSensor = numSensores;
		tempo = System.nanoTime();
		
		ArrayList<Double> results = new ArrayList<>();
		
		tempoPriori = tempoPriorix;
		tempoProblema = tempoProbl;
		long t = System.nanoTime();
		//setar parâmetros
		this.random = random;
		MAX_EVALUATIONS = maxEvaluations;
		POPULATION_SIZE = populationSize;
		this.DIFFERENTIAL_WEIGHT = differentialWeight;
		this.funcaoFitness = funcaoFitness;
		this.CROSSOVER_PROBABILITY = crossoverRate;
		
		double result = runDE(ODmatrix, virtualSensors, odm.getNumeroClusters(), tempoPriori, tempoProblema, arestasSensor, runs);
		
		/*results = runDE(ODmatrix, virtualSensors, odm.getNumeroClusters(), tempoPriori, tempoProblema, arestasSensor, runs); //(ODmatrix, virtualSensors, clusters, tempoPriori, tempoProblema, arestasSensor, min)
		 for (int r = 0; r < runs; r++) {
				results.add(geh[r]);
			}
		 */
		tempo = System.nanoTime() - t;
		return result;
	}

	public double runParametersOnGA(String generationReplacement, String selectionGA, String crossoverGA, String mutationGA, double elitistRateGA, String funcaoFitness, int runs, ODMatrix odm, VirtualSensors vts,
			int pop, int geracoes, double mutationRate, double crossoverRate, int numSensores, int tempoPriorix, int tempoProbl, Random random) {
		ArrayList<Double> results = new ArrayList<>();
		//rnGA(ODMatrix ODmatrix1, VirtualSensors virtualSensors1,
		//int clusters, int tempoPriori, int batchPriori, int tempoProblema, int batchProblema, int arestasSensor, int runs) 
		ODmatrix = odm;
		virtualSensors = vts;
		arestasSensor = numSensores;
		tempo = System.nanoTime();

		tempoPriori = tempoPriorix;
		tempoProblema = tempoProbl;
		long t = System.nanoTime();
		//setar parâmetros
		//this.MAX_GERACOES = geracoes;
		//this.pop = pop;
		
		this.random = random;
		
		this.generationReplacement = generationReplacement;
		this.selectionGA = selectionGA;
		this.crossoverGA = crossoverGA;
		this.mutationGA = mutationGA;
		this.elitistRateGA = elitistRateGA;
		this.funcaoFitness = funcaoFitness;
		POPULATION_SIZE = pop;
		MAX_EVALUATIONS = geracoes;
		
		MUTATION_PROBABILITY = mutationRate;
		CROSSOVER_PROBABILITY = crossoverRate;
		//MUTATION_PROBABILITY = mutationRate;
		//CROSSOVER_PROBABILITY = crossoverRate;
		double result;
		
		
		//result = runGAOneMutation(ODmatrix, virtualSensors, odm.getNumeroClusters(), tempoPriori, tempoProblema, arestasSensor, runs);

		result = runGACorrigido(ODmatrix, virtualSensors, odm.getNumeroClusters(), tempoPriori, tempoProblema, arestasSensor, runs);

		//result = runGARandomMutations(ODmatrix, virtualSensors, odm.getNumeroClusters(), tempoPriori, tempoProblema, arestasSensor, runs);

		/*		for (int r = 0; r < runs; r++) {
			results.add(geh[r]);
		}
*/
		tempo = System.nanoTime() - t;
		return result;
	}



	public void gerarCodPLGusek(int numSens, int tempoPriori1, int tempoProblema1, int batch1, VirtualSensors vt, ODMatrix odm, Mapping map) {

		if (testeRedeFechada) {
			tempoPriori1 = 0;
			tempoProblema1 = 0;
			batch1 = 0;
			sensoresFluxoNotRotas = true;
			sensoresMaisRotas = false;
			sensoresCorrelacao = false;
			if (numSens > 71) {
				numSens = 71;
				arestasSensor = 71;
				System.out.println("ATENCAO: arestasSensor reduzidas para " + arestasSensor + ", por ser numero maximo de sensores da rede cadastrada.");
			}
		}


	}

	public String bestMODToString() {
		String s = "";
		for (int cx1 = 0; cx1 < clusters; cx1++) {
			for (int cx2 = 0; cx2 < clusters; cx2++) {
				s = s + bestMOD[cx1 * clusters + cx2] + ", ";
			}
			s = s + "\n";
		}
		return s;
	}
	
	public String probODToString() {
		String s = "";
		for (int cx1 = 0; cx1 < clusters; cx1++) {
			for (int cx2 = 0; cx2 < clusters; cx2++) {
				s = s + prob_od_aV[cx1 * clusters + cx2] + ", ";
			}
			s = s + "\n";
		}
		return s;
	}

	public String maeRmseGehToString() {
		String s = "";
		for (int a = 0; a < mae.length; a++) {
			s = s + mae[a] + ", " + rmse[a] + ", " + geh[a] + "\n";
			//Versao com apenas geh :  s = s + geh[a] + "\n";
		}
		return s.substring(0, s.length() - 2);
	}

	public void digerirPLCadastrado(int hora, int sensors, VirtualSensors vt, ODMatrix odm, int runs) {

		ArrayList<String> results = lerTxt("GUSEK\\resultados.txt");

		for (int r = 0; r < results.size(); r++) {
			String solution = results.get(r);
			solution = solution.replace("   ", " ");
			solution = solution.replace("  ", " ");
			String[] div = solution.split(" ");
			int tempoProblema1 = Integer.valueOf(div[1]);
			int arestasSensor1 = Integer.valueOf(div[2]);

			if (tempoProblema1 == hora && sensors == arestasSensor1) {
				digerirResultadosPL(solution, vt, odm, runs);
				return;
			}

		}
		System.out.println("ERROR: Nao ha resultados salvos para MILP t" + hora + " e " + sensors + " sensores em GUSEK\\resultados.txt");

	}

	public ArrayList<String> lerTxt(String file) {

		ArrayList<String> resp = new ArrayList<>();
		try {
			Scanner scanner = new Scanner(new File(file));
			scanner.useDelimiter("\n");

			while (scanner.hasNext()) {

				resp.add(scanner.next());
			}

		} catch (FileNotFoundException ex) {
			// Logger.getLogger(TaxiODSolver.class.getName()).log(Level.SEVERE, null, ex);
		}
		return resp;
	}

	public void digerirResultadosPL(String solution, VirtualSensors virtualSensors1, ODMatrix ODmatrix1, int runs) {

		//recuperar valores da String (tempoProblema, tempoPriori, sensores
		solution = solution.replace("   ", " ");
		solution = solution.replace("  ", " ");
		String[] div = solution.split(" ");

		tempoPriori = Integer.valueOf(div[0]);
		tempoPriori2 = tempoPriori;
		clusters = ODmatrix1.getNumeroClusters();
		//System.out.println("String " + div[0] + " vira Integer " + Integer.valueOf(div[0]));
		tempoProblema = Integer.valueOf(div[1]);
		//System.out.println("String " + div[1] + " vira Integer " + Integer.valueOf(div[1]));
		arestasSensor = Integer.valueOf(div[2]);
		//System.out.println("String " + div[2] + " vira Integer " + Integer.valueOf(div[2]));

		nomeTeste = "MILP_" + (arestasSensor) + "s_";


		nomeTeste = nomeTeste + "_" + tempoPriori + "t" + tempoProblema;

		System.out.println("PROC: Digerindo " + nomeTeste + "... " + horaAtual());

		N = clusters * clusters;
		parODcoberto = new boolean[clusters * clusters];
		for (int c = 0; c < clusters * clusters; c++) {
			parODcoberto[c] = false;
		}

		mae = new Double[runs];
		rmse = new Double[runs];
		fitness = new Double[runs];
		maeIn = new Double[runs];
		gehIn = new Double[runs];
		geh = new Double[runs];
		percGeh = new Double[runs];
		r2links = new Double[runs];
		r2odm = new Double[runs];
		rmseIn = new Double[runs];
		fitnessIn = new Double[runs];

		this.virtualSensors = virtualSensors1;
		this.ODmatrix = ODmatrix1;
		ODmatrix.calcMatrixClust();
		ODmatrix.calcVarianciaODMatrix();

		virtualSensors.calcVarianciaArestas();
		//this.clusters = clusters;

		definirArestas_NSLP();

		descobrirODparIndArestaMaisMov(); //ODparIndAresta(indice geral) -> (indice vetor sensorAresta)


		if (!probArestaODdefinidas) {
			/*this.definirVariaveisProbabilidadeArestaRota(clusters);
            posPart = new double[POPULATION_SIZE][N + doParOD.size()];
            velPart = new double[POPULATION_SIZE][N + doParOD.size()];
            localBest = new double[POPULATION_SIZE][N + doParOD.size()];
            virtualSensors = encontrarMinMaxPorTipoAresta(virtualSensors);*/
		} else {
			definirAssignmentMatrix2(tempoPriori);
			posPart = new double[POPULATION_SIZE][N];
			velPart = new double[POPULATION_SIZE][N];
			localBest = new double[POPULATION_SIZE][N];
		}

		double[][] sensores = new double[arestasSensor][1];
		localBest = new double[20][N];
		//this.iniciaParts(0,0, false);

		minInicio = this.getMinutosAtual();

		String tempo = horaAtual().replace(",", "") + ", " + runs + ", ";
		String resumo = 0 + "," + 0 + ", " + N + ", " + arestasSensor + ", ";

		System.out.println("OK: Iniciando MILP Digest!" + resumo + nomeTeste + "; " + horaAtual());

		//preencher matrizA  (probabilidade de uso de aresta)
		/*for (int o = 0; o < clusters; o++) {
            for (int d = 0; d < clusters; d++) {
                for (int ars = 0; ars < ODparIndArestaMaisMov[o][d].size(); ars++) {
                    if (ODparIndArestaMaisMovENCONTRADOS[o][d].contains(ars)) {
                        matrizA[ODparIndArestaMaisMov[o][d].get(ars)][o * clusters + d]
                                = ODmatrix.getODParArestaCont(o, d, tempoPriori, ars);
                    }
                }
            }
        }*/
		//preencher matriz de sensores  (quantidade por sensor)
		for (int a = 0; a < arestasSensor; a++) {
			sensores[a][0] = virtualSensors.getContArestaBatch(sensorAresta[a], tempoProblema, batchProblema);
		}

		bestMOD = new double[clusters * clusters];

		//avalia qualidade da solução
		globalBest = 0;
		System.out.println("localBest[].lenght = " + localBest[globalBest].length + "; div[].lenght = " + div.length);
		for (int a = 0; a < N; a++) {
			localBest[globalBest][a] = Double.valueOf(div[3 + a]);
			posPart[globalBest][a] = Double.valueOf(div[3 + a]);
			bestMOD[a] = Double.valueOf(div[3 + a]);
		}


		geh[0] = this.calcGEH(true);
		gehIn[0] = geh[0];
		percGeh[0] = this.percGehAbaixo5;
		r2links[0] = r2Global;
		r2odm[0] = this.calcR2Odm();
		rmse[0] = this.calcRMSE();
		rmseIn[0] = rmse[0];
		mae[0] = this.calcMAE();
		maeIn[0] = mae[0];
		fitness[0] = 0.0;
		fitnessIn[0] = 0.0;

		for(int c=1;c<mae.length;c++){
			geh[c] = geh[0];
			gehIn[c] = gehIn[0];
			percGeh[c] = percGeh[0];
			r2links[c] = r2links[0];
			r2odm[c] = r2odm[0];
			rmse[c] = rmse[0];
			rmseIn[c] = rmseIn[0];
			mae[c] = mae[0];
			maeIn[c] = maeIn[0];
			fitness[c] = 0.0;
			fitnessIn[c] = 0.0;
		}



		//salva resultado
		minInicio = (((double) getMinutosAtual()) - minInicio) / ((double) runs);
		tempo = tempo + horaAtual().replace(",", "") + ", " + minInicio;
		System.out.println("RUN  " + nomeTeste + " " + (1) + "/" + runs + printMetricasFinais(0) + " \n");



	}

	private String printMetricasFinais(int rodada) {
		//se algum valor for invalido
		if( !(Double.isFinite(rmse[rodada]) &&
				Double.isFinite(mae[rodada])&&
				Double.isFinite(geh[rodada])&&
				Double.isFinite(percGeh[rodada])&&
				Double.isFinite(r2links[rodada])&&
				Double.isFinite(r2odm[rodada]) ) ) {
			fitness[rodada] = 999999999.0;
			this.fitnessBestMod = 999999999.0;
			
			return "\n---"+ horaAtual().replace(",", "")+"------------------"
			+ "-----------------------------------\n"
			+ "ERRO: VALORES NÃO FINITOS! TORNANDO FITNESS GRANDE!\n"
			+ getFuncaoFitness()+"(objetivo): " + fitness[rodada] + "\n"
			+ "RMSE: " + rmse[rodada] +"\n"
			+ "MAE: " + mae[rodada] + "\n"
			+ "GEH: " + geh[rodada] + "\n"
			+ "pGEH5: " + percGeh[rodada] + "\n"
			+ "R2Links: " + r2links[rodada] + "\n"
			+ "R2Odm: " + r2odm[rodada] + "\n"
			+ "------------------------------------------------------" ;
		}
		this.metricProgressStatisticsGEH = geh[rodada].toString();
		this.metricProgressStatisticspGEH5 = percGeh[rodada].toString();
		this.metricProgressStatisticsR2Links = r2links[rodada].toString();
		return "\n---"+ horaAtual().replace(",", "")+"------------------"
		+ "-----------------------------------\n"
		+ getFuncaoFitness()+"(objetivo): " + fitness[rodada] + "\n"
		+ "RMSE: " + rmse[rodada] +"\n"
		+ "MAE: " + mae[rodada] + "\n"
		+ "GEH: " + geh[rodada].toString() + "\n"
		+ "pGEH5: " + percGeh[rodada].toString() + "\n"
		+ "R2Links: " + r2links[rodada].toString() + "\n"
		+ "R2Odm: " + r2odm[rodada] + "\n"
		+ "------------------------------------------------------" ;
		
		
		
	}



	public PSO1(String VirtualSensorsFil, int discretT) {
		VirtualSensorsFile = VirtualSensorsFil;
		discretTemporal = discretT;
	}



	/* private void definirAssignmentMatrix(int t1, int t2) {

        //sensorAresta;
        for (int o = 0; o < clusters; o++) {
            for (int d = 0; d < clusters; d++) //para todos os pares OD
            {
                for (int ars = 0; ars < ODparIndArestaMaisMovENCONTRADOS[o][d].size(); ars++) {

                    doParOD.add(o * clusters + d);
                    //daAresta.add(ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars));
                    int ax = -1;
                    for (int x = 0; x < arestasSensor; x++) //sensorAresta tem indices das arestas mais movimentadas. Se encontrar tal aresta no vetor arestas do par OD
                    {
                        if (sensorAresta[x] == ODmatrix.getODparArestaIndexGeralAresta(o, d, discretTemporal, ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars))) {
                            ax = x;
                            x = arestasSensor; //
                        }
                    }

                    daAresta.add(ax);    // daAresta é o índice para fluxosSens. Do tipo: 0 para a aresta mais movimentada. 1 para a segunda mais movimentada
                    prob_od_a.add(ODmatrix.getODParArestaCont(o, d, discretTemporal, ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars)));

                }
            }
        }

        System.out.println("INFO: Existem " + prob_od_a.size() + " relações Prob_OD_aresta!");

    }*/
	private void definirAssignmentMatrix2(int t1) {

		if (clusters == 0) {
			System.out.println("ERROR: Numero de clusters indefinido! (definirAssignmentMatrix2)");
		}
		if (sensorAresta.length == 0) {
			System.out.println("ERROR: Vetor de sensores indefinido! (definirAssignmentMatrix2)");
		}

		doParOD = new ArrayList<>();
		doIndiceSensor = new ArrayList<>();
		daAresta = new ArrayList<>();
		prob_od_a = new ArrayList<>();

		//sensorAresta;
		for (int o = 0; o < clusters; o++) {
			for (int d = 0; d < clusters; d++) //para todos os pares OD
			{
				for (int ars = 0; ars < sensorAresta.length; ars++) {

					if (ODmatrix.getProbUsoArestaPorParODeIndexAresta(o, d, t1, sensorAresta[ars]) > 0.0) {

						doParOD.add(o * clusters + d);
						daAresta.add(sensorAresta[ars]);
						doIndiceSensor.add(ars);
						prob_od_a.add(ODmatrix.getProbUsoArestaPorParODeIndexAresta(o, d, t1, sensorAresta[ars]));

					}

				}
			}
		}

		doParODV = new int[doParOD.size()];
		doIndiceSensorV = new int[doParOD.size()];
		daArestaV = new int[doParOD.size()];
		prob_od_aV = new double[doParOD.size()];

		for (int x = 0; x < doParOD.size(); x++) {
			doParODV[x] = doParOD.get(x);
			doIndiceSensorV[x] = doIndiceSensor.get(x);
			daArestaV[x] = daAresta.get(x);
			prob_od_aV[x] = prob_od_a.get(x);
		}

		System.out.println("INFO: Existem " + prob_od_a.size() + " relações Prob_OD_aresta!");

	}

	private VirtualSensors encontrarMinMaxPorTipoAresta(VirtualSensors vt) {

		System.out.println("PROC: Calculando faixas de valores para tipo de aresta..." + horaAtual());

		ArrayList<DescriptiveStatistics> sts = new ArrayList<>();
		ArrayList<String> kinds = new ArrayList<>();
		int index;

		//passa por todas as arestas
		for (int a = 0; a < vt.getFromNodCod().size(); a++) {

			index = kinds.indexOf(vt.getArestaKind(a));

			//se não existe esse tipo cadastrado
			if (index == -1) {

				kinds.add(vt.getArestaKind(a));
				sts.add(new DescriptiveStatistics());
				index = kinds.size() - 1;

			}//else{    
				//se já existe esse tipo cadastrado
			// }

			//para todos os pares O-D que passam pela aresta
			for (int o = 0; o < clusters; o++) {
				for (int d = 0; d < clusters; d++) {
					if (ODparIndArestaMaisMovENCONTRADOS[o][d].contains(a))//se par OD passa pela aresta A
					{
						sts.get(index).addValue(ODmatrix.getODParArestaCont(o, d, tempoPriori, a)); //adiciona porcentagem da aresta A
					}
				}
			}
			vt.setIndiceArestaKind(a, index);

		} // fim do laço de arestas

		for (int k = 0; k < kinds.size(); k++) { //encontra 
			minKind.add(sts.get(k).getPercentile(1));
			maxKind.add(sts.get(k).getPercentile(99));
		}

		vt.salvarDat(VirtualSensorsFile);

		return vt;
	}

	private void definirVariaveisProbabilidadeArestaRota(int clusters) {

		//ODmatrix.getODParArestaCont(o, d, ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars))
		for (int o = 0; o < clusters; o++) {
			for (int d = 0; d < clusters; d++) {
				//por par origem destino
				DescriptiveStatistics dx = new DescriptiveStatistics();
				for (int ars = 0; ars < ODparIndArestaMaisMovENCONTRADOS[o][d].size(); ars++) {
					dx.addValue(ODmatrix.getODParArestaCont(o, d, tempoPriori, ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars)));
				}

				double minimo = dx.getPercentile(25); //define mínimo para ser relevante

				for (int ars = 0; ars < ODparIndArestaMaisMovENCONTRADOS[o][d].size(); ars++) {
					if (ODmatrix.getODParArestaCont(o, d, tempoPriori, ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars)) > minimo) {  //se fator da aresta

						doParOD.add(o * clusters + d);

						int ax = -1;
						for (int x = 0; x < arestasSensor; x++) //sensorAresta tem indices das arestas mais movimentadas. Se encontrar tal aresta no vetor arestas do par OD
						{
							if (sensorAresta[x] == ODmatrix.getODparArestaIndexGeralAresta(o, d, discretTemporal, ODparIndArestaMaisMov[o][d].get(ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars)))) {
								ax = x;
								x = arestasSensor; //
							}
						}

						daAresta.add(ax);
						//daAresta.add(ODparIndArestaMaisMov[o][d].get(ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars)));

						if (ODmatrix.getODParArestaCont(o, d, tempoPriori, ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars)) > maiorProbabilidade) {
							maiorProbabilidade = ODmatrix.getODParArestaCont(o, d, tempoPriori, ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars));
						}

					}
				}

			}
		}

		System.out.println("OK: Total de " + doParOD.size() + " probabilidades de aresta/par_o-d");
	}

	

	public void setUseMatrixPriori(boolean b) {
		useMatrixPriori = b;
	}

	private double minInicio;
	

 public void testBatchesOptions(ODMatrix ODmatrix) {

		int sum;
		clusters = ODmatrix.getNumeroClusters();
		System.out.println("N = clusters^2 = " + N);
		N = clusters * clusters;
		double[] respMOD = new double[N];

		for (int t = 6; t < 19; t++) {
			for (int b = 0; b < 6; b++) {
				for (int o = 0; o < clusters; o++) {
					for (int d = 0; d < clusters; d++) {

						respMOD[o * clusters + d] = ODmatrix.getODMatrixClustersBatch(o, d, t, b);

					}
				}

				sum = 0;
				for (int i = 0; i < N; i++) {
					sum += respMOD[i];
				}
				System.out.println("T=" + t + "; B=" + b + "> Sum: " + sum + ";Med " + ((double) sum / (double) N));

			}
		}

	}

	
	

	boolean algebParaWarmUp = false;
	
	public double calculaCustoComProbArestasDEMutante(int part, boolean calcGeh, boolean gerarScatter, boolean isMutante) {

		/*if (debug && part == 0) {System.out.println("PROC: Iniciando calculo de fitness... " + horaAtual());}*/
		double population[][];
		if (isMutante) {
			population = posMutantPart;
		}
		
		else {
			population = posPart;
		}
		
		double custoSoma1 = 0.0;
		double custoSoma2 = 0.0;
		double gehX = 0.0;
		double m, c;
		int id1, id2;
		int probs = doParOD.size();
		// calcGeh = true;

		//int tempoProri, int batchPriori, int tempoProblema, int batchProblema;
		double[] fluxosSens = new double[arestasSensor];
		//sensorAresta[], sensorNodeFrom[], sensorNodeTo[]
		double[] real = new double[arestasSensor];

		int reserva = -1;
		if (juncaoProbArestas) {
			reserva = tempoPriori;
			tempoPriori = discretTemporal;
		}

		//solucao alternativa 
		if (!calcGeh) {
			for (int z = 0; ++z < probs;) {
				//fluxosSens[daAresta.get(z)] += posPart[part][doParOD.get(z)] * prob_od_a.get(z);
				//fluxosSens[doIndiceSensor.get(z)] += posPart[part][doParOD.get(z)] * prob_od_a.get(z);
				fluxosSens[doIndiceSensorV[z]] += population[part][doParODV[z]] * prob_od_aV[z];
			}
		} else {
			percGehAbaixo5 = 0;
			for (int z = 0; ++z < probs;) {
				//fluxosSens[daAresta.get(z)] += posPart[part][doParOD.get(z)] * prob_od_a.get(z);
				//fluxosSens[doIndiceSensor.get(z)] += posPart[part][doParOD.get(z)] * prob_od_a.get(z);
				if (population[part][doParODV[z]] > 0) {
					fluxosSens[doIndiceSensorV[z]] += population[part][doParODV[z]] * prob_od_aV[z];
				}
			}
		}
		if (juncaoProbArestas) {
			tempoPriori = reserva;
		}

		
		//todos os pontos OD  (estimado - priori)^2
		if (useMatrixPriori) {
			for (int n = 0; n < N; n++) {
				id2 = n % clusters;
				id1 = (n - id2) / clusters;

			
				if (useVariance) {
					custoSoma1 += ((population[part][n] - ((double) ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori)))
							* (population[part][n] - ((double) ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori))))
							/ (ODmatrix.getODMatrixClusterVariance(id1, id2, tempoProblema) * 1000);
				} else {
					
					double deltaV = (population[part][n] - ((double) ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori)));
					
					custoSoma1 += (deltaV * deltaV)  / 1000;

				}

			}

		}

		//GEH = SQRT ( 2(M-C)^2 / (M+C) )
		
		double auz;

		double folga = 0;

		//todos os fluxos  (variancia aresta Tproblema)*(consequência - virtualSensors)^2
		for (int ar = 0; ar < arestasSensor; ar++) {

			/*if(part==0){
                System.out.println("(fluxosSens["+ar+"] - vtSnr.getContArestaBatch(sensorAresta["+ar+"], tPriori, bPriori)) ^2 ="
                        + " ("+fluxosSens[ar]+" - "+virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori)+")^2  "
                                + " = " + ((fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori))* (fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori)) ));             
            } */
			if (useVariance) {
				custoSoma2 += mod(((fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema))
						* (fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema)))
						/ (virtualSensors.getArestaVariance(sensorAresta[ar], tempoProblema) * 1000));
				
				
			} else if(getFuncaoFitness().equals("reg")) {
				double modDeltaV = Math.abs(fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema));
				custoSoma2 += (modDeltaV * modDeltaV) / 1000;
			}

			if (calcGeh || getFuncaoFitness().equals("geh")) {
				
				//calculo de GEH(z)

				m = fluxosSens[ar];
				c = virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema);
				double mMinusc = m - c;
				double mPlusc = m + c;
				
				if (mPlusc > 0) {
					auz = Math.sqrt((2 * mMinusc * mMinusc) / mPlusc);
					gehX = gehX + auz;

				}
				real[ar] = c;

			} else if (getFuncaoFitness().equals("folga")){

				folga = folga + Math.abs(fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema));

			}

			if (fluxosSens[ar] < 0) {
				System.out.println("ERRO: fluxoSens[" + ar + "]=" + fluxosSens[ar]);
			}
			if (virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema) < 0) {
				System.out.println("ERRO: virtualSensors.getContArestaBatch(sensorAresta[" + ar + "])=" + fluxosSens[ar]);
			}

		}


		// System.out.println("(p="+part+") ODM E:  "+(custoSoma1/N)+"; Fluxos E: "+(custoSoma2/arestasSensor)+" ps. mudar linha 280");
		//return (custoSoma1 / N + custoSoma2 / arestasSensor);


		if (calcGeh || getFuncaoFitness().equals("geh")) {
			
			//somatório de todos os GEH(z)
			double gehGlobal = gehX / ((double) arestasSensor);
	
			if(getFuncaoFitness().equals("geh")) {
	
				return gehGlobal;
			}
				


			// System.out.println("GEH: "+gehX/((double)arestasSensor)+" = "+gehX+"/"+arestasSensor);
		}

		if (getFuncaoFitness().equals("folga")) {
			
			return folga;
		}
			

		//  return gehGlobal;
		// reg
	
		
		return (custoSoma1 + custoSoma2);
	}
	public double runDE(ODMatrix ODmatrix1, VirtualSensors virtualSensors1,
			int clusters, int tempoPriori, int tempoProblema, int arestasSensor1, int runs) {

		// Output output = new Output();
		
		arestasSensor = arestasSensor1;
		if (testeRedeFechada) {
			System.out.println("OK: Mudando teste para rede fechada...");
			tempoPriori = 0;
			tempoProblema = 0;
			batchPriori = 0;
			batchProblema = 0;
			sensoresFluxoNotRotas = true;
			sensoresMaisRotas = false;
			sensoresCorrelacao = false;
			if (arestasSensor > 71) {
				arestasSensor = 71;
				System.out.println("ATENCAO: arestasSensor reduzidas para " + arestasSensor + ", por ser numero maximo de sensores da rede cadastrada.");
			}
		}

		if(funcaoFitness.equals("reg"))        
			nomeTeste = "DE_" + (arestasSensor) + "s_";
		else if(funcaoFitness.equals("geh"))        
			nomeTeste = "DEg_" + (arestasSensor) + "s_";
		else if(funcaoFitness.equals("folga"))        
			nomeTeste = "DEf_" + (arestasSensor) + "s_";
		/*if (useMatrixPriori) {
            nomeTeste = nomeTeste + "P1";
        } else {
            nomeTeste = nomeTeste + "P0";
        }
        if (useVariance) {
            nomeTeste = nomeTeste + "_V1";
        } else {
            nomeTeste = nomeTeste + "_V0";
        }*/
		
		/* Usados em portugal
		if (sensoresFluxoNotRotas) {
			
			nomeTeste = nomeTeste + "_F";
		} else {
			if(sensoresMaisRotas)
				nomeTeste = nomeTeste + "_Rmais";
			else
				nomeTeste = nomeTeste + "_Rmenos";
		}

		if(sensoresCorrelacao)
			nomeTeste = nomeTeste + "C";
		
		*/

		nomeTeste = nomeTeste + "_" + tempoPriori + "t" + tempoProblema;

		System.out.println("PROC: Preparando para iniciar " + nomeTeste + "... " + horaAtual());


		N = clusters * clusters;
		parODcoberto = new boolean[clusters * clusters];
		for (int c = 0; c < clusters * clusters; c++) {
			parODcoberto[c] = false;
		}

		mae = new Double[runs];
		rmse = new Double[runs];
		fitness = new Double[runs];
		maeIn = new Double[runs];
		geh = new Double[runs];
		gehIn = new Double[runs];
		percGeh = new Double[runs];
		r2links = new Double[runs];
		r2odm = new Double[runs];
		rmseIn = new Double[runs];
		fitnessIn = new Double[runs];

		this.virtualSensors = virtualSensors1;
		this.ODmatrix = ODmatrix1;
		ODmatrix.calcMatrixClust();
		ODmatrix.calcVarianciaODMatrix();
		//ODmatrix.normalizarPathLinkMatrix(); - ja normalizado na criação
		//ODmatrix.printStatsVariancia(tempoProblema);
		//ODmatrix.redefinirODparArestaContAPartirDeVirtualSensors(virtualSensors, tempoProblema, batchProblema);
		//this.map = map1;

		virtualSensors.calcVarianciaArestas();
		// virtualSensors.printStatsVariancia(tempoProblema, arestasSensor);
		this.tempoPriori = tempoPriori;
		this.tempoPriori2 = tempoPriori;
		//this.batchPriori = batchPriori;
		this.tempoProblema = tempoProblema;
		//this.batchProblema = batchProblema;
		//this.arestasSensor = arestasSensor; above
		this.clusters = clusters;

		definirArestas_NSLP();

		descobrirODparIndArestaMaisMov();

		if (!probArestaODdefinidas) {
			/*  this.definirVariaveisProbabilidadeArestaRota(clusters);
            posPart = new double[POPULATION_SIZE][N + doParOD.size()];
            velPart = new double[POPULATION_SIZE][N + doParOD.size()];
            localBest = new double[POPULATION_SIZE][N + doParOD.size()];
            virtualSensors = encontrarMinMaxPorTipoAresta(virtualSensors);*/
		} else {
			definirAssignmentMatrix2(tempoPriori);
			
			posPart = new double[POPULATION_SIZE][N];
			posMutantPart = new double[POPULATION_SIZE][N];
			localBest = new double[POPULATION_SIZE][N];
		}

		//inicia matrizes
		menorCustoLocal = new double[POPULATION_SIZE];

		minInicio = this.getMinutosAtual();

		String tempo = horaAtual().replace(",", "") + ", " + runs + ", ";
		String resumo = "Population size: " + POPULATION_SIZE + "\nGenerations: " + MAX_EVALUATIONS + "\nOD =" + N + "\nSensores=" + arestasSensor + "\n";

		int inicio = 0;



		Double[] evo = new Double[MAX_EVALUATIONS];
		Double[] evo2 = new Double[MAX_EVALUATIONS];
		ArrayList[] data = new ArrayList[runs];
		ArrayList[] data2 = new ArrayList[runs];
		ArrayList<String> labelSeries = new ArrayList<>();
		ArrayList<Double> labelValues = new ArrayList<>();

		//determinação de parametros
		System.out.println("OK: Iniciando DE com "+ runs+ " runs! " + resumo + nomeTeste + "; " + horaAtual());
		for (int r = inicio; r < runs; r++) {   ///INICIO DE RODADA

			labelSeries.add("r" + r);
			
			double menorCusto;
			//double aux1,aux2,aux3;
			double tempCusto;
					//inicia velocidades randomicamente
	

			//inicia posições e atribui melhor local   
			iniciaParts(min, max, false);

			for (int i = 0; i < POPULATION_SIZE; i++) {
				System.arraycopy(posPart[i], 0, localBest[i], 0, N);  
				menorCustoLocal[i] = calculaCusto(i, false, false);
			}
			
		
			//determina global best
			globalBest = 0;
			menorCusto = menorCustoLocal[0];

			for (int i = 1; i < POPULATION_SIZE; i++) {
				tempCusto = menorCustoLocal[i]; //calculaCustoGLS(aux);
				if (tempCusto < menorCusto) {
					globalBest = i;
					menorCusto = tempCusto;
				}
			}

			double maeI, rmseI, gehI, fI;

			maeI = calcMAE();//maeIn[r] = calcMAE();
			rmseI = calcRMSE();//rmseIn[r] = calcRMSE();
			gehI = this.calcGEH(false);//gehIn[r] = this.calcGEH(false);
			fI = menorCusto;//fitnessIn[r] = menorCusto;

			System.out.println("RUN " + nomeTeste + " " + (r + 1) + "/" + runs + ": Menor custo inicial é " + menorCusto + "(RMSE: " + rmseI + "; MAE: " + maeI + "; GEH: " + gehI + ") id do melhor indivíduo=" + globalBest);
			int a = 0;
			
			
			
			while (a < MAX_EVALUATIONS) {  //ou outra condição de parada
				

				evo[a] = calcGEH(false);
				evo2[a] = menorCusto;
				
				
				//w = wIn + (wF - wIn) * ((MAX_EVALUATIONS - a) / MAX_EVALUATIONS);


			
				for (int i = 0; i < POPULATION_SIZE; i++) {
					
					int X = i; //elemento mutante
	                int A,B,C = -1;
	                
	                do{
	                    A = random.nextInt(POPULATION_SIZE);
	                }while(X == A);
	                
	                do{
	                    B = random.nextInt(POPULATION_SIZE);
	                }while(B==X || B==A);
	                
	                do{
	                    C = random.nextInt(POPULATION_SIZE);
	                }while(C == X || C == A || C == B);
	                
	                int pivot = random.nextInt(N);	   // posição que com certeza irá ser alterada             
	                
	                double crossoverOnJ; //variável aleatória que verifica se uma posição do indivíduo será alterada
	                boolean doCrossover;
					for (int j = 0; j < N; j++) { //todas as posicoes da matriz OD para gerar a população mutante        
						
						if (parODcoberto[j]) { //somente se par OD é utilizado

							crossoverOnJ = random.nextDouble();
							doCrossover = crossoverOnJ <= this.CROSSOVER_PROBABILITY;
							
							if(doCrossover || j == pivot) { //se o crossover será feito
								posMutantPart[i][j] =  posPart[A][j] + DIFFERENTIAL_WEIGHT * (posPart[B][j] - posPart[C][j]);
								
							}
							
							if(posMutantPart[i][j] < 0 || ! doCrossover ) {  //caso o mutante fique fora do dominio ou não faça o crossover
								posMutantPart[i][j] = posPart[i][j];
								

							}
							
							

						}
					}
					
		
					

				}
					
				//recalcula fitness da população e obtem o melhor individuo
				
				for (int i = 0; i < POPULATION_SIZE; i++) {
					
					double custoCandidato,custoMutante;
					
					custoCandidato = calculaCustoComProbArestasDEMutante(i, false, false, false); //para os originais	
					custoMutante = calculaCustoComProbArestasDEMutante(i, false,false, true);    //para os respectivos mutantes
				
					if(custoMutante < custoCandidato) { //se mutante é melhor que o candidato
						for(int j = 0; j < N; j++) { //
							posPart[i][j] = posMutantPart [i][j];
						}
						
					}
					


				}
				
				double armazenaLastMelhor =  menorCusto;
				
				for (int i = 0; i < POPULATION_SIZE; i++) {
					
					tempCusto = calculaCustoComProbArestas(i,false,false);
					if (tempCusto <= menorCustoLocal[i]) { // É localBest
						for (int j = 0; j < N; j++) {
							localBest[i][j] = posPart[i][j];
						}
						menorCustoLocal[i] = tempCusto; //calculaCustoGLS(i);

						if (tempCusto < menorCusto) { // É globalBest
							globalBest = i;
							menorCusto = tempCusto;
						}
					}
				}
				
				if (menorCusto < armazenaLastMelhor && debug) { // É globalBest
					System.out.println("Melhor da geracao "+ a + ": " +funcaoFitness+"("+ a +") = " + menorCusto + " " +(armazenaLastMelhor-menorCusto));
					functionProgressStatistics += "" + a + " " + menorCusto + " " + (armazenaLastMelhor-menorCusto) + "\n";
				}


				/*contReset++;
				if (contReset == reset) {
					iniciaParts(min, max, true);
					contReset = 0;
					//System.out.print(".");
				}*/

	
				a++;
			} //fim do laço principal



			if (r < runs) {
				data[r] = vetorToArray(evo);
				data2[r] = vetorToArray(evo2);
				maeIn[r] = maeI;
				mae[r] = calcMAE();
				rmseIn[r] = rmseI;
				rmse[r] = calcRMSE();
				gehIn[r] = gehI;
				geh[r] = calcGEH(r==2);
				percGeh[r] = this.percGehAbaixo5;
				fitnessIn[r] = fI;
				fitness[r] = menorCustoLocal[globalBest];
				r2links[r] = r2Global;
				r2odm[r] = calcR2Odm();
			} else {
				System.out.println("WARNING: Resultados descartados. Vetores de resultados ja estao completos.");
				return 666.0;
			}

			if (r == 0) {
				fitnessBestMod = menorCusto;
				bestMOD = localBest[globalBest];
			} else if (menorCusto < fitnessBestMod) {
				fitnessBestMod = menorCusto;
				bestMOD = localBest[globalBest];
			}

			System.out.println("RUN " + nomeTeste + " " + (r + 1) + "/" + runs + ": Menor  custo  final é " + fitnessBestMod + printMetricasFinais(r) + " ´p=" + globalBest + "\n");


		} //fim da RUN   ( r < runs )


		if (!testeParametros) {
			minInicio = (((double) getMinutosAtual()) - minInicio) / ((double) runs);
			tempo = tempo + horaAtual().replace(",", "") + ", " + minInicio;


			System.out.println("ALERT: Salvando última MOD resposta. Não a melhor.");

			double[] respMOD = new double[N];
			for (int o = 0; o < clusters; o++) {
				for (int d = 0; d < clusters; d++) {
					respMOD[o * clusters + d] = ODmatrix.getODMatrixClustersBatch(o, d, tempoProblema, batchProblema);
				}
			}
			Double vet[] = new Double[1];
			vet[0] = 0.0;
			//rest.addResultados("MOD_t" + tempoProblema + "b" + batchProblema, 0, vet, vet, vet, vet, vet, vet, vet, vet, respMOD, "", vet, vet, vet);


		}

		if(!salvandoBackup){
			for (int a = 0; a < MAX_EVALUATIONS; a++) {
				labelValues.add(a * 1.0);
			}
			/*GeraGraficos gx = new GeraGraficos(1000, 800);
        gx.GeraGraficosLinha("Graficos", "Evolution PSO GEH", data, labelValues, labelSeries, "Iterations (s=" + POPULATION_SIZE + ";t=" + MAX_EVALUATIONS + ")", "Fitness (" + nomeTeste + ")");
        gx = new GeraGraficos(1000, 800);
        gx.GeraGraficosLinha("Graficos", "Evolution PSO F", data2, labelValues, labelSeries, "Iterations (s=" + POPULATION_SIZE + ";t=" + MAX_EVALUATIONS + ")", "Fitness (" + nomeTeste + ")");*/
			return fitnessBestMod;
		}

		return Double.NaN;

	}
	
	
	
	public double runPSO(ODMatrix ODmatrix1, VirtualSensors virtualSensors1,
			int clusters, int tempoPriori, int tempoProblema, int arestasSensor1, int runs) {

		// Output output = new Output();
		arestasSensor = arestasSensor1;
		if (testeRedeFechada) {
			System.out.println("OK: Mudando teste para rede fechada...");
			tempoPriori = 0;
			tempoProblema = 0;
			batchPriori = 0;
			batchProblema = 0;
			sensoresFluxoNotRotas = true;
			sensoresMaisRotas = false;
			sensoresCorrelacao = false;
			if (arestasSensor > 71) {
				arestasSensor = 71;
				System.out.println("ATENCAO: arestasSensor reduzidas para " + arestasSensor + ", por ser numero maximo de sensores da rede cadastrada.");
			}
		}

		if(funcaoFitness.equals("reg"))        
			nomeTeste = "PSO_" + (arestasSensor) + "s_";
		else if(funcaoFitness.equals("geh"))        
			nomeTeste = "PSOg_" + (arestasSensor) + "s_";
		else if(funcaoFitness.equals("folga"))        
			nomeTeste = "PSOf_" + (arestasSensor) + "s_";
		/*if (useMatrixPriori) {
            nomeTeste = nomeTeste + "P1";
        } else {
            nomeTeste = nomeTeste + "P0";
        }
        if (useVariance) {
            nomeTeste = nomeTeste + "_V1";
        } else {
            nomeTeste = nomeTeste + "_V0";
        }*/
		
		/* Usados em portugal
		if (sensoresFluxoNotRotas) {
			
			nomeTeste = nomeTeste + "_F";
		} else {
			if(sensoresMaisRotas)
				nomeTeste = nomeTeste + "_Rmais";
			else
				nomeTeste = nomeTeste + "_Rmenos";
		}

		if(sensoresCorrelacao)
			nomeTeste = nomeTeste + "C";
		
		*/

		nomeTeste = nomeTeste + "_" + tempoPriori + "t" + tempoProblema;

		System.out.println("PROC: Preparando para iniciar " + nomeTeste + "... " + horaAtual());


		N = clusters * clusters;
		parODcoberto = new boolean[clusters * clusters];
		for (int c = 0; c < clusters * clusters; c++) {
			parODcoberto[c] = false;
		}

		mae = new Double[runs];
		rmse = new Double[runs];
		fitness = new Double[runs];
		maeIn = new Double[runs];
		geh = new Double[runs];
		gehIn = new Double[runs];
		percGeh = new Double[runs];
		r2links = new Double[runs];
		r2odm = new Double[runs];
		rmseIn = new Double[runs];
		fitnessIn = new Double[runs];

		this.virtualSensors = virtualSensors1;
		this.ODmatrix = ODmatrix1;
		ODmatrix.calcMatrixClust();
		ODmatrix.calcVarianciaODMatrix();
		//ODmatrix.normalizarPathLinkMatrix(); - ja normalizado na criação
		//ODmatrix.printStatsVariancia(tempoProblema);
		//ODmatrix.redefinirODparArestaContAPartirDeVirtualSensors(virtualSensors, tempoProblema, batchProblema);
		//this.map = map1;

		virtualSensors.calcVarianciaArestas();
		// virtualSensors.printStatsVariancia(tempoProblema, arestasSensor);
		this.tempoPriori = tempoPriori;
		this.tempoPriori2 = tempoPriori;
		//this.batchPriori = batchPriori;
		this.tempoProblema = tempoProblema;
		//this.batchProblema = batchProblema;
		//this.arestasSensor = arestasSensor; above
		this.clusters = clusters;

		definirArestas_NSLP();

		descobrirODparIndArestaMaisMov();

		if (!probArestaODdefinidas) {
			/*  this.definirVariaveisProbabilidadeArestaRota(clusters);
            posPart = new double[POPULATION_SIZE][N + doParOD.size()];
            velPart = new double[POPULATION_SIZE][N + doParOD.size()];
            localBest = new double[POPULATION_SIZE][N + doParOD.size()];
            virtualSensors = encontrarMinMaxPorTipoAresta(virtualSensors);*/
		} else {
			definirAssignmentMatrix2(tempoPriori);
			posPart = new double[POPULATION_SIZE][N];
			velPart = new double[POPULATION_SIZE][N];
			localBest = new double[POPULATION_SIZE][N];
		}

		//inicia matrizes
		menorCustoLocal = new double[POPULATION_SIZE];

		minInicio = this.getMinutosAtual();

		String tempo = horaAtual().replace(",", "") + ", " + runs + ", ";
		String resumo = "Population size: " + POPULATION_SIZE + "\nGenerations: " + MAX_EVALUATIONS + "\nOD =" + N + "\nSensores=" + arestasSensor + "\n";


		int inicio = 0;



		Double[] evo = new Double[MAX_EVALUATIONS];
		Double[] evo2 = new Double[MAX_EVALUATIONS];
		ArrayList[] data = new ArrayList[runs];
		ArrayList[] data2 = new ArrayList[runs];
		ArrayList<String> labelSeries = new ArrayList<>();
		ArrayList<Double> labelValues = new ArrayList<>();

		//determinação de parametros
		System.out.println("OK: Iniciando PSO com "+ runs+ " runs! " + resumo + nomeTeste + "; " + horaAtual());
		for (int r = inicio; r < runs; r++) {   ///INICIO DE RODADA

			labelSeries.add("r" + r);
			
			double menorCusto;
			//double aux1,aux2,aux3;
			double tempCusto;
					//inicia velocidades randomicamente
			for (int i = 0; i < POPULATION_SIZE; i++) {
				for (int j = 0; j < N; j++) {
					velPart[i][j] = (((max - min) / 2) - ((max - min) * random.nextDouble())) / 2;  //min + ((max - min) * Math.random() / 10);
				}
				/*
				if (!probArestaODdefinidas) {
					for (aux2 = N; aux2 < (N + doParOD.size()); aux2++) {
						velPart[aux][aux2] = (0.1) - (Math.random() * 0.2);  //min + ((max - min) * Math.random() / 10);
					}
				}
				*/
			}

			//inicia posições e atribui melhor local   
			iniciaParts(min, max, false);

			for (int i = 0; i < POPULATION_SIZE; i++) {
				System.arraycopy(posPart[i], 0, localBest[i], 0, N);  //LocalBest é posição inicial da partícula
				menorCustoLocal[i] = calculaCusto(i, false, false);
			}
			
		
			//determina global best
			globalBest = 0;
			menorCusto = menorCustoLocal[0];

			for (int i = 1; i < POPULATION_SIZE; i++) {
				tempCusto = menorCustoLocal[i]; //calculaCustoGLS(aux);
				if (tempCusto < menorCusto) {
					
					globalBest = i;
					menorCusto = tempCusto;
				}
			}

			double maeI, rmseI, gehI, fI;

			maeI = calcMAE();//maeIn[r] = calcMAE();
			rmseI = calcRMSE();//rmseIn[r] = calcRMSE();
			gehI = this.calcGEH(false);//gehIn[r] = this.calcGEH(false);
			fI = menorCusto;//fitnessIn[r] = menorCusto;

			System.out.println("RUN " + nomeTeste + " " + (r + 1) + "/" + runs + ": Menor custo inicial é " + menorCusto + "(RMSE: " + rmseI + "; MAE: " + maeI + "; GEH: " + gehI + ") id do melhor indivíduo=" + globalBest);
			int a = 0;

			while (a < MAX_EVALUATIONS) {  //ou outra condição de parada

				evo[a] = calcGEH(false);
				evo2[a] = menorCusto;

				w = wIn + (wF - wIn) * ((MAX_EVALUATIONS - a) / MAX_EVALUATIONS);

				//movimenta particulas
				for (int i = 0; i < POPULATION_SIZE; i++) {
					for (int j = 0; j < N; j++) {
						//if (!ODparIndArestaMaisMov[(j - (i % clusters)) / clusters][j % clusters].isEmpty()) // int id2 = x % clusters; int id1 = (x - id2) / clusters;
						if (parODcoberto[j]) { //somente se par OD é utilizado

							posPart[i][j] = posPart[i][j] + (/*(int)*/velPart[i][j]);

							if (posPart[i][j] < 0) {
								posPart[i][j] = /*(int)*/ (-posPart[i][j] / 4);
							}
							/*no original do luciano, ele faz essa igualdade.
							 * else if (posPart[i][j] < 0.1) {
								posPart[i][j] = 0;
							}*/
						}
					}
					
					/*
					if (!probArestaODdefinidas) {
						for (int ax = N; ax < (N + doParOD.size()); ax++) {

							posPart[i][ax] = posPart[i][ax] + (velPart[i][j]);

							int k = virtualSensors.getIndiceArestaKind(daAresta.get(ax - N));
							double min1 = minKind.get(k);
							double max1 = maxKind.get(k);

							if (posPart[i][ax] < min1) {
								posPart[i][ax] = min1;
							} else if (posPart[i][ax] > max1) {
								posPart[i][ax] = max1;
							}

						}
					}
					*/

				}
				
				double previousBest = menorCusto;
				//recalcula local e global best  
				for (int i = 0; i < POPULATION_SIZE; i++) {

					tempCusto = calculaCusto(i, false, false);

					if (tempCusto <= menorCustoLocal[i]) { // É localBest
						for (int j = 0; j < N; j++) {
							localBest[i][j] = posPart[i][j];
						}
						menorCustoLocal[i] = tempCusto; //calculaCustoGLS(i);

						if (tempCusto < menorCusto) { // É globalBest
							globalBest = i;
							menorCusto = tempCusto;
						}
					}
				}
				if (previousBest-menorCusto > 0 && debug) {
					System.out.println("Melhor da geracao "+ a + ": " +funcaoFitness+"("+ a +") = " + menorCusto + " " +(previousBest-menorCusto));				
					functionProgressStatistics += "" + a + " " + menorCusto + " " + (previousBest-menorCusto) + "\n";
				}
				/* REMOVER RESET 
				 * contReset++;
				if (contReset == reset) {
					System.out.println("Reset da na geração "+a);
					iniciaParts(min, max, true);
					contReset = 0;
					//System.out.print(".");
				}
				*/

				//calcula novas velocidades
				for (int i = 0; i < POPULATION_SIZE; i++) {
					for (int j = 0; j < N; j++) {
						if (parODcoberto[j]) {
							velPart[i][j] = w * velPart[i][j]
											+ c1 * (localBest[i][j] - posPart[i][j]) * random.nextDouble()
											+ c2 * (localBest[globalBest][j] - posPart[i][j]) * random.nextDouble();
						}
					}
/*
					if (!probArestaODdefinidas) {
						for (int ax = N; ax < (N + doParOD.size()); ax++) {

							velPart[i][j]
									= w * velPart[i][ax]
											+ c1 * (localBest[i][ax] - posPart[i][ax]) * Math.random()
											+ c2 * (localBest[globalBest][ax] - posPart[i][ax]) * Math.random();
						}
					}
					*/
				}

				a++;
			} //fim do laço principal



			if (r < runs) {
				data[r] = vetorToArray(evo);
				data2[r] = vetorToArray(evo2);
				maeIn[r] = maeI;
				mae[r] = calcMAE();
				rmseIn[r] = rmseI;
				rmse[r] = calcRMSE();
				gehIn[r] = gehI;
				geh[r] = calcGEH(r==2);
				percGeh[r] = this.percGehAbaixo5;
				fitnessIn[r] = fI;
				fitness[r] = menorCustoLocal[globalBest];
				r2links[r] = r2Global;
				r2odm[r] = calcR2Odm();
			} else {
				System.out.println("WARNING: Resultados descartados. Vetores de resultados ja estao completos.");
				return 666.0;
			}

			if (r == 0) {
				fitnessBestMod = menorCusto;
				bestMOD = localBest[globalBest];
			} else if (menorCusto < fitnessBestMod) {
				fitnessBestMod = menorCusto;
				bestMOD = localBest[globalBest];
			}
				
			System.out.println("RUN " + nomeTeste + " " + (r + 1) + "/" + runs + ": Menor  custo  final é " + menorCusto + printMetricasFinais(r) + " ´p=" + globalBest + "\n");


		} //fim da RUN   ( r < runs )


		if (!testeParametros) {
			minInicio = (((double) getMinutosAtual()) - minInicio) / ((double) runs);
			tempo = tempo + horaAtual().replace(",", "") + ", " + minInicio;


			System.out.println("ALERT: Salvando última MOD resposta. Não a melhor.");

			double[] respMOD = new double[N];
			for (int o = 0; o < clusters; o++) {
				for (int d = 0; d < clusters; d++) {
					respMOD[o * clusters + d] = ODmatrix.getODMatrixClustersBatch(o, d, tempoProblema, batchProblema);
				}
			}
			Double vet[] = new Double[1];
			vet[0] = 0.0;
			//rest.addResultados("MOD_t" + tempoProblema + "b" + batchProblema, 0, vet, vet, vet, vet, vet, vet, vet, vet, respMOD, "", vet, vet, vet);


		}



		if(!salvandoBackup){

			for (int a = 0; a < MAX_EVALUATIONS; a++) {
				labelValues.add(a * 1.0);
			}

			/*GeraGraficos gx = new GeraGraficos(1000, 800);
        gx.GeraGraficosLinha("Graficos", "Evolution PSO GEH", data, labelValues, labelSeries, "Iterations (s=" + POPULATION_SIZE + ";t=" + MAX_EVALUATIONS + ")", "Fitness (" + nomeTeste + ")");
        gx = new GeraGraficos(1000, 800);
        gx.GeraGraficosLinha("Graficos", "Evolution PSO F", data2, labelValues, labelSeries, "Iterations (s=" + POPULATION_SIZE + ";t=" + MAX_EVALUATIONS + ")", "Fitness (" + nomeTeste + ")");*/
			
			
			
			//return mediaVet(geh);
			return fitnessBestMod;

		}

		return Double.NaN;

	}
	

	//1 pivo apenas
	// elimina filhos quando necessario 
	public double runGAOneMutation(ODMatrix ODmatrix1, VirtualSensors virtualSensors1,
			int clusters, int tempoPriori, int tempoProblema, int arestasSensor1, int runs) {

		// Output output = new Output();
		

		arestasSensor = arestasSensor1;
		if (testeRedeFechada) {
			System.out.println("OK: Mudando teste para rede fechada...");
			tempoPriori = 0;
			tempoProblema = 0;
			batchPriori = 0;
			batchProblema = 0;
			sensoresFluxoNotRotas = true;
			sensoresMaisRotas = false;
			sensoresCorrelacao = false;
			if (arestasSensor > 71) {
				arestasSensor = 71;
				System.out.println("ATENCAO: arestasSensor reduzidas para " + arestasSensor + ", por ser numero maximo de sensores da rede cadastrada.");
			}
		}

		if(funcaoFitness.equals("reg"))        
			nomeTeste = "GA_" + (arestasSensor) + "s_";
		else if(funcaoFitness.equals("geh"))        
			nomeTeste = "GAg_" + (arestasSensor) + "s_";
		else if(funcaoFitness.equals("folga"))        
			nomeTeste = "GAf_" + (arestasSensor) + "s_";
		/*if (useMatrixPriori) {
            nomeTeste = nomeTeste + "P1";
        } else {
            nomeTeste = nomeTeste + "P0";
        }
        if (useVariance) {
            nomeTeste = nomeTeste + "_V1";
        } else {
            nomeTeste = nomeTeste + "_V0";
        }*/
		
		/* Usados em portugal
		if (sensoresFluxoNotRotas) {
			
			nomeTeste = nomeTeste + "_F";
		} else {
			if(sensoresMaisRotas)
				nomeTeste = nomeTeste + "_Rmais";
			else
				nomeTeste = nomeTeste + "_Rmenos";
		}

		if(sensoresCorrelacao)
			nomeTeste = nomeTeste + "C";
		
		*/

		nomeTeste = nomeTeste + "_" + tempoPriori + "t" + tempoProblema;

		System.out.println("PROC: Preparando para iniciar " + nomeTeste + "... " + horaAtual());


		N = clusters * clusters;
		parODcoberto = new boolean[clusters * clusters];
		for (int c = 0; c < clusters * clusters; c++) {
			parODcoberto[c] = false;
		}

		mae = new Double[runs];
		rmse = new Double[runs];
		fitness = new Double[runs];
		maeIn = new Double[runs];
		geh = new Double[runs];
		gehIn = new Double[runs];
		percGeh = new Double[runs];
		r2links = new Double[runs];
		r2odm = new Double[runs];
		rmseIn = new Double[runs];
		fitnessIn = new Double[runs];

		this.virtualSensors = virtualSensors1;
		this.ODmatrix = ODmatrix1;
		ODmatrix.calcMatrixClust();
		ODmatrix.calcVarianciaODMatrix();
		//ODmatrix.normalizarPathLinkMatrix(); - ja normalizado na criação
		//ODmatrix.printStatsVariancia(tempoProblema);
		//ODmatrix.redefinirODparArestaContAPartirDeVirtualSensors(virtualSensors, tempoProblema, batchProblema);
		//this.map = map1;

		virtualSensors.calcVarianciaArestas();
		// virtualSensors.printStatsVariancia(tempoProblema, arestasSensor);
		this.tempoPriori = tempoPriori;
		this.tempoPriori2 = tempoPriori;
		//this.batchPriori = batchPriori;
		this.tempoProblema = tempoProblema;
		//this.batchProblema = batchProblema;
		//this.arestasSensor = arestasSensor; above
		this.clusters = clusters;

		definirArestas_NSLP();

		descobrirODparIndArestaMaisMov();

		if (!probArestaODdefinidas) {
			/*  this.definirVariaveisProbabilidadeArestaRota(clusters);
            posPart = new double[POPULATION_SIZE][N + doParOD.size()];
            velPart = new double[POPULATION_SIZE][N + doParOD.size()];
            localBest = new double[POPULATION_SIZE][N + doParOD.size()];
            virtualSensors = encontrarMinMaxPorTipoAresta(virtualSensors);*/
		} else {
			definirAssignmentMatrix2(tempoPriori);
			
			posPart = new double[POPULATION_SIZE][N];
			posMutantPart = new double[POPULATION_SIZE][N];
			localBest = new double[POPULATION_SIZE][N];
		}

		//inicia matrizes
		menorCustoLocal = new double[POPULATION_SIZE];

		minInicio = this.getMinutosAtual();

		String tempo = horaAtual().replace(",", "") + ", " + runs + ", ";
		String resumo = "Population size: " + POPULATION_SIZE + "\nGenerations: " + MAX_EVALUATIONS + "\nOD =" + N + "\nSensores=" + arestasSensor + "\n";

		int inicio = 0;



		Double[] evo = new Double[MAX_EVALUATIONS];
		Double[] evo2 = new Double[MAX_EVALUATIONS];
		ArrayList[] data = new ArrayList[runs];
		ArrayList[] data2 = new ArrayList[runs];
		ArrayList<String> labelSeries = new ArrayList<>();
		ArrayList<Double> labelValues = new ArrayList<>();

		//determinação de parametros
		System.out.println("OK: Iniciando GA ("+selectionGA+", "+crossoverGA+", "+mutationGA+") com "+ runs+ " runs! " + resumo + nomeTeste + "; " + horaAtual());
		for (int r = inicio; r < runs; r++) {   ///INICIO DE RODADA

			labelSeries.add("r" + r);
			
			double menorCusto;
			//double aux1,aux2,aux3;
			double tempCusto;
					//inicia velocidades randomicamente
	

			//inicia posições e atribui melhor local   
			iniciaParts(min, max, false);

			for (int i = 0; i < POPULATION_SIZE; i++) {
				System.arraycopy(posPart[i], 0, localBest[i], 0, N);  
				menorCustoLocal[i] = calculaCusto(i, false, false);
			}
			
		
			//determina global best
			globalBest = 0;
			menorCusto = menorCustoLocal[0];

			for (int i = 1; i < POPULATION_SIZE; i++) {
				tempCusto = menorCustoLocal[i]; //calculaCustoGLS(aux);
				if (tempCusto < menorCusto) {
					globalBest = i;
					menorCusto = tempCusto;
				}
			}

			double maeI, rmseI, gehI, fI;

			maeI = calcMAE();//maeIn[r] = calcMAE();
			rmseI = calcRMSE();//rmseIn[r] = calcRMSE();
			gehI = this.calcGEH(false);//gehIn[r] = this.calcGEH(false);
			fI = menorCusto;//fitnessIn[r] = menorCusto;

			System.out.println("RUN " + nomeTeste + " " + (r + 1) + "/" + runs + ": Menor custo inicial é " + menorCusto + "(RMSE: " + rmseI + "; MAE: " + maeI + "; GEH: " + gehI + ") id do melhor indivíduo=" + globalBest);
			int a = 0;
			
			while (a < MAX_EVALUATIONS) {  //ou outra condição de parada
				

				evo[a] = calcGEH(false);
				evo2[a] = menorCusto;
				
				
			
				for (int i = 0; i <= POPULATION_SIZE/2; i++) {
					
					int father = -1; //elemento mutante
	                int mother = -1;
	                
	                do{
	                	father = random.nextInt(POPULATION_SIZE);
	                	mother = random.nextInt(POPULATION_SIZE);
	                }while(father == mother);
	                
	                double crossover = random.nextDouble(); //variável aleatória que decide se haverá cruzamentos entre os pais
	                
	                if (crossover > CROSSOVER_PROBABILITY) { //se não houver cruzamento, tenta de novo
	                	--i;
	                	continue;
	                }
	                
	                int crossoverPivot = random.nextInt(N);	   // posição aleatória que dividirá os genes                            
	                
	                ArrayList<Double> fatherGenes1stPart = new ArrayList<Double>();
	                ArrayList<Double> motherGenes1stPart = new ArrayList<Double>();
	                ArrayList<Double> fatherGenes2ndPart = new ArrayList<Double>();
	                ArrayList<Double> motherGenes2ndPart = new ArrayList<Double>();
	                
	                
	                for (int j = 0; j <= crossoverPivot ; ++j) { //obtem primeira parte dos genes
	                	fatherGenes1stPart.add(posPart[father][j]);
	                	motherGenes1stPart.add(posPart[mother][j]);
	                	
	                }
	                
	                for (int j = crossoverPivot + 1; j < N ; ++j) { //obtem segunda parte dos genes
	                	fatherGenes2ndPart.add(posPart[father][j]);
	                	motherGenes2ndPart.add(posPart[mother][j]);
	                }
	                
	                
	                motherGenes1stPart.addAll(fatherGenes2ndPart); //concatena pai e mae
	                fatherGenes1stPart.addAll(motherGenes2ndPart); //resultado = 2 filhos
	                
	                int sonA = i % POPULATION_SIZE;  //caso a populacao seja impar, o sonB ocupará a posição de um outro filho da nova geração
	                int sonB = (i+1) % POPULATION_SIZE; 
	                
	                
	                for (int j = 0; j < N; ++j) { //instancia os filhos
	                	posMutantPart[sonA][j] = motherGenes1stPart.get(j);
	                }
	                
	                for (int j = 0; j < N; ++j) {
	                	posMutantPart[sonB][j] = fatherGenes1stPart.get(j);
	                }
	                
	                double mutation;
	                										//esta mutação altera um gene para no máximo o seu dobro
	                mutation = random.nextDouble();			//e no mínimo sua metade.
	                if (mutation <= MUTATION_PROBABILITY) { //mutação altera 1 gene
	                	int mutationIndex;
	                	
	                	do {
	                		mutationIndex = random.nextInt(N);
	                	} while (!parODcoberto[mutationIndex]);
	                	
	                	posMutantPart[sonA][mutationIndex] *= 0.5 + (random.nextDouble() * 1.5);
	                	
	                }
	                
	                mutation = random.nextDouble();
	                
	                if (mutation <= MUTATION_PROBABILITY) { //mutação para o filhoB
	                	int mutationIndex;
	                	do {
	                		mutationIndex = random.nextInt(N);
	                	} while (!parODcoberto[mutationIndex]);
	                	
	                	posMutantPart[sonB][mutationIndex] *= 0.5 + (random.nextDouble() * 1.5);
	                	
	                }
	                
				}
					
				//torneio deterministico: indivíduos inferiores morrem no coliseu
				
				ArrayList<Integer> collosseum = new ArrayList<Integer>();
				
				for (int i = 0; i < POPULATION_SIZE*2; ++i) {
					collosseum.add(i);
				}
				
				int gladiatorA = -1;
				int gladiatorB = -1;
				
				double custoGladiatorA = -1;
				double custoGladiatorB = -1;
				while (collosseum.size() != POPULATION_SIZE) { //preparar coliseu
					
					do { //um gladiador não pode se matar
						
					gladiatorA = random.nextInt( collosseum.size() );
					gladiatorB = random.nextInt( collosseum.size() );
					
					} while (gladiatorA == gladiatorB);
					
					
					if(gladiatorA < POPULATION_SIZE) { //se faz parte da populacao de pais
						custoGladiatorA = calculaCustoComProbArestasDEMutante(gladiatorA, false, false, false);
					}
					
					else { //se é um filho
						custoGladiatorB = calculaCustoComProbArestasDEMutante(gladiatorA-(POPULATION_SIZE), false, false, true);
					}
					
					if(gladiatorB < POPULATION_SIZE) { //se faz parte da populacao de pais
						custoGladiatorB = calculaCustoComProbArestasDEMutante(gladiatorB, false, false, false);
					}
					
					else { //se é um filho
						custoGladiatorB = calculaCustoComProbArestasDEMutante(gladiatorB-(POPULATION_SIZE), false, false, true);
					}
					
					if (custoGladiatorA < custoGladiatorB) { //se A é melhor que B
						collosseum.remove(gladiatorB);
					}
					
					else {
						collosseum.remove(gladiatorA);
					}	
				
					
				}
				
				int survivor = -1;
				//double[][] survivorVector = new double[POPULATION_SIZE][N];
		
				
				double previousBest = menorCusto;
				for (int i = 0; i < POPULATION_SIZE; i++) {
					
					survivor = collosseum.get(i);
					
					if (survivor < POPULATION_SIZE) {
						posPart[i] = posPart[survivor].clone();
					}
					
					else {
						posPart[i] = posMutantPart[survivor-POPULATION_SIZE].clone();
					}
					
					tempCusto = calculaCustoComProbArestas(i,false,false);
					if (tempCusto < menorCustoLocal[i]) { // É localBest
						for (int j = 0; j < N; j++) {
							localBest[i][j] = posPart[i][j];
						}
						menorCustoLocal[i] = tempCusto; //calculaCustoGLS(i);

						if (tempCusto < menorCusto) { // É globalBest
							globalBest = i;
							menorCusto = tempCusto;
						}
					}
					
					
				}
				
				if (previousBest-menorCusto > 0) {
					System.out.println("Melhor da geracao "+ a + ": " +funcaoFitness+"("+ a +") = " + menorCusto + " " +(previousBest-menorCusto));				
					functionProgressStatistics += "" + a + " " + menorCusto + " " + (previousBest-menorCusto) + "\n";
				}

				/*contReset++;
				if (contReset == reset) {
					iniciaParts(min, max, true);
					contReset = 0;
					//System.out.print(".");
				}*/

	
				a++;
			} //fim do laço principal



			if (r < runs) {
				data[r] = vetorToArray(evo);
				data2[r] = vetorToArray(evo2);
				maeIn[r] = maeI;
				mae[r] = calcMAE();
				rmseIn[r] = rmseI;
				rmse[r] = calcRMSE();
				gehIn[r] = gehI;
				geh[r] = calcGEH(r==2);
				percGeh[r] = this.percGehAbaixo5;
				fitnessIn[r] = fI;
				fitness[r] = menorCustoLocal[globalBest];
				r2links[r] = r2Global;
				r2odm[r] = calcR2Odm();
			} else {
				System.out.println("WARNING: Resultados descartados. Vetores de resultados ja estao completos.");
				return 666.0;
			}

			if (r == 0) {
				fitnessBestMod = menorCusto;
				bestMOD = localBest[globalBest];
			} else if (menorCusto < fitnessBestMod) {
				fitnessBestMod = menorCusto;
				bestMOD = localBest[globalBest];
			}

			System.out.println("RUN " + nomeTeste + " " + (r + 1) + "/" + runs + ": Menor  custo  final é " + menorCusto + printMetricasFinais(r) + " ´p=" + globalBest + "\n");


		} //fim da RUN   ( r < runs )


		if (!testeParametros) {
			minInicio = (((double) getMinutosAtual()) - minInicio) / ((double) runs);
			tempo = tempo + horaAtual().replace(",", "") + ", " + minInicio;


			System.out.println("ALERT: Salvando última MOD resposta. Não a melhor.");

			double[] respMOD = new double[N];
			for (int o = 0; o < clusters; o++) {
				for (int d = 0; d < clusters; d++) {
					respMOD[o * clusters + d] = ODmatrix.getODMatrixClustersBatch(o, d, tempoProblema, batchProblema);
				}
			}
			Double vet[] = new Double[1];
			vet[0] = 0.0;
			//rest.addResultados("MOD_t" + tempoProblema + "b" + batchProblema, 0, vet, vet, vet, vet, vet, vet, vet, vet, respMOD, "", vet, vet, vet);


		}



		if(!salvandoBackup){

			for (int a = 0; a < MAX_EVALUATIONS; a++) {
				labelValues.add(a * 1.0);
			}

			/*GeraGraficos gx = new GeraGraficos(1000, 800);
        gx.GeraGraficosLinha("Graficos", "Evolution PSO GEH", data, labelValues, labelSeries, "Iterations (s=" + POPULATION_SIZE + ";t=" + MAX_EVALUATIONS + ")", "Fitness (" + nomeTeste + ")");
        gx = new GeraGraficos(1000, 800);
        gx.GeraGraficosLinha("Graficos", "Evolution PSO F", data2, labelValues, labelSeries, "Iterations (s=" + POPULATION_SIZE + ";t=" + MAX_EVALUATIONS + ")", "Fitness (" + nomeTeste + ")");*/
			
			return fitnessBestMod;
			//return mediaVet(geh);

		}

		return Double.NaN;

	}
	
	public double runGARandomMutations(ODMatrix ODmatrix1, VirtualSensors virtualSensors1,
			int clusters, int tempoPriori, int tempoProblema, int arestasSensor1, int runs) {

		// Output output = new Output();
		

		arestasSensor = arestasSensor1;
		if (testeRedeFechada) {
			System.out.println("OK: Mudando teste para rede fechada...");
			tempoPriori = 0;
			tempoProblema = 0;
			batchPriori = 0;
			batchProblema = 0;
			sensoresFluxoNotRotas = true;
			sensoresMaisRotas = false;
			sensoresCorrelacao = false;
			if (arestasSensor > 71) {
				arestasSensor = 71;
				System.out.println("ATENCAO: arestasSensor reduzidas para " + arestasSensor + ", por ser numero maximo de sensores da rede cadastrada.");
			}
		}

		if(funcaoFitness.equals("reg"))        
			nomeTeste = "GA_" + (arestasSensor) + "s_";
		else if(funcaoFitness.equals("geh"))        
			nomeTeste = "GAg_" + (arestasSensor) + "s_";
		else if(funcaoFitness.equals("folga"))        
			nomeTeste = "GAf_" + (arestasSensor) + "s_";
		/*if (useMatrixPriori) {
            nomeTeste = nomeTeste + "P1";
        } else {
            nomeTeste = nomeTeste + "P0";
        }
        if (useVariance) {
            nomeTeste = nomeTeste + "_V1";
        } else {
            nomeTeste = nomeTeste + "_V0";
        }*/
		
		/* Usados em portugal
		if (sensoresFluxoNotRotas) {
			
			nomeTeste = nomeTeste + "_F";
		} else {
			if(sensoresMaisRotas)
				nomeTeste = nomeTeste + "_Rmais";
			else
				nomeTeste = nomeTeste + "_Rmenos";
		}

		if(sensoresCorrelacao)
			nomeTeste = nomeTeste + "C";
		
		*/

		nomeTeste = nomeTeste + "_" + tempoPriori + "t" + tempoProblema;

		System.out.println("PROC: Preparando para iniciar " + nomeTeste + "... " + horaAtual());


		N = clusters * clusters;
		parODcoberto = new boolean[clusters * clusters];
		for (int c = 0; c < clusters * clusters; c++) {
			parODcoberto[c] = false;
		}

		mae = new Double[runs];
		rmse = new Double[runs];
		fitness = new Double[runs];
		maeIn = new Double[runs];
		geh = new Double[runs];
		gehIn = new Double[runs];
		percGeh = new Double[runs];
		r2links = new Double[runs];
		r2odm = new Double[runs];
		rmseIn = new Double[runs];
		fitnessIn = new Double[runs];

		this.virtualSensors = virtualSensors1;
		this.ODmatrix = ODmatrix1;
		ODmatrix.calcMatrixClust();
		ODmatrix.calcVarianciaODMatrix();
		//ODmatrix.normalizarPathLinkMatrix(); - ja normalizado na criação
		//ODmatrix.printStatsVariancia(tempoProblema);
		//ODmatrix.redefinirODparArestaContAPartirDeVirtualSensors(virtualSensors, tempoProblema, batchProblema);
		//this.map = map1;

		virtualSensors.calcVarianciaArestas();
		// virtualSensors.printStatsVariancia(tempoProblema, arestasSensor);
		this.tempoPriori = tempoPriori;
		this.tempoPriori2 = tempoPriori;
		//this.batchPriori = batchPriori;
		this.tempoProblema = tempoProblema;
		//this.batchProblema = batchProblema;
		//this.arestasSensor = arestasSensor; above
		this.clusters = clusters;

		definirArestas_NSLP();

		descobrirODparIndArestaMaisMov();

		if (!probArestaODdefinidas) {
			/*  this.definirVariaveisProbabilidadeArestaRota(clusters);
            posPart = new double[POPULATION_SIZE][N + doParOD.size()];
            velPart = new double[POPULATION_SIZE][N + doParOD.size()];
            localBest = new double[POPULATION_SIZE][N + doParOD.size()];
            virtualSensors = encontrarMinMaxPorTipoAresta(virtualSensors);*/
		} else {
			definirAssignmentMatrix2(tempoPriori);
			
			posPart = new double[POPULATION_SIZE][N];
			posMutantPart = new double[POPULATION_SIZE][N];
			localBest = new double[POPULATION_SIZE][N];
		}

		//inicia matrizes
		menorCustoLocal = new double[POPULATION_SIZE];

		minInicio = this.getMinutosAtual();

		String tempo = horaAtual().replace(",", "") + ", " + runs + ", ";
		String resumo = "Population size: " + POPULATION_SIZE + "\nGenerations: " + MAX_EVALUATIONS + "\nOD =" + N + "\nSensores=" + arestasSensor + "\n";

		int inicio = 0;



		Double[] evo = new Double[MAX_EVALUATIONS];
		Double[] evo2 = new Double[MAX_EVALUATIONS];
		ArrayList[] data = new ArrayList[runs];
		ArrayList[] data2 = new ArrayList[runs];
		ArrayList<String> labelSeries = new ArrayList<>();
		ArrayList<Double> labelValues = new ArrayList<>();

		//determinação de parametros
		System.out.println("OK: Iniciando GA _Alterado_ com "+ runs+ " runs! " + resumo + nomeTeste + "; " + horaAtual());
		for (int r = inicio; r < runs; r++) {   ///INICIO DE RODADA

			labelSeries.add("r" + r);
			
			double menorCusto;
			//double aux1,aux2,aux3;
			double tempCusto;
					//inicia velocidades randomicamente
	

			//inicia posições e atribui melhor local   
			iniciaParts(min, max, false);

			for (int i = 0; i < POPULATION_SIZE; i++) {
				System.arraycopy(posPart[i], 0, localBest[i], 0, N);  
				menorCustoLocal[i] = calculaCusto(i, false, false);
			}
			
		
			//determina global best
			globalBest = 0;
			menorCusto = menorCustoLocal[0];

			for (int i = 1; i < POPULATION_SIZE; i++) {
				tempCusto = menorCustoLocal[i]; //calculaCustoGLS(aux);
				if (tempCusto < menorCusto) {
					globalBest = i;
					menorCusto = tempCusto;
				}
			}

			double maeI, rmseI, gehI, fI;

			maeI = calcMAE();//maeIn[r] = calcMAE();
			rmseI = calcRMSE();//rmseIn[r] = calcRMSE();
			gehI = this.calcGEH(false);//gehIn[r] = this.calcGEH(false);
			fI = menorCusto;//fitnessIn[r] = menorCusto;

			System.out.println("RUN " + nomeTeste + " " + (r + 1) + "/" + runs + ": Menor custo inicial é " + menorCusto + "(RMSE: " + rmseI + "; MAE: " + maeI + "; GEH: " + gehI + ") id do melhor indivíduo=" + globalBest);
			int a = 0;
			
			while (a < MAX_EVALUATIONS) {  //ou outra condição de parada
				

				evo[a] = calcGEH(false);
				evo2[a] = menorCusto;
				
				
			
				for (int i = 0; i <= POPULATION_SIZE/2; i++) {
					
					int father = -1; //elemento mutante
	                int mother = -1;
	                
	                do{
	                	father = random.nextInt(POPULATION_SIZE);
	                	mother = random.nextInt(POPULATION_SIZE);
	                }while(father == mother);
	                
	                double crossover = random.nextDouble(); //variável aleatória que decide se haverá cruzamentos entre os pais
	                
	                if (crossover > CROSSOVER_PROBABILITY) { //se não houver cruzamento, tenta de novo
	                	--i;
	                	continue;
	                }
	                
	                int crossoverPivot = random.nextInt(N);	   // posição aleatória que dividirá os genes                            
	                
	                ArrayList<Double> fatherGenes1stPart = new ArrayList<Double>();
	                ArrayList<Double> motherGenes1stPart = new ArrayList<Double>();
	                ArrayList<Double> fatherGenes2ndPart = new ArrayList<Double>();
	                ArrayList<Double> motherGenes2ndPart = new ArrayList<Double>();
	                
	                
	                for (int j = 0; j <= crossoverPivot ; ++j) { //obtem primeira parte dos genes
	                	fatherGenes1stPart.add(posPart[father][j]);
	                	motherGenes1stPart.add(posPart[mother][j]);
	                	
	                }
	                
	                for (int j = crossoverPivot + 1; j < N ; ++j) { //obtem segunda parte dos genes
	                	fatherGenes2ndPart.add(posPart[father][j]);
	                	motherGenes2ndPart.add(posPart[mother][j]);
	                }
	                
	                
	                motherGenes1stPart.addAll(fatherGenes2ndPart); //concatena pai e mae
	                fatherGenes1stPart.addAll(motherGenes2ndPart); //resultado = 2 filhos
	                
	                int sonA = i % POPULATION_SIZE;  //caso a populacao seja impar, o sonB ocupará a posição de um outro filho da nova geração
	                int sonB = (i+1) % POPULATION_SIZE; 
	                
	                
	                for (int j = 0; j < N; ++j) { //instancia os filhos
	                	posMutantPart[sonA][j] = motherGenes1stPart.get(j);
	                }
	                
	                for (int j = 0; j < N; ++j) {
	                	posMutantPart[sonB][j] = fatherGenes1stPart.get(j);
	                }
	                
	                
	                
	                										//esta mutação altera um gene para no máximo o seu dobro
	                int mutationIndex =  0;
	                for(  ; mutationIndex < N; ++mutationIndex) {
	                	
		                if (random.nextGaussian() <= MUTATION_PROBABILITY) { //mutação altera 1 gene		                	
		                	if (!parODcoberto[mutationIndex]) {
		                		posMutantPart[sonA][mutationIndex] *= (0.5 + random.nextGaussian() * 2);
		                	}
		                	
		                }
		                
		                
		                if (random.nextGaussian() <= MUTATION_PROBABILITY) { //mutação para o filhoB
		                	if (!parODcoberto[mutationIndex]) {
		                		posMutantPart[sonB][mutationIndex] *= ( 0.5 +  random.nextGaussian() * 2);
		                	}
		                	
		                }
	                	
	                }
	                
	                
				}
					
				//torneio deterministico: indivíduos inferiores morrem no coliseu
				
				ArrayList<Integer> collosseum = new ArrayList<Integer>();
				
				for (int i = 0; i < POPULATION_SIZE*2; ++i) {
					collosseum.add(i);
				}
				
				int gladiatorA = -1;
				int gladiatorB = -1;
				
				double custoGladiatorA = -1;
				double custoGladiatorB = -1;
				while (collosseum.size() != POPULATION_SIZE) { //preparar coliseu
					
					do { //um gladiador não pode se matar
						
					gladiatorA = random.nextInt( collosseum.size() );
					gladiatorB = random.nextInt( collosseum.size() );
					
					} while (gladiatorA == gladiatorB);
					
					
					if(gladiatorA < POPULATION_SIZE) { //se faz parte da populacao de pais
						custoGladiatorA = calculaCustoComProbArestasDEMutante(gladiatorA, false, false, false);
					}
					
					else { //se é um filho
						custoGladiatorB = calculaCustoComProbArestasDEMutante(gladiatorA-(POPULATION_SIZE), false, false, true);
					}
					
					if(gladiatorB < POPULATION_SIZE) { //se faz parte da populacao de pais
						custoGladiatorB = calculaCustoComProbArestasDEMutante(gladiatorB, false, false, false);
					}
					
					else { //se é um filho
						custoGladiatorB = calculaCustoComProbArestasDEMutante(gladiatorB-(POPULATION_SIZE), false, false, true);
					}
					
					if (custoGladiatorA < custoGladiatorB) { //se A é melhor que B
						collosseum.remove(gladiatorB);
					}
					
					else {
						collosseum.remove(gladiatorA);
					}	
				
					
				}
				
				int survivor = -1;
				//double[][] survivorVector = new double[POPULATION_SIZE][N];
		
				
				double previousBest = menorCusto;
				for (int i = 0; i < POPULATION_SIZE; i++) {
					
					survivor = collosseum.get(i);
					
					if (survivor < POPULATION_SIZE) {
						posPart[i] = posPart[survivor].clone();
					}
					
					else {
						posPart[i] = posMutantPart[survivor-POPULATION_SIZE].clone();
					}
					
					tempCusto = calculaCustoComProbArestas(i,false,false);
					if (tempCusto <= menorCustoLocal[i]) { // É localBest
						for (int j = 0; j < N; j++) {
							localBest[i][j] = posPart[i][j];
						}
						menorCustoLocal[i] = tempCusto; //calculaCustoGLS(i);

						if (tempCusto < menorCusto) { // É globalBest
							globalBest = i;
							menorCusto = tempCusto;
						}
					}
					
					
				}
				
				if (previousBest-menorCusto > 0) {
					System.out.println("Melhor da geracao "+ a + ": " +funcaoFitness+"("+ a +") = " + menorCusto + " " +(previousBest-menorCusto));				
					functionProgressStatistics += "" + a + " " + menorCusto + " " + (previousBest-menorCusto) + "\n";
				}

				/*contReset++;
				if (contReset == reset) {
					iniciaParts(min, max, true);
					contReset = 0;
					//System.out.print(".");
				}*/

	
				a++;
			} //fim do laço principal



			if (r < runs) {
				data[r] = vetorToArray(evo);
				data2[r] = vetorToArray(evo2);
				maeIn[r] = maeI;
				mae[r] = calcMAE();
				rmseIn[r] = rmseI;
				rmse[r] = calcRMSE();
				gehIn[r] = gehI;
				geh[r] = calcGEH(r==2);
				percGeh[r] = this.percGehAbaixo5;
				fitnessIn[r] = fI;
				fitness[r] = menorCustoLocal[globalBest];
				r2links[r] = r2Global;
				r2odm[r] = calcR2Odm();
			} else {
				System.out.println("WARNING: Resultados descartados. Vetores de resultados ja estao completos.");
				return 666.0;
			}

			if (r == 0) {
				fitnessBestMod = menorCusto;
				bestMOD = localBest[globalBest];
			} else if (menorCusto < fitnessBestMod) {
				fitnessBestMod = menorCusto;
				bestMOD = localBest[globalBest];
			}

			System.out.println("RUN " + nomeTeste + " " + (r + 1) + "/" + runs + ": Menor  custo  final é " + menorCusto + printMetricasFinais(r) + " ´p=" + globalBest + "\n");


		} //fim da RUN   ( r < runs )


		if (!testeParametros) {
			minInicio = (((double) getMinutosAtual()) - minInicio) / ((double) runs);
			tempo = tempo + horaAtual().replace(",", "") + ", " + minInicio;


			System.out.println("ALERT: Salvando última MOD resposta. Não a melhor.");

			double[] respMOD = new double[N];
			for (int o = 0; o < clusters; o++) {
				for (int d = 0; d < clusters; d++) {
					respMOD[o * clusters + d] = ODmatrix.getODMatrixClustersBatch(o, d, tempoProblema, batchProblema);
				}
			}
			Double vet[] = new Double[1];
			vet[0] = 0.0;
			//rest.addResultados("MOD_t" + tempoProblema + "b" + batchProblema, 0, vet, vet, vet, vet, vet, vet, vet, vet, respMOD, "", vet, vet, vet);


		}



		if(!salvandoBackup){

			for (int a = 0; a < MAX_EVALUATIONS; a++) {
				labelValues.add(a * 1.0);
			}

			/*GeraGraficos gx = new GeraGraficos(1000, 800);
        gx.GeraGraficosLinha("Graficos", "Evolution PSO GEH", data, labelValues, labelSeries, "Iterations (s=" + POPULATION_SIZE + ";t=" + MAX_EVALUATIONS + ")", "Fitness (" + nomeTeste + ")");
        gx = new GeraGraficos(1000, 800);
        gx.GeraGraficosLinha("Graficos", "Evolution PSO F", data2, labelValues, labelSeries, "Iterations (s=" + POPULATION_SIZE + ";t=" + MAX_EVALUATIONS + ")", "Fitness (" + nomeTeste + ")");*/
			
			return fitnessBestMod;
			//return mediaVet(geh);

		}

		return Double.NaN;

	}
	
	public int getParentSelectionGA() {
		//SELECAO
		ArrayList<Integer> progenitores = new ArrayList<Integer>();
		ArrayList<Integer> torneio = new ArrayList<Integer>();
		ArrayList<Double> torneioValues = new ArrayList<Double>();
		
		
		switch (selectionGA) {
			case "2-tournament":
				
				
				do { //adiciona 2 progenitores
					torneio.clear();
					torneio.add(random.nextInt(POPULATION_SIZE));
					torneio.add(random.nextInt(POPULATION_SIZE));
					
				} while (torneio.get(0) == torneio.get(1));

				torneioValues.add( calculaCustoComProbArestas(torneio.get(0), false, false) );
				torneioValues.add( calculaCustoComProbArestas(torneio.get(1), false, false) );
				
				
				
				
				if(torneioValues.get(0) < torneioValues.get(1)) {
					progenitores.add(torneio.get(0));
				}

				else {
					progenitores.add(torneio.get(1));
				}
				
				
			break;
			
			case "5-tournament":
				
				
				do { //adiciona 2 progenitores
					torneio.clear();
					torneio.add(random.nextInt(POPULATION_SIZE));
					torneio.add(random.nextInt(POPULATION_SIZE));
					torneio.add(random.nextInt(POPULATION_SIZE));
					torneio.add(random.nextInt(POPULATION_SIZE));
					torneio.add(random.nextInt(POPULATION_SIZE));
					
				} while (SetUniqueList.decorate(torneio).size() != 5); // cria um conjunto com necessariamente 5 elementos distintos

				torneioValues.add( calculaCustoComProbArestas(torneio.get(0), false, false) );
				torneioValues.add( calculaCustoComProbArestas(torneio.get(1), false, false) );
				torneioValues.add( calculaCustoComProbArestas(torneio.get(2), false, false) );
				torneioValues.add( calculaCustoComProbArestas(torneio.get(3), false, false) );
				torneioValues.add( calculaCustoComProbArestas(torneio.get(4), false, false) );
				
				int minParentIndex = 0;//valor default
				for (int i = 1; i < 5; ++i) {
					if(torneioValues.get(i) < torneioValues.get(minParentIndex)) {
						minParentIndex = i;
					}
				}
				progenitores.add(torneio.get(minParentIndex));
				
				
			break;	
	
		}
		return progenitores.get(0);
	}
	
	public double runGACorrigido(ODMatrix ODmatrix1, VirtualSensors virtualSensors1,
			int clusters, int tempoPriori, int tempoProblema, int arestasSensor1, int runs) {

		// Output output = new Output();
		

		arestasSensor = arestasSensor1;
		if (testeRedeFechada) {
			System.out.println("OK: Mudando teste para rede fechada...");
			tempoPriori = 0;
			tempoProblema = 0;
			batchPriori = 0;
			batchProblema = 0;
			sensoresFluxoNotRotas = true;
			sensoresMaisRotas = false;
			sensoresCorrelacao = false;
			if (arestasSensor > 71) {
				arestasSensor = 71;
				System.out.println("ATENCAO: arestasSensor reduzidas para " + arestasSensor + ", por ser numero maximo de sensores da rede cadastrada.");
			}
		}
		
		

		if(funcaoFitness.equals("reg"))        
			nomeTeste = "GA_" + (arestasSensor) + "s_";
		else if(funcaoFitness.equals("geh"))        
			nomeTeste = "GAg_" + (arestasSensor) + "s_";
		else if(funcaoFitness.equals("folga"))        
			nomeTeste = "GAf_" + (arestasSensor) + "s_";
		/*if (useMatrixPriori) {
            nomeTeste = nomeTeste + "P1";
        } else {
            nomeTeste = nomeTeste + "P0";
        }
        if (useVariance) {
            nomeTeste = nomeTeste + "_V1";
        } else {
            nomeTeste = nomeTeste + "_V0";
        }*/
		
		/* Usados em portugal
		if (sensoresFluxoNotRotas) {
			
			nomeTeste = nomeTeste + "_F";
		} else {
			if(sensoresMaisRotas)
				nomeTeste = nomeTeste + "_Rmais";
			else
				nomeTeste = nomeTeste + "_Rmenos";
		}

		if(sensoresCorrelacao)
			nomeTeste = nomeTeste + "C";
		
		*/

		nomeTeste = nomeTeste + "_" + tempoPriori + "t" + tempoProblema;

		System.out.println("PROC: Preparando para iniciar " + nomeTeste + "... " + horaAtual());


		N = clusters * clusters;
		parODcoberto = new boolean[clusters * clusters];
		for (int c = 0; c < clusters * clusters; c++) {
			parODcoberto[c] = false;
		}

		mae = new Double[runs];
		rmse = new Double[runs];
		fitness = new Double[runs];
		maeIn = new Double[runs];
		geh = new Double[runs];
		gehIn = new Double[runs];
		percGeh = new Double[runs];
		r2links = new Double[runs];
		r2odm = new Double[runs];
		rmseIn = new Double[runs];
		fitnessIn = new Double[runs];

		this.virtualSensors = virtualSensors1;
		this.ODmatrix = ODmatrix1;
		ODmatrix.calcMatrixClust();
		ODmatrix.calcVarianciaODMatrix();
		//ODmatrix.normalizarPathLinkMatrix(); - ja normalizado na criação
		//ODmatrix.printStatsVariancia(tempoProblema);
		//ODmatrix.redefinirODparArestaContAPartirDeVirtualSensors(virtualSensors, tempoProblema, batchProblema);
		//this.map = map1;

		virtualSensors.calcVarianciaArestas();
		// virtualSensors.printStatsVariancia(tempoProblema, arestasSensor);
		this.tempoPriori = tempoPriori;
		this.tempoPriori2 = tempoPriori;
		//this.batchPriori = batchPriori;
		this.tempoProblema = tempoProblema;
		//this.batchProblema = batchProblema;
		//this.arestasSensor = arestasSensor; above
		this.clusters = clusters;

		definirArestas_NSLP();

		descobrirODparIndArestaMaisMov();

		if (!probArestaODdefinidas) {
			/*  this.definirVariaveisProbabilidadeArestaRota(clusters);
            posPart = new double[POPULATION_SIZE][N + doParOD.size()];
            velPart = new double[POPULATION_SIZE][N + doParOD.size()];
            localBest = new double[POPULATION_SIZE][N + doParOD.size()];
            virtualSensors = encontrarMinMaxPorTipoAresta(virtualSensors);*/
		} else {
			definirAssignmentMatrix2(tempoPriori);
			
			posPart = new double[POPULATION_SIZE][N];
			posMutantPart = new double[POPULATION_SIZE][N];
			localBest = new double[POPULATION_SIZE][N];
		}

		//inicia matrizes
	
		minInicio = this.getMinutosAtual();
		
		int inicio = 0;

		Double[] evo = new Double[MAX_EVALUATIONS];
		Double[] evo2 = new Double[MAX_EVALUATIONS];
		ArrayList[] data = new ArrayList[runs];
		ArrayList[] data2 = new ArrayList[runs];
		ArrayList<String> labelSeries = new ArrayList<>();
		ArrayList<Double> labelValues = new ArrayList<>();
		
		
		//FIXME
		
		int pairsOfChildren = 0; //inicializa
		
		switch (generationReplacement) {
		case "Generational" :
			pairsOfChildren  = POPULATION_SIZE/2; 
			MAX_EVALUATIONS *= 1;
		break;
		
		case "SteadyState" :
			pairsOfChildren  = 1; //cria 1 par
			MAX_EVALUATIONS *= 2; //ajusta de acordo com o numero de filhos
		break;
		
		default :
			System.out.println("Erro! --generationReplacement deve ser \"SteadyState\" ou \"Generational\" ");
		
		}
		
		
		String tempo = horaAtual().replace(",", "") + ", " + runs + ", ";
		String resumo = "Population size: " + POPULATION_SIZE + "\nGenerations: " + MAX_EVALUATIONS + "\nOD =" + N + "\nSensores=" + arestasSensor + "\n";

		//determinação de parametros
		System.out.println("OK: Iniciando GA _Alterado_ com "+ runs+ " runs! " + resumo + nomeTeste + "; " + horaAtual());
		for (int r = inicio; r < runs; r++) {   ///INICIO DE RODADA

			labelSeries.add("r" + r);
			
			double menorCusto;
			//double aux1,aux2,aux3;
			double tempCusto;
					//inicia velocidades randomicamente
	

			//inicia posições e atribui melhor local   
			iniciaParts(min, max, false);

			
			menorCustoLocal = new double[POPULATION_SIZE];
			for (int i = 0; i < POPULATION_SIZE; i++) {
				System.arraycopy(posPart[i], 0, localBest[i], 0, N);  
				menorCustoLocal[i] = calculaCusto(i, false, false);
			}
			
		
			//determina global best
			globalBest = 0;
			menorCusto = menorCustoLocal[0];
	

			for (int i = 1; i < POPULATION_SIZE; i++) {
				tempCusto = menorCustoLocal[i]; //calculaCustoGLS(aux);
				if (tempCusto < menorCusto) {
					globalBest = i;
					menorCusto = tempCusto;
				}
			}

			double maeI, rmseI, gehI, fI;

			maeI = calcMAE();//maeIn[r] = calcMAE();
			rmseI = calcRMSE();//rmseIn[r] = calcRMSE();
			gehI = this.calcGEH(false);//gehIn[r] = this.calcGEH(false);
			fI = menorCusto;//fitnessIn[r] = menorCusto;

			System.out.println("RUN " + nomeTeste + " " + (r + 1) + "/" + runs + ": Menor custo inicial é " + menorCusto + "(RMSE: " + rmseI + "; MAE: " + maeI + "; GEH: " + gehI + ") id do melhor indivíduo=" + globalBest);
			
			int MAX_ELITES ;
			
			
			switch (generationReplacement) {
				case "Generational":
					MAX_ELITES	= (int) Math.floor(POPULATION_SIZE*elitistRateGA);
				break;
				
				case "SteadyState":
					MAX_ELITES	= 2;
				break;
				
				default: 
					MAX_ELITES = 0;
				
			}
			
			int generation = 0;

			int eliminatedA = -1;
			int eliminatedB = -1;
			int eliteQuota = MAX_ELITES;
			ArrayList <Integer> elites = new ArrayList <Integer> ();
			ArrayList <Double> elitesValues = new ArrayList <Double> ();
			
			while (generation <= MAX_EVALUATIONS ) {  //ou outra condição de parada
				
				eliteQuota = MAX_ELITES; 
				elites = new ArrayList <Integer> ();
				elitesValues = new ArrayList <Double> ();

				double previousBest = menorCusto;
				
				for (int i = 0; i < POPULATION_SIZE; i++) {	//evaluation
					
					tempCusto = calculaCustoComProbArestas(i,false,false);
					
					////////////ELITISMO AQUI
					if(eliteQuota > 0) {  //adiciona no vetor de elites até não ter espaço
						elites.add(i);
						elitesValues.add(tempCusto);
						eliteQuota--;
					}
					
					else { //caso nao haja mais espaco, remove o pior
						
						elites.add(i);
						elitesValues.add(tempCusto);
						int worstIndex = -1;
						switch (generationReplacement) { //reaproveita o vetor de elites 
							case "Generational":
								
								worstIndex = elitesValues.indexOf(Collections.max(elitesValues));
							break;
							
							case "SteadyState":
								worstIndex = elitesValues.indexOf(Collections.min(elitesValues));
							break;
							
							default:
								return Double.NaN;
						
						}
				
						elites.remove((int) worstIndex );
						elitesValues.remove((int) worstIndex );
						
					}
					
					if (tempCusto < menorCustoLocal[i]) { // É localBest

						for (int j = 0; j < N; j++) {
							localBest[i][j] = posPart[i][j];
						}
						menorCustoLocal[i] = tempCusto; 

						if (tempCusto < menorCusto) { // É globalBest
							globalBest = i;
							
							menorCusto = tempCusto;
						}
					}

				}
				
				if (previousBest-menorCusto > 0 && debug) {
					System.out.println("Melhor da geracao "+ generation + ": " +funcaoFitness+"("+ generation +") = " + menorCusto + " " +(previousBest-menorCusto));				
					functionProgressStatistics += "" + generation + " " + menorCusto + " " + (previousBest-menorCusto) + "\n";
				}
				if(generation == MAX_EVALUATIONS) break;
				
				//evo[generation] = calcGEH(false);
				//evo2[generation] = menorCusto;

				for (int i = 0; i < pairsOfChildren ; i +=2  ) {//instancia a próxima geração
					
					int father, mother;
					ArrayList<Double> fatherGenes1stPart = new ArrayList<Double>();
					ArrayList<Double> motherGenes1stPart = new ArrayList<Double>();
					ArrayList<Double> fatherGenes2ndPart = new ArrayList<Double>();
					ArrayList<Double> motherGenes2ndPart = new ArrayList<Double>();
					
					int sonA = i % POPULATION_SIZE;  //caso a populacao seja impar, o sonB ocupará a posição de um outro filho da nova geração
					int sonB = (i+1) % POPULATION_SIZE; 
					father = getParentSelectionGA();
					mother = getParentSelectionGA();
					
					if (random.nextDouble() <= this.CROSSOVER_PROBABILITY) { //se houver crossover
						while(father == mother){
							father = getParentSelectionGA();
							mother = getParentSelectionGA();
						}
						
						//crossover
						switch (crossoverGA) {
							case "singlepoint":
								//**SINGLE POINT CROSSOVER
								int crossoverPivot = random.nextInt(N);	   // posição aleatória que dividirá os genes                            

								for (int j = 0; j <= crossoverPivot ; ++j) { //obtem primeira parte dos genes
									fatherGenes1stPart.add(posPart[father][j]);
									motherGenes1stPart.add(posPart[mother][j]);

								}

								for (int j = crossoverPivot + 1; j < N ; ++j) { //obtem segunda parte dos genes
									fatherGenes2ndPart.add(posPart[father][j]);
									motherGenes2ndPart.add(posPart[mother][j]);
								}

								motherGenes1stPart.addAll(fatherGenes2ndPart); //concatena pai e mae
								fatherGenes1stPart.addAll(motherGenes2ndPart); //resultado = 2 filhos
													
							break;
							
							case "intermediate":
								///////////////////////////////
					            //INTERMEDIATE CROSSOVER    
								//Algorithm 29 Intermediate Recombination (Essentials of Metaheuristics)
								double alpha;
				                double complementAlpha;
				                double sonAGene = Double.NaN;
				               	double sonBGene = Double.NaN;
				               	
				                	for (int j = 0; j < N ; ++j) { //obtem primeira parte dos genes
				                	// Metaheuristics essentials	-0.25 .. 1.25
				                		
				                		do {
					                		alpha = (random.nextDouble() * 1.50) - 0.25;
					                    	complementAlpha = 1 - alpha;
					                		sonAGene = 0.0;
							                sonBGene = 0.0;
							               
					                		if (!parODcoberto[j]) {
												sonAGene = (alpha * posPart[father][j]) + complementAlpha*posPart[mother][j];
							                	sonBGene = ( (complementAlpha * posPart[father][j]) + alpha*posPart[mother][j]);
					                		}
				                		
				                		} while (sonAGene < 0 && sonBGene < 0);
				                		
			                			fatherGenes1stPart.add( sonAGene );
					                	motherGenes1stPart.add( sonBGene );
				
									}
				                	//valores precisam ser > 0
									//////////////////////////////////
				                	///////////////////////////////////////////
							break;			
					
						}
												
						for (int j = 0; j < N; ++j) { //instancia os filhos	
							posMutantPart[sonA][j] = motherGenes1stPart.get(j);
							posMutantPart[sonB][j] = fatherGenes1stPart.get(j);
						}
						
					}
					
					else {
						
						for (int j = 0; j < N; ++j) { //instancia clones dos pais
							posMutantPart[sonA][j] = posPart[mother][j];
							posMutantPart[sonB][j] = posPart[father][j];
						}
					}
					 
					//mutacao
					double mutationValue;
					
					
					switch (mutationGA) {
						case "MaxEquals2Uniform":
							/////////////////////////////////////////
						//esta mutação altera um gene para no máximo o seu dobro
						/////////////////////////////////////////
						for (int j = 0; j < N; ++ j) {
							if (!parODcoberto[j]) continue;
							
							mutationValue = random.nextDouble();			//e no mínimo sua metade.
							
							if (mutationValue <= MUTATION_PROBABILITY) { //mutação altera 1 gene
				
								posMutantPart[sonA][j] *= 0.5 + (random.nextDouble() * 1.5);
							}
				
							mutationValue = random.nextDouble();
				
							if (mutationValue <= MUTATION_PROBABILITY) { //mutação para o filhoB
									
								posMutantPart[sonB][j] *= 0.5 + (random.nextDouble() * 1.5);
				
							}
						}
						////////////////////////////////////////
						////////////////////////////////////////////
						break;
						
						case "MaxEquals3Gaussian":
							////////GAUSSIAN MUTATION////////////
							 ////////dentro do primeiro desvio padrao, tende a dividir por numeros entre 0 e 1//////
							  ///////fora deste desvio, tende a multiplicar por numeros entre 1 e 3
							double gaussian;
							for (int j = 0; j < N; ++ j) {
								if (!parODcoberto[j]) continue;
								mutationValue = random.nextDouble();	
											//e no mínimo sua metade.
								
								if (mutationValue <= MUTATION_PROBABILITY) { //mutação altera 1 gene

									gaussian = random.nextGaussian();
									posMutantPart[sonA][j] *= Math.abs(gaussian);
								}
								mutationValue = random.nextDouble();	
								
			
								if (mutationValue <= MUTATION_PROBABILITY) { //mutação para o filhoB
		 							
									gaussian = random.nextGaussian();
									posMutantPart[sonB][j] *= Math.abs(gaussian);
			
								}
							}
							///////////////////////////////////////
							////////////////////////////////////////
						break;
						
						
						case "MaxEquals1500Uniform":
							////////FULL RANDOM MUTATION////////////
							for (int j = 0; j < N; ++ j) {
								if (!parODcoberto[j]) continue;
								
								mutationValue = random.nextDouble();			//e no mínimo sua metade.
								
								if (mutationValue <= MUTATION_PROBABILITY) { //mutação altera 1 gene

									posMutantPart[sonA][j] = random.nextDouble()*1500;
								}
			
								mutationValue = random.nextDouble();
			
								if (mutationValue <= MUTATION_PROBABILITY) { //mutação para o filhoB
		 							
									posMutantPart[sonB][j] =  random.nextDouble()*1500;
			
								}
							}
							///////////////////////////////////////
							////////////////////////////////////////
						break;
						
						case "MaxEquals2UniformSingleMutation":
							//// ONE MUTATION
							mutationValue = random.nextDouble();			//e no mínimo sua metade.
							if (mutationValue <= MUTATION_PROBABILITY) { //mutação altera 1 gene
								int mutationIndex;

								do {
									mutationIndex = random.nextInt(N);
								} while (!parODcoberto[mutationIndex]);
								
								
								posMutantPart[sonA][mutationIndex] *= 0.5 + (random.nextDouble() * 1.5);

							}

							mutationValue = random.nextDouble();

							if (mutationValue <= MUTATION_PROBABILITY) { //mutação para o filhoB
								int mutationIndex;
								do {
									mutationIndex = random.nextInt(N);
								} while (!parODcoberto[mutationIndex]);

								posMutantPart[sonB][mutationIndex] *= 0.5 + (random.nextDouble() * 1.5);

							}
							
							///////////////////////////
						break;	
					}
					fatherGenes1stPart = null ;
					motherGenes1stPart = null ;
					fatherGenes2ndPart = null ;
					motherGenes2ndPart = null ;
				}
				
				switch (generationReplacement) {
					case "Generational" :
						for (int i = 0; i < MAX_ELITES; ++i) { //elites tomam o lugar dos primeiros elementos do vetor de filhos
							int eliteNextIndex = i;
							int elitePreviousIndex = elites.get(i);
							//System.out.println("Elite "+elitePreviousIndex+ " tomou o lugar de "+eliteNextIndex );
								for (int j = 0; j < N; ++j) {
								posPart[eliteNextIndex][j] = posPart[elitePreviousIndex][j];
							}
							
						}
						
						for (int i = MAX_ELITES; i < POPULATION_SIZE; ++i) {
							for (int j = 0; j < N; ++j) {
								posPart[i][j] = posMutantPart[i][j];
							}
							
						}
					break;
					
					case "SteadyState" : //elimina os 2 piores membros da população
						
						eliminatedA = elites.get(0);
						eliminatedB = elites.get(1);
						
						
						/*eliminatedA = random.nextInt(POPULATION_SIZE);
						
						do {
							eliminatedB = random.nextInt(POPULATION_SIZE);
						}while(eliminatedA == eliminatedB);
						
						*/
						for (int j = 0; j < N; ++j) {
							posPart[eliminatedA][j] = posMutantPart[0][j];
						}
						
						for (int j = 0; j < N; ++j) {
							posPart[eliminatedB][j] = posMutantPart[1][j];
						}
						
					break;			
				
				}

				generation++;
			} //fim do laço principal

			if (r < runs) {
				data[r] = vetorToArray(evo);
				data2[r] = vetorToArray(evo2);
				maeIn[r] = maeI;
				mae[r] = calcMAE();
				rmseIn[r] = rmseI;
				rmse[r] = calcRMSE();
				gehIn[r] = gehI;
				geh[r] = calcGEH(r==2);
				percGeh[r] = this.percGehAbaixo5;
				fitnessIn[r] = fI;
				fitness[r] = menorCustoLocal[globalBest];
				r2links[r] = r2Global;
				r2odm[r] = calcR2Odm();
			} else {
				System.out.println("WARNING: Resultados descartados. Vetores de resultados ja estao completos.");
				return 666.0;
			}

			if (r == 0) {
				fitnessBestMod = menorCusto;
				bestMOD = localBest[globalBest];
			} else if (menorCusto < fitnessBestMod) {
				fitnessBestMod = menorCusto;
				bestMOD = localBest[globalBest];
			}

			System.out.println("RUN " + nomeTeste + " " + (r + 1) + "/" + runs + ": Menor  custo  final é " + menorCusto + printMetricasFinais(r) + " ´idBest=" + globalBest + "\n");


		} //fim da RUN   ( r < runs )
			


		if (!testeParametros) {
			minInicio = (((double) getMinutosAtual()) - minInicio) / ((double) runs);
			tempo = tempo + horaAtual().replace(",", "") + ", " + minInicio;


			System.out.println("ALERT: Salvando última MOD resposta. Não a melhor.");

			double[] respMOD = new double[N];
			for (int o = 0; o < clusters; o++) {
				for (int d = 0; d < clusters; d++) {
					respMOD[o * clusters + d] = ODmatrix.getODMatrixClustersBatch(o, d, tempoProblema, batchProblema);
				}
			}
			Double vet[] = new Double[1];
			vet[0] = 0.0;
			//rest.addResultados("MOD_t" + tempoProblema + "b" + batchProblema, 0, vet, vet, vet, vet, vet, vet, vet, vet, respMOD, "", vet, vet, vet);


		}



		if(!salvandoBackup){

			for (int a = 0; a < MAX_EVALUATIONS; a++) {
				labelValues.add(a * 1.0);
			}

			/*GeraGraficos gx = new GeraGraficos(1000, 800);
        gx.GeraGraficosLinha("Graficos", "Evolution PSO GEH", data, labelValues, labelSeries, "Iterations (s=" + POPULATION_SIZE + ";t=" + MAX_EVALUATIONS + ")", "Fitness (" + nomeTeste + ")");
        gx = new GeraGraficos(1000, 800);
        gx.GeraGraficosLinha("Graficos", "Evolution PSO F", data2, labelValues, labelSeries, "Iterations (s=" + POPULATION_SIZE + ";t=" + MAX_EVALUATIONS + ")", "Fitness (" + nomeTeste + ")");*/
			
			return fitnessBestMod;
			//return mediaVet(geh);

		}

		return Double.NaN;

	}
	
	
	
	public static ArrayList<Double> vetorToArray(Double[] ve) {
		ArrayList<Double> d = new ArrayList<>();
		d.addAll(Arrays.asList(ve));
		return d;
	}

	public double mediaVet(Double[] vet) {
		double med = 0;
		for (int a = 0; a < vet.length; a++) {
			med = med + vet[a];
		}
		return med / vet.length;
	}

	double pFrac = 0.075;
	double pRedux = 0.1;

	public void iniciaParts(double min, double max, boolean reset) {

	//	double r1 = Math.random();
		double frac = pFrac; //* Math.random()
		double redux = pRedux;  //* Math.random()

		double offset = 0;//(max - min) * 0.4 * r1;
		double offset2 = 0;//(max - min) * (0.4 + (r1 * 0.4)) * Math.random();

		for (int p = 0; p < posPart.length; p++) {
			if (!reset || (reset && p != globalBest && (random.nextDouble()>this.resetPerct))) //iniciar part SE (não for reset) OU se for reset, se não for globalBest e com 70% de chance
			{
				
				for (int x = 0; x < N; x++) {

					//if (!ODparIndArestaMaisMov[(x - (x % clusters)) / clusters][x % clusters].isEmpty())
					if (parODcoberto[x]) {
						posPart[p][x] = (min + offset + (random.nextDouble() * (max - min - offset2)));
						if (random.nextDouble() > frac) {
							posPart[p][x] = posPart[p][x] * redux;
						}

					} else {
						posPart[p][x] = 0;
					}
					
					if (posPart[p][x] == Double.NaN ||  Double.isInfinite( posPart[p][x] )) {
						System.out.println(posPart[p][x] + " foi detectado. Tentando gerar um novo valor");
						x--; //tenta atribuir novo valor novamente
					}
					
					//   if(Math.random()>=0.8 && p!=globalBest){
					//       localBest[p] = posPart[p];
					//      menorCustoLocal[p] = calculaCustoGLS(p);
					//  }                 
				}
			}

			if (!probArestaODdefinidas) {
				double min1, max1;
				for (int x = N; x < (N + doParOD.size()); x++) {
					int k = virtualSensors.getIndiceArestaKind(daAresta.get(x - N));
					min1 = minKind.get(k);
					max1 = maxKind.get(k);
					posPart[p][x] = min1 + (max1 - min1) * random.nextDouble();        //(Math.random()*maiorProbabilidade*0.7);
				}
			}

			if (p == 0) { // para uso de matrix priori  OU warmStartUp

				if (warmStartup && !reset) {
					algebParaWarmUp = true;
					//roda algeb

					//algeb ja deixa resultado na particula 0

					algebParaWarmUp = false;
				}

				if (useMatrixPriori && !reset) {
					for (int x = 0; x < N; x++) {
						int id2 = x % clusters;
						int id1 = (x - id2) / clusters;
						posPart[p][x] = ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori);
						// posPart[p][x] = ODmatrix.getODMatrixClustersBatch(id1, id2, tempoProblema, batchProblema);
					}
				}
			}

		}

	}

	public double calcRMSE() {

		int id1, id2;
		double LocalRMSE = 0.0;
		double somaTrips = 0.0;

		for (int n = 0; n < N; n++) { //soma das diferencas ao quadrado
			id2 = n % clusters;
			id1 = (n - id2) / clusters;
			LocalRMSE += (localBest[globalBest][n] - ODmatrix.getODMatrixClustersBatch(id1, id2, tempoProblema, batchProblema))
					* (localBest[globalBest][n] - ODmatrix.getODMatrixClustersBatch(id1, id2, tempoProblema, batchProblema));
			somaTrips += ODmatrix.getODMatrixClustersBatch(id1, id2, tempoProblema, batchProblema);
		}

		//dividido por N, raiz quadrada
		LocalRMSE = Math.sqrt(LocalRMSE / N);
		somaTrips = somaTrips / N;

		return 100 * (LocalRMSE / somaTrips);
	}

	public double calcGEH(boolean gerarScatter) {

		for (int x = 0; x < N; x++) {
			posPart[globalBest][x] = localBest[globalBest][x];
		}

		this.calculaCusto(globalBest, true, gerarScatter);
		return gehGlobal;

	}

	public double calcMAE() {

		int cmais = 0, cmenos = 0;
		DescriptiveStatistics d = new DescriptiveStatistics();

		int id1, id2;
		double LocalMAE = 0.0;

		for (int n = 0; n < N; n++) { //soma das diferencas ao quadrado
			id2 = n % clusters;
			id1 = (n - id2) / clusters;

			if (localBest[globalBest][n] > ODmatrix.getODMatrixClustersBatch(id1, id2, tempoProblema, batchProblema)) {
				cmais++;
				d.addValue(localBest[globalBest][n] - ODmatrix.getODMatrixClustersBatch(id1, id2, tempoProblema, batchProblema));
			} else if (localBest[globalBest][n] < ODmatrix.getODMatrixClustersBatch(id1, id2, tempoProblema, batchProblema)) {
				cmenos++;

			}

			LocalMAE += Math.sqrt((localBest[globalBest][n] - ODmatrix.getODMatrixClustersBatch(id1, id2, tempoProblema, batchProblema))
					* (localBest[globalBest][n] - ODmatrix.getODMatrixClustersBatch(id1, id2, tempoProblema, batchProblema)));                //modulo da diferença por par OD
		}

		//System.out.println("MAE: Para mais em " + cmais + ", para menos em " + cmenos + "; Mean p+: " + d.getMean() + "; p50 p+: " + d.getPercentile(50) + "; p5 p+: " + d.getPercentile(5) + "; p95 p+: " + d.getPercentile(95));
		return LocalMAE / N;         //sobre número de pares OD
	}

	int ultimaMelhoriaGA;

	public double calculaCustoGA(int p, int ger) {

		double f = calculaCusto(p, false, false);

		double f2 = (10000.0 / (f));
		//double f2 = 250000 - f;

		/*for (int mults = 0; mults < 1; mults++) {
            f2 = f2 * 10000.0 / f;
        }*/
		/*double f = calculaCusto(p, true, false);
        double f2;
        if(gehGlobal<10)
        f2 = 10* (10 - gehGlobal);
        else
            f2 = 0;*/
		if (f2 < melhorFitness) {
			double tempCusto = f2;
			double anteriorCusto = melhorFitness;
			System.out.println("Melhor da geracao "+ ger + ": " +funcaoFitness+"("+ ger +") = " + tempCusto + " " +(anteriorCusto-tempCusto));
			
			melhorFitness = f2;
			ultimaMelhoriaGA = ger;
		} else if (f2 < 0) {
			System.out.println("ALERT: Fitness < 0 = " + f2 + "; (calculaCustoGA)");
			f2 = 0;
		}

		return f2;

	}
	

	public double calculaCusto(int particleIndex, boolean calcGeh, boolean salvarScater) {

		if (probArestaODdefinidas) {
			double fitnessResult = calculaCustoComProbArestas(particleIndex, calcGeh, salvarScater);
			
			//mostra resultado de fitness
			//System.out.println(getFuncaoFitness() +" : "+ fitnessResult);
		
			return fitnessResult; 
			
		} else {
			return calculaCustoSemProbArestas(particleIndex);
		}

	}

	public double calculaCustoSemProbArestas(int part) {

		/*if (debug && part == 0) {System.out.println("PROC: Iniciando calculo de fitness... " + horaAtual());}*/
		double custoSoma1 = 0.0;
		double custoSoma2 = 0.0;
		int id1, id2;
		int tamanhoODPar = doParOD.size();
		//int tempoProri, int batchPriori, int tempoProblema, int batchProblema;
		double[] fluxosSens = new double[arestasSensor];
		//sensorAresta[], sensorNodeFrom[], sensorNodeTo[]

		//por todos pares ARESTA/PAR_O-D, adiciona no fluxo da aresta o Ti do par_OD * probabilidade de uso da aresta pelo par o-d
		for (int z = 0; ++z < tamanhoODPar;) {
			fluxosSens[daAresta.get(z)] += posPart[part][doParOD.get(z)] * posPart[part][N + z];

			//if(posPart[part][doParOD.get(z)] * posPart[part][N+z] < 0)
			//   System.out.println("<0 = posPart[part]["+doParOD.get(z)+"]= "+ posPart[part][doParOD.get(z)]+" *  posPart[part][N+"+z+"] = "+ posPart[part][N+z] );
			/*if(daAresta.get(z)==3604){
                        System.out.println("("+co++ +", +"+posPart[part][doParOD.get(z)] * posPart[part][N+z]+")fluxosSens[3604] = "+fluxosSens[3604]);
                    }*/
		}


		/*if (debug && part == 0) {
            System.out.println("OK: Calculou fluxosSens " + horaAtual());
        }*/
		//   stats(fluxosSens);
		//todos os pontos OD  (estimado - priori)^2
		if (useMatrixPriori) {
			/*for (int n = 0; n < N; n++) {
                id2 = n % clusters;
                id1 = (n - id2) / clusters;


                if (useVariance) {
                    custoSoma1 += ((posPart[part][n] - ((double) ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori)))
			 * (posPart[part][n] - ((double) ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori))))
                            / (ODmatrix.getODMatrixClusterVariance(id1, id2, tempoProblema) * 1000);
                } else {
                    custoSoma1 += ((posPart[part][n] - ((double) ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori)))
			 * (posPart[part][n] - ((double) ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori)))) / 1000;
                }

            }*/

		}

		DescriptiveStatistics dx = new DescriptiveStatistics();
		//todos os fluxos  (variancia aresta Tproblema)*(consequência - virtualSensors)^2
		for (int ar = 0; ar < arestasSensor; ar++) {

			/*if(part==0){
                System.out.println("(fluxosSens["+ar+"] - vtSnr.getContArestaBatch(sensorAresta["+ar+"], tPriori, bPriori)) ^2 ="
                        + " ("+fluxosSens[ar]+" - "+virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori)+")^2  "
                                + " = " + ((fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori))* (fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori)) ));             
            } */
			if (useVariance) {
				custoSoma2 += mod(((fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema))
						* (fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema)))
						/ (virtualSensors.getArestaVariance(sensorAresta[ar], tempoProblema) * 1000));
			} else {
				custoSoma2 += mod(((fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema))
						* (fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema))) / 1000);
			}

			if (fluxosSens[ar] < 0) {
				System.out.println("ERRO: fluxoSens[" + ar + "]=" + fluxosSens[ar]);
			}
			if (virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema) < 0) {
				System.out.println("ERRO: virtualSensors.getContArestaBatch(sensorAresta[" + ar + "])=" + fluxosSens[ar]);
			}

			//   if((fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori))
			//             * (fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori))
			//           /   ( virtualSensors.getArestaVariance(sensorAresta[ar], tempoProblema) * 10000) <0)
			if (/*part == 0 &&*/debug) {
				//  System.out.println("DIF Fluxos: ("+fluxosSens[ar]+"-"+virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori)+")^2 / "+virtualSensors.getArestaVariance(sensorAresta[ar], tempoProblema));
				dx.addValue(fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori));
			}
		}

		if (debug/* && part == 0*/) {
			printStats(dx, "Desvio fluxos");
			//System.out.println("OK: Calculou erros de fluxosSens. END " + horaAtual());
			//debug = false;
		}
		// System.out.println("(p="+part+") ODM E:  "+(custoSoma1/N)+"; Fluxos E: "+(custoSoma2/arestasSensor)+" ps. mudar linha 280");
		//return (custoSoma1 / N + custoSoma2 / arestasSensor);
		return (custoSoma1 + custoSoma2);
	}

	double percGehAbaixo5;
	
	public String obtemDeltaFluxos(int particula) {
		int probs = doParOD.size();	

		double[] fluxosSens = new double[arestasSensor];

		for (int z = 0; ++z < probs;) {
			if (posPart[particula][doParODV[z]] > 0) {
				fluxosSens[doIndiceSensorV[z]] += posPart[particula][doParODV[z]] * prob_od_aV[z];
			}
		}
		
		double auz;
		int contAZ = 0;
		double folga = 0;

		//todos os fluxos  (variancia aresta Tproblema)*(consequência - virtualSensors)^2
		String deltaFluxos ="";
		
		
		for (int z = 0; z < probs; ++z) {
			//fluxosSens[daAresta.get(z)] += posPart[part][doParOD.get(z)] * prob_od_a.get(z);
			//fluxosSens[doIndiceSensor.get(z)] += posPart[part][doParOD.get(z)] * prob_od_a.get(z);
			if (posPart[particula][doParODV[z]] > 0) {
				fluxosSens[doIndiceSensorV[z]] += posPart[particula][doParODV[z]] * prob_od_aV[z];
			}
		}
		
		
		for (int ar = 0; ar < arestasSensor; ar++) {
			deltaFluxos += String.valueOf(fluxosSens[ar]/2) + "," + String.valueOf(virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema) ) +"\n"	;
			
			
		}
		
		/*
		int id1,id2;
		for (int n = 0; n < N; n++) {
			id2 = n % clusters;
			id1 = (n - id2) / clusters;
			deltaFluxos += String.valueOf(posPart[particula][n]) + "," + String.valueOf(ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori) ) +"\n"	;
				

		}
		 */
		return deltaFluxos;
}
	

	public double calculaCustoComProbArestas(int part, boolean calcGeh, boolean gerarScatter) {

		/*if (debug && part == 0) {System.out.println("PROC: Iniciando calculo de fitness... " + horaAtual());}*/
		double custoSoma1 = 0.0;
		double custoSoma2 = 0.0;
		double gehX = 0.0;
		double m, c;
		int id1, id2;
		int probs = doParOD.size();
		
		// calcGeh = true;

		//int tempoProri, int batchPriori, int tempoProblema, int batchProblema;
		double[] fluxosSens = new double[arestasSensor];
		//sensorAresta[], sensorNodeFrom[], sensorNodeTo[]
		double[] real = new double[arestasSensor];

		int reserva = -1;
		if (juncaoProbArestas) {
			reserva = tempoPriori;
			tempoPriori = discretTemporal;
		}

		//solucao alternativa 
		if (!calcGeh) {
			for (int z = 0; ++z < probs;) {
				//fluxosSens[daAresta.get(z)] += posPart[part][doParOD.get(z)] * prob_od_a.get(z);
				//fluxosSens[doIndiceSensor.get(z)] += posPart[part][doParOD.get(z)] * prob_od_a.get(z);
				fluxosSens[doIndiceSensorV[z]] += posPart[part][doParODV[z]] * prob_od_aV[z];
				
			}
		} else {
			percGehAbaixo5 = 0;
			for (int z = 0; ++z < probs;) {
				//fluxosSens[daAresta.get(z)] += posPart[part][doParOD.get(z)] * prob_od_a.get(z);
				//fluxosSens[doIndiceSensor.get(z)] += posPart[part][doParOD.get(z)] * prob_od_a.get(z);
				if (posPart[part][doParODV[z]] > 0) {
					fluxosSens[doIndiceSensorV[z]] += posPart[part][doParODV[z]] * prob_od_aV[z];
				}
			}
		}
		//(vetor maiores arestas, não do indice geral de arestas)
		/*for (int o = 0; o < clusters; o++) {
            for (int d = 0; d < clusters; d++) //para todos os pares OD
            {
                //for (int ars = 0; ars < ODparIndArestaMaisMov[o][d].size(); ars++) //percorre array de arestas que par OD passa

                  for (int ars = 0; ars < ODparIndArestaMaisMovENCONTRADOS[o][d].size(); ars++) {

                    fluxosSens[ODparIndArestaMaisMov[o][d].get(ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars))]
                            += (((double) posPart[part][o * clusters + d])
		 * ODmatrix.getODParArestaCont(o, d, tempoPriori, ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars))); //aresta acrescentada pela part*fator(aresta)   

                    if (((double) posPart[part][o * clusters + d])
		 * ODmatrix.getODParArestaCont(o, d, tempoPriori, ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars)) < 0) {
                        System.out.println("fluxosSens[" + ODparIndArestaMaisMov[o][d].get(ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars)) + "] += "
                                + "" + posPart[part][o * clusters + d] + " * " + ODmatrix.getODParArestaCont(o, d, tempoPriori, ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars)) + " "
                                + "== " + (((double) posPart[part][o * clusters + d]) * ODmatrix.getODParArestaCont(o, d, tempoPriori, ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars))));
                    }

                    //System.out.println("fluxosSens["+ODparIndArestaMaisMov[o][d].get(ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars))+"] += "
                    //      + ""+posPart[part][o * clusters + d]+" * " + ODmatrix.getODParArestaCont(o, d, ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars) ) +  " "
                    //               + "== "+(((double) posPart[part][o * clusters + d]) * ODmatrix.getODParArestaCont(o, d, ODparIndArestaMaisMovENCONTRADOS[o][d].get(ars))));                    
                } 
            }
        }*/
		if (juncaoProbArestas) {
			tempoPriori = reserva;
		}

		/*if (debug && part == 0) {
            System.out.println("OK: Calculou fluxosSens " + horaAtual());
        }*/
		//   stats(fluxosSens);
		//todos os pontos OD  (estimado - priori)^2
		if (useMatrixPriori) {
			for (int n = 0; n < N; n++) {
				id2 = n % clusters;
				id1 = (n - id2) / clusters;

				/* if(part==0){  //debug tempo real
                    System.out.println("((posPart[0]["+n+"] - ODmatrix.getODMatrixClustersBatch("+id1+", "+id2+", tPriori, bPriori))^2  = ("+posPart[part][n]+" - "+ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori)+")^2 =  "+ ((posPart[part][n] - ((double)ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori)))
				 * (posPart[part][n] - ((double)ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori)))));                    
                } */
				if (useVariance) {
					custoSoma1 += ((posPart[part][n] - ((double) ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori)))
							* (posPart[part][n] - ((double) ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori))))
							/ (ODmatrix.getODMatrixClusterVariance(id1, id2, tempoProblema) * 1000);
				} else {
								
					double deltaV = (posPart[part][n] - ((double) ODmatrix.getODMatrixClustersBatch(id1, id2, tempoPriori, batchPriori)));
					
					custoSoma1 += (deltaV * deltaV)  / 1000;

				}

			}

		}
		//GEH = SQRT ( 2(M-C)^2 / (M+C) )
		DescriptiveStatistics dx = new DescriptiveStatistics();
		double auz;
		int contAZ = 0;
		double folga = 0;

		//todos os fluxos  (variancia aresta Tproblema)*(consequência - virtualSensors)^2
		for (int ar = 0; ar < arestasSensor; ar++) {

			/*if(part==0){
                System.out.println("(fluxosSens["+ar+"] - vtSnr.getContArestaBatch(sensorAresta["+ar+"], tPriori, bPriori)) ^2 ="
                        + " ("+fluxosSens[ar]+" - "+virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori)+")^2  "
                                + " = " + ((fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori))* (fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori)) ));             
            } */
			if (useVariance) {
				custoSoma2 += mod(((fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema))
						* (fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema)))
						/ (virtualSensors.getArestaVariance(sensorAresta[ar], tempoProblema) * 1000));
				
				
			} else if(getFuncaoFitness().equals("reg")) {
				double modDeltaV = Math.abs(fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema));
				custoSoma2 += (modDeltaV * modDeltaV) / 1000;
			}

			if (calcGeh || getFuncaoFitness().equals("geh")) {
				
				//calculo de GEH(z)

				m = fluxosSens[ar];
				c = virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema);
				double mMinusc = m - c;
				double mPlusc = m + c;
				
				if (mPlusc > 0) {
					auz = Math.sqrt((2 * mMinusc * mMinusc) / mPlusc);
					gehX = gehX + auz;
					if (auz < 5.0) {
						percGehAbaixo5++;
					}
					contAZ++;
				}
				real[ar] = c;

			} else if (getFuncaoFitness().equals("folga")){

				folga = folga + Math.abs(fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema));

			}

			if (fluxosSens[ar] < 0) {
				System.out.println("ERRO: fluxoSens[" + ar + "]=" + fluxosSens[ar]);
			}
			if (virtualSensors.getContArestaBatch(sensorAresta[ar], tempoProblema, batchProblema) < 0) {
				System.out.println("ERRO: virtualSensors.getContArestaBatch(sensorAresta[" + ar + "])=" + fluxosSens[ar]);
			}

			//   if((fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori))
			//             * (fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori))
			//           /   ( virtualSensors.getArestaVariance(sensorAresta[ar], tempoProblema) * 10000) <0)
			if (part == 0 && debug) {
				//  System.out.println("DIF Fluxos: ("+fluxosSens[ar]+"-"+virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori)+")^2 / "+virtualSensors.getArestaVariance(sensorAresta[ar], tempoProblema));
				dx.addValue(fluxosSens[ar] - virtualSensors.getContArestaBatch(sensorAresta[ar], tempoPriori, batchPriori));
			}
		}

		//if (debug && part == 0) {
			//printStats(dx, "Desvio fluxos");
			//System.out.println("OK: Calculou erros de fluxosSens. END " + horaAtual());
			//debug = false;
		//}
		// System.out.println("(p="+part+") ODM E:  "+(custoSoma1/N)+"; Fluxos E: "+(custoSoma2/arestasSensor)+" ps. mudar linha 280");
		//return (custoSoma1 / N + custoSoma2 / arestasSensor);


		if(testeRedeFechada && gerarScatter){
			if(ger==null){    
				ger = new geradordeRede2();

			}
			//ger.setMape(mape);
			//ger.setFluxosParaBPR(fluxosSens);
			//ger.estatisticasEngarrafamentoVias(fluxosSens, sensorAresta); 
		}


		if (calcGeh || getFuncaoFitness().equals("geh")) {
			//somatório de todos os GEH(z)
			gehGlobal = gehX / ((double) arestasSensor);
			r2Global = this.calcR2(fluxosSens, real);
			percGehAbaixo5 = percGehAbaixo5 / contAZ;


			if(getFuncaoFitness().equals("geh")) {
	
				return gehGlobal;
			}
				


			// System.out.println("GEH: "+gehX/((double)arestasSensor)+" = "+gehX+"/"+arestasSensor);
		}

		if (getFuncaoFitness().equals("folga")) {
			
			return folga;
		}
			

		//  return gehGlobal;
		// reg
	
		
		return (custoSoma1 + custoSoma2);
	}

	double gehGlobal;
	double r2Global;
	geradordeRede2 ger;

	private double mod(double d) {

		if (d < 0) {
			return -d;
		} else {
			return d;
		}
	}

	public void descobrirODparIndArestaMaisMov() {
		ODparIndArestaMaisMov = new ArrayList[clusters][clusters];
		ODparIndArestaMaisMovENCONTRADOS = new ArrayList[clusters][clusters];

		ODparIndArestaMaisMov2 = new ArrayList[clusters][clusters];
		ODparIndArestaMaisMovENCONTRADOS2 = new ArrayList[clusters][clusters];

		int cont = 0;
		int cob = 0;
		int reserva = -1;
		if (juncaoProbArestas) {
			reserva = tempoPriori;
			tempoPriori = discretTemporal;
		}

		for (int o = 0; o < clusters; o++) {
			for (int d = 0; d < clusters; d++) {

				ODparIndArestaMaisMov[o][d] = ODmatrix.encontrarIndicesArestasVetor(o, d, tempoPriori, sensorAresta); //tempoPriori1
				ODparIndArestaMaisMov2[o][d] = ODmatrix.encontrarIndicesArestasVetor(o, d, tempoPriori2, sensorAresta);

				ODparIndArestaMaisMovENCONTRADOS[o][d] = new ArrayList<>();
				ODparIndArestaMaisMovENCONTRADOS2[o][d] = new ArrayList<>();

				if (ODparIndArestaMaisMov[o][d].isEmpty()) {
					// System.out.println("ALERT: Par OD " + o + "," + d + " sem arestas!");
					parODcoberto[o * clusters + d] = false;
				} else {
					for (int x = 0; x < ODparIndArestaMaisMov[o][d].size(); x++) {
						if (ODparIndArestaMaisMov[o][d].get(x) != -1) {
							ODparIndArestaMaisMovENCONTRADOS[o][d].add(x);

							cont++;
						}

					}
					parODcoberto[o * clusters + d] = true;
					cob++;
				}
				//pt 2

				if (ODparIndArestaMaisMov2[o][d].isEmpty()) {
					// System.out.println("ALERT: Par OD " + o + "," + d + " sem arestas!");
					//parODcoberto[o * clusters + d] = false;
				} else {
					for (int x = 0; x < ODparIndArestaMaisMov2[o][d].size(); x++) {
						if (ODparIndArestaMaisMov2[o][d].get(x) != -1) {
							ODparIndArestaMaisMovENCONTRADOS2[o][d].add(x);
							//  cont++;
						}

					}
					//parODcoberto[o * clusters + d] = true;
					//cob++;
				}
			}
		}

		if (juncaoProbArestas) {
			tempoPriori = reserva;
		}

		System.out.println("INFO: descobrirODparIndArestaMaisMov = " + cont + " casos! - " + cob + "/" + (clusters * clusters) + " pares O-D cobertos");
	}

	public String horaAtual() {
		return (new SimpleDateFormat("dd/MM, HH:mm:ss").format(Calendar.getInstance().getTime()));
	}

	public int getMinutosAtual() {
		int d = Integer.valueOf(new SimpleDateFormat("dd").format(Calendar.getInstance().getTime()));
		int h = Integer.valueOf(new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()));
		int m = Integer.valueOf(new SimpleDateFormat("mm").format(Calendar.getInstance().getTime()));
		//System.out.println("d="+d+"; h"+h+"; m="+m+" sum: "+(60*24*d + 60*h + m));
		return 60 * 24 * d + 60 * h + m;
	}

	private void stats(int[] fluxosSens) {
		DecimalFormat df2 = new DecimalFormat(".##");
		DescriptiveStatistics stats = new DescriptiveStatistics();

		for (int f = 0; f < fluxosSens.length; f++) {
			stats.addValue(fluxosSens[f]);
		}

		System.out.println("STATS: FluxosSensores (vehicles). Min: " + df2.format(stats.getMin()) + "; perc25: " + df2.format(stats.getPercentile(25)) + "; mean: " + df2.format(stats.getMean()) + "; perc50: " + df2.format(stats.getPercentile(50)) + "; perc75: " + df2.format(stats.getPercentile(75)) + "; max: " + df2.format(stats.getMax()) + ";");

	}

	/**
	 * @param useVariance the useVariance to set
	 */
	public void setUseVariance(boolean useVariance) {
		this.useVariance = useVariance;
	}

	//GA GA GA GA GA GA GA
	// Parametros do GA
	//int[][] Populacao = new int[pop * 2][15];
	double[] Fitness;
	double melhorFitness;
	int[] pai;
	int[] maeGA;
	//int contadorAvaliacoes;
	int[] ordem;

	private int Evolucao = 0;
	int ResultadoPorRodada = 0;

	boolean casaisDinamicos = false;
	//1 para sim, em que filhos recem gerados possam ser pais (maior desempenho, similar versão 1.8), 0 para que não (visão mais tradicional)

	//int [] fitg = new int[MAX_GERACOES];  
	private String texto;
	private String seqOtima;

	public void escolhaPais(int g) {

		int cont;
		double soma = 0.0;
		double menorFitness = 0.0;//Fitness[ordem[pop - 1]] * 0.95;

		for (cont = 0; cont <= (POPULATION_SIZE - 1); cont++) {
			soma += (Fitness[ordem[cont]] - menorFitness);        //soma dos fitness da populaçao
		}
		//System.out.println("("+g+")Menor Fitness = "+Fitness[ordem[pop-1]]+"; Melhor fitness = "+ Fitness[ordem[0]]);

		for (cont = 0; cont <= ((POPULATION_SIZE / 2) - 1); cont++) {
			pai[cont] = GARoleta(soma, menorFitness);
			maeGA[cont] = GARoleta(soma, menorFitness);
		}

	}

	public int GARoleta(double soma, double menorFitness) {
		int aux, GARoleta1 = (int) (random.nextDouble() * (POPULATION_SIZE - 1));
		double sorteado = soma * random.nextDouble();    //define ponto de sorteio
		//System.out.println("Soma="+soma+"; sorteado="+sorteado+" best="+Fitness[ordem[0]]+"; worst ="+Fitness[ordem[pop-1]]);
		soma = 0.0;

		for (aux = 0; aux <= (POPULATION_SIZE - 1); aux++) {
			soma += (Fitness[ordem[aux]] - menorFitness);

			if (soma >= sorteado) {     //se chegou no ponto, retorna o indice
				//System.out.println("GARoleta: "+aux+"° colocado. Index "+ordem[aux]);
				return aux;//ordem[aux];
			}
		}
		//System.out.println("ERROR: Não conseguiu definir GARoleta");
		return GARoleta1;
	}

	public int GARoletaOriginal() {
		int aux, GARoleta1 = 0;
		double soma = 0, sorteado;

		for (aux = 0; aux <= (POPULATION_SIZE - 1); aux++) {
			soma = Fitness[ordem[aux]] + soma;        //soma dos fitness da populaçao
		}

		sorteado = soma * random.nextDouble();    //define ponto de sorteio
		soma = 0;

		for (aux = 0; aux <= (POPULATION_SIZE - 1); aux++) {
			soma = soma + Fitness[ordem[aux]];

			if (soma > sorteado) {     //se chegou no ponto, retorna o indice
				return aux;
				//return ordem[aux] ??????
			}

		}
		System.out.println("ERROR: Não conseguiu definir GARoleta");
		return GARoleta1;
	}

	public double Roleta(int min, int max) {

		if (max > min) {
			return (random.nextDouble() * (max - min)) + min;
		} else {
			System.out.println("ERROR: min(" + min + ")!<max(" + max + ") PSO.Roleta()");
			return 0;
		}

	}

	public void setParamGA(int populacao, int geracoes, double crossover, double mutacao, double varMut) {

		POPULATION_SIZE = populacao;
		MAX_EVALUATIONS = geracoes;
		this.CROSSOVER_PROBABILITY = crossover;
		this.MUTATION_PROBABILITY = mutacao;
		this.varMutacao = varMut;
		System.out.println("GA PARAM: pop = " + POPULATION_SIZE + "; ger = " + geracoes + " (" + (POPULATION_SIZE * geracoes) + " avaliacoes); Crossover = " + crossover + "; Mutacao = " + mutacao + "; varMut = " + varMut);
	}

	public void setParamPSO(int numP, int iterat, double wIn, double wF, double c1, double c2, int itReset, double pReset) {

		this.POPULATION_SIZE = numP;
		this.MAX_EVALUATIONS = iterat;
		this.wIn = wIn;
		this.wF = wF;
		this.c1 = c1;
		this.c2 = c2;
		this.reset = itReset;
		this.resetPerct = pReset;

		System.out.println("PSO PARAM: POPULATION_SIZE = " + numP + "; Iterat = " + MAX_EVALUATIONS + " (" + (numP * MAX_EVALUATIONS) + " avaliacoes); Win = " + wIn + "; Wf = " + wF + "; c1 = " + c1 + "; c2 = " + c2);

	}


	public void definirArestas_NSLP(){

		if(sensoresCorrelacao)
			arestasSensor = arestasSensor*2;


		if (sensoresFluxoNotRotas) {
			virtualSensors.getArestasMaismovimentadasIndex(arestasSensor, tempoPriori);
		} else {
			if(sensoresMaisRotas)
				virtualSensors.getArestasMaisRotasIndex(ODmatrix, arestasSensor, tempoPriori);
			else
				virtualSensors.getArestasMenosRotasIndex(ODmatrix, arestasSensor, tempoPriori);
		}
		//sensorNodeTo = virtualSensors.getArestasMaismovimentadasIndexTONODE();
		sensorAresta = virtualSensors.getArestasMaismovimentadasIndexARESTA();   


		if(sensoresCorrelacao){
			arestasSensor = arestasSensor/2;
			lerArquivoOSMDat();
			sensorAresta = mape.calcNSLPcorrelacao(arestasSensor, sensorAresta, ODmatrix);//calcNSLPcorrelacao(int qtdeFinal, int [] arestasOpcao, ODMatrix odm)

		}

	}



	

	public void printSolution() {
		String z = "";

		if (ordem.length > 0) {
			for (int a = 0; a < posPart[ordem[0]].length; a++) {
				z = z + posPart[ordem[0]][a] + " ";
			}
		} else {
			for (int a = 0; a < localBest[globalBest].length; a++) {
				z = z + localBest[globalBest][a] + " ";
			}
		}

		System.out.println(z + "\n");
	}

	public void CalcAllFitness() {

		int individuo;

		for (individuo = 0; individuo <= (POPULATION_SIZE * 2 - 1); individuo++) {
			ordem[individuo] = individuo;
			Fitness[ordem[individuo]] = calculaCustoGA(ordem[individuo], 0);
			//   trocaEsquerda(individuo);
		}
		//Arrays.sort(Fitness, 0, pop * 2);
		//System.out.println("BUBBLE");
		bubble(0, POPULATION_SIZE * 2);
		//quickSort(0, pop * 2);
		
		globalBest = ordem[0];
		melhorFitness = Fitness[ordem[0]];
		System.arraycopy(posPart[globalBest], 0, localBest[globalBest], 0, N);
	}

	public void trocaEsquerda(int esc) {
		int i;

		if (esc > 0) {
			if (Fitness[ordem[esc]] > Fitness[ordem[esc - 1]]) {

				i = ordem[esc];
				ordem[esc] = ordem[esc - 1];
				ordem[esc - 1] = i;

				trocaEsquerda(esc - 1); //recursão. Continua com mesmo individuo, basicamente
			}
		} else {
			globalBest = ordem[0];
			System.arraycopy(posPart[globalBest], 0, localBest[globalBest], 0, N);

		}

	}

	public void bubble(int inicio, int fim) {
		int i;

		for (int esc = inicio; esc < fim; esc++) {
			if (esc > 0) {
				if (Fitness[ordem[esc]] > Fitness[ordem[esc - 1]]) {

					i = ordem[esc];
					ordem[esc] = ordem[esc - 1];
					ordem[esc - 1] = i;

					esc = esc - 2;//volta 2 atrás, avança um pelo for, está testando se o valor que acabou de mudar de posição vai mudar outra posição
				}
			}
		}
		//fim da ordenação
		globalBest = ordem[0];
		melhorFitness = Fitness[ordem[0]];
		System.arraycopy(posPart[globalBest], 0, localBest[globalBest], 0, N);
	}

	private void quickSort(int low, int high) {
		int i = low;
		int j = high-1;

		// pivot is middle index
		int pivot = ordem[low + (high - low) / 2];
		int swapAux;
		// Divide into two arrays
		while (i <= j) {
			/**
			 * As shown in above image, In each iteration, we will identify a
			 * number from left side which is greater then the pivot value, and
			 * a number from right side which is less then the pivot value. Once
			 * search is complete, we can swap both numbers.
			 */
			while (Fitness[ordem[i]] < Fitness[pivot]) {
				i++;
			}
			while (Fitness[ordem[j]] > Fitness[pivot]) {
				j--;
			}
			if (i <= j) {
				swapAux = ordem[i];
				ordem[i] = ordem[j];
				ordem[j] = swapAux;
				// move index to next position on both sides
				i++;
				j--;
			}
		}

		// calls quickSort() method recursively
		if (low < j) {
			quickSort(low, j);
		}

		if (i < high) {
			quickSort(i, high);
		}
	}





	public void Mutate(int mutante, boolean forceMutate) {

		/* 
        int aux1, aux2;
        int cont = 0;
        while (Math.random() < MUTATION_PROBABILITY  && cont<10) {

            boolean antes = Math.random() > 0.5;

            aux1 = (int) (Math.random() * (N));
            aux2 = (int) (Math.random() * (N));

            if (parODcoberto[aux1]) {
                if (antes) {
                    posPart[mutante][aux1] = posPart[mutante][aux1] * (1 + 0.2*Math.random()); ///  - 6 + (4 * Math.random());
                } else {
                    posPart[mutante][aux1] = posPart[mutante][aux1] * (1 - 0.2*Math.random()); ///  - 6 + (4 * Math.random());  + 6 - (4 * Math.random());
                }
                if (posPart[mutante][aux1] < 0) {
                    posPart[mutante][aux1] = 0;
                }
            }
            if (parODcoberto[aux2]) {
                if (antes) {
                    posPart[mutante][aux1] = posPart[mutante][aux1] * (1 - 0.2*Math.random()); ///  - 6 + (4 * Math.random());
                } else {
                    posPart[mutante][aux1] = posPart[mutante][aux1] * (1 + 0.2*Math.random()); ///  - 6 + (4 * Math.random());  + 6 - (4 * Math.random());
                }
                if (posPart[mutante][aux2] < 0) {
                    posPart[mutante][aux2] = 0;
                }
            }
            cont++;
        } */
		if (random.nextDouble() < MUTATION_PROBABILITY || forceMutate) {
			for (int aux = 0; aux < N; aux++) {
				if (parODcoberto[aux]) {
					if (random.nextDouble() < MUTATION_PROBABILITY) {
						posPart[mutante][aux]
								= posPart[mutante][aux]
										+ posPart[mutante][aux] * (varMutacao - 2 * random.nextDouble() * varMutacao);        //((varMutacao)*2*Math.random());

										if (posPart[mutante][aux] < 0) {
											posPart[mutante][aux] = 0;
										}

					}
				}
			}
		}

	}

	public void crossover(int g) {

		int crossPoint1, crossPoint2, casal, xx;
		int numeroFilhos = 0;
		boolean forceMutate;

		for (casal = 0; casal <= ((POPULATION_SIZE / 2) - 1); casal++) {

			if (random.nextDouble() <= CROSSOVER_PROBABILITY) {

				forceMutate = false;
				crossPoint1 = (int) Roleta(1, N / 2);
				crossPoint2 = (int) Roleta(crossPoint1 + 1, N - 1);
				//System.out.println("Crossover(0,"+crossPoint1+","+crossPoint2+","+N+") pop="+pop+";");

				for (xx = 0; xx < crossPoint1; xx++) { //gera primeira parte dos dois filhos                                             	 

					posPart[ordem[POPULATION_SIZE + numeroFilhos]][xx] = posPart[ordem[pai[casal]]][xx];
					posPart[ordem[POPULATION_SIZE + numeroFilhos + 1]][xx] = posPart[ordem[maeGA[casal]]][xx];

				}

				for (xx = crossPoint1; xx < crossPoint2; xx++) {//gera segunda parte dos dois filhos                                             	 

					posPart[ordem[POPULATION_SIZE + numeroFilhos]][xx] = posPart[ordem[maeGA[casal]]][xx];
					posPart[ordem[POPULATION_SIZE + numeroFilhos + 1]][xx] = posPart[ordem[pai[casal]]][xx];

				}

				for (xx = crossPoint2; xx < N; xx++) {  //gera terceira parte dos dois filhos                                             	 

					posPart[ordem[POPULATION_SIZE + numeroFilhos]][xx] = posPart[ordem[pai[casal]]][xx];
					posPart[ordem[POPULATION_SIZE]][xx] = posPart[ordem[maeGA[casal]]][xx];

				}

			} else {
				forceMutate = true;
				for (xx = 0; xx < N; xx++) {  //não houve crossover

					posPart[ordem[POPULATION_SIZE + numeroFilhos]][xx] = posPart[ordem[pai[casal]]][xx];
					posPart[ordem[POPULATION_SIZE + numeroFilhos + 1]][xx] = posPart[ordem[maeGA[casal]]][xx];

				}

			}

			for (int z = 0; z < 2; z++) {

				Mutate(ordem[POPULATION_SIZE + numeroFilhos + z], forceMutate);
				Fitness[ordem[POPULATION_SIZE + numeroFilhos + z]] = calculaCustoGA(ordem[POPULATION_SIZE + numeroFilhos + z], g);
				if (casaisDinamicos) {
					trocaEsquerda(POPULATION_SIZE + numeroFilhos + z); //ordena
				}
			}

			/*Mutate(ordem[pop + numeroFilhos]);
            Fitness[ordem[pop + numeroFilhos]] = calculaCustoGA(ordem[pop + numeroFilhos]);
            if (casaisDinamicos == 1) {
                trocaEsquerda(pop + numeroFilhos); //ordena
            }
            Mutate(ordem[pop + numeroFilhos + 1]);
            Fitness[ordem[pop + numeroFilhos + 1]] = calculaCustoGA(ordem[pop + numeroFilhos + 1]);
            if (casaisDinamicos == 1) {
                trocaEsquerda(pop + numeroFilhos + 1);
            }*/
			numeroFilhos = numeroFilhos + 2;

		}//Next casal

		//acabou geraçao
		/* 
		 *  Este if não contia codigo ativo.
		 * */
		//if (!casaisDinamicos) {


		// bubble(0, pop * 2);

		//quickSort(0, pop * 2);     
		/*for (casal = 0; casal < pop*2; casal++) { //apenas aproveitando a variavel
                trocaEsquerda(casal); //ordena dos primeiros filhos antes
            }*/

		//}


	}

	public static void printStats(DescriptiveStatistics d, String name) {
		DecimalFormat df2 = new DecimalFormat(".######");
		String t = "STATS: " + name + ": Min. " + df2.format(d.getMin()) + "; Mean. " + df2.format(d.getMean()) + "; Max. " + df2.format(d.getMax()) + "; ";

		for (int p = 5; p < 100; p = p + 15) {
			t = t + "p" + p + " " + df2.format(d.getPercentile(p)) + "; ";
		}

		System.out.println(t);
	}

	/**
	 * @param output the output to set
	 */


	/**
	 * @param testeRedeFechada the testeRedeFechada to set
	 */
	public void setTesteRedeFechada(boolean testeRedeFhada, Mapping m) {
		this.testeRedeFechada = testeRedeFhada;
		if(testeRedeFhada)
			mape = m;
	}

	public double calcR2Odm() {

		double[] odm = new double[N];

		for (int a = 0; a < clusters; a++) {
			for (int b = 0; b < clusters; b++) {
				odm[a * clusters + b] = ODmatrix.getODMatrixClustersBatch(a, b, tempoProblema, batchProblema);
			}
		}

		return calcR2(localBest[globalBest], odm);
	}

	public double calcR2(double[] x, double[] y) {
		
	
		
		
		double intercept, slope;
		double r2;
		double svar0, svar1;

		if (x.length != y.length) {
			throw new IllegalArgumentException("array lengths are not equal: " + x.length + " and " + y.length);
		}
		
		int n = x.length;

		// first pass
		double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
		for (int i = 0; i < n; i++) {
			sumx += x[i];
			sumx2 += x[i] * x[i];
			sumy += y[i];
		}
		double xbar = sumx / n;
		double ybar = sumy / n;

		// second pass: compute summary statistics
		double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
		for (int i = 0; i < n; i++) {
			xxbar += (x[i] - xbar) * (x[i] - xbar);
			yybar += (y[i] - ybar) * (y[i] - ybar);
			xybar += (x[i] - xbar) * (y[i] - ybar);
		}
		slope = xybar / xxbar;
		intercept = ybar - slope * xbar;

		// more statistical analysis
		double rss = 0.0;      // residual sum of squares
		double ssr = 0.0;      // regression sum of squares
		for (int i = 0; i < n; i++) {
			double fit = slope * x[i] + intercept;
			rss += (fit - y[i]) * (fit - y[i]);
			ssr += (fit - ybar) * (fit - ybar);
		}

		int degreesOfFreedom = n - 2;
		r2 = ssr / yybar;
		double svar = rss / degreesOfFreedom;
		svar1 = svar / xxbar;
		svar0 = svar / n + xbar * xbar * svar1;

		return r2;
	}

	/**
	 * @param varMutacao the varMutacao to set
	 */
	public void setVarMutacao(double varMutacao) {
		this.varMutacao = varMutacao;
	}

	/**
	 * @param sensoresFluxoNotRotas sensores por fluxo
	 * @param sensoresMaisRotas sensores mais rotas
	 * @param correlacaoSens usa correlacao
	 * @param OSMFileLocation2 local do arquivo map
	 * to set
	 */
	public void setSensoresFluxoNotRotas(boolean sensoresFluxoNotRotas, boolean sensoresMaisRotas, boolean correlacaoSens, String OSMFileLocation2) {
		this.sensoresFluxoNotRotas = sensoresFluxoNotRotas;
		this.sensoresMaisRotas = sensoresMaisRotas;
		this.sensoresCorrelacao = correlacaoSens;
		OSMFileLocation = OSMFileLocation2;
	}

	public void setMinMaxRand(int min1, int max1) {
		min = min1;
		max = max1;
	}

	/**
	 * @param max the max to set
	 */
	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * @param min the min to set
	 */
	public void setMin(int min) {
		this.min = min;
	}

	//Codigo super importante, só que não.
	private ImageIcon icone = null;

	public ImageIcon createImageIcon(String path,
			String description) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	public ImageIcon getIcone() {

		if (icone == null) {
			icone = createImageIcon("genius.jpg", "processing");
		}

		return icone;

	}

	/**
	 * @param salvandoBackup the salvandoBackup to set
	 */
	public void setSalvandoBackup(boolean salvandoBackup) {
		this.salvandoBackup = salvandoBackup;
	}

	/**
	 * @return the funcaoFitness
	 */
	public String getFuncaoFitness() {
		return funcaoFitness;
	}

	/**
	 * @param funcaoFitness the funcaoFitness to set
	 */
	public void setFuncaoFitness(String funcaoFitness) {
		this.funcaoFitness = funcaoFitness;
	}

	/**
	 * @param mape the mape to set
	 */
	public void setMape(Mapping mape) {
		this.mape = mape;
	}



	// definição NSLP

	//int [] sensorAresta = virtualSensors.getArestasMaismovimentadasIndexARESTA();


	private boolean lerArquivoOSMDat() {

		if (mape != null) {
			return true;
		}

		try {

			ObjectInputStream objectIn
			= new ObjectInputStream(
					new BufferedInputStream(
							new FileInputStream(OSMFileLocation + ".dat")));
			mape = (Mapping) objectIn.readObject();

			objectIn.close();

			System.out.println("OK: Recuperou objeto mapa de '" + OSMFileLocation + ".dat'!");
			return true;
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("ALERT: Não recuperou registros de arquivo '" + OSMFileLocation + ".dat'");
			return false;
		}
	}



}
