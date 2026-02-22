/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aleksandar Milicevic
 */
public class PokreniServer extends Thread {
    
    private static final int PORT = 9000;
    private boolean kraj = false;
    private ServerSocket serverSocket;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("SERVER JE POCEO SA RADOM");
            
            while(!kraj) {
                Socket socket = serverSocket.accept();
                System.out.println("KLIJENT JE POVEZAN: " + socket.getInetAddress());
                
                ObradaKlijentskihZahteva nit = new ObradaKlijentskihZahteva(socket);
                nit.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(PokreniServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void zaustaviServer() {
        System.out.println("SOKET JE ZATVOREN");
        kraj = true;
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(PokreniServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
