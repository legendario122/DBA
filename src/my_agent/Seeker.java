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
 * @author samuel
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
           // Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
            abortSession();
        }      
        Info("Checkeo realizado");
    }

    public void plainExecute(){
        //recibe mensaje con trayectorias.
        //comprobar si tengo energia para percibir una vez
        //Bucle: (si estoy en mi ultima posicion me salgo o si no hay tickets de recarga)
        //
        //2. Percibo con thermal y reviso si hay algun aleman:
        //2.1 si encuentro un aleman, calculo su posicion y la envio al controlador
        //3. if Posicion!= Trayectoria.size()-1 (Calculo acciones a realizar 
        //( envio mi posicion inicial Trayectoria[x] y posicion final Trayectoria[x+1])) espero mensaje con acciones, posicion++.
        //4. comprobar energia (Si no tengo solicito ticket de recarga y recargo)
        //5. Ejecuto las acciones. 
        //Fuera del bucle:
        // mensaje al controlador para decirle que ya no estoy activo.
        in = this.blockingReceive();
        if(in.getPerformative() != ACLMessage.REQUEST){
            // Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
             abortSession();
         }  
        //ConvID = in.getConversationId(); El conversation ID viene en el content del mensaje que nos llega y hay que desparsearlo
        Info("Haciendo SUSCRIBE a WorldManager en seeker"); 
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("BBVA",AID.ISLOCALNAME));   
        out.setConversationId(ConversationID);
        out.setProtocol("REGULAR");
        out.setContent(new JsonObject().add("type", "SEEKER").toString()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
        out.setEncoding("");
        out.setPerformative(ACLMessage.SUBSCRIBE);
        this.send(out);
    
        in = this.blockingReceive();
        if(in.getPerformative() != ACLMessage.INFORM){
            // Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
             abortSession();
        } 
        Info("Enviando monedas a controlador"); 
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("controlador",AID.ISLOCALNAME));    
        out.setProtocol("");
        out.setContent(in.getContent()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
        out.setEncoding("");
        out.setPerformative(ACLMessage.INFORM);
        this.send(out);
        
        //in = this.blockingReceive();
        
        Info("Recibiendo sensores de controlador y enviandoselas a sphinx");
        int mensj_recibidos = 0;
        ArrayList<String> sensores = new ArrayList<String>();
        while(mensj_recibidos != 2){
        in = this.blockingReceive();

            if(in.getPerformative() != ACLMessage.INFORM){
                //Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
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
        if(hay_energia(coste_percibir)){
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
        }else{
            recargar();
        }
        for(int i=0; i<31; i++){
            for(int j=0; j<31; j++){
               int aux = (int) thermal[i][j];
               System.out.print(aux+" ");
            }
            System.out.println("");
        }
        System.out.print(energia);
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
                out.addReceiver(new AID("controlador",AID.ISLOCALNAME));    
                out.setProtocol("");
                out.setContent(aux.toString()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
                out.setEncoding("");
                out.setPerformative(ACLMessage.INFORM);
                this.send(out);
            }
            lista_encontrados.clear();
            
        
        }
        
        if(pos_actual!=trayectoria.size()-1){
            pos_actual++;
            Info("pillado");
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
            
            Info("EL SEEKER TIENE QUE MOVERSE HACIA: ");
            System.out.print(trayectoria.get(pos_actual).getX() + trayectoria.get(pos_actual).getY() + trayectoria.get(pos_actual).getZ());
            
            
            out.setSender(getAID());
            out.addReceiver(new AID("greedy",AID.ISLOCALNAME));    
            out.setProtocol("");
            out.setContent(aux.toString()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
            out.setEncoding("");
            out.setPerformative(ACLMessage.REQUEST);
            this.send(out);
            Info("SEEKER ESPERANDO A GREEDY");

            in = this.blockingReceive();
            Info("recibo respuesta de greedy");
            if(in.getPerformative() != ACLMessage.INFORM){
               Info(in.getContent());
               abortSession();
            }else{
               Info(in.getContent()); //PERCIBO.
               desparsearMovimientos(in); //IMPLEMENTAR DESPARSEO DE ENERGY Y THERMALDELUX.
        
            }
            
            int cost = coste_movimientos();
            System.out.print("COOOOOOOOOOOOOOOOSTE de lista movimientos" + cost);
            if(!hay_energia(cost)){
                Info("ENTRO EN RECARGAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAR");
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
        System.out.print("TAAAAAAAAAAAAAAAAAAAAMAÑO" + movimientos.size());
        while(!movimientos.isEmpty()){
            Info("ENTRANDO EN REALIZAR MOVIMIENTOS SEEKER");
            Info(movimientos.get(iterator));
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
                Info("EJECUTANDO ACCIONES EN SEEKER");
                Info(out.getConversationId());
                Info(in.getConversationId());
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
    out.addReceiver(new AID("controlador",AID.ISLOCALNAME));
    out.setProtocol("");  
    out.setContent("Adios");
    out.setEncoding("");
    out.setPerformative(ACLMessage.INFORM);
    this.send(out);
    
    
    
    
    }
    
    public int coste_movimientos(){
        int coste=0;
        String move;
        System.out.print("TAMAÑO LISTA DE MOVIMIENTOS: "+ movimientos.size());
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
    
    
    //ESTA EN PRUEBAS ME FALTA POR SABER DONDE ESTA EL 0,0 en LOS MAPAS DEL PROFESOR. SE HA HECHO SUPONIENDO EL 0,0 en la esquina superior izquierda.
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
                    Info("He encontrado un ALEMAAAAAAAAAN");
                    if(encontrado_b==true){
                        encontrado = new posicion(-1,-1,-1,-1);
                        encontrado.setOrientacion(90);
                        if(indice_j<15){
                            System.out.println("EL INDICE DE j es menor: " + indice_j);
                            System.out.println("DISTANCIA j: " + distancia_j);
                            System.out.println("ACTUAL POSICION ES: " + actual.getX());
                            
                            encontrado.setX(actual.getX()-distancia_j);
                        }else if(indice_j>15){
                            System.out.println("EL INDICE DE j es mayor: " + indice_j);
                            System.out.println("DISTANCIA J: " + distancia_j);
                            System.out.println("ACTUAL POSICION ES: " + actual.getX());
                            encontrado.setX(actual.getX()+distancia_j);
                        }else{
                            encontrado.setX(actual.getX());
                        }

                        if(indice_i<15){
                            System.out.println("EL INDICE DE i es menor: " + indice_i);
                            System.out.println("DISTANCIA i: " + distancia_i);
                            System.out.println("ACTUAL POSICION  Y ES: " + actual.getY());
                            encontrado.setY(actual.getY()-distancia_i);
                        }else if(indice_i>15){
                            System.out.println("EL INDICE DE i es mayor: " + indice_i);
                            System.out.println("DISTANCIA i: " + distancia_i);
                            System.out.println("ACTUAL POSICION  Y ES: " + actual.getY());
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
    //Calcular moveUP para saber cuanto hay que bajar para recargar.
    public void recargar(){
        String ticket = "";
        if(actual.getX()==trayectoria.get(0).getX() && actual.getY()==trayectoria.get(0).getY()){
            out = new ACLMessage();
            out.setSender(getAID());
            out.addReceiver(new AID("controlador",AID.ISLOCALNAME));    
            out.setProtocol("");
            out.setContent("ticketRecarga"); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
            out.setEncoding("");
            out.setPerformative(ACLMessage.REQUEST);
            this.send(out);
            
            in =this.blockingReceive();
            if(in.getPerformative() != ACLMessage.INFORM){
               Info(in.getContent());
               abortSession();
            }else{
               Info(in.getContent()); //PERCIBO.
               ticket = in.getContent(); //IMPLEMENTAR DESPARSEO DE ENERGY Y THERMALDELUX.
               
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
               Info(in.getContent()); //PERCIBO.
               
        
            }
        }else{
            //FUNCION QUE DEVUELVE EL GREEDY
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
                       Info(in.getContent()); //PERCIBO.
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
                                out.addReceiver(new AID("controlador",AID.ISLOCALNAME));    
                                out.setProtocol("");
                                out.setContent("ticketRecarga"); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
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
    
    public boolean hay_energia(int coste){
        Boolean resultado = false;
        if(energia>coste){
            resultado=true;
        }
        
        return resultado;
    }

    @Override
    /**
     * Funcion que se encarga de hacer el checkout de larva y la plataforma.
     */    
    public void takeDown() {
        //HACEMOS CANCEL A SPHINX CREO QUE AQUI HAY ERROR
        /*
        Info("Request closing the session with " + _identitymanager);
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID(_identitymanager, AID.ISLOCALNAME));
        out.setProtocol("ANALYTICS");
        out.setContent("");
        out.setConversationId(ConversationID);
        out.setPerformative(ACLMessage.CANCEL);
        this.send(out);
*/
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("BBVA",AID.ISLOCALNAME));
        out.setProtocol("ANALYTICS");  
        out.setContent("");
        out.setEncoding("");
        out.setConversationId(ConversationID);
        out.setPerformative(ACLMessage.CANCEL);
        this.send(out);
        
        in = this.blockingReceive();
        Info(in.getContent());

        //doCheckoutLARVA();
        
        
    }
}
