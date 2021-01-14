/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my_agent;
 
/** 
 *
 * @author adrian
 
 */
public class Estado {
     public int x, y, z;
     public double orientacion;

     
    /**
     * @author Adrián 
     * Setter del atributo x.
     * @param x
     */    
     public void setX(int x) {
        this.x = x;
    }
     
    /**
     * @author Adrián 
     * Getter del atributo x.
     * @return x
     */     
    public int getX() {
        return x;
    }

    
    /**
     * @author Adrián 
     * Getter del atributo y.
     * @return y
     */ 
    public int getY() {
        return y;
    }

    /**
     * @author Adrián 
     * Getter del atributo y.
     * @param y
     */      
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @author Adrián 
     * Getter del atributo z.
     * @return z
     */     
    public int getZ() {
        return z;
    }

    /**
     * @author Adrián 
     * Setter del atributo z.
     * @param z
     */      
    public void setZ(int z) {
        this.z = z;
    }

    /**
     * @author Adrián 
     * Getter de orientacion.
     * @return orientacion.
     */      
    public double getOrientacion() {
        return orientacion;
    }
    /**
     * @author Adrián 
     * Setter del atributo orientacion.
     * @param orientacion
     */  
    public void setOrientacion(double orientacion) {
        this.orientacion = orientacion;
    }
    
    /**
     * @author Samuel, Adrián y Rafa
     * Constructor con parametros.
     * @param x
     * @param y
     * @param z
     * @param orientacion
     */
    Estado(int x, int y, int z, double orientacion){
        this.x=x;
        this.y=y;
        this.z=z;
        this.orientacion=orientacion;
    }
}
