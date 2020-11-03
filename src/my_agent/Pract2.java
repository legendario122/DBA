/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my_agent;

import AppBoot.ConsoleBoot;

public class Pract2{

    public static void main(String[] args) {
        ConsoleBoot _app = new ConsoleBoot("Pract2",args);
        _app.selectConnection();
        _app.launchAgent("BBVA127", MyWorldExplorer.class);
        _app.shutDown();
    }

    
}
