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
 * @author Samuel, Adrián y Rafael
 */
public class Greedy extends IntegratedAgent {
    
    Set<Estado> generados = new HashSet<Estado>();
    Queue<nodo> cola = new PriorityQueue<nodo>();
    static Map2DGrayscale mapa = new Map2DGrayscale();
    static final double IZQUIERDA=-45;
    static final double DERECHA=45;
    static final double GIRAR=0.25;
    static final double AVANZAR=1;
    
    /**
     * @author Rafa
     * Funcion que se encarga de hacer el setup del agente y cargar los
     * diferentes mapas dependiendo de la sesión que iniciemos.
     */
    public void setup() {
        super.setup();
        String playground = "Playground1.png";
        String world = "World5.png";
        try {
            mapa = mapa.loadMap(world);            
            Info("HE LEIDO CORRECTAMENTE EL MAPA");
        } catch (IOException ex) {
            Logger.getLogger(Greedy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @author Samuel, Adrián y Rafa
     * Constructor por defecto del agente Greedy, que en este caso se
     * encuentra vacío.
     */
    public Greedy(){        
    }   

    /**
     * @author Samuel y Adrián
     * Funcion que se encarga de orientar el drone hacia la izquierda.
     * Devuelve un double.
     * @param orientacion
     * @param izquierda 
     * @return resultado
     */
    public double orientarizquierda(double orientacion, double izquierda){
         double resultado=-1;
         if(orientacion==-135){
             resultado=180;
         }else{
             resultado = orientacion + izquierda;
         }
         return resultado;
    }
    
    /**
     * @author Samuel y Adrián
     * Funcion que se encarga de orientar el drone hacia la derecha.
     * Devuelve un double.
     * @param orientacion
     * @param derecha
     * @return resultado
     */
    public double orientarderecha(double orientacion, double derecha){
        double resultado = -1;
        if(orientacion == 180){
            resultado = -135; 
        }else{
            resultado = orientacion + derecha;
        }
        return resultado;
    }

    /**
     * @author Rafa
     * Funcion que se encarga de comparar el estado del nodo actual con
     * los nodos de la cola de generados. Devuelve verdadero si el nodo actual
     * no se encuentra en la cola de generados. Devuelve falso si el nodo actual
     * se encuentra en la lsita de generados.
     * Devuelve un booleano.
     * @param nodo_actual
     * @return boolean
     */
    public boolean comparaEstado(nodo nodo_actual){
        for(Estado e : generados){
            if(e.x==nodo_actual.st.x && e.y==nodo_actual.st.y && e.z==nodo_actual.st.z && e.orientacion==nodo_actual.st.orientacion){
                return false;
            }
        }
        return true;
    }

    /**
     * @author Samuel y Adrián
     * Funcion que se encarga de comprobar si el drone puede realizar
     * un movimiento hacia arriba. Devuelve un booleano.
     * @param e
     * @return puedo_subir
     */
    public boolean puedoSubir(Estado e){
        boolean puedo_subir = false;
        if(e.z<300){
            puedo_subir = true;
            e.z+=5;
        }
        return puedo_subir;
    }

    /**
     * @author Samuel y Adrián
     * Funcion que se encarga de comprobar si hay un obstaculo en una posición.
     * Se comprueban todas las opciones de posibles movimientos del drone.
     * Devuelve un booleano.
     * @param estado
     * @return resultado
     */
    public Boolean hayObstaculo(Estado estado){
        boolean resultado = false;
        int x = estado.x;
        int y = estado.y;
        if(estado.orientacion == 0){ 
            if((y-1)< 0){
                resultado=true;
            }else{
                if(mapa.getLevel( x,y-1) > estado.z){
                    resultado=true;              
                }else{
                    estado.y=y-1;
                    estado.x=x; 
                }
            }            
        }else if(estado.orientacion == 45){
            if((y-1)<0 || (x+1)>mapa.getHeight()-1){
                   resultado=true;
            }else{
                if(mapa.getLevel( x-1,y-1) > estado.z){    
                   resultado=true;                  
                }else{
                    estado.y=y-1;
                    estado.x=x+1; 
                }  
            }        
        }else if(estado.orientacion == 90){
            if((x+1)>mapa.getHeight()-1){
                resultado=true;
            }else{
                if(mapa.getLevel( x+1,y) > estado.z){
                    resultado=true;            
                }else{
                    estado.y=y;
                    estado.x=x+1; 
                }
            }  
        }else if(estado.orientacion == 135){
            if((y+1)>mapa.getWidth()-1 || (x+1) >mapa.getHeight()-1){
                resultado=true;
            }else{
                if(mapa.getLevel( x+1,y+1) > estado.z){
                    resultado=true;
                }else{
                    estado.y=y+1;
                    estado.x=x+1; 
                }
            }
        }else if(estado.orientacion == 180){
            if((y+1)>mapa.getWidth()-1){
                resultado=true;
            }else{
                if(mapa.getLevel(x,y+1) > estado.z){
                    resultado=true;
                }else{
                    estado.y=y+1;
                    estado.x=x; 
                }
            }
        }else if(estado.orientacion == -135){
            if((y+1)>mapa.getWidth()-1 || (x-1)<0){
                resultado=true;
            }else{
                if(mapa.getLevel( x-1,y+1) > estado.z){
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
                if(mapa.getLevel( x-1,y) > estado.z){
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
                if(mapa.getLevel( x-1,y-1) > estado.z){
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

    /**
     * @author Samuel, Adrián y Rafael
     * Bloque principal del agente Greedy donde se ejecuta su funcionalidad.
     * Esta consiste en esperar hasta que recibe mensajes con posiciones iniciales
     * y finales dentro del mapa. Su cometido es, dados ese origen y destino, 
     * encontrar un camino y las acciones correspondientes para ejecutar ese
     * camino. Una vez lo ha encontrado, le comunica ese camino al drone que ha
     * solicitado sus servicios y queda a la espera de recibir más mensajes para
     * calcular nuevas rutas.
     */
    public void plainExecute() {
    int x1, x2, y1, y2, z1, z2;
    double orientacion1, orientacion2;
    ArrayList<String> acciones = new ArrayList<String>();
    ACLMessage in = new ACLMessage();
    String answer;
    JsonObject objeto = new JsonObject();
    in = this.blockingReceive();
    Info("MENSAJES DEL RESCUER");
    Info(in.getContent());

    while(in.getPerformative() == ACLMessage.REQUEST){
        Info("MENSAJES DEL greedy");        
        answer = in.getContent();
        Info(in.getContent());
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
        
        acciones.clear();  
        nodo actual = new nodo(origen);   
        actual.setAcciones(acciones);
        actual.setDistancia(actual.distancia(actual.getSt(), destino));
        actual.acciones.clear();
        cola.clear();
        generados.clear();
        cola.add(actual);
        
        while(!cola.isEmpty() && (actual.st.x!=destino.x || actual.st.y!=destino.y || actual.st.orientacion!=destino.orientacion)){
            if(!cola.isEmpty()){
                actual = cola.poll();
            }        
            generados.add(actual.st);
            Estado aux = actual.getSt();
            ArrayList<String> auxS = actual.getAcciones();
            nodo hijoTurnR = new nodo(actual.getSt());
            nodo hijoTurnL = new nodo(actual.getSt());
            nodo hijoMoveF= new nodo(actual.getSt());
            nodo hijoMoveUp = new nodo(actual.getSt());
            nodo hijoMoveD = new nodo(actual.getSt());
        
            hijoTurnR.setSt(aux);
            hijoTurnR.setAcciones(auxS);
            hijoTurnR.st.orientacion = orientarderecha(hijoTurnR.st.orientacion, DERECHA);
            if(comparaEstado(hijoTurnR)){
                hijoTurnR.acciones.add("rotateR");
                hijoTurnR.setDistancia(hijoTurnR.distancia(hijoTurnR.getSt(), destino));
                cola.add(hijoTurnR);
            }

            hijoTurnL.setSt(aux);
            hijoTurnL.setAcciones(auxS);
            hijoTurnL.st.orientacion = orientarizquierda(hijoTurnL.st.orientacion, IZQUIERDA);        
            if(comparaEstado(hijoTurnL)){
                hijoTurnL.acciones.add("rotateL");                
                hijoTurnL.setDistancia(hijoTurnL.distancia(hijoTurnL.getSt(), destino));
                cola.add(hijoTurnL);
            }
            
            hijoMoveF.setSt(aux);
            hijoMoveF.setAcciones(auxS);
            if(!hayObstaculo(hijoMoveF.st)){
               if(comparaEstado(hijoMoveF)){
                    hijoMoveF.acciones.add("moveF");                    
                    hijoMoveF.setDistancia(hijoMoveF.distancia(hijoMoveF.getSt(), destino));
                    cola.add(hijoMoveF);
                } 
            }
            
            hijoMoveUp.setSt(aux);
            hijoMoveUp.setAcciones(auxS);
            if(puedoSubir(hijoMoveUp.st)){
                if(comparaEstado(hijoMoveUp)){
                    hijoMoveUp.acciones.add("moveUP");
                    hijoMoveUp.setDistancia(hijoMoveUp.distancia(hijoMoveUp.getSt(), destino));
                    cola.add(hijoMoveUp);
                }     
            }

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
        JsonObject obj = new JsonObject();
        ACLMessage out = in.createReply();
        JsonArray vector = new JsonArray();
        for(int i = 0; i < acciones.size(); i++)
            vector.add(acciones.get(i));
        obj.add("movimientos", vector);
        out.setContent(obj.toString());
        out.setPerformative(ACLMessage.INFORM);
        this.send(out);
        in = this.blockingReceive();
    }
    }
    
    /**
     * @author Rafa
     * Funcion que se encarga de obtener la altura de una coordenada.
     * @param x
     * @param y
     * @return altura
     */
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