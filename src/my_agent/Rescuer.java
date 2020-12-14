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
         Info("Haciendo checkin to" + "Sphinx");
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
    ConvID = in.getConversationId();
    Info("Haciendo SUSCRIBE a WorldManager"); 
    out = new ACLMessage();
    out.setSender(getAID());
    out.addReceiver(new AID("BBVA",ConvID));    
    out.setProtocol("REGULAR");
    out.setContent(JsonObject().add("type", "RESCUER").toString()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
    out.setEncoding("");
    out.setPerformative(ACLMessage.SUSCRIBE);
    this.send(out);

    in = this.blockingReceive();
    if(in.getPerformative() != ACLMessage.INFORM){
        // Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
         abortSession();
    } 
    Info("Enviando monedas a controlador"); 
    out = new ACLMessage();
    out.setSender(getAID());
    out.addReceiver("Hitler");    
    out.setProtocol("");
    out.setContent(in.getContent()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
    out.setEncoding("");
    out.setPerformative(ACLMessage.INFORM);
    this.send(out);

       
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
