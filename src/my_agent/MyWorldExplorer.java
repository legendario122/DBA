package my_agent;

import IntegratedAgent.IntegratedAgent;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;

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
        objeto = parsearJson("read",key,null);
        out.setContent(objeto.toString());
        this.send(out);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void ejecutar() {
        out.setSender(getAID());
        out.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        String accion="moveF";
        ArrayList<String> acciones = new ArrayList<String>();
        acciones.add(accion);
        objeto = parsearJson("execute",key,acciones);
        out.setContent(objeto.toString());
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void logout() {
        out.setSender(getAID());
        out.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        objeto = parsearJson("logout",null,null);
        out.setContent(objeto.toString());
        this.send(out);
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private JsonObject parsearJson(String comando, String argumento1, ArrayList<String> argumento2) {
        JsonObject json_parseado = new JsonObject();
        json_parseado.add("command", comando);
        switch (comando) {
            case "login":
                json_parseado.add("world", argumento1);
                JsonArray vector = new JsonArray();
                for (int i=0; i<argumento2.size(); i++)
                    vector.add(argumento2.get(i));
                json_parseado.add("attach", vector);
                
            case "read":
                json_parseado.add("key", argumento1);
                
                
            case "execute":
                json_parseado.add("action", argumento1);
                json_parseado.add("key", argumento2.get(1));

            case "logout":     
        }

        return json_parseado;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
