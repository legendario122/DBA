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
public class Seeker extends IntegratedAgent {

    ACLMessage in = new ACLMessage();
    ACLMessage out = new ACLMessage();
    static String AIDSeeker = new String();
    static String ConvID = new String();
    
    public void setup() {
        super.setup();
         Info("Haciendo checkin to" + "Sphinx");
        out = new ACLMessage();
        AIDSeeker = getAID();
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
        ConvID = in.getConversationId();
        Info("Haciendo SUSCRIBE a WorldManager"); 
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("BBVA",ConvID));    
        out.setProtocol("REGULAR");
        out.setContent(JsonObject().add("type", "SEEKER").toString()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
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
