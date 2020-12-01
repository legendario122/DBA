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
public class nodo {
    Estado st;
    ArrayList<String> acciones;

    public Estado getSt() {
        return st;
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
    
}
