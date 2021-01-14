/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */ 
package my_agent;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import IntegratedAgent.IntegratedAgent;
import static my_agent.Controlador.ConversationID;
import static my_agent.Rescuer.ConvID;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import static java.lang.Math.abs;

import java.util.ArrayList;
/**
 *
 * @author Adrian
 */
public class Seeker extends IntegratedAgent {
    int energia;
    boolean salir=false;
    Boolean recarga;
    double thermal[][] = new double[31][31];
    int contador_UP=0;
    posicion actual, encontrado;
    ArrayList<posicion> lista_encontrados = new ArrayList<posicion>();
    ArrayList<posicion> trayectoria = null;
    ArrayList<String> movimientos = null;
    ACLMessage in = new ACLMessage();
    ACLMessage out = new ACLMessage();
    
    static String ConvID = new String();
    
    /**
     * @author Samuel, Adrián y Rafael
     * Funcion que se encarga de hacer el checkin en Sphinx.
     */     
    public void setup() {
        super.setup();
        Info("Haciendo checkin to" + "Sphinx" + " seeker");
        out = new ACLMessage();
        
        out.setSender(getAID());
        out.addReceiver(new AID("Sphinx",AID.ISLOCALNAME));
        out.setProtocol("ANALYTICS");
        out.setContent("");
        out.setEncoding(_myCardID.getCardID());
        out.setPerformative(ACLMessage.SUBSCRIBE);
        this.send(out);
        in = this.blockingReceive();
        if(in.getPerformative() != ACLMessage.INFORM){
            abortSession();
        }      
        Info("Checkeo realizado");
    }
    
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Es el comportamiento principal del Agente seeker. Primero recibe un mensaje con el conversationID. Se suscribe a
     * World Manager y recibe las monedas, que seguidamente se las manda a controlador. Despues recibe los sensores del 
     * agente controlador y se los manda a World Manager. Recibe la trayectoria y se suscribe al mundo con los sensores,
     * y en el primer punto de la trayectoria, accediendo al bucle principal de su ejecucion. Mientras queden tickets de recarga
     * y no llegue al final de la trayectoria:
     * 
     * 1. Si no tiene energia para percibir, recarga. Tras esto percibe los sensores que recibio de controlador.
     * 2. Interpreta la percepcion anterior, para averiguar si hay un aleman cerca suya y en el caso de que lo hubiera
     * manda la posicion del aleman al agente controlador.
     * 3. Comprueba si se encuentra en la ultima posicion de la trayectoria, en caso de que no lo este, solicita al
     * agente greedy el camino a seguir (movimientos a ejecutar) para llegar hasta la siguiente posicion. Comprueba
     * que le queda energia suficiente para realizar los movimientos, si no hubiera, recarga. Y realiza los movimientos
     * indicados por greedy.
     */
    public void plainExecute(){
        
        in = this.blockingReceive();
        if(in.getPerformative() != ACLMessage.REQUEST){
             abortSession();
         }  
        
        Info("Haciendo SUSCRIBE a WorldManager en seeker"); 
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("BBVA",AID.ISLOCALNAME));   
        out.setConversationId(ConversationID);
        out.setProtocol("REGULAR");
        out.setContent(new JsonObject().add("type", "SEEKER").toString()); 
        out.setEncoding("");
        out.setPerformative(ACLMessage.SUBSCRIBE);
        this.send(out);
    
        in = this.blockingReceive();
        if(in.getPerformative() != ACLMessage.INFORM){
             abortSession();
        } 
        Info("Enviando monedas a controlador"); 
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("controlador2_bbva",AID.ISLOCALNAME));    
        out.setProtocol("");
        out.setContent(in.getContent()); 
        out.setEncoding("");
        out.setPerformative(ACLMessage.INFORM);
        this.send(out);
        
        
        Info("Recibiendo sensores de controlador y enviandoselas a sphinx");
        int mensj_recibidos = 0;
        ArrayList<String> sensores = new ArrayList<String>();
        while(mensj_recibidos != 2){
        in = this.blockingReceive();

            if(in.getPerformative() != ACLMessage.INFORM){
                abortSession();
            }else{
                Info(in.getContent());
                sensores.add(in.getContent());
                mensj_recibidos++;
            }    
    }    
    //mensaje con trayectorias    
    in =this.blockingReceive();
    if(in.getPerformative() != ACLMessage.INFORM){
        abortSession();
    }else{
        Info(in.getContent());
        String world = in.getContent();
        AID pepe = new AID();
        pepe = getAID();        
        trayectoria = new ArrayList<posicion>(Controlador.get_trayectoria(world,pepe.getLocalName() ));
    }
    
    JsonArray vector = new JsonArray();
    for(int i = 0; i < sensores.size(); i++)
        vector.add(sensores.get(i));
    
    System.out.print(trayectoria.get(0).getX());
    
    JsonObject objeto = new JsonObject();
    objeto.add("operation", "login");
    objeto.add("attach", vector);
    objeto.add("posx", trayectoria.get(0).getX());
    objeto.add("posy", trayectoria.get(0).getY());
    trayectoria.get(0).setZ(Greedy.obtenerAltura(trayectoria.get(0).getX(), trayectoria.get(0).getY()));
 
    out = new ACLMessage();
    out.setSender(getAID());
    out.addReceiver(new AID("BBVA",AID.ISLOCALNAME));
    out.setProtocol("REGULAR");   
    out.setContent(objeto.toString());
    out.setEncoding("");
    out.setConversationId(ConversationID);
    out.setPerformative(ACLMessage.REQUEST);
    
    try{
        Thread.sleep(2000);
    }catch(Exception ex){};
    
    this.send(out);
    
    in =this.blockingReceive();
    if(in.getPerformative() != ACLMessage.INFORM){
        Info(in.getContent());
        abortSession();
    }else{
        Info(in.getContent());
        
    }
    //INFORM ALEMAN REQUEST TICKET RECARGA
    recarga = true;
    int pos_actual=0;
    actual= new posicion(trayectoria.get(pos_actual).getX(),trayectoria.get(pos_actual).getY(), trayectoria.get(pos_actual).getZ(), trayectoria.get(pos_actual).getOrientacion());
    energia=10;
    int coste_percibir=9;
    
    
    while((recarga!=false) && (salir!=true)){
        
        
        if(!hay_energia(coste_percibir)){
            recargar();
        }
        out = in.createReply();
        out.setContent(new JsonObject().add("operation", "read").toString());
        out.setEncoding("");
        out.setConversationId(ConversationID);
        out.setPerformative(ACLMessage.QUERY_REF);
        this.send(out);
            
        in =this.blockingReceive();
        if(in.getPerformative() != ACLMessage.INFORM){
            Info(in.getContent());
            abortSession();
        }else{
            Info(in.getContent()); //PERCIBO.
            desparsearSensores(in); //IMPLEMENTAR DESPARSEO DE ENERGY Y THERMALDELUX.
        
        }
        
        
        if(hay_aleman()){
            
            
            for(int i=0; i<lista_encontrados.size(); i++){
                System.out.print("CONFIRMACION" + lista_encontrados.get(i).getX()+ " " + lista_encontrados.get(i).getY());
                out = new ACLMessage();
                JsonObject aux = new JsonObject();
                aux.add("x",lista_encontrados.get(i).getX());
                aux.add("y",lista_encontrados.get(i).getY());
                aux.add("z",lista_encontrados.get(i).getZ());
                aux.add("orientacion",lista_encontrados.get(i).getOrientacion());
                out.setSender(getAID());
                out.addReceiver(new AID("controlador2_bbva",AID.ISLOCALNAME));    
                out.setProtocol("");
                out.setContent(aux.toString()); 
                out.setEncoding("");
                out.setPerformative(ACLMessage.INFORM);
                this.send(out);
            }
            lista_encontrados.clear();
            
        
        }
        
        if(pos_actual!=trayectoria.size()-1){
            pos_actual++;
        
            out = new ACLMessage();
            JsonObject aux = new JsonObject();
            aux.add("x1",actual.getX());
            aux.add("y1",actual.getY());
            aux.add("z1",actual.getZ());
            aux.add("orientacion1",actual.getOrientacion());
            aux.add("x2",trayectoria.get(pos_actual).getX());
            aux.add("y2",trayectoria.get(pos_actual).getY());
            aux.add("z2",trayectoria.get(pos_actual).getZ());
            aux.add("orientacion2",trayectoria.get(pos_actual).getOrientacion());
            
            
            System.out.print(trayectoria.get(pos_actual).getX() + trayectoria.get(pos_actual).getY() + trayectoria.get(pos_actual).getZ());
            
            
            out.setSender(getAID());
            out.addReceiver(new AID("greedy",AID.ISLOCALNAME));    
            out.setProtocol("");
            out.setContent(aux.toString()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
            out.setEncoding("");
            out.setPerformative(ACLMessage.REQUEST);
            this.send(out);
            

            in = this.blockingReceive();
            
            if(in.getPerformative() != ACLMessage.INFORM){
               Info(in.getContent());
               System.err.println(in.getSender());
               abortSession();
            }else{
               Info(in.getContent()); //PERCIBO.
               desparsearMovimientos(in); //IMPLEMENTAR DESPARSEO DE ENERGY Y THERMALDELUX.
        
            }
            
            int cost = coste_movimientos();
            
            if(!hay_energia(cost)){
                recargar();
            }
            
            actual.setX(trayectoria.get(pos_actual).getX());
            actual.setY(trayectoria.get(pos_actual).getY());
            actual.setZ(trayectoria.get(pos_actual).getZ());
            actual.setOrientacion(trayectoria.get(pos_actual).getOrientacion());
            
        }else{
            salir=true;
        }
        
        //CALCULAR COSTE PARA LLEGAR A LA SIGUIENTE POSICION.
       
        int iterator=0;
        
        while(!movimientos.isEmpty()){
          
            out = new ACLMessage();
            out.setSender(getAID());
            out.addReceiver(new AID("BBVA",AID.ISLOCALNAME));
            out.setProtocol("REGULAR");  
            objeto = new JsonObject();
            
            objeto.add("operation", movimientos.get(iterator));
            out.setContent(objeto.toString());
            out.setEncoding("");
            out.setConversationId(ConversationID);
            out.setPerformative(ACLMessage.REQUEST);
            this.send(out);

            in =this.blockingReceive();
            
            if(in.getPerformative() != ACLMessage.INFORM){
                Info(in.getContent());
                abortSession();
            }else{
                movimientos.remove(iterator);
                Info(in.getContent());

            }
        }
        
    }
    
        
    out = new ACLMessage();
    out.setSender(getAID());
    out.addReceiver(new AID("controlador2_bbva",AID.ISLOCALNAME));
    out.setProtocol("");  
    out.setContent("Adios");
    out.setEncoding("");
    out.setPerformative(ACLMessage.INFORM);
    this.send(out);   
    
    }
    
   /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     *Funcion que calcula el coste de todos los movimientos que hay en el array movimientos.
     */    
    public int coste_movimientos(){
        int coste=0;
        String move;
        for(int i=0; i<movimientos.size(); i++){
            move=movimientos.get(i);
            if(move.equals("moveF") || move.equals("rotateL") || move.equals("rotateR")){
                coste++;
            }else if(move.equals("moveD") || move.equals("moveUP")){
                coste+=5;
            }
        }
        return coste;
    }
    
   /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que mediante la lectura del sensor thermar comprueba si debajo hay un aleman.Si hay un alemán 
     * lo añade al array encontrados. Y si encuentra un alemán devuelve true.
     * @return encontrado_b
     */
    public boolean hay_aleman(){
        boolean encontrado_b=false;
        int i=0, j=0;
        int indice_i=-1, indice_j=-1;
        int distancia_i=-1, distancia_j=-1;
        lista_encontrados = new ArrayList<posicion>();
        
        for(i=0; i<31; i++){
            for(j=0; j<31; j++){
                if(thermal[i][j]==0){
                    distancia_i=abs(i-15);
                    distancia_j=abs(j-15);
                    indice_i=i;
                    indice_j=j;
                    encontrado_b=true;
                    
                    if(encontrado_b==true){
                        encontrado = new posicion(-1,-1,-1,-1);
                        encontrado.setOrientacion(90);
                        if(indice_j<15){
                            encontrado.setX(actual.getX()-distancia_j);
                        }else if(indice_j>15){                           
                            encontrado.setX(actual.getX()+distancia_j);
                        }else{
                            encontrado.setX(actual.getX());
                        }

                        if(indice_i<15){                           
                            encontrado.setY(actual.getY()-distancia_i);
                        }else if(indice_i>15){
                            encontrado.setY(actual.getY()+distancia_i);
                        }else{
                            encontrado.setY(actual.getY());
                        }

                        encontrado.setZ(Greedy.obtenerAltura(actual.getX(),actual.getY()));
                    }
                    
                    lista_encontrados.add(encontrado);
                }
            }
        }
        
        
        
        
        return encontrado_b;
    }
    
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que se encarga de desparsear los movimientos y los añade al array movimientos.
     * @param in
    */    
    public void desparsearMovimientos(ACLMessage in){
        JsonObject json = new JsonObject();
        JsonArray vector = new JsonArray();
        movimientos = new ArrayList<String>();
        String answer = in.getContent();
        json = Json.parse(answer).asObject();
        
        vector = json.get("movimientos").asArray();
        for(JsonValue j : vector){
            movimientos.add(j.asString());
        }
    }
    
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que se encarga de desparsear los movimientos y los añade al array movimientos.
    */    
    
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que se encarga de hacer el desparseo despues de haber enviado una peticion de read para asignar los valores
     * a la matriz thermal y de la energia.
     * @param in
    */      
    public void desparsearSensores(ACLMessage in){
        JsonObject json = new JsonObject();        
        JsonObject json1 = new JsonObject();

        JsonArray vector = new JsonArray();        
        JsonArray vector2 = new JsonArray();

        JsonArray prueba = new JsonArray();
        JsonArray matriz = new JsonArray();
        JsonArray matriz_bis = new JsonArray();
        int i;
        int zeta=0;
        String sensor;
        
        String answer = in.getContent();
        json = Json.parse(answer).asObject();
        
        json1 = json.get("details").asObject();
        vector2 = json1.get("perceptions").asArray();
        for(JsonValue j : vector2){
            sensor = j.asObject().get("sensor").asString();
            if("energy".equals(sensor)){
                for(JsonValue a : prueba = j.asObject().get("data").asArray()){
                    energia = a.asInt();
                }
            }else if("thermal".equals(sensor)){
                matriz = j.asObject().get("data").asArray();
                i=0;
                for(JsonValue v : matriz){
                    matriz_bis = v.asArray();
                    for(JsonValue s : matriz_bis){
                        thermal[i][zeta]=s.asDouble();
                        zeta++;
                    }
                    zeta=0;
                    i++;
                }
            }
        }
        
    }
    
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que se encarga de que el dron baje hasta el suelo y aterrice. Le pide un ticket de recarga al controlador 
     * le manda la peticion de recargar a BBVA.
    */      
    public void recargar(){
        String ticket = "";
        if(actual.getX()==trayectoria.get(0).getX() && actual.getY()==trayectoria.get(0).getY()){
            out = new ACLMessage();
            out.setSender(getAID());
            out.addReceiver(new AID("controlador2_bbva",AID.ISLOCALNAME));    
            out.setProtocol("");
            out.setContent("ticketRecarga"); 
            out.setEncoding("");
            out.setPerformative(ACLMessage.REQUEST);
            this.send(out);
            
            in =this.blockingReceive();
            if(in.getPerformative() != ACLMessage.INFORM){
               Info(in.getContent());
               abortSession();
            }else{
               Info(in.getContent()); 
               ticket = in.getContent(); 
               
            }
            
            out = new ACLMessage();
            out.setSender(getAID());
            out.addReceiver(new AID("BBVA",AID.ISLOCALNAME));
            out.setProtocol("REGULAR");               
            JsonObject aux = new JsonObject();
            aux.add("operation", "recharge");
            aux.add("recharge", ticket );
            out.setContent(aux.toString());
            out.setEncoding("");
            out.setConversationId(ConversationID);
            out.setPerformative(ACLMessage.REQUEST);
            this.send(out);
            
            in =this.blockingReceive();
            if(in.getPerformative() != ACLMessage.INFORM){
               Info(in.getContent());
               abortSession();
            }else{
               Info(in.getContent()); 
               
        
            }
        }else{
            
            if(actual.getZ()!=Greedy.obtenerAltura(actual.getX(),actual.getY())){
                while(actual.getZ()>Greedy.obtenerAltura(actual.getX(),actual.getY())){
                    out = new ACLMessage();
                    out.setSender(getAID());
                    out.addReceiver(new AID("BBVA",AID.ISLOCALNAME));
                    out.setProtocol("REGULAR"); 
                    out.setContent(new JsonObject().add("operation", "moveD").toString());
                    out.setEncoding("");
                    out.setConversationId(ConversationID);
                    out.setPerformative(ACLMessage.REQUEST);
                    this.send(out);

                    in =this.blockingReceive();
                    if(in.getPerformative() != ACLMessage.INFORM){
                       Info(in.getContent());
                       abortSession();
                    }else{
                       Info(in.getContent()); 
                       if(actual.getZ()== Greedy.obtenerAltura(actual.getX(),actual.getY())){
                            out = new ACLMessage();
                            out.setSender(getAID());
                            out.addReceiver(new AID("BBVA",AID.ISLOCALNAME));
                            out.setProtocol("REGULAR"); 
                            out.setContent(new JsonObject().add("operation", "touchD").toString());
                            out.setEncoding("");
                            out.setConversationId(ConversationID);
                            out.setPerformative(ACLMessage.REQUEST);
                            this.send(out);
                            
                            in =this.blockingReceive();
                            if(in.getPerformative() != ACLMessage.INFORM){
                               Info(in.getContent());
                               abortSession();
                            }else{
                               Info(in.getContent()); 
                               
                                out = new ACLMessage();
                                out.setSender(getAID());
                                out.addReceiver(new AID("controlador2_bbva",AID.ISLOCALNAME));    
                                out.setProtocol("");
                                out.setContent("ticketRecarga"); 
                                out.setEncoding("");
                                out.setPerformative(ACLMessage.REQUEST);
                                this.send(out);

                                in =this.blockingReceive();
                                if(in.getPerformative() != ACLMessage.INFORM){
                                   Info(in.getContent());
                                   abortSession();
                                }else{
                                    Info(in.getContent()); 
                                    ticket = in.getContent(); 

                                    if(!ticket.equals("Vacio")){

                                        out = new ACLMessage();
                                        out.setSender(getAID());
                                        out.addReceiver(new AID("BBVA",AID.ISLOCALNAME));
                                        out.setProtocol("REGULAR");               
                                        JsonObject aux = new JsonObject();
                                        aux.add("operation", "recharge");
                                        aux.add("recharge", ticket );
                                        out.setContent(aux.toString());
                                        out.setEncoding("");
                                        out.setConversationId(ConversationID);
                                        out.setPerformative(ACLMessage.REQUEST);
                                        this.send(out);

                                        in =this.blockingReceive();
                                        if(in.getPerformative() != ACLMessage.INFORM){
                                           Info(in.getContent());
                                           abortSession();
                                        }else{
                                           Info(in.getContent()); 


                                        }
                                    }else{
                                        recarga=false;
                                    }

                                }

                                
                               
                            }
                            
                       }
                    }
            }
        }
    }
    }
    
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que se encarga de comprobar si hay energia suficiente para aterrizar.
     * @param coste
    */     
    public boolean hay_energia(int coste){
        Boolean resultado = false;
        if(energia>coste){
            resultado=true;
        }
        
        return resultado;
    }


    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que se encarga de hacer el logout de Sphinx.
    */  
    public void takeDown() {
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("Sphinx",AID.ISLOCALNAME));
        out.setProtocol("ANALYTICS");  
        out.setContent("");
        out.setEncoding("");
        out.setConversationId(ConversationID);
        out.setPerformative(ACLMessage.CANCEL);
        this.send(out);
        
        in = this.blockingReceive();
        Info(in.getContent());         
    }
}
