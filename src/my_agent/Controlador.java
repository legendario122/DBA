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
     *
     * @author Samuel, Adrián y Rafael
     */  
    TTYControlPanel myControlPanel;
    int width; 
    int height;
    int maxflight = 256;
    static String ConversationID = "";
    Map2DGrayscale myMap;
    
    ArrayList<producto> lista_productos = new ArrayList<producto>();
    ArrayList<producto> lista_compra = new ArrayList<producto>();
    ArrayList<producto> lista_productos_ordenada;
    ArrayList<String> billetes = new ArrayList<String>();
    ArrayList<String> seekers;
    ArrayList<String> referencias_tickets = new ArrayList<String>();
    ArrayList<String> referencias_sensores = new ArrayList<String>();
    ArrayList<posicion> alemanes = new ArrayList<posicion>(); 
    int dinero = 0;

    static ArrayList<posicion> Trayectoria_seek1 = new ArrayList<posicion>();
    static ArrayList<posicion> Trayectoria_seek2 = new ArrayList<posicion>();
    static ArrayList<posicion> Trayectoria_seek3 = new ArrayList<posicion>();
    
    ACLMessage in = new ACLMessage();
    ACLMessage out = new ACLMessage();
    YellowPages yp;
    
    /**
     * @author Samuel, Adrián y Rafael
     * Devuelve la trayectoria de uno de los drones seeker.
     * @return ArrayList<posicion>
     */
    public static ArrayList<posicion> get_trayectoria(String tam, String nombre){
        if(tam.equals("100")){
            if(nombre.equals("buscatrufas_1")){
                return Trayectoria_seek1;
            }else if(nombre.equals("buscatrufas_2")){
                return Trayectoria_seek2;
            }else if(nombre.equals("buscatrufas_3")){
                return Trayectoria_seek3;
            }
        }
        return null;
    }
    
   /**
     * @author Samuel, Adrián y Rafael
     * Inicializa la primera posicion de los drones seeker para ver desde donde se empiezan a mover.
     */
    void inicializar_trayectorias(){
        
        for(int i=15; i<100; i+=30){
            posicion aux = new posicion(15,i,255,90);
            Trayectoria_seek1.add(aux);
        }
        Trayectoria_seek1.get(0).setZ(0);
        posicion aux = new posicion(15,85,255,90);
        Trayectoria_seek1.add(aux);
        for(int i=15; i<100; i+=30){
             aux = new posicion(45,i,255,90);
            Trayectoria_seek2.add(aux);
        }
        Trayectoria_seek2.get(0).setZ(0);
        aux = new posicion(45,85,255,90);
        Trayectoria_seek2.add(aux);
        for(int i=15; i<100; i+=30){
             aux = new posicion(75,i,255,90);
            Trayectoria_seek3.add(aux);
        }
        Trayectoria_seek3.get(0).setZ(0);
        aux = new posicion(75,85,255,90);
        Trayectoria_seek3.add(aux);
        for(int i=85; i>0; i-=30){
             aux = new posicion(85,i,255,90);
            Trayectoria_seek3.add(aux);
        }
        aux = new posicion(85,15,255,90);
        Trayectoria_seek3.add(aux);
    }
    @Override
    /**
     * @author Samuel, Adrián y Rafael
     * Funcion que se encarga de hacer el checkin en Sphinx y en nuestro agente BBVA. Obtiene también las
     * páginas amarillas. Se encarga de desparsear el mapa y enviarselo al agente que nos muestra el mapa
     * en una ventana aparte.
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
            abortSession();
        }      
        Info("Checkeo realizado");
        inicializar_trayectorias();

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
            abortSession();
        }  

        yp = new YellowPages();
        yp.updateYellowPages(in);
        
        ArrayList<String> pepe = new ArrayList(yp.queryProvidersofService("Analytics groupid 13"));
            
        Info("Haciendo checkin to" + "BBVA"); 
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID(pepe.get(0),AID.ISLOCALNAME)); 
        out.setProtocol("ANALYTICS");
        out.setContent(new JsonObject().add("problem", "World5").toString()); 
        out.setEncoding("");
        out.setPerformative(ACLMessage.SUBSCRIBE);
        this.send(out);
        in = this.blockingReceive();
        
        if(in.getPerformative() != ACLMessage.INFORM){
            Error(ACLMessage.getPerformative(in.getPerformative()) + " Could not"+" confirm the registration in LARVA due to ");
            Info(in.getContent());
            abortSession();
        }      
        
        System("Save map of world ");
        JsonObject jscontent = getJsonContentACLM(in);
        if (jscontent.names().contains("map")) {
	        JsonObject jsonMapFile = jscontent.get("map").asObject();
	        String mapfilename = jsonMapFile.getString("filename", "nonamefound");
            Info("Found map " + mapfilename);            
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

        desparsearConvID(in);
        Info(ConversationID);

        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("AWACS",AID.ISLOCALNAME));
        out.setProtocol("");
        out.setContent("");
        out.setConversationId(ConversationID);
        out.setEncoding("");
        out.setPerformative(ACLMessage.QUERY_IF);
        this.send(out);
        
    }

    @Override
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Es el comportamiento principal del Agente controlador. Primero le manda los convesationID a los drones seeker
     * y rescuer para que se puedan comunicar con el World Manager, seguidamente espera a que los drones les manden
     * las monedas que les corresponde a cada uno. Obtiene las páginas amarillas y los objetos que hay en la tienda.
     * Compra los sensores que necesitamos y se los manda a cada dron. Y tiene un while donde mientras no reciba 4 adios
     * se encarga de esperar para ver si la performativa es INFORM o REQUEST, si es INFORM puede ser para que un dron se 
     * desconecte o que un seeker ha encontrado un aleman y se lo envia al rescuer. Si es REQUEST es que algun dron esta 
     * pidiendo un tiquet de recarga. Una vez que este bucle ha terminado significa que ya han acabado todos los drones 
     * seeker y rescuer y le manda un mensaje a greedy para que se desconecte.
     */
    public void plainExecute() {

        Info("Haciendo Query-if a Drones"); 
        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("buscatrufas_1",AID.ISLOCALNAME));  
        out.setProtocol("");
        out.setContent(new JsonObject().add("ConversationID", ConversationID).toString()); 
        out.setEncoding("");
        out.setPerformative(ACLMessage.REQUEST);
        this.send(out);

        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("buscatrufas_2",AID.ISLOCALNAME));  
        out.setProtocol("");
        out.setContent(new JsonObject().add("ConversationID", ConversationID).toString()); 
        out.setEncoding("");
        out.setPerformative(ACLMessage.REQUEST);
        this.send(out);

        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("buscatrufas_3",AID.ISLOCALNAME));  
        out.setProtocol("");
        out.setContent(new JsonObject().add("ConversationID", ConversationID).toString()); 
        out.setEncoding("");
        out.setPerformative(ACLMessage.REQUEST);
        this.send(out);

        out = new ACLMessage();
        out.setSender(getAID());
        out.addReceiver(new AID("spidercerdo",AID.ISLOCALNAME));  
        out.setProtocol("");
        out.setContent(new JsonObject().add("ConversationID", ConversationID).toString()); 
        out.setEncoding("");
        out.setPerformative(ACLMessage.REQUEST);
        this.send(out);

        int cont=0;

        do{
            in = this.blockingReceive();
            if(in.getPerformative() != ACLMessage.INFORM){
               abortSession();
            }else{
                Info(in.getContent());
                desparsearMonedas(in);
                cont++;
            }
            
        }while(cont<4);
        
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
            abortSession();
        }  

        yp = new YellowPages();
        yp.updateYellowPages(in);
        
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
                abortSession();
            }else{
                Info(in.getContent());
                desparsearProductos(in);          
                cont++;
            }
        }while(cont<Tiendas.size());
        Info("Obtuve los productos");  
        
        lista_productos_ordenada = ordenar_productos(lista_productos);

        seleccionar_productos(lista_productos_ordenada); 
        
        //COMPRAR SENSORES
        for(int i=0; i<lista_compra.size(); i++){
            out = new ACLMessage();
            out.setSender(getAID());
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
            in = this.blockingReceive();
            if(in.getPerformative() != ACLMessage.INFORM){
                abortSession();
            }else{                
                String referencia = desparsearReferencia(in);
                String partes[];
                partes = referencia.split("#");
                
                if(partes[0].equals("CHARGE")){
                    referencias_tickets.add(referencia);
                }else{
                    referencias_sensores.add(referencia);
                }
                
                
            }
            
        }
        seekers = new ArrayList<String>();
        seekers.add("buscatrufas_1");
        seekers.add("buscatrufas_2");
        seekers.add("buscatrufas_3");
        int cont1, cont2;
        for(int j=0; j< seekers.size(); j++){
            cont1=0;
            cont2=0;
            for(int i=0; i<referencias_sensores.size(); i++){
                String partes[];
                partes = referencias_sensores.get(i).split("#");
                if(partes[0].equals("THERMALDELUX") && cont1 < 1){
                    out = new ACLMessage();
                    out.setSender(getAID());
                    out.addReceiver(new AID(seekers.get(j),AID.ISLOCALNAME));  
                    out.setProtocol("");
                    out.setContent(referencias_sensores.get(i)); 
                    out.setEncoding("");
                    out.setPerformative(ACLMessage.INFORM);
                    cont1++;
                    referencias_sensores.remove(i);
                    this.send(out);
                }else if(partes[0].equals("ENERGY") && cont2 <1){                    
                    out = new ACLMessage();
                    out.setSender(getAID());
                    out.addReceiver(new AID(seekers.get(j),AID.ISLOCALNAME));  
                    out.setProtocol("");
                    out.setContent(referencias_sensores.get(i)); 
                    out.setEncoding("");
                    out.setPerformative(ACLMessage.INFORM);
                    this.send(out);
                    cont2++;
                    referencias_sensores.remove(i);
                }
                
            }
            
            ArrayList<posicion> aux = new ArrayList<posicion>();
            
                out = new ACLMessage();
                out.setSender(getAID());
                out.addReceiver(new AID(seekers.get(j),AID.ISLOCALNAME));  
                out.setProtocol("");
                out.setContent("100"); 
                Info(out.getContent());
                out.setEncoding("");
                out.setPerformative(ACLMessage.INFORM);
                this.send(out);
            
            
        }
        
    String[] partes;
    int gps = 0, energy = 0;  
    boolean lotenemos = false;
    int tam = referencias_sensores.size();
    for(int i = 0; i < tam && lotenemos != true; i++){  
        partes = referencias_sensores.get(i).split("#");
        if(partes[0].equals("GPS") && gps != 1){
        
            out = new ACLMessage();
            out.setSender(getAID());
            out.addReceiver(new AID("spidercerdo",AID.ISLOCALNAME));    
            out.setProtocol("");
            out.setContent(referencias_sensores.get(i));
            out.setEncoding("");
            out.setPerformative(ACLMessage.INFORM);
            this.send(out);
            
            gps++;
        }else if (partes[0].equals("ENERGY") && energy != 1){
            out = new ACLMessage();
            out.setSender(getAID());
            out.addReceiver(new AID("spidercerdo",AID.ISLOCALNAME));    
            out.setProtocol("");
            out.setContent(referencias_sensores.get(i)); 
            out.setEncoding("");
            out.setPerformative(ACLMessage.INFORM);
            this.send(out);
            
            energy++;
        }
        
                    
        if(gps == 1 && energy == 1)
            lotenemos = true;
    }

    int count=0;
    int especial=0;
    while(count<4){
        in = this.blockingReceive();
        if(in.getPerformative() == ACLMessage.INFORM){
            String mensaje = in.getContent();
            if("Adios".equals(mensaje)){
                count++;
            }else{
                
                if(NoEsta(desparsearPosicion(in)) && especial<1){
                    alemanes.add(desparsearPosicion(in));
                    System.out.println(alemanes.get((alemanes.size()-1)).getX()+ "  "+ alemanes.get((alemanes.size()-1)).getY());
                    out = new ACLMessage();
                    out.setSender(getAID());
                    out.addReceiver(new AID("spidercerdo",AID.ISLOCALNAME));    
                    out.setProtocol("");
                    out.setContent(in.getContent());
                    out.setEncoding("");
                    out.setPerformative(ACLMessage.REQUEST);
                    this.send(out);
                    especial++;
                }
                
            }
        }else if(in.getPerformative() == ACLMessage.REQUEST && "ticketRecarga".equals(in.getContent())){

            out = in.createReply();
            if(!referencias_tickets.isEmpty()){
                String ticket = referencias_tickets.get(0);
                out.setContent(ticket);
                out.setPerformative(ACLMessage.INFORM);
                this.send(out);
                referencias_tickets.remove(0);
            }else{
                out.setContent("Vacio");
                out.setPerformative(ACLMessage.INFORM);
                this.send(out);
            }
        }else{
            Info(in.getContent());
        }
    }
    
    //DECIR ADIOS AL AGENTE GREEDY
    //una vez han terminado y se han despedido le resto de drones
    out = new ACLMessage();
    out.setSender(getAID());
    out.addReceiver(new AID("greedy",AID.ISLOCALNAME));    
    out.setProtocol("");
    out.setContent("Fin");
    out.setEncoding("");
    out.setPerformative(ACLMessage.INFORM);
    this.send(out);
    }
    
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que comprueba si el aleman que le ha pasado un seeker por mensaje está en el vector de alemanes 
     * encontrados.
     * @return true si no está y false si ya está.
     */    
    boolean NoEsta(posicion aux){
        boolean no_esta=true;
        
        for(int i=0; i<alemanes.size(); i++){
            if(alemanes.get(i).getX()==aux.getX() && alemanes.get(i).getY()==aux.getY()){
                no_esta=false;
            }
        }
        
        return no_esta;
    }
    
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Función que se encarga de añadir a la lista de la compra los sensores y tiquets de recarga que necesitamos.
     */      
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
    
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Función que devuelve un arrayList de productos con los mismos ordenados por el precio de menos¡r a mayor
     * @return Un ArrayList de productos.
    */    
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
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que se encarga de desparsear una posición y lo devuelve en una posición.
     * @return posicion.
    */    
    public posicion desparsearPosicion(ACLMessage in){
        
        String answer = in.getContent();
        JsonObject objeto = new JsonObject();
        objeto = Json.parse(answer).asObject();
        int x = objeto.get("x").asInt();
        int y = objeto.get("y").asInt();
        int z = objeto.get("z").asInt();
        int orientacion = objeto.get("orientacion").asInt();
        
        posicion pos = new posicion(x,y,z,orientacion);
        
        
        return pos;
    }
  
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que se encarga de hacer el logout de Sphinx y BBVA
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
        out.addReceiver(new AID("Sphinx", AID.ISLOCALNAME));
        out.setProtocol("ANALYTICS");
        out.setContent("");
        out.setConversationId(ConversationID);
        out.setPerformative(ACLMessage.CANCEL);
        this.send(out);
        in = this.blockingReceive();

    }
    
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que se encarga de desparsear los mensajes de monedas para añadirlas al array que tenemos con todas las monedas.
    */     
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
    
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que se encarga de desparsear productos de la tienda y los añade a nuestra lista de productos.
    */ 
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
    
        /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que se encarga de desparsear la conversationID de un mensaje y lo guarda en nuestra variable global
     * para que el resto de drones la usen para comunicarse con World Manager.
    */ 
    public void desparsearConvID(ACLMessage in){
        String answer = in.getContent();
        JsonObject objeto = new JsonObject();
        objeto = Json.parse(answer).asObject();
        String resultado = objeto.get("result").asString();
        if("ok".equals(resultado)){
            ConversationID = in.getConversationId();
        }
    }
    /**
     * @author Adrian
     * @author Rafael
     * @author Samuel
     * Funcion que se encarga de desparsear un mensaje con una referencia.
     * @return String.
    */     
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