/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my_agent;

import java.util.ArrayList;

/**
 *
 * @author adrian
 */
public class nodo implements Comparable<nodo> {
    Estado st;
    ArrayList<String> acciones;
    double distancia; 

    public Estado getSt() {
        return st;
    }

    public Estado getDistancia() {
        return distancia;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public void setSt(Estado st) {
        this.st = st;
    }

    public ArrayList<String> getAcciones() {
        return acciones;
    }

    public void setAcciones(ArrayList<String> acciones) {
        this.acciones = acciones;
    }

    public nodo(Estado st) {
        this.acciones = new ArrayList<String>();
        this.st=st;
    }

    public int distancia(Estado origen, Estado destino){
        int distancia;
        
        distancia = (int) sqrt(pow((destino.x-origen.x),2)+pow((destino.y-origen.y),2)+pow((destino.z-origen.z),2));
        
        return distancia;
    };

    @Override
    public int compareTo(nodo o) {
        if (distancia < o.getDistancia()) {
            return 1;
        } else if ((distancia > o.getDistancia()) {
            return -1;
        } else {
            return 0;
        }
    }
    
}
