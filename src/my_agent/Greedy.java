/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my_agent;

import IntegratedAgent.IntegratedAgent;
/**
 *
 * @author samuel
 */
public class Greedy extends IntegratedAgent {
    public void setup() {
        super.setup();
        
    }

    public void plainExecute() {
    
    //Se queda escuchando:
    //1. Recibe mensaje con posicion inicial y final:
    //   LLama algoritmo greedy, calcula acciones a ejecutar
    //   Envia mensaje al drone con lista de acciones
    //2. Recibe mensaje de Goodbye
    //   Finaliza el "agente" (termina while o si es un agente mensaje cancel)
    
       
    }
    
    

    @Override
    /**
     * Funcion que se encarga de hacer el checkout de larva y la plataforma.
     */    
    public void takeDown() {
        this.doCheckoutLARVA();
        this.doCheckoutPlatform();
        super.takeDown();
    }
    
    
    
    }
    

