/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my_agent;

import jade.core.AID;

/**
 *
 * @author Samuel, Adrián y Rafael
 */
public class producto {
    int serie;
    int precio;
    String referencia;
    AID Tienda;
    
    /**
     * @author Samuel, Adrián y Rafael
     * Getter del atributo Tienda.
     * @return Tienda
     */
    public AID getTienda() {
        return Tienda;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Setter del atributo Tienda.
     * @param Tienda
     */
    public void setTienda(AID Tienda) {
        this.Tienda = Tienda;
    }
    
    /**
     * @author Samuel, Adrián y Rafael
     * Getter del atributo serie.
     * @return serie
     */
    public int getSerie() {
        return serie;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Setter del atributo serie.
     * @param serie
     */
    public void setSerie(int serie) {
        this.serie = serie;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Getter del atributo precio.
     * @return precio
     */
    public int getPrecio() {
        return precio;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Setter del atributo precio.
     * @param precio
     */
    public void setPrecio(int precio) {
        this.precio = precio;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Getter del atributo referencia.
     * @return referencia
     */
    public String getReferencia() {
        return referencia;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Setter del atributo referencia.
     * @param referencia
     */
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }  
}