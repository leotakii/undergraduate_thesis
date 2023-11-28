/*
 * Copyright 2016 Jackson Antonio do Prado Lima <jacksonpradolima at gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.ufpr.inf.gres.irace.core;

import taxi.od.solver.ODMatrix;
import auxs.DE;
import auxs.PSO1;
import auxs.geradordeRede2;
import java.io.BufferedInputStream;
import taxi.od.solver.VirtualSensors;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import taxi.od.solver.Mapping;

import auxs.PSO_OLD;
/**
 * This class is a external part to use the <i>irace package</i> (for R) with
 * Java programs. This hook is the command that is executed every run and define
 * how will be executed the target program.
 *
 * <b>Details extracted from
 * http://iridia.ulb.ac.be/IridiaTrSeries/link/IridiaTr2011-004.pdf:</b>
 * <p>
 * HookRun is invoked for each candidate configuration, passing as arguments:
 * the instance, a numeric identifier, and the <i>commandline</i> parameters of
 * the candidate configuration. The numeric identifier uniquely identifies a
 * configuration within a race (but not across the races in a single iterated
 * race). The
 * <i>commandline</i> is constructed by appending to each parameter label
 * (switch), without separator, the value of the parameter, following the order
 * given in the parameter table. The program hookRun must print (only) a real
 * number, which corresponds to the cost measure of the candidate configuration
 * for the given instance. The working directory of hookRun is set to the
 * execution directory specified by the option execDir. This allows the user to
 * execute several runs of irace in parallel without the runs interfering with
 * each other.</p>
 *
 * @author Jackson Antonio do Prado Lima <jacksonpradolima at gmail.com>
 * @version 1.0
 */
public class HookRun {

    static boolean runPSO = !true;
    static boolean runGA = !runPSO;
    
    /*  PSO:
    # 1:                 2:                        3: 4:      5:
    populationSize       "--populationSize="       r  (20,1000)
    maxEvaluations       "--maxEvaluations="       c  (2000000)
    c1 "--c1=" r  (0.01,2.0)
    c2 "--c2=" r  (0.01,2.0)
    wIn "--wIn=" r  (0.04,2.0)
    wF "--wF=" r  (0.01,1.0)
    crossoverProbability "--crossoverProbability=" r  (0.01,1.0) 
    mutationProbability  "--mutationProbability="  r  (10,500) 
    */
    
    /*  GA:
    # 1:                 2:                        3: 4:      5:
    populationSize       "--populationSize="       r  (20,1000)
    maxEvaluations       "--maxEvaluations="       c  (2000000)
    c1 "--c1=" r  (0.1,0.9)
    c2 "--c2=" c  (1.0)
    wIn "--wIn=" c  (1.0)
    wF "--wF=" c  (1.0)
    crossoverProbability "--crossoverProbability=" r  (0.1,1.0) 
    mutationProbability  "--mutationProbability="  r  (0.1,1.0) 
    */

    // cd /mnt/c/Users/lucia/OneDrive/Documentos/NetBeansProjects/irace
    private static String ODMatrixFile = "porto odmatrix 7a17de24";
    private static String VirtualSensorsFile = "porto virt sen";

    private static ODMatrix ODmatrix;
    private static VirtualSensors virtualSensors;
    private static HookRunCommands jct;
    static int tempoPriori = 8;
    static int discretTemporal = 24;
    static boolean testeCwb = true;
    static int minRand = 0;
    static int maxRand = 800;
    static int numSensores = 300;
    
    static int runs = 1;

    //instalar bash ubuntu do windows
    //instalar java sdk NO UBUNTU
    //setar JAVA_HOME NO UBUNTU
    //instalar R
    //instalar pacote irace (tem que ser da versão certa)
    //instalar maven
    

    public static void main(String[] args) throws Exception {
        jct = new HookRunCommands();
        // JCommander jCommander = new JCommander(jct, args);
        // jCommander.setProgramName(HookRun.class.getSimpleName());
        //jct.directory = "execDir";

        /*for(int a=0;a<args.length;a++)
            System.out.println(args[a]);
        --candidateID
        1  args[1]
        --directory
        "./"
        --populationSize=150  args[4]
        --maxEvaluations=5000  args[5]
        --c1=0.26  args[6]
        --c2=0.68  args[7]
        --wInf=0.7  args[8]
        --wF=0.45  args[9]
        --crossoverprob=0  args[10]
        --mutationprob=0  args[11]  */
        
        String[] splitArg = args[0].split("=");
        
        for (String arg : args) {
        	System.out.println(arg);
        }
        
        for (int a =0; a < args.length; ++a) {
        	splitArg = args[a].split("=");
        	
        	if (splitArg[0].equals("--algorithm")) {
         	   
         	   jct.algorithm = splitArg[1];
         	   break;
            }
        	
        }
   
       
       
       switch (jct.algorithm) {
       		case "DE" :
       			setParamsDE(args);
       		break;
       		
       		case "PSO":
       			setParamsPSO(args);
       		break;
       		 		
       		case "GA" :
       			setParamsGA(args);
       		break;
       		
       		default:
       			System.out.println("Erro: Verifique o parâmetro --algoritmo");
       			System.exit(1);
       			
       }
        
       jct.directory = "execDir";
       
       
        if (jct.help) {
            //  jCommander.usage();
            return;
        }

        System.out.println(horaAtual()+"\n[HookRun] Parameters used:");

        System.out.println( jct.toString() );

        med = 9999999.0;
        
        
        run(jct);//irracerunner
        System.out.println("Fitness final ("+ jct.fitnessFunction +") = "+med);
        if (med > 999999.0 ) {
        	 System.out.println("HOUVE UM ERRO NUMÉRICO! SAIDA NÃO É FINITA!");
        }
        
         //salvar cX.log (Parametros utilizados)
      //  salvarTxt(/*jct.directory + "\\"*/"c" + jct.candidateId + ".log", jct.toString());
        //salvar VAR_X (Melhor solução encontrada)
        salvarTxt(/*jct.directory + */"VAR_" + jct.candidateId, alg.bestMODToString());
       // salvarTxt(/*jct.directory + */"VARPROB_" + jct.candidateId, alg.probODToString());
        
      //salvarTxt(/*jct.directory + */"VARFLUX_" + jct.candidateId, alg.fluxosODToString());
      //  if(jct.statistics == true) {
        	salvarTxt("DELTA_"+jct.candidateId,alg.obtemDeltaFluxos(PSO1.globalBest));
        //}
        
        //salvar TIME_X (Tempo de execuçao)
        salvarTxt(/*jct.directory + */"TIME_" + jct.candidateId, alg.getTempoExec() + "");
        salvarTxt(/*jct.directory + */"TIME_FUN_" + jct.candidateId, alg.getTempoExec() + ""); //????? iRace pediu. Não sei o que precisa nesse arquivo
        //salvar FUN_X (Fitness encontrados)
        
        salvarTxt(/*jct.directory + */"FUN_" + jct.candidateId, med + "");
        
        //criando arquivo
        salvarTxt(/*jct.directory + */"c" + jct.candidateId + ".dat", (med) + "");
        
        //gera seeds para os testes estatisticos
     //   salvarTxt(/*jct.directory + */"SEEDS_" + jct.candidateId, generate2000Seeds());
        
        System.gc();
        System.exit(0);
        
        
    }


    public static String generate2000Seeds() {
    	Random rand = new Random();
    	String seeds2000 = "";
    	for (int i = 0; i<2000; ++i) {
    		seeds2000+= String.valueOf(rand.nextLong())+"\n";
    	}
    	
    	return seeds2000;
    }
	public static double med = 0.0;
    public static PSO1 alg;
    //public static PSO_OLD alg;
    public static ArrayList<Double> results;
    
    //retirado de iRaceRunner
    public static void run(HookRunCommands jct) {

        med = 0.0;
        
        if (testeCwb) {
        	System.out.println("Teste Curitiba!");
            mudarParaEstudoDeCaso();
        } else {
        	System.out.println("Teste Porto!");
            lerArquivoODMatrixDat();
            lerArquivoVirtualSensors();
        }

        //iniciar algoritmo
        //FIXME FORMULA ORIGINAL int maxit = (int) (jct.maxEvaluations / jct.populationSize);
        
        
      //  alg = new PSO_OLD(VirtualSensorsFile, discretTemporal);
        
        alg = new PSO1(VirtualSensorsFile, discretTemporal);
        alg.setMinMaxRand(minRand, maxRand);
        alg.setTesteRedeFechada(testeCwb, null);
        alg.setUseMatrixPriori(false); //define se irá salvar a melhor matriz a cada execução.
        alg.setUseVariance(false); //estava em falso
        
        //TODO implementar DE para o problema 
        /*
        alg.setParamDE(runs, ODmatrix, virtualSensors, jct.populationSize, maxit, jct.crossoverProbability, jct.differentialWeight, numSensores, tempoPriori);
        alg.runDE();
        */
      
        
        /*
        switch (jct.algorithm) {
	        case PSO:
	        	//alg.setParamPSO
	        	//results = alg.runPSO
	        break;
	        case GA:
	        	//alg.setParamGA
	        break;
	        case DE:
	        	DE = new DE(20, 50, 0.4, 0.6, "rastrigin",true);
	        	//alg.setParamDE
	        break;
	        default:
	        	return;
	        break;
        
        }
        */
        
        
        double result = -1;
 
        jct.maxEvaluations = 3000000/jct.populationSize;
        Random random = new Random();
        switch (jct.algorithm) {
	        case "PSO" :	        	
	        	result = alg.runParametersOnPSO( jct.fitnessFunction, runs, ODmatrix, virtualSensors,jct.populationSize, jct.maxEvaluations, jct.c1, jct.c2, jct.wIn, jct.wF,jct.itReset,jct.pReset, tempoPriori, numSensores,random);
	        break;
	        
	        case "GA" :
	        	result = alg.runParametersOnGA( jct.generationReplacement ,jct.selectionGA, jct.crossoverGA, jct.mutationGA, jct.elitistRateGA, jct.fitnessFunction, runs, ODmatrix, virtualSensors, jct.populationSize, jct.maxEvaluations, jct.mutationProbability, jct.crossoverProbability, numSensores, tempoPriori,tempoPriori,random);
	        break;
	            
	        case "DE" :
	        	result = alg.runParametersOnDE( jct.fitnessFunction, runs, ODmatrix, virtualSensors, jct.populationSize, jct.maxEvaluations, jct.differentialWeight, jct.crossoverProbability, numSensores, tempoPriori,tempoPriori,random);
	        break;
        
        }

        
        med = result;
        /*
        for (int f = 0; f < results.size(); f++) {
            med = med + results.get(f);
        }
        */
     

    }

    private static void mudarParaEstudoDeCaso() {

        ODMatrixFile = "cwb odmatrix";
        VirtualSensorsFile = "cwb virt sen";
        tempoPriori = 0;
        discretTemporal = 1;
        minRand = 0;
        maxRand = 1500;//TODO Entender MIN E MAX RAND

        if (lerArquivoODMatrixDat() && lerArquivoVirtualSensors()) {
            return;
        }

        geradordeRede2 gerador = new geradordeRede2(); //TODO verificar esta classe (faz coisas místicas)
    
        System.out.println("PROC: Processamento de VirtualSensores, utilizando HARDCODE ");

        //System.out.println("PROC: Nao utiliza xml OSM! Gerando mapa por hardcode. ");
        Mapping mape = new Mapping(20, "");

        //if (!lerArquivoVirtualSensors()) {
        virtualSensors = new VirtualSensors(1, mape, 0, VirtualSensorsFile); //(String[] nodes, int contNod, double coberSensr, int discretTemporal) 
        virtualSensors.criarArestasDirecionadas(mape, 1);
        mape.cadastrarVizinhos();
        //} 

        mape.criarROIs();
        mape.salvarDat("mapa");

        //Colocando contadores nos sensores/links
        int codFrom;
        int codTo;
        for (int c = 0; c < gerador.getContArestas(); c++) {

            codFrom = mape.getNodeIndex(gerador.getFromNoC().get(c));
            codTo = mape.getNodeIndex(gerador.getToNoC().get(c));

            for (int g = 0; g < gerador.getContLink().get(c); g++) {

                virtualSensors.addContREDUX(codFrom, codTo, 0, 0);

            }
        }

        //aproveita para criar arquivo OD-Matrix
        System.out.println("PROC: Processamento de O-D Matrix. Harcoded, estudo de caso  ");

        //createODMatrix();
        ODmatrix = new ODMatrix(mape.getROIsizeM(), mape.getROInumberLat(), mape.getROInumberLon(),
                1, gerador.getNumPontosOD(), 20,
                mape.getMenorLat(), mape.getMenorLon(), 1); //(int RoiSizeM, int roiNmbLat, int roiNumbLon, int discretizacaoTemporal)

        ODmatrix.setODMatrixHardCode(gerador.getMatrizPriori(), gerador.getNumPontosOD());
        ODmatrix.definirCaminhosRedeFechada(mape, virtualSensors, gerador.getNumPontosOD());

        ODmatrix.salvarDat(ODMatrixFile);
        virtualSensors.salvarDat(VirtualSensorsFile);

    }

    private static boolean lerArquivoODMatrixDat() {

        try {

            
             ObjectInputStream objectIn =
                     new ObjectInputStream(
                     new BufferedInputStream(
                     new FileInputStream(ODMatrixFile+ ".dat")));
      ODmatrix = (ODMatrix) objectIn.readObject();

      objectIn.close();  
            
            /*try (FileInputStream arquivoLeitura = new FileInputStream(ODMatrixFile + ".dat"); ObjectInputStream objLeitura = new ObjectInputStream(arquivoLeitura)) {

                ODmatrix = (ODMatrix) objLeitura.readObject();

            }*/
            System.out.println("OK: Recuperou objeto ODMatrix de '" + ODMatrixFile + ".dat'!\n");
            return true;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("ALERT: Não recuperou registros de arquivo '" + ODMatrixFile + ".dat'");
            return false;
        }
    }

    private static boolean lerArquivoVirtualSensors() {

        try {

             ObjectInputStream objectIn =
                     new ObjectInputStream(
                     new BufferedInputStream(
                     new FileInputStream(VirtualSensorsFile + ".dat")));
      virtualSensors = (VirtualSensors) objectIn.readObject();

      objectIn.close();  
            
            /*try (FileInputStream arquivoLeitura = new FileInputStream(VirtualSensorsFile + ".dat")) {
                ObjectInputStream objLeitura = new ObjectInputStream(arquivoLeitura);
                virtualSensors = (VirtualSensors) objLeitura.readObject();
                objLeitura.close();
            }*/
            System.out.println("OK: Recuperou objeto VirtualSensors de '" + VirtualSensorsFile + ".dat'!\n");
            return true;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("ALERT: Não recuperou objeto de arquivo '" + VirtualSensorsFile + ".dat'");
            return false;
        }
    }

    public static boolean salvarTxt(String name, String conteudo) {

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(name);
            fileWriter.append(conteudo);
        } catch (IOException e) {
            System.out.println("ERROR: FileWriter de '" + name + ".");
            //e.printStackTrace();
            return false;
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                return true;
            } catch (IOException e) {
                System.out.println("ERROR: While flushing/closing fileWriter de '" + name + "'.");
                // e.printStackTrace();
                return false;
            }
        }
    }

     public static String horaAtual() {
        return (new SimpleDateFormat("dd/MM, HH:mm:ss").format(Calendar.getInstance().getTime()));
    }
     
     private static void setParamsGA(String[] args) {    
    	    for(int a=0;a<args.length;a++) {
    	     	String[] splitArg = args[a].split("=");
    	     	
    	     	
    	     	switch(splitArg[0]) {
    	     	
    	     		case "--populationSize" :
    	     			jct.populationSize = Integer.valueOf(splitArg[1]);
    	     		break;
    	     		
    	     		case "--maxEvaluations" :
    	     			//jct.maxEvaluations = Integer.valueOf(splitArg[1]);
    	     		break;
    	     		
    	     		case "--candidateId" :
    	     			jct.candidateId = Integer.valueOf(splitArg[1]);
    	     		break;
    	     		
    	     		case "--selectionGA" :
    	     			jct.selectionGA = (splitArg[1]);
    	     		break;
    	     		case "--crossoverGA" :
    	     			jct.crossoverGA = (splitArg[1]);
    	     		break;
    	     		
    	     		case "--mutationGA" :
    	     			jct.mutationGA = (splitArg[1]);
    	     		break;
    	     		
    	     		case "--elitistRateGA" :
    	     			jct.elitistRateGA = Double.valueOf(splitArg[1]);
    	     		break;
    	     		
    	     		case "--fitnessFunction" :
    	     			jct.fitnessFunction = (splitArg[1]);
    	     		break;
    	            
    	     		case "--crossoverProbability" :
    	     			jct.crossoverProbability = Double.valueOf(splitArg[1]);
    	     		break;
    	     		
    	     		case "--mutationProbability" :
    	     			jct.mutationProbability = Double.valueOf(splitArg[1]);
    	     		break;
    	     		
    	     		case "--generationReplacement":
    	     			jct.generationReplacement =  (splitArg[1]);
    	     		break;
    	     		
    	     		case "--statistics":
    	     			jct.statistics = Boolean.valueOf(splitArg[1]);
    	     		break;
    	    	           		
    	     	}
    	     	
    	     }
    	}

    	    private static void setParamsPSO(String[] args) {
    	    	for(int a=0;a<args.length;a++) {
    	         	String[] splitArg = args[a].split("=");
    	         	
    	         	
    	         	switch(splitArg[0]) {
    	         	
    	         		case "--populationSize" :
    	         			jct.populationSize = Integer.valueOf(splitArg[1]);
    	         		break;
    	         		
    	         		case "--maxEvaluations" :
    	         		//	jct.maxEvaluations = Integer.valueOf(splitArg[1]);
    	         		break;
    	         		
    	                
    	         		case "--candidateId" :
    	         			jct.candidateId = Integer.valueOf(splitArg[1]);
    	         		break;
    	         		
    	         		case "--fitnessFunction" :
    	         			jct.fitnessFunction = (splitArg[1]);
    	         		break;
    	                
    	         		case "--c1" :
    	         			jct.c1 = Double.valueOf(splitArg[1]);
    	         		break;
    	             		
    	         		case "--c2" :
    	         			jct.c2 = Double.valueOf(splitArg[1]);
    	         		break;
    	         		
    	         		case "--wIn" :
    	         			jct.wIn = Double.valueOf(splitArg[1]);
    	             		break;
    	             		
    	         		case "--wF" :
    	         			jct.wF = Double.valueOf(splitArg[1]);
    	             	break;
    	             	
    	         		case "--itReset" :
    	         			jct.itReset = Integer.valueOf(splitArg[1]);
    	             	break;
    	             		
    	         		case "--pReset" :
    	         			jct.pReset = Double.valueOf(splitArg[1]);
    	             	break;
    	             		
    	         		case "--statistics":
        	     			jct.statistics = Boolean.valueOf(splitArg[1]);
        	     		break;
    	         	}
    	         	
    	         }
    			
    		}
    	    
    	    private static void setParamsDE(String[] args) {
    	    	for(int a=0;a<args.length;a++) {
    	         	String[] splitArg = args[a].split("=");
    	         	
    	         	
    	         	switch(splitArg[0]) {
    	 	     		case "--populationSize" :
    	         			jct.populationSize = Integer.valueOf(splitArg[1]);
    	         		break;
    	         		
    	         		case "--maxEvaluations" :
    	         			//jct.maxEvaluations = Integer.valueOf(splitArg[1]);
    	         		break;
    	                
    	         		case "--candidateId" :
    	         			jct.candidateId = Integer.valueOf(splitArg[1]);
    	         		break;
    	         		
    	         		case "--fitnessFunction" :
    	         			jct.fitnessFunction = (splitArg[1]);
    	         		break;
    	         		
    	         		case "--crossoverProbability" :
    	         			jct.crossoverProbability = Double.valueOf(splitArg[1]);
    	         		break;
    	         		
    	         		case "--differentialWeight" :
    	         			jct.differentialWeight = Double.valueOf(splitArg[1]);
    	         		break;
    	         		
    	         		case "--statistics":
        	     			jct.statistics = Boolean.valueOf(splitArg[1]);
        	     		break;
    	                
    	         		
    	             		
    	         	}
    	         	
    	         }
    			
    		}
    
}
