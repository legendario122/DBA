package my_agent;


import AppBoot.ConsoleBoot;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import my_agent.Estado;

public class Pract2{
    
    
    
    public static void main(String[] args) {
        /**
        ConsoleBoot _app = new ConsoleBoot("Pract2",args);
        _app.selectConnection();
        _app.launchAgent("BB99VA", MyWorldExplorer.class);
        _app.shutDown();
        * **/
        
        Estado origen= new Estado(20,2,2, 90);
        Estado destino = new Estado(73,7,20,-1);
        int resultado;
        ArrayList<String> acciones = new ArrayList<String>(); 
        Greedy ejemplo = new Greedy();
        acciones = ejemplo.plainExecute(origen, destino, acciones);
        
        resultado = distancia(origen,destino);
        
        System.out.println(resultado);
        
        
    }

    
}
