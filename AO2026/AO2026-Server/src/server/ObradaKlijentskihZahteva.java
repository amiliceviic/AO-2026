/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package server;

import controller.Controller;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Korisnik;
import model.PrijavaVolontiranja;
import model.StatusPrijave;
import operacije.Operacije;
import repo.RepozitorijumKorisnika;
import transfer.KlijentskiZahtev;
import transfer.ServerskiOdgovor;

/**
 *
 * @author Aleksandar Milicevic
 */
public class ObradaKlijentskihZahteva extends Thread {
    
    private Socket socket;
    Korisnik noviKorisnik;
    private Korisnik ulogovaniKorisnik;
    PrijavaVolontiranja p;
    boolean uspesno;
    boolean uspesnoUBazi;
    List<String> listaGresaka;

    public ObradaKlijentskihZahteva() {
    }

    public ObradaKlijentskihZahteva(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        
        while(true) {
            KlijentskiZahtev kz = primiZahtev();
            
            if(kz == null) {
                System.out.println("Klijent se diskonektovao: " + socket.getInetAddress());
                break;
            }
            
            ServerskiOdgovor so = new ServerskiOdgovor();
            
            switch (kz.getOperacija()) {
                case Operacije.SIGN_UP:
                    noviKorisnik = (Korisnik) kz.getParametar();
                    
                    uspesno = RepozitorijumKorisnika.getInstance().dodajKorisnika(noviKorisnik);
                    uspesnoUBazi = Controller.getInstance().sacuvajKorisnikaUBazu(noviKorisnik);
                    
                    if(uspesno && uspesnoUBazi) so.setPoruka("USPESNA REGISTRACIJA");
                    else {
                        so.setPoruka("KORISNICKO IME VEC POSTOJI!");
                    }
                    break;
                case Operacije.LOGIN:
                    noviKorisnik = (Korisnik) kz.getParametar();
                    
                    Korisnik pronadjen = RepozitorijumKorisnika.getInstance().pronadjiPoKorisnickovomImenu(noviKorisnik.getUsername(),noviKorisnik.getPassword());
                    
                    Korisnik pronadjenUBazi = Controller.getInstance().pronadjiKorisnikaUBazi(noviKorisnik.getUsername(),noviKorisnik.getPassword());
                    
                    if(pronadjen != null && pronadjenUBazi != null) {
                        this.ulogovaniKorisnik = pronadjenUBazi;
                        so.setOdgovor(pronadjen);
                        so.setPoruka("USPESNO LOGOVANJE");
                    }
                    else {
                        so.setPoruka("NEISPRAVAN USERNAME ILI PASSWORD");
                    }
                    break;
                case Operacije.PRIJAVA_ZA_VOLONTIRANJE:
                    p = (PrijavaVolontiranja) kz.getParametar();
                    p.setStatusPrijave(StatusPrijave.U_OBRADI);
                    
                    listaGresaka = Controller.getInstance().prijaviVolontera(p);
                    
                    String username = (ulogovaniKorisnik != null) ? ulogovaniKorisnik.getUsername() : null;
                    uspesnoUBazi = Controller.getInstance().sacuvajPrijavuUBazu(p, username);
                    
                    if(!listaGresaka.isEmpty() || !uspesnoUBazi) {
                        so.setPoruka("GRESKA PRI PRIJAVI");
                        if (!listaGresaka.isEmpty()) {
                            so.setOdgovor(listaGresaka);
                        } else {
                            so.setOdgovor("GRESKA PRI UPISU U BAZU");
                        }
                    }
                    else {
                        so.setPoruka("USPESNO STE SE PRIJAVILI");
                        if (Controller.getInstance().getData() != null) {
                            so.setOdgovor(Controller.getInstance().getData());
                        } else {
                            so.setOdgovor("Prijava uspešno kreirana, ali fajl sa podacima trenutno nije dostupan.");
                        }
                    }
                    break;
                case Operacije.PREGLED_PRIJAVA:
                    Korisnik k = (Korisnik) kz.getParametar();
                    List<PrijavaVolontiranja> lista = Controller.getInstance().vratiPrijaveZaKorisnika(k);
                    so.setOdgovor(lista);
                    if(lista.isEmpty()) {
                        so.setPoruka("NEMATE NI JEDNU PRIJAVU");
                    }
                    else {
                        so.setPoruka("USPESNO NADJENE PRIJAVE");
                    }
                    break;
                case Operacije.IZMENA_PRIJAVE:
                    List<PrijavaVolontiranja> listaStaraNovaPrijava = (List<PrijavaVolontiranja>) kz.getParametar();
                    
                    if(listaStaraNovaPrijava.size() == 2) {
                        PrijavaVolontiranja staraPrijava = listaStaraNovaPrijava.get(0);
                        PrijavaVolontiranja novaPrijava = listaStaraNovaPrijava.get(1);
                        
                        listaGresaka = Controller.getInstance().izmeniPrijavu(staraPrijava, novaPrijava);
                        
                        boolean izmeniUBazi = Controller.getInstance().izmeniPrijavuUBazi(staraPrijava, novaPrijava);
                        
                        if(!izmeniUBazi) listaGresaka.add("Greska! Nije moguće izmeniti prijavu u bazi!");
                        
                        if(listaGresaka.isEmpty() && izmeniUBazi) {
                            so.setPoruka("USPESNO IZMENJENA PRIJAVA");
                            so.setOdgovor(Controller.getInstance().getData());
                        }
                        else {
                            so.setPoruka("GRESKA PRI IZMENI PRIJAVE");
                            so.setOdgovor(listaGresaka);
                        }
                    }
                    break;
                case Operacije.OTKAZ_PRIJAVE:
                    p = (PrijavaVolontiranja) kz.getParametar();
                    
                    uspesno = Controller.getInstance().otkaziPrijavu(p);
                    
                    uspesnoUBazi = Controller.getInstance().otkaziPrijavuUBazi(p);
                    
                    so.setOdgovor(uspesno && uspesnoUBazi);
                    if(uspesno && uspesnoUBazi) so.setPoruka("USPESNO OBRISANA PRIJAVA");
                    else so.setPoruka("GRESKA PRI BRISANJU PRIJAVE");
                    break;
                case Operacije.IZLAZ:
                    System.out.println("Klijent se odjavio: " + ulogovaniKorisnik.getUsername());
                    break;
                default:
                    throw new AssertionError();
            }
            posaljiOdgovor(so);
        }
    }
    
    private KlijentskiZahtev primiZahtev() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            return (KlijentskiZahtev) in.readObject();
        } catch (SocketException ex) {
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    private void posaljiOdgovor(ServerskiOdgovor so) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(so);
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(ObradaKlijentskihZahteva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
