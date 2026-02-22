/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Aleksandar Milicevic
 */
public class ModelTabelePregledMojihPrijava extends AbstractTableModel {
    
    private List<PrijavaVolontiranja> lista;
    private String[] kolone = {"Datum", "Smena", "Pozicija", "Status"};
    
    public ModelTabelePregledMojihPrijava() {
        this.lista = new ArrayList<>();
    }
    
    public ModelTabelePregledMojihPrijava(List<PrijavaVolontiranja> lista) {
        this.lista = lista;
    }
    
    @Override
    public int getRowCount() {
        return lista.size();
    }

    @Override
    public int getColumnCount() {
        return kolone.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PrijavaVolontiranja p= lista.get(rowIndex);
        
        switch (columnIndex) {
            case 0:
                return p.getDatumVolontiranja();
            case 1:
                return p.getSmena();
            case 2:
                return p.getPozicija();
            case 3:
                return p.getStatusPrijave();
            default:
                return "N/A";
        }
    }

    @Override
    public String getColumnName(int column) {
        return kolone[column];
    }

    public List<PrijavaVolontiranja> getLista() {
        return lista;
    }

    public void setLista(List<PrijavaVolontiranja> lista) {
        this.lista = lista;
        fireTableDataChanged();
    }

    public void updateTabelaPrijava() {
        fireTableDataChanged();
    }

}
