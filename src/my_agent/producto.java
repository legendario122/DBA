/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my_agent;

import jade.core.AID;

/**
 *
 * @author adrian
 */
public class producto {
    int serie;
    int precio;
    String referencia;
    AID Tienda;

    public AID getTienda() {
        return Tienda;
    }

    public void setTienda(AID Tienda) {
        this.Tienda = Tienda;
    }
    
    public int getSerie() {
        return serie;
    }

    public void setSerie(int serie) {
        this.serie = serie;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
    
}
