/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package repo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import model.Korisnik;

/**
 *
 * @author Aleksandar Milicevic
 */
public class RepozitorijumKorisnika {
    
    private static RepozitorijumKorisnika instance;
    private static final String FILE = "korisnici.dat";
    private List<Korisnik> korisnici;

    private RepozitorijumKorisnika() {
        korisnici = load();
    }
    
    public static RepozitorijumKorisnika getInstance() {
        if(instance == null) {
            instance = new RepozitorijumKorisnika();
        }
        return instance;
    }
    
    public boolean dodajKorisnika(Korisnik k) {
        if(postojiUsername(k.getUsername())) return false;
        korisnici.add(k);
        save();
        return true;
    }
    
    public Korisnik pronadjiPoKorisnickovomImenu(String username, String password) {
        for(Korisnik k : korisnici) {
            if(k.getUsername().equals(username) && k.getPassword().equals(password)) return k;
        }
        return null;
    }
    
    private void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE))) {
            out.writeObject(korisnici);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Korisnik> load() {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE))) {
            return (List<Korisnik>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }
    
    public boolean postojiUsername(String username) {
        for (Korisnik k : korisnici) {
            if(k.getUsername().equals(username)) return true;
        }
        return false;
    }
    
}
