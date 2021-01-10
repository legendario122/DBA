/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor. 
 */ 
package my_agent;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import IntegratedAgent.IntegratedAgent;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import static java.lang.Math.abs;
import java.util.ArrayList;
import static my_agent.Controlador.ConversationID;
/**
 *
 * @author samuel
 */
public class Rescuer extends IntegratedAgent {

    ACLMessage in = new ACLMessage();
    ACLMessage out = new ACLMessage();
    ArrayList<String> movimientos = null;
    static String ConvID = new String();
    boolean hay_tiquets = true;
    ArrayList<posicion> alemanes = new ArrayList<posicion>();
    int x;
    int y;
    int orientacion;
    int z;
    int energy=10;
    int n_aleman = 0;
    boolean recarga = true;
    boolean hay_tickets = true;
    int altura;

    public void setup() {
        super.setup();
         Info("Haciendo checkin to" + "Sphinx" + "rescuer");
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

        in = this.blockingReceive();
        if(in.getPerformative() != ACLMessage.REQUEST){
            // Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
             abortSession();
         }  
        //ConvID = in.get(); El conversation ID viene en el content del mensaje que nos llega y hay que desparsearlo

        Info("Haciendo SUSCRIBE a WorldManager en rescuer"); 
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("BBVA",AID.ISLOCALNAME));    
        out.setConversationId(ConversationID);
        out.setProtocol("REGULAR");
        out.setContent(new JsonObject().add("type", "RESCUER").toString()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
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
        JsonArray vector = new JsonArray();
        for(int i = 0; i < sensores.size(); i++)
            vector.add(sensores.get(i));


        JsonObject objeto = new JsonObject();
        objeto.add("operation", "login");
        objeto.add("attach", vector);
        objeto.add("posx", 0);
        objeto.add("posy", 0);
        x=0;
        y=0;
        z=Greedy.obtenerAltura(x, y);

        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("BBVA",AID.ISLOCALNAME));
        out.setProtocol("REGULAR"); 
        out.setConversationId(ConversationID);
        out.setContent(objeto.toString());
        out.setEncoding("");
        out.setPerformative(ACLMessage.REQUEST);
        this.send(out);


        ACLMessage prueba = new ACLMessage();
        
        in =this.blockingReceive();
        if(in.getPerformative() != ACLMessage.INFORM){
            Info(in.getContent());
            abortSession();
        }else{
            Info(in.getContent());

        }

        in = this.blockingReceive();
        Info(in.getContent() +": ALEMAAAAAAAAAAAAAAAAAAAAN");
        alemanes.add(desparsearPosicion(in));
        
        while(hay_tickets && n_aleman!=10 ){
            //FALTA INICIALIZAR X, Y y Z
            
            
            if(hay_energia(2)){
                JsonObject read = new JsonObject();
                read.add("operation", "read");

                out = new ACLMessage();
                out.setSender(getAID());
                out.addReceiver(new AID("BBVA", AID.ISLOCALNAME));
                out.setProtocol("REGULAR");
                out.setContent(read.toString());
                out.setConversationId(ConversationID);
                out.setPerformative(ACLMessage.QUERY_REF);
                this.send(out);

                do{
                    in = this.blockingReceive();
                    if(in.getPerformative()==ACLMessage.REQUEST){
                        alemanes.add(desparsearPosicion(in));
                    }
                }while(in.getPerformative()!= ACLMessage.INFORM);

                if(in.getPerformative() != ACLMessage.INFORM){
                    Info(in.getContent());
                    abortSession();
                }else{

                    Info("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
                    Info(in.getContent() + " " + in.getSender());
                    desparsearRead(in);  
                }
            
            }else{
                altura = z - Greedy.obtenerAltura(x, y);
                System.out.print("ALTURA DEL RESCUER es" + altura);
       
                while(altura > 0){
                    prerecarga();
                    altura = z - Greedy.obtenerAltura(x, y);
                }
                Info("RECARGUEMOSSSSSSSSSSS");
                recargar();
            }
            
            orientacion = 90;
            JsonObject posini = new JsonObject();
            posini.add("x1", x);
            posini.add("y1", y);
            posini.add("z1", z);
            posini.add("orientacion1", orientacion);
            posini.add("x2", alemanes.get(n_aleman).getX());
            posini.add("y2", alemanes.get(n_aleman).getY());
            posini.add("z2", Greedy.obtenerAltura(alemanes.get(n_aleman).getX(), alemanes.get(n_aleman).getY()));
            posini.add("orientacion2", alemanes.get(n_aleman).getOrientacion());

            

            out = new ACLMessage();
            out.setSender(getAID());
            out.addReceiver(new AID("greedy",AID.ISLOCALNAME));
            out.setProtocol("");
            out.setContent(posini.toString());
            out.setEncoding("");
            out.setPerformative(ACLMessage.REQUEST);
            this.send(out);
            Info("RESCUER ESPERANDO A GREEDY");
            
            do{
                in = this.blockingReceive();
                if(in.getPerformative()==ACLMessage.REQUEST){
                    alemanes.add(desparsearPosicion(in));
                }
            }while(in.getPerformative()!= ACLMessage.INFORM);

            
            
            if(in.getPerformative() != ACLMessage.INFORM){
                Info(in.getContent());
                abortSession();
            }else{
                movimientos = new ArrayList<String>();
                desparsearMovimientos(in);

            }

            System.out.print("TAMAÑO MOVIMIENTOS: "+movimientos.size());
            ////////////////////////////////////////////////////////////////////////////////////////////////////////
            //AQUI VAMOS A DESPARSEAR LA LISTA DE MOOVIMIENTOS QUE AUN NO HE PENSADO COMO 
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            int estimacion_energia = abs((z - Greedy.obtenerAltura(alemanes.get(n_aleman).getX(), alemanes.get(n_aleman).getY()))*4);
            int estimacion_energia2 = coste_movimientos();
            
            JsonObject movimiento = new JsonObject();
            if(!hay_energia(estimacion_energia + estimacion_energia2 )){
                altura = z - Greedy.obtenerAltura(x, y);
                System.out.print("ALTURA DEL RESCUER es" + altura);
       
                while(altura > 0){
                    prerecarga();
                    altura = z - Greedy.obtenerAltura(x, y);
                }
                
                Info("RECARGUEMOSSSSSSSSSSS");
                recargar();
            }
            
            for(int i = 0; i < movimientos.size(); i++){

                                

                movimiento.add("operation", movimientos.get(i));
                Info("MOVIMIENTOS RESCUER");
                if(movimientos.get(i).equals("moveUP")){
                    z+=5;
                }
                Info(movimientos.get(i));
                out = new ACLMessage();
                out.setSender(getAID());
                out.addReceiver(new AID("BBVA", AID.ISLOCALNAME));
                out.setProtocol("REGULAR");
                out.setContent(movimiento.toString());
                out.setConversationId(ConversationID);
                out.setPerformative(ACLMessage.REQUEST);
                this.send(out);
                
                
                do{
                    in = this.blockingReceive();
                    if(in.getPerformative()==ACLMessage.REQUEST){
                        alemanes.add(desparsearPosicion(in));
                    }
                }while(in.getPerformative()!= ACLMessage.INFORM);
                
                
                if(in.getPerformative() != ACLMessage.INFORM){
                    Info(in.getContent());
                    abortSession();
                }                

            }
            if(movimientos.isEmpty()){
                x = alemanes.get(n_aleman).getX();
                y = alemanes.get(n_aleman).getY();
                altura = z - Greedy.obtenerAltura(x, y);
                while(altura > 0){
                    prerecarga();
                    altura = z - Greedy.obtenerAltura(x, y);
                }
                
                rescatar();
                
                
                do{
                    in = this.blockingReceive();
                    if(in.getPerformative()==ACLMessage.REQUEST){
                        alemanes.add(desparsearPosicion(in));
                    }
                    if(in.getPerformative()==ACLMessage.FAILURE){
                        Info(in.getContent());
                    }
                }while(in.getPerformative()!= ACLMessage.INFORM);
                
                
                if(in.getPerformative() != ACLMessage.INFORM){
                    Info(in.getContent());
                    abortSession();
                }  
                Info(in.getContent());
                n_aleman++;
            }
            
        }
        orientacion = 90;
        JsonObject posini = new JsonObject();
        posini.add("x1", x);
        posini.add("y1", y);
        posini.add("z1", z);
        posini.add("orientacion1", orientacion);
        posini.add("x2", 0);
        posini.add("y2", 0);
        posini.add("z2", Greedy.obtenerAltura(0, 0));
        posini.add("orientacion2", 90);

            

        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("greedy",AID.ISLOCALNAME));
        out.setProtocol("");
        out.setContent(posini.toString());
        out.setEncoding("");
        out.setPerformative(ACLMessage.REQUEST);
        this.send(out);
        
        in = this.blockingReceive();
        
        if(in.getPerformative() != ACLMessage.INFORM){
            Info(in.getContent());
            abortSession();
        }else{
            movimientos = new ArrayList<String>();
            desparsearMovimientos(in);
        }  
        
        int estimacion_energia = abs((z - Greedy.obtenerAltura(0, 0))*4);
        int estimacion_energia2 = coste_movimientos();
            
        JsonObject movimiento = new JsonObject();
        if(!hay_energia(estimacion_energia + estimacion_energia2 )){
            altura = z - Greedy.obtenerAltura(x, y);
            System.out.print("ALTURA DEL RESCUER es" + altura);
       
            while(altura > 0){
                prerecarga();
                altura = z - Greedy.obtenerAltura(x, y);
            }
                
            Info("RECARGUEMOSSSSSSSSSSS");
            recargar();
        }
            
        for(int i = 0; i < movimientos.size(); i++){

                                

            movimiento.add("operation", movimientos.get(i));
            Info("MOVIMIENTOS RESCUER");
            if(movimientos.get(i).equals("moveUP")){
                z+=5;
            }
            Info(movimientos.get(i));
            out = new ACLMessage();
            out.setSender(getAID());
            out.addReceiver(new AID("BBVA", AID.ISLOCALNAME));
            out.setProtocol("REGULAR");
            out.setContent(movimiento.toString());
            out.setConversationId(ConversationID);
            out.setPerformative(ACLMessage.REQUEST);
            this.send(out);
                 
            in = this.blockingReceive();
                    
                
                
            if(in.getPerformative() != ACLMessage.INFORM){
                Info(in.getContent());
                abortSession();
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
    
    

    @Override
    /**
     * Funcion que se encarga de hacer el checkout de larva y la plataforma.
     */    
    public void takeDown() {
        Info("Request closing the session with " + "BBVA");
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("BBVA", AID.ISLOCALNAME));
        out.setProtocol("ANALYTICS");
        out.setContent("");
        out.setConversationId(ConversationID);
        out.setPerformative(ACLMessage.CANCEL);
        this.send(out);
        in = this.blockingReceive();
        //Info(getDetailsLARVA(in));

        doCheckoutLARVA();
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
    
    public posicion desparsearPosicion(ACLMessage in){
        
        String answer = in.getContent();
        JsonObject objeto = new JsonObject();
        objeto = Json.parse(answer).asObject();
        int x = objeto.get("x").asInt();
        int y = objeto.get("y").asInt();
        int z = objeto.get("z").asInt();
        int orientacion = objeto.get("orientacion").asInt();
        
        posicion pos = new posicion(x,y,z,orientacion);
        
        
        return pos;
    }
    
    public void rescatar(){
            JsonObject objeto = new JsonObject();
            objeto.add("operation", "rescue");
            Info("RESCATANDOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
            out = new ACLMessage();
            out.setSender(getAID());
            out.addReceiver(new AID("BBVA", AID.ISLOCALNAME));
            out.setProtocol("REGULAR");
            out.setContent(objeto.toString());
            out.setConversationId(ConversationID);
            out.setPerformative(ACLMessage.REQUEST);
            this.send(out);
    }
    
    public void recargar(){

        String ticket;    
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("controlador",AID.ISLOCALNAME));
        out.setProtocol("");
        out.setContent("ticketRecarga");
        out.setEncoding("");
        out.setPerformative(ACLMessage.REQUEST);
        this.send(out);

        in = this.blockingReceive();
        Info("SOLICITANDO TICKET DE RECARGA");
        Info(in.getContent());
        if(in.getPerformative() != ACLMessage.INFORM){
            Info(in.getContent());
            abortSession();
        }else{
            ticket = in.getContent();
            if(!ticket.equals("Vacio")){
                JsonObject rech = new JsonObject();
                rech.add("operation", "recharge");
                rech.add("recharge", ticket);


                out = new ACLMessage();
                out.setSender(getAID());
                out.addReceiver(new AID("BBVA", AID.ISLOCALNAME));
                out.setProtocol("REGULAR");
                out.setContent(rech.toString());
                out.setConversationId(ConversationID);
                out.setPerformative(ACLMessage.REQUEST);
                this.send(out);
                
                do{
                    in = this.blockingReceive();
                    if(in.getPerformative()==ACLMessage.REQUEST){
                        alemanes.add(desparsearPosicion(in));
                    }
                }while(in.getPerformative()!= ACLMessage.INFORM);
                

                if(in.getPerformative() != ACLMessage.INFORM){
                    Info(in.getContent());
                    abortSession();
                }
                Info("SOLICITANDO RECARGA AL BBVA");
                Info(in.getContent());
            }else{
                hay_tickets=false;
            }
            
        }            
    }
    
        public void prerecarga() {

            String accion = "";
            JsonObject movimiento = new JsonObject();
            
            
            if(altura>5){
                accion="moveD";
                z-=5;
            }else if(altura>0 && altura<5){
                accion="touchD";
                z-=5;
            }else{
                z-=5;
                accion="moveD";
            }
            
            
            movimiento.add("operation", accion);

            out = new ACLMessage();
            out.setSender(getAID());
            out.addReceiver(new AID("BBVA", AID.ISLOCALNAME));
            out.setProtocol("REGULAR");
            out.setContent(movimiento.toString());
            out.setConversationId(ConversationID);
            out.setPerformative(ACLMessage.REQUEST);
            this.send(out);
            
            do{
                in = this.blockingReceive();
                if(in.getPerformative()==ACLMessage.REQUEST){
                    alemanes.add(desparsearPosicion(in));
                }
            }while(in.getPerformative()!= ACLMessage.INFORM);
            
            if(in.getPerformative() != ACLMessage.INFORM){
                Info(in.getContent());
                abortSession();
            }
        }
        
    public boolean hay_energia(int coste){
        Boolean resultado = false;
        if(energy>coste){
            resultado=true;
        }
        
        return resultado;
    }
    
    public int coste_movimientos(){
        int coste=0;
        String move;
        System.out.print("TAMAÑO LISTA DE MOVIMIENTOS: "+ movimientos.size());
        for(int i=0; i<movimientos.size(); i++){
            move=movimientos.get(i);
            if(move.equals("moveF") || move.equals("rotateL") || move.equals("rotateR")){
                coste+=4;
            }else if(move.equals("moveD") || move.equals("moveUP")){
                coste+=(5*4);
            }
        }
        return coste;
    }
    
    private void desparsearRead(ACLMessage in){
        String answer = in.getContent();
        Info(in.getContent());
        String sensor;
        int i;
        int cont=0;
        JsonArray vector = new JsonArray();
        JsonArray prueba = new JsonArray();
        JsonArray prueba1 = new JsonArray();
        JsonObject json = new JsonObject();
        json = Json.parse(answer).asObject();
        //String resultado = json.get("result").asString();

        vector = json.get("details").asObject().get("perceptions").asArray();
        for(JsonValue j : vector){
            
            sensor = j.asObject().get("sensor").asString();
            if("energy".equals(sensor)){
                for(JsonValue a : prueba = j.asObject().get("data").asArray()){
                    energy = a.asInt();
                }
            }else if("gps".equals(sensor)){
                prueba = j.asObject().get("data").asArray();
                cont=0;
                for(JsonValue v : prueba){
                    prueba1 = v.asArray();
                    for(JsonValue s : prueba1){
                        if(cont==0){
                            x = s.asInt();
                        }else if(cont==1){
                            y = s.asInt();
                        }else if(cont==2){
                            z = s.asInt();
                        }
                        cont++;
                    }
                }
            }
        }
                   
    }   
}
        
  
    
    

