/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package transfer;

import java.io.Serializable;

/**
 *
 * @author Aleksandar Milicevic
 */
public class ServerskiOdgovor implements Serializable {
    
    private Object odgovor;
    private String poruka;

    public ServerskiOdgovor() {
    }

    public ServerskiOdgovor(Object odgovor, String poruka) {
        this.odgovor = odgovor;
        this.poruka = poruka;
    }

    public Object getOdgovor() {
        return odgovor;
    }

    public void setOdgovor(Object odgovor) {
        this.odgovor = odgovor;
    }

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }
    
}
