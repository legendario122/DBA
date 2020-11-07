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

public class MyWorldExplorer extends IntegratedAgent {

    String receiver;
    String key;
    ACLMessage in = new ACLMessage();
    ACLMessage out = new ACLMessage();
    JsonObject objeto = new JsonObject();
    Boolean on_target = false;
    String accion; //Parametro para ejecutar 
    String estado="orientacion";
    TTYControlPanel myControlPanel;
    //VARIABLES PARA GUARDAR LOS DATOS DE LOS SENSORES
    int width; 
    int height;
    int maxflight = 255;
    //int compass;
    double compass;
    double angular;
    double altimeter;
    double distance;
    int x;
    int y;
    int z;
    int energy;
    int alive;
    int lidar[][] = new int[7][7];
    
    @Override
    public void setup() {
        super.setup();
        doCheckinPlatform();
        doCheckinLARVA();
        receiver = this.whoLarvaAgent();
        myControlPanel = new TTYControlPanel(getAID());
    }

    @Override
    public void plainExecute() {
    
    /// Dialogar con receiver para entrar en el mundo
    //  moverse y leer los sensores

    //Funcion que enviara un mensaje al agente worldmanager para loguearse.
           
    loguearse();
    in = this.blockingReceive();  
    String resultado = desparsearJson(in,"login");
    Info("El resultado del login es "+resultado);
    if("ok".equals(resultado)){
       while(on_target==false){
           read();
           in= this.blockingReceive();
           resultado = desparsearJson(in,"read");
           myControlPanel.feedData(in, width, height, maxflight);
           myControlPanel.fancyShow();
           if("ok".equals(resultado)){
               
                //DEBE DEVOLVER EL SIGUIENTE ESTADO
               switch (estado){
                   case "orientacion":
                       estado = operacion_orientarse(-1); //DONE
                       break;
                   case "desplazamiento":
                       estado = operacion_altura();   //DONE
                       break;
                   case "objetivo":
                       estado = operacion_objetivo(); //DONE
                       break;
                   case "recargar":
                       estado = operacion_recargar(); //DONE
                       break;
                   case "finalizado":
                       if(altimeter==5){
                           accion="moveD";
                       }else{
                            accion="touchD";
                            logout();
                            on_target=true;
                       }
                       break;
              
               }
           ejecutar();
           in = this.blockingReceive();
           String answer = in.getContent();
           resultado = desparsearJson(in,"execute");
           estado = comprobar_energia();
           Info("El estado es: "+estado);
           Info("La altura: "+altimeter);
           Info("La distancia que tenemos es "+distance);
           Info("La energia que tenemos es "+energy);
           Info("La coordenada x es "+x);
           Info("La coordenada y es "+y);
           Info("La coordenada z es "+z);
          } 
       }
                      
       }
    myControlPanel.close();
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
        String mundo = "World6";
        ArrayList<String> sensores = new ArrayList<String>();
        sensores.add("alive");
        sensores.add("energy");
        sensores.add("gps");
        sensores.add("compass");
        sensores.add("altimeter");
        sensores.add("lidar");
        sensores.add("distance");
        sensores.add("angular");
        objeto = parsearJson("login",mundo,sensores);
        out.setContent(objeto.toString());
        this.sendServer(out);
    }

    private String desparsearJson(ACLMessage in, String operacion) {
        String answer = in.getContent();
        String sensor;
        int i;
        int zeta=0;
        int cont=0;
        JsonArray vector = new JsonArray();
        JsonArray prueba = new JsonArray();
        JsonArray prueba1 = new JsonArray();
        JsonArray matriz =  new JsonArray();
        JsonArray matriz_bis =  new JsonArray();
        JsonObject json = new JsonObject();
        json = Json.parse(answer).asObject();
        String resultado = json.get("result").asString();
        if("ok".equals(resultado)){
            switch (operacion) {
                case "login":
                    key = json.get("key").asString();
                    width = json.get("width").asInt(); 
                    height = json.get("height").asInt();
                    maxflight = json.get("maxflight").asInt();
                    break;

                case "read":
                    vector = json.get("details").asObject().get("perceptions").asArray();
                    for(JsonValue j : vector){
                        sensor = j.asObject().get("sensor").asString();
                        if("alive".equals(sensor)){
                            //alive = j.asObject().get("data").asInt();
                            for(JsonValue a : prueba = j.asObject().get("data").asArray()){
                                alive = a.asInt();
                            }
                        }else if("compass".equals(sensor)){
                            //compass = j.asObject().get("data").asInt();
                            for(JsonValue a : prueba = j.asObject().get("data").asArray()){
                                //compass = a.asInt();
                                compass = a.asDouble();
                            }
                        }else if("altimeter".equals(sensor)){
                           //altimeter = j.asObject().get("data").asDouble();
                           for(JsonValue a : prueba = j.asObject().get("data").asArray()){
                                altimeter = a.asDouble();
                            }
                        }else if("lidar".equals(sensor)){
                            matriz = j.asObject().get("data").asArray();
                            i=0;
                            for(JsonValue v : matriz){
                                //matriz_bis = v.asObject().asArray();
                                matriz_bis = v.asArray();
                                for(JsonValue s : matriz_bis){
                                    lidar[i][zeta]=s.asInt();
                                    zeta++;
                                }
                                zeta=0;
                                i++;
                            }
                        }else if("distance".equals(sensor)){
                            //distance = j.asObject().get("data").asInt();
                            for(JsonValue a : prueba = j.asObject().get("data").asArray()){
                                distance = a.asDouble();
                            }
                        }else if("angular".equals(sensor)){
                            //angular = j.asObject().get("data").asInt();
                            for(JsonValue a : prueba = j.asObject().get("data").asArray()){
                                angular = a.asDouble();
                            }
                        }else if("energy".equals(sensor)){
                            //angular = j.asObject().get("data").asInt();
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
                    
                    break;

                case "execute":
                    break;
            }   
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
        ArrayList<String> vector_vacio = new ArrayList<String>();
        out.setSender(getAID());
        out.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        objeto = parsearJson("logout",key,vector_vacio);
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
                
                
            case "logout":  
                break;
        }

        return json_parseado;
        
    }

    private String operacion_orientarse(int angulo_aux) {
        Info("He entrado en operacion_orientarse");
        int x=(int)compass;
        int angulo_mas_cercano;
        int derecha=45;
        int izquierda=-45;
        ArrayList<Integer> rotacion_derecha = new ArrayList<Integer>();
        ArrayList<Integer> rotacion_izquierda = new ArrayList<Integer>();
        
            
        ArrayList<Integer> lista_angulos=new ArrayList<Integer>();
        lista_angulos = calcular_lista_angulos();
        
        angulo_mas_cercano = verificarAngulos(lista_angulos);
        
        if((int) compass!=angulo_mas_cercano){
            
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
            
            x=(int)compass;
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
            
            Info("La longitud derecha es "+rotacion_derecha.size());
            Info("La longitud izquierda es "+rotacion_izquierda.size());
            
            if(rotacion_derecha.size()>rotacion_izquierda.size()){
                if(rotacion_izquierda.size()==1){ //aqui iba 1
                    if(distance==0){
                        estado="objetivo";
                    }else{
                        estado="desplazamiento";
                    }
                    accion="rotateL";
                }else{
                    estado="orientacion";
                    accion="rotateL";
                }
            }else{
                if(rotacion_derecha.size()==1){ //aqui iba 1
                    if(distance==0){
                        estado="objetivo";
                    }else{
                        estado="desplazamiento";
                    }
                    accion="rotateR";
                }else{
                    estado="orientacion";
                    accion="rotateR";
                }
            }
            
        }else{
            if(distance>=1){
                               
                estado = operacion_altura();
            }else{
               
                estado = operacion_objetivo();
                
            }
            
        }
        
        return estado;
    }

    private String operacion_altura() { //DEL SABUFU
        int angulo = (int)compass;
        if(distance >= 1){
            if(angulo == 0){
                if(lidar[2][3] >= 0){
                    accion = "moveF";
                    estado = "orientacion";
                }else{
                    
                    accion = "moveUP";
                    estado = "desplazamiento";
                }
            }else if(angulo == 45){
                if(lidar[2][4] >= 0){
                    accion = "moveF";
                    estado = "orientacion";
                    
                }else{
                    
                    accion = "moveUP";
                    estado = "desplazamiento";
                
                }
            }else if(angulo == 90){
                if(lidar[3][4] >= 0){
                    accion = "moveF";
                    estado = "orientacion";
                }else{
                    
                    accion = "moveUP";
                    estado = "desplazamiento";
                }
            }else if(angulo == 135){
                if(lidar[4][4] >= 0){
                    accion = "moveF";
                    estado = "orientacion";
                }else{
                    
                    accion = "moveUP";
                    estado = "desplazamiento";
                }
            }else if(angulo == 180){
                if(lidar[4][3] >= 0){
                    accion = "moveF";
                    estado = "orientacion";
                }else{
                    
                    accion = "moveUP";
                    estado = "desplazamiento";
                }
            }else if(angulo == -135){
                if(lidar[4][2] >= 0){
                    accion = "moveF";
                    estado = "orientacion";
                }else{
                    
                    accion = "moveUP";
                    estado = "desplazamiento";
                }
            }else if(angulo == -90){
                if(lidar[3][2] >= 0){
                    accion = "moveF";
                    estado = "orientacion";
                }else{
                    
                    accion = "moveUP";
                    estado = "desplazamiento";
                }
            }else if(angulo == -45){
                if(lidar[2][2] >= 0){
                    accion = "moveF";
                    estado = "orientacion";
                }else{
                    
                    accion = "moveUP";
                    estado = "desplazamiento";
                }
            }
        }else{
            if(altimeter==0){
                estado = "finalizado";
            }else{
                estado = "objetivo";
            }
            accion = "moveF";
        }
        return estado;        
    }
    

    private String operacion_objetivo() {
        
        if(altimeter>=15){
            
            estado="objetivo";
            accion="moveD";
            
        }else{
            
            estado="finalizado";
            if(altimeter!=0){
                accion="moveD";
            }else{
                accion="touchD";
            }
            
        }
        
        
        return estado;
    }

    private String operacion_recargar() {
        if(altimeter>5 || altimeter==5){
            accion="moveD";
        }else if(altimeter>0 && altimeter<5){
            accion="touchD";
        }else if(altimeter==0){
            accion="recharge";
        }
        if(altimeter>0){
            estado="recargar";
        }else if(altimeter==0){
            estado="desplazamiento";
        }
        return estado;
    }

    private String comprobar_energia() { //DEL SABUFU
        int energia = energy - 100;
        if (energia <= (int) altimeter + (altimeter/5)*8){ //si la energia restante es la justa para aterrizar, recargamos
            return "recargar";
        }
        return estado; 
    }

    private int calcular_angulo_mas_cercano(ArrayList<Integer> angulos,int angular1) {
        int angulo_calc=-1;
        int menor=-1, mayor=-1;
        int i;
        boolean encontrado=false;
        if(angular1>=-135 && angular1<180){
            for(i=0; i<angulos.size(); i++){
                if(angulos.get(i)>=angular1 && encontrado==false){
                    if(angulos.size()!=1 && i!=0){
                        menor= angulos.get(i-1);
                    }else{
                        menor=-180;
                    }
                    mayor=angulos.get(i);
                    encontrado=true;
                }
            }
            
            if(encontrado==false && angulos.size()==1){
                angulo_calc=angulos.get(0);
            }else if(angular1>=157.5 && angulos.get(angulos.size()-1)!=180){
                angulo_calc= angulos.get(angulos.size()-1);
            }else{
                if((menor-angular1)<= (angular1-mayor)){
                angulo_calc=mayor;
                }else{
                    angulo_calc=menor;
                }
            }
            
            
        }else{
            if(angular1>=180){
                angulo_calc=180;
            }else{
                if(angulos.get(angulos.size()-1)==180 && angulos.get(0)==-135){
                    menor=-135;
                    mayor=180;
                    if(menor-angular1 >= mayor - Math.abs(angular1)){
                        angulo_calc=mayor;               
                    }else{
                        angulo_calc=menor;
                    }
                }else if(angulos.get(angulos.size()-1)!=180){
                        angulo_calc=-135;
                }else{
                        angulo_calc=180;
                }
            }

        }
        
        
        return angulo_calc;
    }

    private ArrayList<Integer> calcular_lista_angulos() {
        ArrayList<Integer> angulos=new ArrayList<Integer>(Arrays.asList(-135, -90, -45, 0, 45, 90, 135, 180));
        boolean encontrado=false;
        int iterador1 = calcular_angulo_mas_cercano(angulos, (int) angular);
        
        ArrayList<Integer> lista_angulos = new ArrayList<Integer>();
        lista_angulos.add(iterador1);
        for( int i=0; i<angulos.size(); i++){
            if(angulos.get(i)==iterador1 && encontrado==false){
                angulos.remove(i);
                iterador1=i;
                encontrado=true;
            }
        }
        int iterador2 = calcular_angulo_mas_cercano(angulos, (int) angular);
        
        lista_angulos.add(iterador2);
        encontrado=false;
        angulos=new ArrayList<Integer>(Arrays.asList(-135, -90, -45, 0, 45, 90, 135, 180));
        for( int i=0; i<angulos.size(); i++){
            if(angulos.get(i)==iterador2 && encontrado==false){
                iterador2=i;
                encontrado=true;
            }
        }
        boolean extremos=false;
        if(iterador1>iterador2){
            if(iterador1==7 && iterador2==0){
                iterador1=iterador2;
                iterador2=7;
                extremos=true;
            }
            while(lista_angulos.size()!=8){
                if(angulos.get(iterador1)==180){
                    if(iterador2!=0){
                        iterador1=0;
                    }else{
                        break;
                    }
                }else{
                    iterador1++;
                }
                
                if(angulos.get(iterador2)==-135){
                    if(iterador1!=angulos.size()-1){
                        iterador2=angulos.size()-1;
                    }else{
                        break;
                    }
                }else{
                    iterador2--;
                }
                if(extremos==false){
                    lista_angulos.add(angulos.get(iterador1));
                    lista_angulos.add(angulos.get(iterador2));
                }else{
                    lista_angulos.add(angulos.get(iterador2));
                    lista_angulos.add(angulos.get(iterador1));
                }
                
            }
        }else{
            if(iterador1==0 && iterador2==7){
                iterador1=iterador2;
                iterador2=0;
                extremos=true;
            }
            while(lista_angulos.size()!=8){
                if(angulos.get(iterador1)==-135){
                    if(iterador2!=angulos.size()-1){
                        iterador1=angulos.size()-1;
                    }else{
                        break;
                    }
                }else{
                    iterador1--;
                }
                
                if(angulos.get(iterador2)==180){
                    if(iterador1!=0){
                        iterador2=0;
                    }else{
                        break;
                    }
                }else{
                    iterador2++;
                }
                if(extremos==false){
                    lista_angulos.add(angulos.get(iterador1));
                    lista_angulos.add(angulos.get(iterador2));
                }else{
                    lista_angulos.add(angulos.get(iterador2));
                    lista_angulos.add(angulos.get(iterador1));
                }
            }
        }
        return lista_angulos;
    }
    
    int verificarAngulos(ArrayList<Integer> Lista_angulo_ordenados){ // comprueba los angulos con el lidar y devuelve el angulo mas cercano al que nos podemos mover
    boolean encontrado = false;
    int angulo_factible = -1;
    int i = 0;
    int maxflight1 = maxflight - 5;
        while(encontrado != true){
    
            if(Lista_angulo_ordenados.get(i) == 0){
                if(lidar[2][3] >= 0 || maxflight1 > z){
                    encontrado = true;
                    angulo_factible = Lista_angulo_ordenados.get(i);
                }
            }else if(Lista_angulo_ordenados.get(i) == 45){
                if(lidar[2][4] >= 0 || maxflight1 > z){
                    encontrado = true;
                    angulo_factible = Lista_angulo_ordenados.get(i);
                }
            }else if(Lista_angulo_ordenados.get(i) == 90){
                if(lidar[3][4] >= 0 || maxflight1 > z){
                    encontrado = true;
                    angulo_factible = Lista_angulo_ordenados.get(i);
                }
            }else if(Lista_angulo_ordenados.get(i) == 135){
                if(lidar[4][4] >= 0 || maxflight1 > z){
                    encontrado = true;
                    angulo_factible = Lista_angulo_ordenados.get(i);
                }
            }else if(Lista_angulo_ordenados.get(i) == 180){
                if(lidar[4][3] >= 0 || maxflight1 > z){
                    encontrado = true;
                    angulo_factible = Lista_angulo_ordenados.get(i);
                }
            }else if(Lista_angulo_ordenados.get(i) == -135){
                if(lidar[4][2] >= 0 || maxflight1 > z){
                    encontrado = true;
                    angulo_factible = Lista_angulo_ordenados.get(i);
                }
            }else if(Lista_angulo_ordenados.get(i) == -90){
                if(lidar[3][2] >= 0 || maxflight1 > z){
                    encontrado = true;
                    angulo_factible = Lista_angulo_ordenados.get(i);
                }
            }else if(Lista_angulo_ordenados.get(i) == -45){
                if(lidar[2][2] >= 0 || maxflight1 > z){
                    encontrado = true;
                    angulo_factible = Lista_angulo_ordenados.get(i);
                }
            }
            
            i++;
            
        }    
    return angulo_factible;
    }
}
