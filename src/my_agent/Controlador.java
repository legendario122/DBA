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
    ArrayList<producto> lista_productos = new ArrayList<producto>();
    ArrayList<producto> lista_compra = new ArrayList<producto>();
    ArrayList<producto> lista_productos_ordenada;
    ArrayList<String> referencias_sensores = new ArrayList<String>();
    ArrayList<String> referencias_tickets = new ArrayList<String>();
    ArrayList<String> seekers;
    int dinero = 0;
    ArrayList<String> billetes = new ArrayList<String>();
    /**
    * Variables para el controlador
    * DBAMap mapa = new DBAMap();
        mapa.fromJson(map);
    **/
    ArrayList<posicion> Trayectoria_BasePlayground1_seek1 = new ArrayList<posicion>();
    ArrayList<posicion> Trayectoria_BasePlayground1_seek2 = new ArrayList<posicion>();
    ArrayList<posicion> Trayectoria_BasePlayground1_seek3 = new ArrayList<posicion>();
    
    ACLMessage in = new ACLMessage();
    ACLMessage out = new ACLMessage();
    YellowPages yp;
    
    void inicializar_trayectorias(){
        //BASEPLAYGROUNd1:
        
        for(int i=15; i<3; i+=30){
            posicion aux = new posicion(15,i);
            Trayectoria_BasePlayground1_seek1.add(aux);
        }
        
        for(int i=15; i<3; i+=30){
            posicion aux = new posicion(45,i);
            Trayectoria_BasePlayground1_seek2.add(aux);
        }
        for(int i=15; i<3; i+=30){
            posicion aux = new posicion(75,i);
            Trayectoria_BasePlayground1_seek3.add(aux);
        }
    }
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

        //ConversationID = in.getConversationId();
        desparsearConvID(in);
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
                desparsearMonedas(in);
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
                desparsearProductos(in);
                //guardar lista de objetos            
                cont++;
            }
           //PEPE
        }while(cont<Tiendas.size());
        Info("Obtuve los productos");
        Info("Tenemos estas monedas: "+billetes.size());

        Info("NUMERO DE PRODUCTOS: " + lista_productos.size());
        
        
        lista_productos_ordenada = ordenar_productos(lista_productos);
        for(int i = 0; i < lista_productos.size(); i++){
            
            Info(lista_productos.get(i).getReferencia());
            System.out.print(lista_productos.get(i).getPrecio()+"\n");

        }
        seleccionar_productos(lista_productos_ordenada); //AHORA TENEMOS EN LISTA_COMPRA LOS SENSORES A COMPRAR FALTAN TICKETS.
        
        //COMPRAR SENSORES
        for(int i=0; i<lista_compra.size(); i++){
            out = new ACLMessage();
            out.setSender(getAID());
            //out.addReceiver(new AID(lista_compra.get(i).getTienda(),AID.ISLOCALNAME));
            out.addReceiver(lista_compra.get(i).getTienda());
            out.setProtocol("REGULAR");
            JsonArray pago = new JsonArray();
            for(int j=0; j<lista_compra.get(i).getPrecio(); j++){
                pago.add(billetes.get(j));
                billetes.remove(j);
            }
            JsonObject objeto = new JsonObject();
            objeto.add("operation", "buy");
            objeto.add("reference", lista_compra.get(i).getReferencia());
            objeto.add("payment", pago);
            out.setContent(objeto.toString());
            
            out.setEncoding("");
            out.setConversationId(ConversationID);
            out.setPerformative(ACLMessage.REQUEST);
            this.send(out);
            Info(out.getContent());
            in = this.blockingReceive();
            Info(in.getContent());
            if(in.getPerformative() != ACLMessage.INFORM){
                //Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to "+ getDetailsLarva(in));
                abortSession();
            }else{
                Info(in.getContent());
                
                //DIVIDIR ENTRE SENSORES Y TICKETS DE RECARGA.
                String referencia = desparsearReferencia(in);
                partes = referencia.split("#");
                String partes[];
                if(partes[0].equals("CHARGE")){
                    referencias_tickets.add(referencia);
                }else{
                    referencias_sensores.add(referencia);
                }
                
                
            }
            
        }
        seekers = new ArrayList<String>();
        seekers.add("seek1");
        seekers.add("seek2");
        seekers.add("seek3");
        int cont1, cont2;
        for(int j=0; j< seekers.size(); j++){
            cont1=0;
            cont2=0;
            for(int i=0; i<referencias_sensores.size(); i++){
                String partes[];
                partes = referencias_sensores.get(i).split("#");
                if(partes[0].equals("thermal") && cont1 < 1){
                    out = new ACLMessage();
                    out.setSender(getAID());
                    out.addReceiver(new AID(seekers.get(j),AID.ISLOCALNAME));  
                    out.setProtocol("");
                    out.setContent(referencias_sensores.get(i)); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
                    out.setEncoding("");
                    out.setPerformative(ACLMessage.INFORM);
                    cont1++;
                    referencias_sensores.remove(i);
                    this.send(out);
                }else if(partes[0].equals("energy") && cont2 <1){                    
                    out = new ACLMessage();
                    out.setSender(getAID());
                    out.addReceiver(new AID(seekers.get(j),AID.ISLOCALNAME));  
                    out.setProtocol("");
                    out.setContent(referencias_sensores.get(i)); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
                    out.setEncoding("");
                    out.setPerformative(ACLMessage.INFORM);
                    cont2++;
                    referencias_sensores.remove(i);
                }
                
            }
            //AQUI HABRIA QUE CAMBIAR LA ASIGNACION DE TRAYECTORIAS AL CAMBIAR DE MUNDO
            ArrayList<posicion> aux = new ArrayList<posicion>();
            if(seekers.get(j)=="seek1"){
                aux=Trayectoria_BasePlayground1_seek1;
            }else if(seekers.get(j)=="seek2"){
                aux=Trayectoria_BasePlayground1_seek2;
            }else if(seekers.get(j)=="seek3"){
                aux=Trayectoria_BasePlayground1_seek3;
            }
            out = new ACLMessage();
            out.setSender(getAID());
            out.addReceiver(new AID(seekers.get(j),AID.ISLOCALNAME));  
            out.setProtocol("");
            out.setContent(aux.toString()); //Aqui se pone {"problem":"id-problema"} pero no se como se pone bien
            out.setEncoding("");
            out.setPerformative(ACLMessage.INFORM);
            this.send(out);
        }
        
        Info("EL numero de tickets de recarga es: " + referencias_tickets.size());
        Info("EL numero de sensores es: " +referencias_sensores.size());
        
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
    
    
    void seleccionar_productos(ArrayList<producto> lista_ordenada){ //devuelvo un array con rescuer un gps y energy, seeker thermal y energy; 4 energy, 3 thermal y 1 gps;
        //lista_compra
        dinero = billetes.size();
        String[] partes;
        int gps = 0, energy = 0, thermal = 0;
        boolean lotenemos = false;
        for (int i = 0; i < lista_ordenada.size() && lotenemos != true; i++){
            partes = lista_ordenada.get(i).getReferencia().split("#");
            
            if(partes[0].equals("GPS") && gps != 1){
                lista_compra.add(lista_ordenada.get(i));
                gps++;
                dinero = dinero - lista_ordenada.get(i).getPrecio();
            }else if(partes[0].equals("ENERGY") && energy != 4){
                lista_compra.add(lista_ordenada.get(i));
                energy++;
                dinero = dinero - lista_ordenada.get(i).getPrecio();
            }else if(partes[0].equals("THERMALDELUX") && thermal != 3){
                lista_compra.add(lista_ordenada.get(i));
                thermal++;
                dinero = dinero - lista_ordenada.get(i).getPrecio();
            }
            
            if(gps == 1 && energy ==4 && thermal == 3)
                lotenemos = true;
        }
        
        for (int i = 0; i < lista_ordenada.size() && dinero > 0; i++){
            partes = lista_ordenada.get(i).getReferencia().split("#");
            
            if(partes[0].equals("CHARGE") && dinero > lista_ordenada.get(i).getPrecio()){
                lista_compra.add(lista_ordenada.get(i));
                dinero = dinero - lista_ordenada.get(i).getPrecio();
            }
        }
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
    
    public void desparsearMonedas(ACLMessage in){
        String answer = in.getContent();
        JsonObject objeto = new JsonObject();
        String variable;
        objeto = Json.parse(answer).asObject();
        String resultado = objeto.get("result").asString();
        if("ok".equals(resultado)){
            for(JsonValue j : objeto.get("coins").asArray()){
                variable = j.asString();
                billetes.add(variable);
            }
        }
    }
    
    public void desparsearProductos(ACLMessage in){
        String answer = in.getContent();
        JsonObject objeto = new JsonObject();
        producto p; 
        int precio;
        int serie;
        String referencia;
        AID tienda = in.getSender();
        objeto = Json.parse(answer).asObject();
        JsonArray vector = objeto.get("products").asArray();
        for(JsonValue j : vector){
            precio = 0;
            serie = 0;
            referencia = "";
            p = new producto();
            referencia = j.asObject().get("reference").asString();
            serie = j.asObject().get("serie").asInt();
            precio = j.asObject().get("price").asInt();
            p.setPrecio(precio);
            p.setReferencia(referencia);
            p.setSerie(serie);
            p.setTienda(tienda);
            lista_productos.add(p);
        }
    }
    
    public void desparsearConvID(ACLMessage in){
        String answer = in.getContent();
        JsonObject objeto = new JsonObject();
        objeto = Json.parse(answer).asObject();
        String resultado = objeto.get("result").asString();
        if("ok".equals(resultado)){
            ConversationID = in.getConversationId();
        }
    }
    
    public String desparsearReferencia(ACLMessage in){
        String reference="";
        String answer = in.getContent();
        JsonObject objeto = new JsonObject();
        objeto = Json.parse(answer).asObject();
        String resultado = objeto.get("result").asString();
        if("ok".equals(resultado)){
            reference = objeto.get("reference").asString();
        }
        return reference;
    }

}    