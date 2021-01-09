/** 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */  
package my_agent;

import IntegratedAgent.IntegratedAgent;
import Map2D.Map2DGrayscale;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
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
    static Map2DGrayscale mapa = new Map2DGrayscale();
    static final double IZQUIERDA=-45;
    static final double DERECHA=45;
    static final double GIRAR=0.25;
    static final double AVANZAR=1;
    public void setup() {
        super.setup();
        String playground = "Playground2.png";
        String world = "World1.png";
        try {
            mapa = mapa.loadMap(playground);
            
            Info("HE LEIDO CORRECTAMENTE EL MAPA");
        } catch (IOException ex) {
            Logger.getLogger(Greedy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Greedy(){        
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
        if(e.z<255){
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
            if((y-1)< 0){
                resultado=true;
            }else{
                //if(mapa[x-1][y] > estado.z){
                if(mapa.getLevel(y-1, x) > estado.z){
                    resultado=true;              
                }else{
                    estado.y=y-1;
                    estado.x=x; 
                }
            }
            
        }else if(estado.orientacion == 45){
            if((y-1)<0 || (x+1)>mapa.getHeight()){
                   resultado=true;

            }else{
                //if(mapa[x-1][y+1] > estado.z){
                if(mapa.getLevel(y-1, x-1) > estado.z){    
                   resultado=true;
                   
                }else{
                    estado.y=y-1;
                    estado.x=x+1; 
                }  
            }
                  
        }else if(estado.orientacion == 90){
            if((x+1)>mapa.getHeight()){
                resultado=true;
            }else{
                //if(mapa[x][y+1] > estado.z){
                if(mapa.getLevel(y, x+1) > estado.z){
                    resultado=true;
                
                }else{
                    estado.y=y;
                    estado.x=x+1; 
                }
            }
            
        }else if(estado.orientacion == 135){
            if((y+1)>mapa.getWidth() || (x+1) >mapa.getHeight()){
                resultado=true;
            }else{
                //if(mapa[x+1][y+1] > estado.z){
                if(mapa.getLevel(y+1, x+1) > estado.z){
                    resultado=true;
                
                }else{
                    estado.y=y+1;
                    estado.x=x+1; 
                }
            }
            
        }else if(estado.orientacion == 180){
            if((y+1)>mapa.getWidth()){
                resultado=true;
            }else{
                //if(mapa[x+1][y] > estado.z){
                if(mapa.getLevel(y+1, x) > estado.z){
                    resultado=true;
                }else{
                    estado.y=y+1;
                    estado.x=x; 
                }
            }
            
        }else if(estado.orientacion == -135){
            if((y+1)>mapa.getWidth() || (x-1)<0){
                resultado=true;
            }else{
                //if(mapa[x+1][y-1] > estado.z){
                if(mapa.getLevel(y+1, x-1) > estado.z){
                    resultado=true;
                }else{
                    estado.y=y+1;
                    estado.x=x-1; 
                }
            }
            
        }else if(estado.orientacion == -90){
            if((x-1)<0){
                resultado=true;
            }else{
                //if(mapa[x][y-1] > estado.z){
                if(mapa.getLevel(y, x-1) > estado.z){
                    resultado=true;
                }else{
                    estado.y=y;
                    estado.x=x-1; 
                }
            }
            
        }else if(estado.orientacion == -45){
            if((y-1)<0 || (x-1)<0){
                resultado=true;
            }else{
                //if(mapa[x-1][y-1] > estado.z){
                if(mapa.getLevel(y-1, x-1) > estado.z){
                    resultado=true;
                }else{
                    estado.y=y-1;
                    estado.x=x-1; 
                }
            }
            
        }else{
            resultado=false;
        }
        return resultado;       
    }

    //public void plainExecute(Estado origen, Estado destino, ArrayList<String> acciones) {
    public void plainExecute() {
    int x1, x2, y1, y2, z1, z2;
    double orientacion1, orientacion2;
    ArrayList<String> acciones = new ArrayList<String>();
    ACLMessage in = new ACLMessage();
    String answer;
    JsonObject objeto = new JsonObject();
    in = this.blockingReceive();
    Info(in.getContent());
    //while(siga_recibiendo_mensajes){
    while(in.getPerformative() == ACLMessage.REQUEST){
                
        answer = in.getContent();
        
        objeto = Json.parse(answer).asObject();
        x1 = objeto.get("x1").asInt();
        y1 = objeto.get("y1").asInt();
        z1 = objeto.get("z1").asInt();
        orientacion1 = objeto.get("orientacion1").asDouble();
        Estado origen = new Estado(x1,y1,z1,orientacion1);
        x2 = objeto.get("x2").asInt();
        y2 = objeto.get("y2").asInt();
        z2 = objeto.get("z2").asInt();
        orientacion2 = objeto.get("orientacion2").asDouble();
        Estado destino = new Estado(x2,y2,z2,orientacion2);
        
        //comienzo del algoritmo greedy
        acciones.clear();  
        nodo actual = new nodo(origen);   
        actual.setAcciones(acciones);
        actual.setDistancia(actual.distancia(actual.getSt(), destino));
        actual.acciones.clear();
        cola.clear();
        generados.clear();
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
            hijoTurnR.setSt(aux);
            hijoTurnR.setAcciones(auxS);
            hijoTurnR.st.orientacion = orientarderecha(hijoTurnR.st.orientacion, DERECHA);
            if(comparaEstado(hijoTurnR)){
                hijoTurnR.acciones.add("rotateR");
                hijoTurnR.setDistancia(hijoTurnR.distancia(hijoTurnR.getSt(), destino));
                hijoTurnR.añadirValor(-GIRAR);
                cola.add(hijoTurnR);
            }

            //GIRAR IZQUIERDA
            hijoTurnL.setSt(aux);
            hijoTurnL.setAcciones(auxS);
            hijoTurnL.st.orientacion = orientarizquierda(hijoTurnL.st.orientacion, IZQUIERDA);        
            if(comparaEstado(hijoTurnL)){
                hijoTurnL.acciones.add("rotateL");
                
                hijoTurnL.setDistancia(hijoTurnL.distancia(hijoTurnL.getSt(), destino));
                hijoTurnL.añadirValor(-GIRAR);
                cola.add(hijoTurnL);
            }
        
            //Avanzar
            hijoMoveF.setSt(aux);
            hijoMoveF.setAcciones(auxS);
            if(!hayObstaculo(hijoMoveF.st)){
               if(comparaEstado(hijoMoveF)){
                    hijoMoveF.acciones.add("moveF");
                    
                    hijoMoveF.setDistancia(hijoMoveF.distancia(hijoMoveF.getSt(), destino));
                    
                    cola.add(hijoMoveF);
                } 
            }

            //Subir
            hijoMoveUp.setSt(aux);
            hijoMoveUp.setAcciones(auxS);
            if(puedoSubir(hijoMoveUp.st)){
                if(comparaEstado(hijoMoveUp)){
                    hijoMoveUp.acciones.add("moveUP");
                    hijoMoveUp.setDistancia(hijoMoveUp.distancia(hijoMoveUp.getSt(), destino));
                    cola.add(hijoMoveUp);
                }     
            }

            //Bajar
            hijoMoveD.setSt(aux);
            hijoMoveD.setAcciones(auxS);
            if(actual.st.z - 5 >= mapa.getLevel(actual.st.x,actual.st.y)){
                if(comparaEstado(hijoMoveD)){
                    hijoMoveD.acciones.add("moveD");
                    hijoMoveD.setDistancia(hijoMoveD.distancia(hijoMoveD.getSt(), destino));
                    cola.add(hijoMoveD);
                }
            }
        }
        acciones = actual.getAcciones();
        Info("ha calculado el caminoOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO PARA IR DE AHI A ALLI");
        Info(in.getContent());
        //for(int i=0; i<acciones.size(); i++){
        //    Info(acciones.get(i));
        //}
        //metemos array de acciones y se las mandamos al solicitante
        JsonObject obj = new JsonObject();
        ACLMessage out = in.createReply();
        JsonArray vector = new JsonArray();
        for(int i = 0; i < acciones.size(); i++)
            vector.add(acciones.get(i));
        obj.add("movimientos", vector);
        out.setContent(obj.toString());
        out.setPerformative(ACLMessage.INFORM);
        this.send(out);
        //esperamos al siguiente mensaje
        in = this.blockingReceive();
    }
    
    //ALGORITMO GREEDY INICIAL
    //acciones.clear();
    
    
    //nodo actual = new nodo(origen);
    
    //actual.setAcciones(acciones);
    //actual.setDistancia(actual.distancia(actual.getSt(), destino));
    //actual.acciones.clear();
    
    //cola.add(actual);

    /*while(!cola.isEmpty() && (actual.st.x!=destino.x || actual.st.y!=destino.y)){
        
        
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
    //   Finaliza el "agente" (termina while o si es un agente mensaje cancel)*/

    }
    
    public static int obtenerAltura(int x, int y){
        int altura;
        altura = mapa.getLevel(x,y);
        return altura;
    }

    @Override
    /**
     * Funcion que se encarga de hacer el checkout de larva y la plataforma.
     */    
    public void takeDown() {
        
        super.takeDown();
    }
    
    
    
    }
    

