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
public class Rescuer extends IntegratedAgent {
    public void setup() {
        super.setup();
        
    }

    public void plainExecute() {
    
    //bucle (si no hay mas tickets de energia o recibe mensaje de controlador diciendo que no hay mas seeker y lista de alemanes vacia se sale)
    //{
    //Espera mensaje con la posicion del aleman 
    //percibe energy y gps
    //llama a greedy y le pasa su posicion y la posicion del aleman 
    //Recibe mensaje con lista de acciones
    //comprobar energia
    //ejecuta lista de acciones}
    //mensaje a drone de adios
    
       
    }
    
    

    @Override
    /**
     * Funcion que se encarga de hacer el checkout de larva y la plataforma.
     */    
    public void takeDown() {
        //mensaje cancel
        this.doCheckoutLARVA();
        this.doCheckoutPlatform();
        super.takeDown();
    }
}
