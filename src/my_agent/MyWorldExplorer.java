package my_agent;

import IntegratedAgent.IntegratedAgent;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;

public class MyWorldExplorer extends IntegratedAgent {

    String receiver;
    String key;
    ACLMessage in = new ACLMessage();
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
    in = this.blockingReceive();  
    String resultado = desparsearJson(in,true);
                
    Info("El resultado del login es "+resultado);
    
    //if(resultado == "ok"){
       Info("He entrado en el primer if");
       read();
       in= this.blockingReceive();
       resultado = desparsearJson(in,false);
       Info("El resultado del read es "+resultado);
       //if(resultado=="ok"){    
           ejecutar();
           in = this.blockingReceive();
           String answer = in.getContent();
           Info("La respuesta es: "+answer);
           resultado = desparsearJson(in,false);
           //if(resultado=="ok"){
           //}
           logout();      
       //}
    //}
    
    }

    @Override
    public void takeDown() {
        this.doCheckoutLARVA();
        this.doCheckoutPlatform();
        super.takeDown();
    }

    private void loguearse() {
        out.setSender(getAID());
        out.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        String mundo = "BasePlayground";
        ArrayList<String> sensores = new ArrayList<String>();
        sensores.add("alive");
        sensores.add("compass");
        sensores.add("altimeter");
        sensores.add("visual");
        objeto = parsearJson("login",mundo,sensores);
        out.setContent(objeto.toString());
        this.send(out);
    }

    private String desparsearJson(ACLMessage in, boolean b) {
        String answer = in.getContent();
        JsonObject json = new JsonObject();
        json = Json.parse(answer).asObject();
        String resultado = json.get("result").asString();
        if(b==true){
            key = json.get("key").asString();
        }      
        return resultado;
    }

    private void read() {
        ArrayList<String> vacio = new ArrayList<String>();
        objeto = parsearJson("read",key,vacio);
        Info("He pasado el parsear");
        out = in.createReply();
        out.setContent(objeto.toString());
        this.sendServer(out);
    }

    private void ejecutar() {
        String accion="moveF";
        ArrayList<String> acciones = new ArrayList<String>();
        acciones.add(accion);
        objeto = parsearJson("execute",key,acciones);
        out = in.createReply();
        out.setContent(objeto.toString());
        this.sendServer(out);
    }

    private void logout() {
        String vacio="";
        ArrayList<String> vector_vacio = new ArrayList<String>();
        out.setSender(getAID());
        out.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        objeto = parsearJson("logout",vacio,vector_vacio);
        out.setContent(objeto.toString());
        this.send(out);
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
                json_parseado.add("key", argumento1);
                Info("Argumento2.get0 : "+argumento2.get(0));
                json_parseado.add("action", argumento2.get(0));
                /*JsonArray vector_ejecutar = new JsonArray();
                for (int i=0; i<argumento2.size(); i++)
                    vector_ejecutar.add(argumento2.get(i));
                json_parseado.add("action", vector_ejecutar);*/
                

            case "logout":     
        }

        return json_parseado;
        
    }
}
