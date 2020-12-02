/** 
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
    Queue<nodo> cola = new PriorityQueue<nodo>();
    double mapa[][];
    static final double IZQUIERDA=-45;
    static final double DERECHA=45;
    public void setup() {
        super.setup();
        
    }

    public Greedy(){
        double mapa[][] = new double[7][7];
        for (int i=0; i<7; i++){
            for (int j=0; i<7; j++){
                mapa[i][j]=200;
            }
        }    
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

    public boolean puedoSubir(Estado e){
        boolean puedo_subir = false;
        if(e.z<250){
            puedo_subir = true;
        }
        return puedo_subir;
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

    public ArrayList<String> plainExecute(Estado origen, Estado destino, ArrayList<String> acciones) {
    
    acciones.clear();
    
    
    nodo actual = new nodo(origen);
    actual.setDistancia(actual.distancia(actual.getSt(), destino));
    actual.acciones.clear();
    
    cola.add(actual);

    while(!cola.isEmpty() && (actual.st.x!=destino.x || actual.st.y!=destino.y)){
        cola.remove();
        generados.add(actual.st);

        //GENERAR DESCENDIENTES

        nodo hijoTurnR = new Nodo();
        nodo hijoTurnL = new Nodo();
        nodo hijoMoveF= new Nodo();
        nodo hijoMoveUp = new Nodo();
        nodo hijoMoveD = new Nodo();

        //GIRAR DERECHA
        //nodo hijoTurnR = actual;
        hijoTurnR = actual;
        hijoTurnR.st.orientacion = orientarderecha(hijoTurnR.st.orientacion, DERECHA);
        if(comparaEstado(hijoTurnR)){
            //hijoTurnR.acciones.push_back("RotateR");
            hijoTurnR.acciones.add("RotateR");
            hijoTurnR.setDistancia(hijoTurnR.distancia(hijoTurnR.getSt(), destino));
            cola.add(hijoTurnR);
        }


        //GIRAR IZQUIERDA
        //nodo hijoTurnL = actual;
        hijoTurnL = actual;
        hijoTurnL.st.orientacion = orientarizquierda(hijoTurnL.st.orientacion, IZQUIERDA);        
        if(comparaEstado(hijoTurnL)){
            //hijoTurnL.acciones.push_back("RotateL");
            hijoTurnL.acciones.add("RotateL");
            hijoTurnL.setDistancia(hijoTurnL.distancia(hijoTurnL.getSt(), destino));
            cola.add(hijoTurnL);
        }
        
        //Avanzar
        //nodo hijoMoveF = actual;
        hijoMoveF = actual;
        if(!hayObstaculo(hijoMoveF.st)){
           if(comparaEstado(hijoTurnL)){
                //hijoMoveF.acciones.push_back("MoveF");
                hijoMoveF.acciones.add("MoveF");
                hijoMoveF.setDistancia(hijoMoveF.distancia(hijoMoveF.getSt(), destino));
                cola.add(hijoMoveF);
            } 
        }

        //Subir
        //nodo hijoMoveUp = actual;
        hijoMoveUp = actual;
        if(puedoSubir(hijoMoveUp.st)){
            if(comparaEstado(hijoMoveUp)){
                //hijoMoveUp.acciones.push_back("MoveUp");
                hijoMoveUp.acciones.add("MoveUp");
                hijoMoveUP.setDistancia(hijoMoveUP.distancia(hijoMoveUP.getSt(), destino));
                cola.add(hijoMoveUp);
            }     
        }

        //Bajar
        //nodo hijoMoveD = actual;
        hijoMoveD = actual;
        if(actual.st.z + 5 >= mapa[actual.st.x][actual.st.y]){
            if(comparaEstado(hijoMoveD)){
                //hijoMoveD.acciones.push_back("MoveDown");
                hijoMoveD.acciones.add("MoveDown");
                hijoMoveD.setDistancia(hijoMoveD.distancia(hijoMoveD.getSt(), destino));
                cola.add(hijoMoveD);
            }
        }


        //TOMAR SIGUIENTE VALOR DE LA cola. 
        if(!cola.isEmpty()){
            actual = cola.poll();
        }

    }
    
    acciones = actual.getAcciones();
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
    

