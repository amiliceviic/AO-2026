/*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package repo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Pozicija;
import model.PrijavaVolontiranja;

/**
 *
 * @author Aleksandar Milicevic
 */
public class RepozitorijumPrijava {

    private static RepozitorijumPrijava instance;
    private static final String FILE = "prijave.dat";
    private List<PrijavaVolontiranja> prijave;

    private RepozitorijumPrijava() {
        List<PrijavaVolontiranja> ucitanePrijave = load();
        if (ucitanePrijave == null) {
            ucitanePrijave = new ArrayList<>();
        }
        prijave = Collections.synchronizedList(ucitanePrijave);
    }

    public static RepozitorijumPrijava getInstance() {
        if (instance == null) {
            instance = new RepozitorijumPrijava();
        }
        return instance;
    }

    public synchronized void dodajPrijavu(PrijavaVolontiranja p) {
        if (prijave == null) {
            prijave = Collections.synchronizedList(new ArrayList<>());
        }
        prijave.add(p);
        save();
    }

    public List<PrijavaVolontiranja> getPrijave() {
        return prijave;
    }

    private synchronized void save() {
        try ( ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE))) {
            out.writeObject(prijave);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<PrijavaVolontiranja> load() {
        File f = new File(FILE);
        if (!f.exists()) {
            return new ArrayList<>();
        }
        try ( ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            return (List<PrijavaVolontiranja>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean korisnikVecPrijavljenZaDatum(String jmbg, LocalDate datum) {
        if (prijave == null || prijave.isEmpty()) {
            return false;
        }

        synchronized (prijave) {
            for (PrijavaVolontiranja p : prijave) {
                if (p.getJmbg().equals(jmbg) && p.getDatumVolontiranja().equals(datum)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<Pozicija> getRazlicitePozicijeKorisnika(String jmbg) {
        Set<Pozicija> pozicije = new HashSet<>();

        if (prijave == null) {
            return pozicije;
        }

        synchronized (prijave) {
            for (PrijavaVolontiranja p : prijave) {
                if (p.getJmbg().equals(jmbg)) {
                    pozicije.add(p.getPozicija());
                }
            }
        }
        return pozicije;
    }

    public synchronized boolean obrisi(PrijavaVolontiranja p) {
        for (int i = 0; i < prijave.size(); i++) {
            PrijavaVolontiranja prijava = prijave.get(i);

            if (prijava.getJmbg().equals(p.getJmbg()) && prijava.getDatumVolontiranja().equals(p.getDatumVolontiranja())
                    && prijava.getSmena() == p.getSmena() && prijava.getPozicija() == p.getPozicija()) {

                prijave.remove(i);
                save();

                String imeFajla = "prijave_txt" + File.separator + p.getJmbg() + "_" + p.getDatumVolontiranja() + ".txt";
                File fajl = new File(imeFajla);

                if (fajl.exists()) {
                    if (fajl.delete()) {
                        System.out.println("Fajl uspešno obrisan: " + fajl.getName());
                    } else {
                        System.out.println("Greška pri brisanju fajla: " + fajl.getName());
                    }
                } else {
                    System.out.println("Fajl ne postoji: " + fajl.getName());
                }

                return true;
            }
        }
        return false;
    }

}
