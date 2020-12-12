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
     
    
     public void setX(int x) {
        this.x = x;
    }
     
    public int getX() {
        return x;
    }

    

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public double getOrientacion() {
        return orientacion;
    }

    public void setOrientacion(double orientacion) {
        this.orientacion = orientacion;
    }
    
    Estado(int x, int y, int z, double orientacion){
        this.x=x;
        this.y=y;
        this.z=z;
        this.orientacion=orientacion;
    }
}
