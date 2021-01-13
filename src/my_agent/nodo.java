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
 * @author Samuel, Adrián y Rafael
 */
public class nodo implements Comparable<nodo> {
    
    public Estado st;
    public ArrayList<String> acciones;
    public double distancia; 
    
    /**
     * @author Samuel, Adrián y Rafael
     * Constructor de la clase nodo.
     * @param estado
     */
    public nodo(Estado estado) {
        acciones = new ArrayList<String>();
        st = new Estado(estado.getX(), estado.getY(), estado.getZ(), estado.getOrientacion());
    }
    
    /**
     * @author Samuel, Adrián y Rafael
     * Getter del atributo st.
     * @return st
     */
    public Estado getSt() {
        return st;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Getter del atributo distancia.
     * @return distancia
     */
    public double getDistancia() {
        return distancia;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Setter del atributo distancia.
     * @param distancia
     */
    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }
    
    /**
     * @author Samuel, Adrián y Rafael
     * Función que añade valor a un nodo.
     * Se utiliza para obtener una heurística en el algoritmo Greedy.
     * @param valor
     */
    public void añadirValor(double valor){
        this.distancia+=valor;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Setter del atributo st.
     * @param st
     */
    public void setSt(Estado st) {
        this.st.setX(st.getX());
        this.st.setY(st.getY());
        this.st.setZ(st.getZ());
        this.st.setOrientacion(st.getOrientacion());
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Getter del atributo acciones.
     * @return acciones
     */
    public ArrayList<String> getAcciones() {
        return acciones;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Setter del atributo acciones.
     * @param acciones
     */
    public void setAcciones(ArrayList<String> acciones) {
        if(acciones.size()>0)
            for(int i=0; i<acciones.size(); i++){
                this.acciones.add(acciones.get(i));
            }
    }
    
    /**
     * @author Samuel, Adrián y Rafael
     * Función que se encarga de calcular la distancia entre dos puntos.
     * @param origen
     * @param destino
     * @return distancia
     */
    public double distancia(Estado origen, Estado destino){
        double distancia;
        distancia =  sqrt(pow((destino.x-origen.x),2)+pow((destino.y-origen.y),2)+pow((destino.z-origen.z),2));
        return distancia;
    };

    /**
     * @author Samuel, Adrián y Rafael
     * Función que modifica el comportamiento de la función compareTo para 
     * adaptarla a las necesidades de nuestro algoritmo Greedy.
     * Devuelve un entero para indicar el resultado de la comparación.
     * @param o
     * @return int 
     */
    @Override
    public int compareTo(nodo o) {
        if (distancia < o.getDistancia()) {
            return -1;
        } else if(distancia > o.getDistancia()) {
            return 1;
        } else {
            return 0;
        }
    }
}