/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my_agent;

/**
 *
 * @author Samuel, Adrián y Rafael
 */
public class posicion {
    int x;
    int y;
    int z;
    public double orientacion;

    /**
     * @author Samuel, Adrián y Rafael
     * Constructor de la clase posicion.
     * @param x
     * @param y
     * @param z
     * @param orientacion
     */    
    public posicion(int x, int y, int z, double orientacion) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.orientacion = orientacion;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Getter del atributo z.
     * @return z
     */    
    public int getZ() {
        return z;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Setter del atributo z.
     * @param z
     */    
    public void setZ(int z) {
        this.z = z;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Getter del atributo orientacion.
     * @return orientacion
     */     
    public double getOrientacion() {
        return orientacion;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Setter del atributo orientacion.
     * @param orientacion
     */    
    public void setOrientacion(double orientacion) {
        this.orientacion = orientacion;
    }
    
    /**
     * @author Samuel, Adrián y Rafael
     * Getter del atributo x.
     * @return x
     */ 
    public int getX() {
        return x;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Setter del atributo x.
     * @param x
     */    
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Getter del atributo y.
     * @return y
     */     
    public int getY() {
        return y;
    }

    /**
     * @author Samuel, Adrián y Rafael
     * Setter del atributo y.
     * @param y
     */    
    public void setY(int y) {
        this.y = y;
    }
    
}
