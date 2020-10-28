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
    Boolean on_target = false;
    String accion; //Parametro para ejecutar 
    int energia =1000;
    String estado="orientacion";
    //VARIABLES PARA GUARDAR LOS DATOS DE LOS SENSORES
    int compass;
    int angular;
    int altimeter;
    int distance;
    
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
    if("ok".equals(resultado)){
       while(on_target==false){
           read();
           in= this.blockingReceive();
           resultado = desparsearJson(in,false);
           if("ok".equals(resultado)){
               
                //DEBE DEVOLVER EL SIGUIENTE ESTADO
               switch (estado){
                   case "orientacion":
                       estado = operacion_orientarse(); //DONE
                       break;
                   case "desplazamiento":
                       estado = operacion_altura();
                       break;
                   case "objetivo":
                       estado = operacion_objetivo(); //DONE
                       break;
                   case "recargar":
                       estado = operacion_recargar();
                       break;
                   case "finalizado":
                       logout();
                       on_target=true;
                       break;
              
               }
           ejecutar();
           in = this.blockingReceive();
           String answer = in.getContent();
           resultado = desparsearJson(in,false);
           estado = comprobar_energia();
          } 
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
        out.setSender(getAID());
        out.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        String mundo = "BasePlayground";
        ArrayList<String> sensores = new ArrayList<String>();
        sensores.add("alive");
        sensores.add("compass");
        sensores.add("altimeter");
        sensores.add("lidar");
        sensores.add("distance");
        sensores.add("angular");
        objeto = parsearJson("login",mundo,sensores);
        out.setContent(objeto.toString());
        this.sendServer(out);
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
        out = in.createReply();
        out.setContent(objeto.toString());
        this.sendServer(out);
    }

    private void ejecutar() {
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
                break;
             
            case "read":
                json_parseado.add("key", argumento1);
                break;
                
            case "execute":
                json_parseado.add("key", argumento1);
                json_parseado.add("action", argumento2.get(0));
                break;
                /*JsonArray vector_ejecutar = new JsonArray();
                for (int i=0; i<argumento2.size(); i++)
                    vector_ejecutar.add(argumento2.get(i));
                json_parseado.add("action", vector_ejecutar);*/
                
            case "logout":  
                break;
        }

        return json_parseado;
        
    }

    private String operacion_orientarse() {
        int x=compass;
        int angulo_mas_cercano;
        int derecha=45;
        int izquierda=-45;
        ArrayList<Integer> rotacion_derecha = new ArrayList<Integer>();
        ArrayList<Integer> rotacion_izquierda = new ArrayList<Integer>();
        
        if(angular<=-90 || angular>=-135){
            if((-135-angular)<= (angular-(-90))){
                angulo_mas_cercano=-90;
            }else{
                angulo_mas_cercano=-135;
            }
        }else if(angular<=-45 || angular>=-90){
            if((-90-angular)<= (angular-(-45))){
                angulo_mas_cercano=-45;
            }else{
                angulo_mas_cercano=-90;
            }
        }else if(angular<=0 || angular>=-45){
            if((-45-angular)<= (angular-(0))){
                angulo_mas_cercano=0;
            }else{
                angulo_mas_cercano=-45;
            }
        }else if(angular<=45 || angular>=0){
            if((0-angular)<= (angular-(45))){
                angulo_mas_cercano=45;
            }else{
                angulo_mas_cercano=0;
            }
        }else if(angular<=90 || angular>=45){
            if((45-angular)<= (angular-(90))){
                angulo_mas_cercano=90;
            }else{
                angulo_mas_cercano=45;
            }
        }else if(angular<=135 || angular>=90){
            if((90-angular)<= (angular-(135))){
                angulo_mas_cercano=135;
            }else{
                angulo_mas_cercano=90;
            }
        }else if(angular<=180 || angular>=135){
            if((135-angular)<= (angular-(180))){
                angulo_mas_cercano=180;
            }else{
                angulo_mas_cercano=135;
            }
        }else{
            if((-135)-angular >= 180 - Math.abs(angular)){
                angulo_mas_cercano=180;               
            }else{
                angulo_mas_cercano=-135;
            }
        }
        
        if(compass!=angulo_mas_cercano){
            
            rotacion_derecha.add(x);
            if(x!=180){
            x+=derecha;
            }else{
                x=-135;
            }
            
            while(x!=angulo_mas_cercano){      
                rotacion_derecha.add(x);
                if(x==180){
                    x=-135;
                    if(x!=angulo_mas_cercano)
                        rotacion_derecha.add(x);
                }
                if(x!=angulo_mas_cercano)
                    x+=derecha;
            }
            
            x=compass;
            rotacion_izquierda.add(x);
            if(x!=-135){
            x+=izquierda;
            }else{
                x=180;
            }
            
            while(x!=angulo_mas_cercano){           

                rotacion_izquierda.add(x);
                if(x==-135){
                    x=180;
                    if(x!=angulo_mas_cercano)
                        rotacion_izquierda.add(x);
                }
                if(x!=angulo_mas_cercano)
                    x+=izquierda;
            }
            
            if(rotacion_derecha.size()>rotacion_izquierda.size()){
                if(rotacion_izquierda.size()==1){
                    estado="desplazamiento";
                    accion="rotateL";
                }else{
                    estado="orientacion";
                    accion="rotateL";
                }
            }else{
                if(rotacion_derecha.size()==1){
                    estado="desplazamiento";
                    accion="rotateR";
                }else{
                    estado="orientacion";
                    accion="rotateR";
                }
            }
            
        }else{
            estado = operacion_altura();
        }
        
        return estado;
    }

    private String operacion_altura() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String operacion_objetivo() {
        
        if(altimeter>=5){
            
            estado="objetivo";
            accion="moveD";
            
        }else{
            
            estado="finalizado";
            accion="touchD";
        }
        
        
        return estado;
    }

    private String operacion_recargar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String comprobar_energia() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
