/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my_agent;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;

/**
 *
 * @author adrian
 */
public class nodo implements Comparable<nodo> {
    
    public Estado st;
    public ArrayList<String> acciones;
    public double distancia; 
    
    public nodo(Estado estado) {
        acciones = new ArrayList<String>();
        st = new Estado(estado.getX(), estado.getY(), estado.getZ(), estado.getOrientacion());
    }
    

    public Estado getSt() {
        return st;
    }

    public double getDistancia() {
        return distancia;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public void setSt(Estado st) {
        
        this.st.setX(st.getX());
        this.st.setY(st.getY());
        this.st.setZ(st.getZ());
        this.st.setOrientacion(st.getOrientacion());
    }

    public ArrayList<String> getAcciones() {
        return acciones;
    }

    public void setAcciones(ArrayList<String> acciones) {
        if(acciones.size()>0)
            for(int i=0; i<acciones.size(); i++){
                this.acciones.add(acciones.get(i));
            }
       
    }

    

    public double distancia(Estado origen, Estado destino){
        double distancia;
        
        distancia = (int) sqrt(pow((destino.x-origen.x),2)+pow((destino.y-origen.y),2)+pow((destino.z-origen.z),2));
        
        return distancia;
    };

    @Override
    public int compareTo(nodo o) {
        if (distancia < o.getDistancia()) {
            return 1;
        } else if(distancia > o.getDistancia()) {
            return -1;
        } else {
            return 0;
        }
    }
    
}
