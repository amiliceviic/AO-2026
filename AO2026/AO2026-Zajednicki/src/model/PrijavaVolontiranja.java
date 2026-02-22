/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 *
 * @author Aleksandar Milicevic
 */
public class PrijavaVolontiranja implements Serializable {
    
    private String ime;
    private String prezime;
    private String jmbg;
    private String email;
    private LocalDate datumVolontiranja;
    private Smena smena;
    private Pozicija pozicija;
    private LocalDate datumPrijave;
    private StatusPrijave statusPrijave;

    public PrijavaVolontiranja() {
    }

    public PrijavaVolontiranja(String ime, String prezime, String jmbg, String email, LocalDate datumVolontiranja, Smena smena, Pozicija pozicija, LocalDate datumPrijave) {
        this.ime = ime;
        this.prezime = prezime;
        this.jmbg = jmbg;
        this.email = email;
        this.datumVolontiranja = datumVolontiranja;
        this.smena = smena;
        this.pozicija = pozicija;
        this.datumPrijave = datumPrijave;
    }

    public PrijavaVolontiranja(String ime, String prezime, String jmbg, String email, LocalDate datumVolontiranja, Smena smena, Pozicija pozicija, LocalDate datumPrijave, StatusPrijave statusPrijave) {
        this.ime = ime;
        this.prezime = prezime;
        this.jmbg = jmbg;
        this.email = email;
        this.datumVolontiranja = datumVolontiranja;
        this.smena = smena;
        this.pozicija = pozicija;
        this.datumPrijave = datumPrijave;
        this.statusPrijave = statusPrijave;
    }
    
    public String getJmbg() {
        return jmbg;
    }

    public void setJmbg(String jmbg) {
        this.jmbg = jmbg;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDatumVolontiranja() {
        return datumVolontiranja;
    }

    public void setDatumVolontiranja(LocalDate datumVolontiranja) {
        this.datumVolontiranja = datumVolontiranja;
    }

    public Smena getSmena() {
        return smena;
    }

    public void setSmena(Smena smena) {
        this.smena = smena;
    }

    public Pozicija getPozicija() {
        return pozicija;
    }

    public void setPozicija(Pozicija pozicija) {
        this.pozicija = pozicija;
    }

    public LocalDate getDatumPrijave() {
        return datumPrijave;
    }

    public void setDatumPrijave(LocalDate datumPrijave) {
        this.datumPrijave = datumPrijave;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public StatusPrijave getStatusPrijave() {
        return statusPrijave;
    }

    public void setStatusPrijave(StatusPrijave statusPrijave) {
        this.statusPrijave = statusPrijave;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PrijavaVolontiranja other = (PrijavaVolontiranja) obj;
        if (!Objects.equals(this.jmbg, other.jmbg)) {
            return false;
        }
        if (!Objects.equals(this.email, other.email)) {
            return false;
        }
        return Objects.equals(this.datumVolontiranja, other.datumVolontiranja);
    }
    
}
