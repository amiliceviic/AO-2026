/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package model;

import java.io.Serializable;

/**
 *
 * @author Aleksandar Milicevic
 */
public class Korisnik implements Serializable {
    
    private String username;
    private String password;
    private String ime;
    private String prezime;
    private String jmbg;
    private String email;

    public Korisnik() {
    }

    public Korisnik(String username, String password, String ime, String prezime, String jmbg, String email) {
        this.username = username;
        this.password = password;
        this.ime = ime;
        this.prezime = prezime;
        this.jmbg = jmbg;
        this.email = email;
    }
    
        public Korisnik(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
    
}
