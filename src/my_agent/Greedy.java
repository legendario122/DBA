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
    const double IZQUIERDA=-45;
    const double DERECHA=45;
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

    public boolean ComparaEstado(nodo nodo_actual){
        //si encuentra false
        //si no encuentra true
        for(Estado e : generados){
            if(e.st.x==nodo_actual.st.x && e.st.y==nodo_actual.st.y && e.st.z==nodo_actual.st.orientacion && e.st.x==nodo_actual.st.orientacion){
                return False;
            }
        }
        return True;
    }

    public Booelan hayObstaculo(Estado estado){
        boolean resultado = false
        if(angulo == 0){ //3 3 
                if(mapa[x-1][y] > estado.z){
                   resultado=true;
                }
        }else if(angulo == 45){
            if(mapa[x-1][y+1] > estado.z){
                   resultado=true;    
            }       
        }else if(angulo == 90){
            if(lidar[x][y+1] >= 0){
                accion = "moveF";
                estado = "orientacion";
            }
        }else if(angulo == 135){
            if(lidar[4][4] >= 0){
                    accion = "moveF";
                    estado = "orientacion";
                }else{
                    
                    accion = "moveUP";
                    estado = "orientacion";
                }
            }else if(angulo == 180){
                if(lidar[4][3] >= 0){
                    accion = "moveF";
                    estado = "orientacion";
                }else{
                    
                    accion = "moveUP";
                    estado = "orientacion";
                }
            }else if(angulo == -135){
                if(lidar[4][2] >= 0){
                    accion = "moveF";
                    estado = "orientacion";
                }else{
                    
                    accion = "moveUP";
                    estado = "orientacion";
                }
            }else if(angulo == -90){
                if(lidar[3][2] >= 0){
                    accion = "moveF";
                    estado = "orientacion";
                }else{
                    
                    accion = "moveUP";
                    estado = "orientacion";
                }
            }else if(angulo == -45){
                if(lidar[2][2] >= 0){
                    accion = "moveF";
                    estado = "orientacion";
                }
    }

    public void plainExecute(nodo origen, nodo destino, ArrayList<String> acciones) {
    
    acciones.clear();
    
    
    nodo actual;
    actual.st=origen;
    actual.acciones.clear();
    
    pila.push(actual);

    while(!pila.empty() and (actual.st.x!=destino.x or actual.st.y!=destino.y)){
        pila.pop();
        generados.insert(actual.st);

        //GENERAR DESCENDIENTES

        //GIRAR DERECHA
        nodo hijoTurnR = actual;
        hijoTurnR.st.orientacion = orientarderecha(hijoTurnR.st.orientacion, DERECHA);
        if(comparaEstado(hijoTurnR)){
            hijoTurnR.st.acciones.push_back("RotateR");
            pila.push(hijoTurnR);
        }


        //GIRAR IZQUIERDA
        nodo hijoTurnL = actual;
        hijoTurnL.st.orientacion = orientarizquierda(hijoTurnL.st.orientacion, IZQUIERDA);        
        if(comparaEstado(hijoTurnL)){
            hijoTurnL.st.acciones.push_back("RotateL");
            pila.push(hijoTurnL);
        }
        
        //Avanzar
        nodo hijoMoveF = actual;
        if(!hayObstaculo(hijoMoveF.st)){
           if(comparaEstado(hijoTurnL)){
                hijoMoveF.st.acciones.push_back("MoveF");
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
    

