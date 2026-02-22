/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package controller;

import baza.DBBroker;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Korisnik;
import model.Pozicija;
import model.PrijavaVolontiranja;
import model.StatusPrijave;
import repo.RepozitorijumPrijava;

/**
 *
 * @author Aleksandar Milicevic
 */
public class Controller {
    
    private static Controller instance;
    private byte[] data;
    private DBBroker dbb;

    private Controller() {
        dbb = new DBBroker();
    }
    
    public static Controller getInstance() {
        if(instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public List<String> prijaviVolontera(PrijavaVolontiranja p) {
        List<String> listaGresaka = validiraj(p);
        
        listaGresaka.addAll(validirajBrojSmena(p));
        
        if(listaGresaka.isEmpty()) {
            RepozitorijumPrijava.getInstance().dodajPrijavu(p);
            napraviTekstualniFajl(p);
        }

        return listaGresaka;
    }

    private List<String> validiraj(PrijavaVolontiranja p) {
        List<String> listaGresaka = new ArrayList<>();
        
        if(!p.getJmbg().matches("\\d{13}")) listaGresaka.add("Greska! JMBG mora imati tačno 13 cifara!");
        
        if(!p.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) listaGresaka.add("Greska! Neispravan format e-mail adrese!");
        
        if(p.getDatumVolontiranja().isBefore(LocalDate.now().plusDays(1))) listaGresaka.add("Greska! Datum prijave za volontiranje mora biti najmanje sutrasnji!");
        
        if(p.getDatumVolontiranja().isAfter(LocalDate.of(2026, 2, 5))) listaGresaka.add("Greska! Datum ne može biti posle 05.02.2026!");
        
        if(p.getPozicija() == null) listaGresaka.add("Greska! Morate izabrati poziciju!");
        
        if(p.getSmena() == null) listaGresaka.add("Greska! Morate izabrati smenu!");
        
        if(RepozitorijumPrijava.getInstance().korisnikVecPrijavljenZaDatum(p.getJmbg(), p.getDatumVolontiranja())) listaGresaka.add("Greska! Korisnik moze imati samo jednu prijavu po danu!");
        
        Set<Pozicija> pozicijeKorisnika = RepozitorijumPrijava.getInstance().getRazlicitePozicijeKorisnika(p.getJmbg());
        
        if(!pozicijeKorisnika.contains(p.getPozicija()) && pozicijeKorisnika.size() >= 2) listaGresaka.add("Greska! Korisnik je već prijavljen za dve različite pozicije, ne moze dodati trecu!");
        
        return listaGresaka;
    }

    private void napraviTekstualniFajl(PrijavaVolontiranja p) {
        String folder = "prijave_txt";
        File dir = new File(folder);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String imeFajla = folder + File.separator + p.getJmbg() + "_" + p.getDatumVolontiranja() + ".txt";
        File fajl = new File(imeFajla);

        try ( PrintWriter out = new PrintWriter(fajl)) {
            out.println("Ime: " + p.getIme());
            out.println("Prezime: " + p.getPrezime());
            out.println("JMBG: " + p.getJmbg());
            out.println("Email: " + p.getEmail());
            out.println("Datum volontiranja: " + p.getDatumVolontiranja());
            out.println("Smena: " + p.getSmena());
            out.println("Pozicija: " + p.getPozicija());
            out.println("Datum prijave: " + p.getDatumPrijave());
            out.println("Status prijave: " + p.getStatusPrijave());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (fajl.exists()) {
                data = Files.readAllBytes(fajl.toPath());
            } else {
                data = new byte[0]; // bar da nije null
            }
        } catch (Exception e) {
            e.printStackTrace();
            data = new byte[0];
        }
    }

    public List<PrijavaVolontiranja> vratiPrijaveZaKorisnika(Korisnik k) {
        try {
            
            List<PrijavaVolontiranja> lista = dbb.vratiPrijaveZaKorisnika(k);
            
            if(lista != null) {
                for(PrijavaVolontiranja p : lista) {
                    p.setStatusPrijave(izracunajStatus(p));
                }
                return lista;
            }
            
        } catch (Exception e) {
            System.err.println("Greška pri čitanju iz baze, koristi se fajl backup");
            e.printStackTrace();
        }
        return ucitajPrijaveIzFajla(k);
    }

    private StatusPrijave izracunajStatus(PrijavaVolontiranja p) {
        LocalDateTime sada = LocalDateTime.now();
        LocalDateTime datum = p.getDatumVolontiranja().atStartOfDay();
        
        if(datum.isBefore(sada)) return StatusPrijave.ZAVRSENA;
        
        if(Duration.between(sada, datum).toHours() < 24) return StatusPrijave.ZAKLJUCANA;
        
        return StatusPrijave.U_OBRADI;
    }

    public boolean otkaziPrijavu(PrijavaVolontiranja p) {
        return RepozitorijumPrijava.getInstance().obrisi(p);
    }

    public List<String> izmeniPrijavu(PrijavaVolontiranja staraPrijava, PrijavaVolontiranja novaPrijava) {
        List<String> listaGresaka = new ArrayList<>();
        
        StatusPrijave status = izracunajStatus(staraPrijava);
        
        if(status != StatusPrijave.U_OBRADI) {
            listaGresaka.add("Greska! Moguće je menjati samo prijave koje su maksimalno 24h do datuma smene!");
            return listaGresaka;
        }
        
        LocalDateTime sada = LocalDateTime.now();
        LocalDateTime datumSmena = novaPrijava.getDatumVolontiranja().atStartOfDay();
        
        if(novaPrijava.getDatumVolontiranja().isBefore(LocalDate.now().plusDays(1))) listaGresaka.add("Greska! Datum prijave mora biti najmanje sutrasnji!");
        
        if(Duration.between(sada, datumSmena).toHours() < 24) listaGresaka.add("Greska! Izmene su moguće samo do 24h pre smene!");
        
        int brojJutarnjih = 0, brojPopodnevnih = 0, brojVecernjih = 0;
        
        for(PrijavaVolontiranja p : RepozitorijumPrijava.getInstance().getPrijave()) {
            if(!p.getJmbg().equals(novaPrijava.getJmbg())) continue;
            
            if(p.equals(staraPrijava)) continue;
            
            switch (p.getSmena()) {
                case JUTARNJA:
                    brojJutarnjih++;
                    break;
                case POPODNEVNA:
                    brojPopodnevnih++;
                    break;
                case VECERNJA:
                    brojVecernjih++;
                    break;
                default:
                    throw new AssertionError();
            }
        }
        
        switch (novaPrijava.getSmena()) {
            case POPODNEVNA:
                if(brojPopodnevnih >= 5) listaGresaka.add("Greska! Korisnik ne moze imati vise od 5 popodnevnih smena!");
                break;
            case VECERNJA:
                if(brojVecernjih >= 3) listaGresaka.add("Greska! Korisnik ne moze imati vise od 3 vecernjih smena!");
                break;
            case JUTARNJA:
                break;
            default:
                throw new AssertionError();
        }
        
        if(!listaGresaka.isEmpty()) return listaGresaka;
        
        RepozitorijumPrijava.getInstance().obrisi(staraPrijava);
        RepozitorijumPrijava.getInstance().dodajPrijavu(novaPrijava);
        
        napraviTekstualniFajl(novaPrijava);
        
        return listaGresaka;
    }

    private List<String> validirajBrojSmena(PrijavaVolontiranja p) {
        int brojJutarnjih = 0;
        int brojPopodnevnih = 0;
        int brojVecernjih = 0;
        
        for(PrijavaVolontiranja prijava : RepozitorijumPrijava.getInstance().getPrijave()) {
            if(!p.getJmbg().equals(prijava.getJmbg())) continue;
            
            switch (prijava.getSmena()) {
                case JUTARNJA:
                    brojJutarnjih++;
                    break;
                case POPODNEVNA:
                    brojPopodnevnih++;
                    break;
                case VECERNJA:
                    brojVecernjih++;
                    break;
                default:
                    throw new AssertionError();
            }
        }
        
        List<String> greske = new ArrayList<>();
        
        switch (p.getSmena()) {
            case POPODNEVNA:
                if(brojPopodnevnih >= 5) greske.add("Greska! Korisnik ne moze imati vise od 5 popodnevnih smena!");
                break;
            case VECERNJA:
                if(brojVecernjih >= 3) greske.add("Greska! Korisnik ne moze imati vise od 3 vecernjih smena!");
                break;
            case JUTARNJA:
                break;
            default:
                throw new AssertionError();
        }
        
        return greske;
    }

    public boolean sacuvajKorisnikaUBazu(Korisnik noviKorisnik) {
        if(!validirajKorisnika(noviKorisnik)) return false;
        return dbb.sacuvajKorisnikaUBazu(noviKorisnik);
    }

    public Korisnik pronadjiKorisnikaUBazi(String username, String password) {
        return dbb.pronadjiKorisnikaUBazi(username, password);
    }

    public boolean sacuvajPrijavuUBazu(PrijavaVolontiranja p, String username) {
        List<String> listaGresaka = validirajBaza(p);
        
        if(!listaGresaka.isEmpty()) return false;
        
        return dbb.sacuvajPrijavuUBazu(p, username);
    }

    private List<PrijavaVolontiranja> ucitajPrijaveIzFajla(Korisnik k) {
        List<PrijavaVolontiranja> lista = new ArrayList<>();

        for (PrijavaVolontiranja p : RepozitorijumPrijava.getInstance().getPrijave()) {
            if (p.getJmbg().equals(k.getJmbg()) && p.getEmail().equals(k.getEmail())) {
                p.setStatusPrijave(izracunajStatus(p));
                lista.add(p);
            }
        }

        return lista;
    }

    public boolean izmeniPrijavuUBazi(PrijavaVolontiranja staraPrijava, PrijavaVolontiranja novaPrijava) {
        StatusPrijave status = izracunajStatus(staraPrijava);
        
        if(status != StatusPrijave.U_OBRADI) return false;
        
        LocalDateTime sada = LocalDateTime.now();
        LocalDateTime datumSmena = novaPrijava.getDatumVolontiranja().atStartOfDay();
        
        if(novaPrijava.getDatumVolontiranja().isBefore(LocalDate.now().plusDays(1))) return false;
        
        if(Duration.between(sada, datumSmena).toHours() < 24) return false;
        
        int brojJutarnjih = 0, brojPopodnevnih = 0, brojVecernjih = 0;
        
        for(PrijavaVolontiranja p : RepozitorijumPrijava.getInstance().getPrijave()) {
            if(!p.getJmbg().equals(novaPrijava.getJmbg())) continue;
            
            if(p.equals(staraPrijava)) continue;
            
            switch (p.getSmena()) {
                case JUTARNJA:
                    brojJutarnjih++;
                    break;
                case POPODNEVNA:
                    brojPopodnevnih++;
                    break;
                case VECERNJA:
                    brojVecernjih++;
                    break;
                default:
                    throw new AssertionError();
            }
        }
        
        switch (novaPrijava.getSmena()) {
            case POPODNEVNA:
                if(brojPopodnevnih >= 5) return false;
                break;
            case VECERNJA:
                if(brojVecernjih >= 3) return false;
                break;
            case JUTARNJA:
                break;
            default:
                throw new AssertionError();
        }
        
        return dbb.izmeniPrijavuUBazi(staraPrijava, novaPrijava);
    }
    
    private boolean validirajKorisnika(Korisnik noviKorsnik) {
        List<String> listaGresaka = new ArrayList<>();

        if (noviKorsnik.getUsername() == null || noviKorsnik.getUsername().trim().isEmpty()) listaGresaka.add("Greska! Username ne sme biti prazan!");
        else if(noviKorsnik.getUsername().length() < 3) listaGresaka.add("Greska! Username mora imati najmanje 3 karaktera!");
        
        if (noviKorsnik.getPassword( )== null || noviKorsnik.getPassword().trim().isEmpty()) listaGresaka.add("Greska! Password ne sme biti prazan!");
        else if(noviKorsnik.getPassword().length() < 4) listaGresaka.add("Greska! Password mora imati najmanje 4 karaktera!");
        
        if (noviKorsnik.getIme() == null || noviKorsnik.getIme().trim().isEmpty()) listaGresaka.add("Greska! Ime ne sme biti prazno!");
        else if (!noviKorsnik.getIme().matches("[A-Za-zŠĐČĆŽšđčćž]+")) listaGresaka.add("Greska! Ime moze sadrzati samo slova!");
        
        if (noviKorsnik.getPrezime() == null || noviKorsnik.getPrezime().trim().isEmpty()) listaGresaka.add("Greska! Prezime ne sme biti prazno!");
        else if (!noviKorsnik.getPrezime().matches("[A-Za-zŠĐČĆŽšđčćž]+")) listaGresaka.add("Greska! Prezime moze sadrzati samo slova!");
        
        if (noviKorsnik.getJmbg() == null || noviKorsnik.getJmbg().trim().isEmpty()) listaGresaka.add("Greska! JMBG ne sme biti prazno!");
        else if (!noviKorsnik.getJmbg().matches("\\d{13}")) listaGresaka.add("Greska! JMBG mora imati tačno 13 cifara!");
        
        if (noviKorsnik.getEmail() == null || noviKorsnik.getEmail().trim().isEmpty()) listaGresaka.add("Greska! E-mail ne sme biti prazno!");
        else if (!noviKorsnik.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) listaGresaka.add("Greska! Neispravan format e-mail adrese!");
            
        return listaGresaka.isEmpty();
    }

    public boolean otkaziPrijavuUBazi(PrijavaVolontiranja p) {
        return dbb.otkaziPrijavuUBazi(p);
    }

    private List<String> validirajBaza(PrijavaVolontiranja p) {
        List<String> listaGresaka = new ArrayList<>();
        
        if(!p.getJmbg().matches("\\d{13}")) listaGresaka.add("Greska! JMBG mora imati tačno 13 cifara!");
        
        if(!p.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) listaGresaka.add("Greska! Neispravan format e-mail adrese!");
        
        if(p.getDatumVolontiranja().isBefore(LocalDate.now().plusDays(1))) listaGresaka.add("Greska! Datum prijave za volontiranje mora biti najmanje sutrasnji!");
        
        if(p.getDatumVolontiranja().isAfter(LocalDate.of(2026, 5, 1))) listaGresaka.add("Greska! Datum ne može biti posle 05.02.2026!");
        
        if(p.getPozicija() == null) listaGresaka.add("Greska! Morate izabrati poziciju!");
        
        if(p.getSmena() == null) listaGresaka.add("Greska! Morate izabrati smenu!");
        
        if (dbb.korisnikVecPrijavljenUBazi(p.getJmbg(), p.getDatumVolontiranja())) {
            listaGresaka.add("Greska! Korisnik moze imati samo jednu prijavu po danu!");
        }
        
        listaGresaka.addAll(validirajBrojSmenaUBazi(p));
        
        Set<Pozicija> pozicijeKorisnika = getRazlicitePozicijeKorisnikaUBazi(p.getJmbg());
        
        if(!pozicijeKorisnika.contains(p.getPozicija()) && pozicijeKorisnika.size() >= 2) listaGresaka.add("Greska! Korisnik je već prijavljen za dve različite pozicije, ne moze dodati trecu!");
        
        return listaGresaka;
    }

    private List<String> validirajBrojSmenaUBazi(PrijavaVolontiranja p) {
        int brojJutarnjih = 0;
        int brojPopodnevnih = 0;
        int brojVecernjih = 0;
        
        List<PrijavaVolontiranja> prijaveUBazi = dbb.vratiPrijaveZaKorisnikaJMBG(p.getJmbg());
        
        for(PrijavaVolontiranja prijava : prijaveUBazi) {
            if(!p.getJmbg().equals(prijava.getJmbg())) continue;
            
            switch (prijava.getSmena()) {
                case JUTARNJA:
                    brojJutarnjih++;
                    break;
                case POPODNEVNA:
                    brojPopodnevnih++;
                    break;
                case VECERNJA:
                    brojVecernjih++;
                    break;
                default:
                    throw new AssertionError();
            }
        }
        
        List<String> greske = new ArrayList<>();

        switch (p.getSmena()) {
            case POPODNEVNA:
                if (brojPopodnevnih >= 5) {
                    greske.add("Greska! Korisnik ne moze imati vise od 5 popodnevnih smena!");
                }
                break;
            case VECERNJA:
                if (brojVecernjih >= 3) {
                    greske.add("Greska! Korisnik ne moze imati vise od 3 vecernjih smena!");
                }
                break;
            case JUTARNJA:
                break;
            default:
                throw new AssertionError();
        }

        return greske;
    }

    private Set<Pozicija> getRazlicitePozicijeKorisnikaUBazi(String jmbg) {
        Set<Pozicija> pozicije = new HashSet<>();
        
        List<PrijavaVolontiranja> prijaveUBazi = dbb.vratiPrijaveZaKorisnikaJMBG(jmbg);
        
        if (prijaveUBazi == null) return pozicije;
        
        for (PrijavaVolontiranja p : prijaveUBazi) {
            if (p.getJmbg().equals(jmbg)) {
                pozicije.add(p.getPozicija());
            }
        }
        return pozicije;
    }
    
}
