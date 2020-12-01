/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my_agent;

import IntegratedAgent.IntegratedAgent;
import java.util.*;

/**
 *
 * @author samuel
 */
public class Greedy extends IntegratedAgent {
    
    Set<Estado> generados = new HashSet<Estado>();
    Stack<nodo> pila;
    static final double IZQUIERDA=-45;
    static final double DERECHA=45;
    public void setup() {
        super.setup();
        
    }
     public double orientarizquierda(double orientacion, double izquierda){
         double resultado=-1;
         if(orientacion==-135){
             resultado=180;
         }else{
             resultado = orientacion - izquierda;
         }
         return resultado;
    }
    
    public double orientarderecha(double orientacion, double derecha){
        double resultado = -1;
        if(orientacion == 180){
            resultado = -135; 
        }else{
            resultado = orientacion + derecha;
        }
        return resultado;
    }

    public boolean comparaEstado(nodo nodo_actual){
        //si encuentra false
        //si no encuentra true
        for(Estado e : generados){
            if(e.x==nodo_actual.st.x && e.y==nodo_actual.st.y && e.z==nodo_actual.st.orientacion && e.x==nodo_actual.st.orientacion){
                return false;
            }
        }
        return true;
    }
    //FALTA LA VARIABLE MAPA
    public Boolean hayObstaculo(Estado estado){
        boolean resultado = false;
        int x = estado.x;
        int y = estado.y;
        if(estado.orientacion == 0){ //3 3 
            if(mapa[x-1][y] > estado.z){
                resultado=true;
            }
        }else if(mapa == 45){
            if(mapa[x-1][y+1] > estado.z){
                   resultado=true;    
            }       
        }else if(estado.orientacion == 90){
            if(mapa[x][y+1] > estado.z){
                resultado=true;
            }
        }else if(estado.orientacion == 135){
            if(mapa[x+1][y+1] > estado.z){
                resultado=true;
            }
        }else if(estado.orientacion == 180){
            if(mapa[x+1][y] > estado.z){
                resultado=true;
            }
        }else if(estado.orientacion == -135){
            if(mapa[x+1][y-1] > estado.z){
                resultado=true;
            }
        }else if(estado.orientacion == -90){
            if(mapa[x][y-1] > estado.z){
                 resultado=true;
            }
        }else if(estado.orientacion == -45){
            if(mapa[x-1][y-1] > estado.z){
                resultado=true;
            }
        }else{
            resultado=false;
        }
        return resultado;       
    }

    public void plainExecute(Estado origen, Estado destino, ArrayList<String> acciones) {
    
    acciones.clear();
    
    
    nodo actual = new nodo(origen);
    actual.acciones.clear();
    
    pila.push(actual);

    while(!pila.empty() && (actual.st.x!=destino.x || actual.st.y!=destino.y)){
        pila.pop();
        generados.insert(actual.st);

        //GENERAR DESCENDIENTES

        //GIRAR DERECHA
        nodo hijoTurnR = actual;
        hijoTurnR.st.orientacion = orientarderecha(hijoTurnR.st.orientacion, DERECHA);
        if(comparaEstado(hijoTurnR)){
            hijoTurnR.acciones.push_back("RotateR");
            pila.push(hijoTurnR);
        }


        //GIRAR IZQUIERDA
        nodo hijoTurnL = actual;
        hijoTurnL.st.orientacion = orientarizquierda(hijoTurnL.st.orientacion, IZQUIERDA);        
        if(comparaEstado(hijoTurnL)){
            hijoTurnL.acciones.push_back("RotateL");
            pila.push(hijoTurnL);
        }
        
        //Avanzar
        nodo hijoMoveF = actual;
        if(!hayObstaculo(hijoMoveF.st)){
           if(comparaEstado(hijoTurnL)){
                hijoMoveF.acciones.push_back("MoveF");
                pila.push(hijoMoveF);
            } 
        }







        


    }
    //Se queda escuchando:
    //1. Recibe mensaje con posicion inicial y final:
    //   LLama algoritmo greedy, calcula acciones a ejecutar
    //   Envia mensaje al drone con lista de acciones
    //2. Recibe mensaje de Goodbye
    //   Finaliza el "agente" (termina while o si es un agente mensaje cancel)
    
       return acciones;
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
    

