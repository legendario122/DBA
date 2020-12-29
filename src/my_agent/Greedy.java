/** 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */  
package my_agent;

import IntegratedAgent.IntegratedAgent;
import Map2D.Map2DGrayscale;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author samuel
 */
public class Greedy extends IntegratedAgent {
    
    Set<Estado> generados = new HashSet<Estado>();
    Queue<nodo> cola = new PriorityQueue<nodo>();
    //double mapa[][] = new double[7][7];
    Map2DGrayscale mapa = new Map2DGrayscale();
    static final double IZQUIERDA=-45;
    static final double DERECHA=45;
    public void setup() {
        super.setup();
        String nombre_mapa = "Playground1.png";
        try {
            mapa = mapa.loadMap(nombre_mapa);
        } catch (IOException ex) {
            Logger.getLogger(Greedy.class.getName()).log(Level.SEVERE, null, ex);
        }
        Info("He pasado la carga del mapa");
        for (int ntimes = 0; ntimes < 3; ntimes++) {
            int px = (int) (Math.random() * mapa.getWidth());
            int py = (int) (Math.random() * mapa.getHeight());
            Info("\tX: " + px + ", Y:" + py + " = " + mapa.getLevel(px, py));
        }
        Info("He cargado y leido correctamente el archivo con el mapa");
    }

    public Greedy(){        
        /*for (int i=0; i<7; i++){
            for (int j=0; j<7; j++){
                mapa[i][j]=200;
            }
        }
        mapa[0][1]=220;
        mapa[1][0]=220;
        mapa[1][1]=220;*/
    }
    

     public double orientarizquierda(double orientacion, double izquierda){
         double resultado=-1;
         if(orientacion==-135){
             resultado=180;
         }else{
             resultado = orientacion + izquierda;
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
            if(e.x==nodo_actual.st.x && e.y==nodo_actual.st.y && e.z==nodo_actual.st.z && e.orientacion==nodo_actual.st.orientacion){
                return false;
            }
        }
        return true;
    }

    public boolean puedoSubir(Estado e){
        boolean puedo_subir = false;
        if(e.z<250){
            puedo_subir = true;
            e.z+=5;
        }
        return puedo_subir;
    }

    //FALTA LA VARIABLE MAPA
    public Boolean hayObstaculo(Estado estado){
        boolean resultado = false;
        int x = estado.x;
        int y = estado.y;
        if(estado.orientacion == 0){ //3 3 
            if((x-1)< 0){
                resultado=true;
            }else{
                //if(mapa[x-1][y] > estado.z){
                if(mapa.getLevel(x-1, y) > estado.z){
                    resultado=true;              
                }else{
                    estado.x=x-1;
                    estado.y=y; 
                }
            }
            
        }else if(estado.orientacion == 45){
            if((x-1)<0 || (y+1)>6){
                   resultado=true;

            }else{
                //if(mapa[x-1][y+1] > estado.z){
                if(mapa.getLevel(x-1, y-1) > estado.z){    
                   resultado=true;
                   
                }else{
                    estado.x=x-1;
                    estado.y=y+1; 
                }  
            }
                  
        }else if(estado.orientacion == 90){
            if((y+1)>6){
                resultado=true;
            }else{
                //if(mapa[x][y+1] > estado.z){
                if(mapa.getLevel(x, y+1) > estado.z){
                    resultado=true;
                
                }else{
                    estado.x=x;
                    estado.y=y+1; 
                }
            }
            
        }else if(estado.orientacion == 135){
            if((x+1)>6 || (y+1) >6){
                resultado=true;
            }else{
                //if(mapa[x+1][y+1] > estado.z){
                if(mapa.getLevel(x+1, y+1) > estado.z){
                    resultado=true;
                
                }else{
                    estado.x=x+1;
                    estado.y=y+1; 
                }
            }
            
        }else if(estado.orientacion == 180){
            if((x+1)>6){
                resultado=true;
            }else{
                //if(mapa[x+1][y] > estado.z){
                if(mapa.getLevel(x+1, y) > estado.z){
                    resultado=true;
                }else{
                    estado.x=x+1;
                    estado.y=y; 
                }
            }
            
        }else if(estado.orientacion == -135){
            if((x+1)>6 || (y-1)<0){
                resultado=true;
            }else{
                //if(mapa[x+1][y-1] > estado.z){
                if(mapa.getLevel(x+1, y-1) > estado.z){
                    resultado=true;
                }else{
                    estado.x=x+1;
                    estado.y=y-1; 
                }
            }
            
        }else if(estado.orientacion == -90){
            if((y-1)<0){
                resultado=true;
            }else{
                //if(mapa[x][y-1] > estado.z){
                if(mapa.getLevel(x, y-1) > estado.z){
                    resultado=true;
                }else{
                    estado.x=x;
                    estado.y=y-1; 
                }
            }
            
        }else if(estado.orientacion == -45){
            if((x-1)<0 || (y-1)<0){
                resultado=true;
            }else{
                //if(mapa[x-1][y-1] > estado.z){
                if(mapa.getLevel(x-1, y-1) > estado.z){
                    resultado=true;
                }else{
                    estado.x=x-1;
                    estado.y=y-1; 
                }
            }
            
        }else{
            resultado=false;
        }
        return resultado;       
    }

    public ArrayList<String> plainExecute(Estado origen, Estado destino, ArrayList<String> acciones) {
    
    acciones.clear();
    
    
    nodo actual = new nodo(origen);
    
    actual.setAcciones(acciones);
    actual.setDistancia(actual.distancia(actual.getSt(), destino));
    actual.acciones.clear();
    
    cola.add(actual);

    while(!cola.isEmpty() && (actual.st.x!=destino.x || actual.st.y!=destino.y)){
        
        
        if(!cola.isEmpty()){
            actual = cola.poll();
        }
        
        generados.add(actual.st);
        //GENERAR DESCENDIENTES
        Estado aux = actual.getSt();
        ArrayList<String> auxS = actual.getAcciones();
        nodo hijoTurnR = new nodo(actual.getSt());
        nodo hijoTurnL = new nodo(actual.getSt());
        nodo hijoMoveF= new nodo(actual.getSt());
        nodo hijoMoveUp = new nodo(actual.getSt());
        nodo hijoMoveD = new nodo(actual.getSt());
        
        //GIRAR DERECHA
        //nodo hijoTurnR = actual;
        //hijoTurnR = actual;
        hijoTurnR.setSt(aux);
        hijoTurnR.setAcciones(auxS);
        hijoTurnR.st.orientacion = orientarderecha(hijoTurnR.st.orientacion, DERECHA);
        if(comparaEstado(hijoTurnR)){
            //hijoTurnR.acciones.push_back("RotateR");
            hijoTurnR.acciones.add("RotateR");
            hijoTurnR.setDistancia(hijoTurnR.distancia(hijoTurnR.getSt(), destino));
            cola.add(hijoTurnR);
        }


        //GIRAR IZQUIERDA
        //nodo hijoTurnL = actual;
        //hijoTurnL = actual;
        hijoTurnL.setSt(aux);
        hijoTurnL.setAcciones(auxS);
        hijoTurnL.st.orientacion = orientarizquierda(hijoTurnL.st.orientacion, IZQUIERDA);        
        if(comparaEstado(hijoTurnL)){
            //hijoTurnL.acciones.push_back("RotateL");
            hijoTurnL.acciones.add("RotateL");
            hijoTurnL.setDistancia(hijoTurnL.distancia(hijoTurnL.getSt(), destino));
            cola.add(hijoTurnL);
        }
        
        //Avanzar
        //nodo hijoMoveF = actual;
        //hijoMoveF = actual;
        hijoMoveF.setSt(aux);
        hijoMoveF.setAcciones(auxS);
        if(!hayObstaculo(hijoMoveF.st)){
           if(comparaEstado(hijoMoveF)){
                //hijoMoveF.acciones.push_back("MoveF");
                hijoMoveF.acciones.add("MoveF");
                hijoMoveF.setDistancia(hijoMoveF.distancia(hijoMoveF.getSt(), destino));
                cola.add(hijoMoveF);
            } 
        }

        //Subir
        //nodo hijoMoveUp = actual;
        //hijoMoveUp = actual;
        hijoMoveUp.setSt(aux);
        hijoMoveUp.setAcciones(auxS);
        if(puedoSubir(hijoMoveUp.st)){
            if(comparaEstado(hijoMoveUp)){
                //hijoMoveUp.acciones.push_back("MoveUp");
                hijoMoveUp.acciones.add("MoveUp");
                hijoMoveUp.setDistancia(hijoMoveUp.distancia(hijoMoveUp.getSt(), destino));
                cola.add(hijoMoveUp);
            }     
        }

        //Bajar
        //nodo hijoMoveD = actual;
        //hijoMoveD = actual;
        hijoMoveD.setSt(aux);
        hijoMoveD.setAcciones(auxS);
        if(actual.st.z - 5 >= mapa.getLevel(actual.st.x,actual.st.y)){
            if(comparaEstado(hijoMoveD)){
                //hijoMoveD.acciones.push_back("MoveDown");
                hijoMoveD.acciones.add("MoveDown");
                hijoMoveD.setDistancia(hijoMoveD.distancia(hijoMoveD.getSt(), destino));
                cola.add(hijoMoveD);
            }
        }


        //TOMAR SIGUIENTE VALOR DE LA cola. 
        

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
    

