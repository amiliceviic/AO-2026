/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package komunikacija;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import transfer.KlijentskiZahtev;
import transfer.ServerskiOdgovor;

/**
 *
 * @author Aleksandar Milicevic
 */
public class Komunikacija {
    
    private static final String ADRESS = "localhost";
    private static final int PORT = 9000;
    
    private Socket socket;
    private static Komunikacija instance;
    
    private Komunikacija() {
        try {
            socket = new Socket(ADRESS, PORT);
        } catch (IOException ex) {
            Logger.getLogger(Komunikacija.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Komunikacija getInstance() {
        if(instance == null) {
            instance = new Komunikacija();
        }
        return instance;
    }
    
    public ServerskiOdgovor primiOdgovor() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            return (ServerskiOdgovor) in.readObject();
        }catch (SocketException ex) {
            System.out.println("Server je prekunio konekciju");
        }  catch (IOException ex) {
            Logger.getLogger(Komunikacija.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Komunikacija.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void posaljiZahtev(KlijentskiZahtev kz) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(kz);
            out.flush();
        } catch (SocketException ex) {
            System.out.println("Server je prekunio konekciju");
        } catch (IOException ex) {
            Logger.getLogger(Komunikacija.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
