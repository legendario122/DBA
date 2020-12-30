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
import java.util.ArrayList;
import static my_agent.Controlador.ConversationID;
/**
 *
 * @author samuel
 */
public class Rescuer extends IntegratedAgent {

    ACLMessage in = new ACLMessage();
    ACLMessage out = new ACLMessage();
    
    static String ConvID = new String();

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
    //ConvID = in.getConversationId(); El conversation ID viene en el content del mensaje que nos llega y hay que desparsearlo
    
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
    out.addReceiver(new AID("control",AID.ISLOCALNAME));    
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
    
 
    out = new ACLMessage();
    out.setSender(getAID());
    out.addReceiver(new AID("BBVA",AID.ISLOCALNAME));
    out.setProtocol("REGULAR"); 
    out.setConversationId(ConversationID);
    out.setContent(objeto.toString());
    out.setEncoding("");
    out.setPerformative(ACLMessage.REQUEST);
    this.send(out);
    
    
    
    in =this.blockingReceive();
    if(in.getPerformative() != ACLMessage.INFORM){
        Info(in.getContent());
        abortSession();
    }else{
        Info(in.getContent());
        
    }

    in =this.blockingReceive();

       
    }
    
    

    @Override
    /**
     * Funcion que se encarga de hacer el checkout de larva y la plataforma.
     */    
    public void takeDown() {
        Info("Request closing the session with " + _identitymanager);
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID(_identitymanager, AID.ISLOCALNAME));
        out.setProtocol("ANALYTICS");
        out.setContent("");
        out.setConversationId(ConversationID);
        out.setPerformative(ACLMessage.CANCEL);
        this.send(out);
        in = this.blockingReceive();
        //Info(getDetailsLARVA(in));

        doCheckoutLARVA();
    }
}
