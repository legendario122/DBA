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
        _app.launchAgent("AWACS", awacs.Awacs.class);
        _app.launchAgent("controlador2_bbva", Controlador.class);
        _app.launchAgent("buscatrufas_1", Seeker.class);
        _app.launchAgent("buscatrufas_2", Seeker.class);
        _app.launchAgent("buscatrufas_3", Seeker.class);
        _app.launchAgent("spidercerdo", Rescuer.class);
        _app.launchAgent("greedy", Greedy.class);
        _app.shutDown();
    }

    
}
