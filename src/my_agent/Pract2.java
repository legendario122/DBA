package my_agent;
 

import AppBoot.ConsoleBoot;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import my_agent.Estado;

public class Pract2{
    
    
    
    public static void main(String[] args) {
        
        ConsoleBoot _app = new ConsoleBoot("Pract2",args);
        _app.selectConnection();
        _app.launchAgent("Hitler", Controlador.class);
        _app.launchAgent("seeker1", Seeker.class);
        _app.launchAgent("seeker2", Seeker.class);
        _app.launchAgent("seeker3", Seeker.class);
        _app.launchAgent("rescuer", Rescuer.class);
        _app.shutDown();
        
        /**
        Estado origen= new Estado(0,0,210, 90);
        Estado destino = new Estado(5,6,220,90);
        int resultado;
        ArrayList<String> acciones = new ArrayList<String>(); 
        Greedy ejemplo = new Greedy();
        acciones = ejemplo.plainExecute(origen, destino, acciones);
        
        
        for(int i=0; i< acciones.size(); i++)
           System.out.println(acciones.get(i));
        
        * **/
    }

    
}
