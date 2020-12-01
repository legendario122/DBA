package my_agent;

import ControlPanel.TTYControlPanel;
import IntegratedAgent.IntegratedAgent;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Arrays;
import YellowPages.YellowPages;

public class Controlador extends IntegratedAgent {
    
    /**
    * Variables para el panel de control.
    **/   
    TTYControlPanel myControlPanel;
    int width; 
    int height;
    int maxflight = 255;
    String ConversationID = "";
    
    /**
    * Variables para el controlador
    * DBAMap mapa = new DBAMap();
        mapa.fromJson(map);
    **/
    
    ACLMessage in = new ACLMessage();
    ACLMessage out = new ACLMessage();
    YellowPages yp;
    
    @Override
        /**
     * Funcion que se encarga de hacer el checkin en larva y en la plataforma. Tambien se encarga de inicializar el panel de
     * control donde veremos la informacion de los sensores.
     */
    public void setup() {
        super.setup();
        Info("Haciendo checkin to" + _identitymanager);
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID(_identitymanager,AID.ISLOCALNAME));
        out.setProtocol("ANALYTICS");
        out.setContent("");
        out.setEncoding(_myCardID.getCardID());
        out.setPerformative(ACLMessage.SUBSCRIBE);
        this.send(out);
        in = this.blockingReceive();
        if(in.getPerformative() != ACLMessage.INFORM){
            Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
            abortSession();
        }      
        Info("Checkeo realizado");
        Info("Requiriendo paginas amarillas");
        out = in.createReply();
        out.setContent("");
        out.setEncoding("");
        out.setPerformative(ACLMessage.QUERY_REF);
        this.send(out);
        in =this.blockingReceive();
        if(in.getPerformative() != ACLMessage.INFORM){
            Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
            abortSession();
        }  

        yp = new YellowPages();
        yp.updateYellowPages(in);
        System.out.println("\n" + yp.prettyPrint());
    
        myControlPanel = new TTYControlPanel(getAID());
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////CHECKING EN WORLD MANAGER///////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        _identitymanager = "WorldManager";
        Info("Haciendo checkin to" + _identitymanager); //No se como poner world manager bien
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID(_identitymanager,AID.ISLOCALNAME));  //No se como poner world manager bien
        out.setProtocol("ANALYTICS");
        out.setContent(""); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
        out.setEncoding("");
        out.setPerformative(ACLMessage.SUBSCRIBE);
        this.send(out);
        in = this.blockingReceive();
        if(in.getPerformative() != ACLMessage.INFORM){
            Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
            abortSession();
        }      
        
        ConversationID = in.getConversationId();

        Info("Checkeo realizado en World manager");
    }

    @Override
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * 
     * Funcion donde se define como va a comportarse el drone.
     * Primeramente, llama a la funcion loguearse. Tras esto y mientras no alcancemos
     * el objetivo, ejecutara un bucle. Leemos las percepciones, se las pasamos al 
     * panel de control y en funcion de la accion anterior y las percepciones anteriores
     * se decidio que el drone entrara ahora al estado pertinente que podria ser:
     * 1 Estado orientarse
     * 2 Estado desplazamiento
     * 3 Esado objetivo
     * 4 Estado recargar
     * 5 Estado finalizado
     * 
     * Las operaciones que realizamos en dichos estados (switch-case) devuelve siempre
     * el estado siguiente del drone.
     * 
     * Tras esta fase de decision de accion y estado siguiente. Se entra a la funcion 
     * ejecutar para realizar la accion pertinente. Asi hasta que el drone llegue al objetivo
     * el estado sea finalizado y on target==true.
     * 
     * @return Nada. 
     */
    public void plainExecute() {
        
        //generar agente greedy (pasarle el mapa) 
        //comprar sensores y tickets de recarga
        //generar drones seeker y rescuer
        //pasamos trayectorias a seeker.
        //se queda escuchando:
        // mensaje de seeker con ubicacion de aleman, añadimos a la lista alemanes interceptados y  le pasamos la ubicacion al rescuer.
        //mensaje de recarga de seeker y rescuer
        //(Si seeker termina trayectoria o se queda sin bateria y no hay mas tickets de recarga) mensaje de cancel, lista de drones activos delete
        //(Si no hay mas seeker activos y no hay mas alemanes en lista rescuer o esta sin bateria y no hay tickets de recarga) mensaje de cancel, lista de drones activos delete)
        //if lista drones activos es ==0)
        //mensaje de cancel a greedy
        //takedown del mundo 
    
    }
    
    

    @Override
    /**
     * Funcion que se encarga de hacer el checkout de larva y la plataforma.
     */    
    @Override
    public void takeDown() {
        Info("Request closing the session with " + myServiceProvider);
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID(myServiceProvider, AID.ISLOCALNAME));
        out.setProtocol("ANALYTICS");
        out.setContent("");
        out.setConversationId(session);
        out.setPerformative(ACLMessage.CANCEL);
        this.send(out);
        in = this.blockingReceive();
        Info(getDetailsLARVA(in));

        doCheckoutLARVA();
    }  

    
}
