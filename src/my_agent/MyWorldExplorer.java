package my_agent;

import IntegratedAgent.IntegratedAgent;
import com.eclipsesource.json.JsonObject;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class MyWorldExplorer extends IntegratedAgent {

    String receiver;
    String key;
    ACLMessage out = new ACLMessage();
    JsonObject objeto = new JsonObject();
    
    @Override
    public void setup() {
        super.setup();
        doCheckinPlatform();
        doCheckinLARVA();
        receiver = this.whoLarvaAgent();


    }

    @Override
    public void plainExecute() {
    
    /// Dialogar con receiver para entrar en el mundo
    //  moverse y leer los sensores

    //Funcion que enviara un mensaje al agente worldmanager para loguearse.
    
        
    loguearse();
    ACLMessage in= this.blockingReceive();
    
    String resultado =desparsearJson(in);
    
    
             
    if(resultado == "ok"){
       read();
       in= this.blockingReceive();
       resultado =desparsearJson(in);
       if(resultado=="ok"){
       
           ejecutar();
           in= this.blockingReceive();
           resultado =desparsearJson(in);
           
           if(resultado=="ok"){
           }
               logout();
       
       }
    }
    
    }

    @Override
    public void takeDown() {
        this.doCheckoutLARVA();
        this.doCheckoutPlatform();
        super.takeDown();
    }

    private void loguearse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String desparsearJson(ACLMessage in) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void read() {
        out.setSender(getAID());
        out.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        objeto = parsearJson("leer",key,null);
        out.setContent(objeto.toString());
        this.send(out);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void ejecutar() {
        out.setSender(getAID());
        out.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        String accion="moveF";
        objeto = parsearJson("ejecutar",key,accion);
        out.setContent(objeto.toString());
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void logout() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private JsonObject parsearJson(String comando, String argumento1, String argumento2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
