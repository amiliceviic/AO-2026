/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package baza;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Korisnik;
import model.Pozicija;
import model.PrijavaVolontiranja;
import model.Smena;
import model.StatusPrijave;

/**
 *
 * @author Aleksandar Milicevic
 */
public class DBBroker {

    public boolean sacuvajKorisnikaUBazu(Korisnik noviKorisnik) {
        if (postojiKorisnikUBazi(noviKorisnik.getUsername())) {
            System.out.println("Korisnik sa username " + noviKorisnik.getUsername() + " vec postoji u bazi!");
            return false;
        }
        
        String query = "INSERT INTO korisnik (username, password, ime, prezime, jmbg, email) VALUES (?, ?, ?, ?, ?, ?)";
        
        PreparedStatement statement = null;
        
        try {
            statement = Konekcija.getInstance().getConnection().prepareStatement(query);
            
            statement.setString(1, noviKorisnik.getUsername());
            statement.setString(2, noviKorisnik.getPassword());
            statement.setString(3, noviKorisnik.getIme());
            statement.setString(4, noviKorisnik.getPrezime());
            statement.setString(5, noviKorisnik.getJmbg());
            statement.setString(6, noviKorisnik.getEmail());
            
            int affectedRows = statement.executeUpdate();
            
            Konekcija.getInstance().getConnection().commit();
            
            return affectedRows > 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
            try {
                Konekcija.getInstance().getConnection().rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return false;
        } finally {
            if(statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public boolean postojiKorisnikUBazi(String username) {
        String query = "SELECT COUNT(*) FROM korisnik WHERE username = ?";

        PreparedStatement statement = null;
        
        try {
            statement = Konekcija.getInstance().getConnection().prepareStatement(query);

            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public Korisnik pronadjiKorisnikaUBazi(String username, String password) {
        String query = "SELECT username, password, ime, prezime, jmbg, email FROM korisnik WHERE username = ? AND password = ?";
        
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        try {
            statement = Konekcija.getInstance().getConnection().prepareStatement(query);
            
            statement.setString(1, username);
            statement.setString(2, password);
            
            rs = statement.executeQuery();
            
            if(rs.next()) {
                String usernameKorisnik = rs.getString("username");
                String passwordKorisnik = rs.getString("password");
                String ime = rs.getString("ime");
                String prezime = rs.getString("prezime");
                String jmbg = rs.getString("jmbg");
                String email = rs.getString("email");
                
                Korisnik k = new Korisnik(usernameKorisnik, passwordKorisnik, ime, prezime, jmbg, email);
                
                return k;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }

    public boolean sacuvajPrijavuUBazu(PrijavaVolontiranja p, String username) {
        Integer korisnikId = null;
        
        if(username != null) {
            String queryId = "SELECT id_korisnik FROM korisnik WHERE username = ?";
            
            PreparedStatement statementKorisnik = null;
            ResultSet rs = null;
            
            try {
                statementKorisnik = Konekcija.getInstance().getConnection().prepareStatement(queryId);
                
                statementKorisnik.setString(1, username);
                
                rs = statementKorisnik.executeQuery();
                
                if(rs.next()) {
                    korisnikId = rs.getInt("id_korisnik");
                }
                else {
                    System.out.println("Username nije pronadjen, prijava će biti sa korisnik_id = NULL");
                }
            } catch (SQLException ex) {
                Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } finally {
                if(rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if(statementKorisnik != null) {
                    try {
                        statementKorisnik.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        
        String query = "INSERT INTO prijava_volontiranja "
                 + "(korisnik_id, ime, prezime, jmbg, email, datum_volontiranja, smena, pozicija, datum_prijave, status_prijave) "
                 + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement statement = null;
        
        try {
            statement = Konekcija.getInstance().getConnection().prepareStatement(query);
            
            if(username != null) {
                statement.setInt(1, korisnikId);
            }
            else {
                statement.setNull(1, Types.INTEGER);
            }
            
            statement.setString(2, p.getIme());
            statement.setString(3, p.getPrezime());
            statement.setString(4, p.getJmbg());
            statement.setString(5, p.getEmail());
            statement.setDate(6, Date.valueOf(p.getDatumVolontiranja()));
            statement.setString(7, String.valueOf(p.getSmena()));
            statement.setString(8, String.valueOf(p.getPozicija()));
            statement.setDate(9, Date.valueOf(p.getDatumPrijave()));
            statement.setString(10, String.valueOf(p.getStatusPrijave()));
            
            int affectedRows = statement.executeUpdate();
            
            Konekcija.getInstance().getConnection().commit();
            
            return affectedRows > 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
            try {
                Konekcija.getInstance().getConnection().rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return false;
        } finally {
            if(statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public List<PrijavaVolontiranja> vratiPrijaveZaKorisnika(Korisnik k) {

        List<PrijavaVolontiranja> lista = new ArrayList<>();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            if (k.getUsername() == null) {

                String query = "SELECT * FROM prijava_volontiranja WHERE jmbg = ? AND email = ?";

                ps = Konekcija.getInstance().getConnection().prepareStatement(query);
                ps.setString(1, k.getJmbg());
                ps.setString(2, k.getEmail());

                rs = ps.executeQuery();
            }
            else {

                String queryKorisnik
                        = "SELECT id_korisnik FROM korisnik WHERE username = ?";

                ps = Konekcija.getInstance().getConnection().prepareStatement(queryKorisnik);
                ps.setString(1, k.getUsername());

                rs = ps.executeQuery();

                if (!rs.next()) {
                    return lista;
                }

                int korisnikId = rs.getInt("id_korisnik");

                rs.close();
                ps.close();

                String queryPrijave
                        = "SELECT * FROM prijava_volontiranja WHERE korisnik_id = ?";

                ps = Konekcija.getInstance().getConnection().prepareStatement(queryPrijave);
                ps.setInt(1, korisnikId);

                rs = ps.executeQuery();
            }

            while (rs.next()) {

                String ime = rs.getString("ime");
                String prezime = rs.getString("prezime");
                String jmbg = rs.getString("jmbg");
                String email = rs.getString("email");
                LocalDate datumVolontiranja = rs.getDate("datum_volontiranja").toLocalDate();
                Smena smena = Smena.valueOf(rs.getString("smena"));
                Pozicija pozicija = Pozicija.valueOf(rs.getString("pozicija"));
                LocalDate datumPrijave = rs.getDate("datum_prijave").toLocalDate();
                PrijavaVolontiranja p = new PrijavaVolontiranja(ime, prezime, jmbg, email, datumVolontiranja, smena, pozicija, datumPrijave);

                lista.add(p);
            }

        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return lista;
    }

    public boolean izmeniPrijavuUBazi(PrijavaVolontiranja staraPrijava, PrijavaVolontiranja novaPrijava) {      
        String query = "UPDATE prijava_volontiranja SET ime = ?, prezime = ?, jmbg = ?, email = ?, datum_volontiranja = ?, smena = ?, pozicija = ?, datum_prijave = ?, status_prijave = ? "
            + "WHERE jmbg = ? AND datum_volontiranja = ? AND smena = ? AND pozicija = ?";
        
        PreparedStatement statement = null;
        
        try {
            statement = Konekcija.getInstance().getConnection().prepareStatement(query);
            
            statement.setString(1, novaPrijava.getIme());
            statement.setString(2, novaPrijava.getPrezime());
            statement.setString(3, novaPrijava.getJmbg());
            statement.setString(4, novaPrijava.getEmail());
            statement.setDate(5, Date.valueOf(novaPrijava.getDatumVolontiranja()));
            statement.setString(6, String.valueOf(novaPrijava.getSmena()));
            statement.setString(7, String.valueOf(novaPrijava.getPozicija()));
            statement.setDate(8, Date.valueOf(novaPrijava.getDatumPrijave()));
            statement.setString(9, String.valueOf(novaPrijava.getStatusPrijave()));
            
            statement.setString(10, staraPrijava.getJmbg());
            statement.setDate(11, Date.valueOf(staraPrijava.getDatumVolontiranja()));
            statement.setString(12, String.valueOf(staraPrijava.getSmena()));
            statement.setString(13, String.valueOf(staraPrijava.getPozicija()));
            
            int affectedRows = statement.executeUpdate();
            
            Konekcija.getInstance().getConnection().commit();
            
            return affectedRows > 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
            try {
                Konekcija.getInstance().getConnection().rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return false;
        } finally {
            if(statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public boolean otkaziPrijavuUBazi(PrijavaVolontiranja p) {
        String query = "DELETE FROM prijava_volontiranja WHERE jmbg = ? AND datum_volontiranja = ? AND smena = ? AND pozicija = ?";
        
        PreparedStatement statement = null;
        
        try {
            statement = Konekcija.getInstance().getConnection().prepareStatement(query);
            
            statement.setString(1, p.getJmbg());
            statement.setDate(2, Date.valueOf(p.getDatumVolontiranja()));
            statement.setString(3, String.valueOf(p.getSmena()));
            statement.setString(4, String.valueOf(p.getPozicija()));
            
            int affectedRows = statement.executeUpdate();
            
            Konekcija.getInstance().getConnection().commit();
            
            return affectedRows > 0;
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
            try {
                Konekcija.getInstance().getConnection().rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return false;
        } finally {
            if(statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public boolean korisnikVecPrijavljenUBazi(String jmbg, LocalDate datumVolontiranja) {
        String query = "SELECT COUNT(*) FROM prijava_volontiranja WHERE jmbg = ? AND datum_volontiranja = ?";
        
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        try {
            statement = Konekcija.getInstance().getConnection().prepareStatement(query);
            
            statement.setString(1, jmbg);
            statement.setDate(2, Date.valueOf(datumVolontiranja));
            
            rs = statement.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            if(rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }

    public List<PrijavaVolontiranja> vratiPrijaveZaKorisnikaJMBG(String jmbg) {
        List<PrijavaVolontiranja> lista = new ArrayList<>();
        String query = "SELECT * FROM prijava_volontiranja WHERE jmbg = ?";
        
        PreparedStatement statemet = null;
        ResultSet rs = null;
        
        try {
            statemet = Konekcija.getInstance().getConnection().prepareStatement(query);
            
            statemet.setString(1, jmbg);
            
            rs = statemet.executeQuery();
            
            while (rs.next()) {
                PrijavaVolontiranja p = new PrijavaVolontiranja();
                p.setJmbg(rs.getString("jmbg"));
                p.setIme(rs.getString("ime"));
                p.setPrezime(rs.getString("prezime"));
                p.setEmail(rs.getString("email"));
                p.setDatumVolontiranja(rs.getDate("datum_volontiranja").toLocalDate());
                p.setSmena(Smena.valueOf(rs.getString("smena")));
                p.setPozicija(Pozicija.valueOf(rs.getString("pozicija")));
                p.setStatusPrijave(StatusPrijave.valueOf(rs.getString("status_prijave")));
                lista.add(p);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(statemet != null) {
                try {
                    statemet.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DBBroker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return lista;
    }

}
