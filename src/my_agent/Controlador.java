package my_agent;
  
import static ACLMessageTools.ACLMessageTools.getDetailsLARVA;
import static ACLMessageTools.ACLMessageTools.getJsonContentACLM;
import Map2D.Map2DGrayscale;
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
    static String ConversationID = "";
    Map2DGrayscale myMap;
    String myWorld = "problem1";
    ArrayList<producto> lista_productos;
    ArrayList<producto> lista_productos_ordenada;
    ArrayList<String> billetes;
    
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
        Info("Haciendo checkin to" + "Sphinx" +" controlador");
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
        

        myControlPanel = new TTYControlPanel(getAID());
        
        
         Info("Requiriendo paginas amarillas");
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("Sphinx",AID.ISLOCALNAME));
        out.setProtocol("ANALYTICS");
        out.setContent("");
        out.setEncoding("");
        out.setPerformative(ACLMessage.QUERY_REF);
        this.send(out);
        in =this.blockingReceive();
        if(in.getPerformative() != ACLMessage.INFORM){
          //  Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
            abortSession();
        }  

        yp = new YellowPages();
        yp.updateYellowPages(in);
        System.out.println("\n" + yp.prettyPrint());
        
        ArrayList<String> pepe = new ArrayList(yp.queryProvidersofService("Analytics groupid 13"));
        for(int i=0; i<pepe.size(); i++){
           Info(pepe.get(i));
        }
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////CHECKING EN WORLD MANAGER///////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      
        Info("Haciendo checkin to" + "BBVA"); //No se como poner world manager bien
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID(pepe.get(0),AID.ISLOCALNAME));  //No se como poner world manager bien
        out.setProtocol("ANALYTICS");
        out.setContent(new JsonObject().add("problem", "Playground1").toString()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
        out.setEncoding("");
        out.setPerformative(ACLMessage.SUBSCRIBE);
        this.send(out);
        in = this.blockingReceive();
        if(in.getPerformative() != ACLMessage.INFORM){
            Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to ");//+ getDetailsLarva(in));
            Info(in.getContent());
            abortSession();
        }      
        
        //Descarga y almacenamiento del mapa
        //Falta guardar el mapa en matriz de enteros
        //falta averiguar agentes/servicios de las paginas amarillas
        System("Save map of world ");
        JsonObject jscontent = getJsonContentACLM(in);
        if (jscontent.names().contains("map")) {
	        JsonObject jsonMapFile = jscontent.get("map").asObject();
	        String mapfilename = jsonMapFile.getString("filename", "nonamefound");
            Info("Found map " + mapfilename);
            
            //myMap.loadMap(mapfilename);
            myMap = new Map2DGrayscale();
            if (myMap.fromJson(jsonMapFile)) {
        	    Info("Map " + mapfilename + "( " + myMap.getWidth() + "cols x" + myMap.getHeight() + "rows ) saved on disk (project's root folder) and ready in memory");
                Info("Sampling three random points for cross-check:");
                int px, py;
                for (int ntimes = 0; ntimes < 3; ntimes++) {
                	px = (int) (Math.random() * myMap.getWidth());
                        py = (int) (Math.random() * myMap.getHeight());
                        Info("\tX: " + px + ", Y:" + py + " = " + myMap.getLevel(px, py));
                }
	        }else{
		        Info("\t" + "There was an error processing and saving the image ");
	        }
        } else {
	        Info("\t" + "There is no map found in the message");
        }

        ConversationID = in.getConversationId();
        Info(ConversationID);
        
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

        Info("Haciendo Query-if a Drones"); 
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("seek1",AID.ISLOCALNAME));  
        out.setProtocol("");
        out.setContent(new JsonObject().add("ConversationID", ConversationID).toString()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
        out.setEncoding("");
        out.setPerformative(ACLMessage.REQUEST);
        this.send(out);

        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("seek2",AID.ISLOCALNAME));  
        out.setProtocol("");
        out.setContent(new JsonObject().add("ConversationID", ConversationID).toString()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
        out.setEncoding("");
        out.setPerformative(ACLMessage.REQUEST);
        this.send(out);

        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("seek3",AID.ISLOCALNAME));  
        out.setProtocol("");
        out.setContent(new JsonObject().add("ConversationID", ConversationID).toString()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
        out.setEncoding("");
        out.setPerformative(ACLMessage.REQUEST);
        this.send(out);

        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("resc",AID.ISLOCALNAME));  
        out.setProtocol("");
        out.setContent(new JsonObject().add("ConversationID", ConversationID).toString()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
        out.setEncoding("");
        out.setPerformative(ACLMessage.REQUEST);
        this.send(out);

        int cont=0;
        ArrayList<String> Bitcoins = new ArrayList<String>();

        do{
            in = this.blockingReceive();
            if(in.getPerformative() != ACLMessage.INFORM){
                //Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
                abortSession();
            }else{
                Info(in.getContent());
                Bitcoins.add(in.getContent());
                cont++;
            }
            
        }while(cont<4);
        
        // PAGINAS AMARILLAS

      
        Info("Requiriendo paginas amarillas");
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("Sphinx",AID.ISLOCALNAME));
        out.setProtocol("ANALYTICS");
        out.setContent("");
        out.setEncoding("");
        out.setPerformative(ACLMessage.QUERY_REF);
        this.send(out);
        in =this.blockingReceive();
        if(in.getPerformative() != ACLMessage.INFORM){
          //  Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
            abortSession();
        }  

        yp = new YellowPages();
        yp.updateYellowPages(in);
        System.out.println("\n" + yp.prettyPrint());
        

        Info("Obtuve las paginas amarillas");
        
        ArrayList<String> Tiendas = new ArrayList<String>(yp.queryProvidersofService("shop@"+ConversationID));
        
        for(int i=0; i<Tiendas.size(); i++){
            out = new ACLMessage();
            out.setSender(getAID());
            out.addReceiver(new AID(Tiendas.get(i),AID.ISLOCALNAME));
            out.setProtocol("REGULAR");
            out.setContent("{}");
            out.setEncoding("");
            out.setConversationId(ConversationID);
            out.setPerformative(ACLMessage.QUERY_REF);
            this.send(out);
        }
        cont=0;
        do{
            in = this.blockingReceive();
            if(in.getPerformative() != ACLMessage.INFORM){
                //Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
                abortSession();
            }else{
                Info(in.getContent());
                //guardar lista de objetos
                cont++;
            }
           //PEPE
        }while(cont<Tiendas.size());
        Info("Obtuve los productos");
        //Buscar tiendas por CONVID
        //regular seeker 
        //
        //if(yp.queryProvidersofService("marketplace")){
            //FUCK
        //}

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
    
    

    
    
    public ArrayList<producto> ordenar_productos(ArrayList<producto> lista){
        int tam = lista.size();
        int a=0, b=0;
        producto aux;
        for(int i=0; i<lista.size();i++){
            a=0;
            b=1;
            for(int j=0; j<tam-1; j++){
                if(lista.get(a).getPrecio()>lista.get(b).getPrecio()){
                    aux=lista.get(a);
                    lista.set(a, lista.get(b));
                    lista.set(b, aux);
                }
                a++;
                b++;
            }
        }
        
        return lista;
    }
    
    
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