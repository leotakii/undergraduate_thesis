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

import java.io.IOException;
//import  br.ufpr.inf.gres.irace.evaluation.indicator.HypervolumeIndicator;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;


/**
 * This class is a external part to use the <i>irace package</i> (for R) with
 * Java programs. This hook is the command that is executed every run and define
 * how will be executed the target program.
 *
 * <b>Details:</b>
 * <p>
 * When a race starts, each configuration is evaluated on the first instance by
 * means of the cost measure.
 *
 * The evaluation of a candidate configuration is done by means of a user-given
 * function or, alternatively, a user-given auxiliary program. The function (or
 * program name) is specified by the option hookRun. In this case, this file
 * defines how each candidate configuration will be evaluated.
 *
 * At each step of the race, the candidate configurations are evaluated on a
 * single instance. After each step, those candidate configurations that perform
 * statistically worse than at least another one are discarded, and the race
 * continues with the remaining surviving configurations.
 * </p>
 *
 * @author Jackson Antonio do Prado Lima <jacksonpradolima at gmail.com>
 * @version 1.0
 */
public class HookEvaluate {

    //args PSO: "prop POP/GENR", "c1", "c2", "Win", "Wf"
    //args GA: "prop POP/GENR", "mutation rate", "crossover rate"
    
    public static void main(String[] args) throws IOException {
        HookEvaluateCommands jct = new HookEvaluateCommands();
     //   JCommander jCommander = new JCommander(jct, args);
     //   jCommander.setProgramName(HookEvaluate.class.getSimpleName());
  
     /*for(int a=0;a<args.length;a++)
            System.out.println("HE"+args[a]); 
        HE--candidateId
        HE1
        HE--fileName  args[2]
        HEc1.dat
        HE--directory
        HE"./"*/
     
        jct.candidateId = Integer.valueOf(args[1].replace("HE", ""));
        jct.fileName = args[3].replace("HE", "");
        jct.directory = "execDir"; //"B:\\";
     
     
        if (jct.help) {
      //      jCommander.usage();
            return;
        }

        System.out.println("[HookEvaluate] Parameters used: \n" + jct.toString());
        
        run(jct);
    }
    
    
    
    //retirado de HyperVolumeIndicator
      public static void run(HookEvaluateCommands jct) {
      
            //String [] fun;
            //ler arquivo FUN
            //fun = lerTxt("FUN_"+jct.candidateId).split("\n");
            //atualiza arquivo com max e min:   EXTREME_POINTS_HV.properties
            //builder.save(); //minumos e máximos  - Realmente necessário?
            //escreve fitness final em cX.dat (?)
            /*double med=0.0;
            for(int f=0;f<fun.length;f++)
                med = med + Double.valueOf(fun[f].replace(" ", "").split(",")[2]);
            med = med/fun.length;
            //criando arquivo
            salvarTxt("c"+jct.candidateId+".dat",(med)+"");*/

            System.gc();
            System.exit(0);
        
    }
    
    public static String lerTxt(String file){
    
     try {
            Scanner scanner;
            scanner = new Scanner(new File(file));
            scanner.useDelimiter("\\Z");
            file = scanner.next();
     }catch(Exception e){
     }
     return file;
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
    
    
    
    
}
