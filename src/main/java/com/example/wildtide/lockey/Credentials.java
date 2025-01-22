package com.example.wildtide.lockey;
import java.util.ArrayList;
import java.io.Serializable;

public class Credentials implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private ArrayList<String> topFields;
    private ArrayList<String> bottomFields;
    private boolean isPinned;

    public Credentials(String name, ArrayList<String> fields1, ArrayList<String> fields2, boolean isPinned) {
        this.name=name;
        topFields=fields1;
        bottomFields=fields2;
        this.isPinned=isPinned;
    }

    public String getName() {
        return name;
    }
    /* public void setName(String name) {
        this.name=name;
    } */

    public ArrayList<String> getTopFields() {
        return topFields;
    }
    /* public void addTopField(String field) {
        topFields.add(field);
    }
    public void removeTopField(String field) {
        topFields.remove(field);
    } */

    public ArrayList<String> getBottomFields() {
        return bottomFields;
    }
    /* public void addBottomField(String field) {
        bottomFields.add(field);
    }
    public void removeBottomField(String field) {
        bottomFields.remove(field);
    } */
    
    /* public void setPinned(boolean isPinned) {
        this.isPinned=isPinned;
    } */
    public boolean isPinned() {
        return isPinned;
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj) return true;
        if (obj instanceof Credentials) {
            Credentials temp=(Credentials) obj;
            if (!this.getName().equals(temp.getName())) return false;
            if (this.topFields.size()!=temp.topFields.size()) return false;
            for (String field1:this.topFields) {
                if (!temp.topFields.contains(field1)) return false;
            }
            if (this.bottomFields.size()!=temp.bottomFields.size()) return false;
            for (String field1:this.bottomFields) {
                if (!temp.bottomFields.contains(field1)) return false;
            }
            if (this.isPinned!=temp.isPinned) return false;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String s = "Name: "+name+"\nTop fields: ";
        if (topFields==null) s+="null";
        else {
            for (String field:topFields) {
                s+=field+" - ";
            }
        }
        s+="\nBottom fields: ";
        if (bottomFields==null) s+="null";
        else {
            for (String field:bottomFields) {
                s+=field+" - ";
            }
        }
        s+="\nPinned: "+isPinned;
        return s;
    }
}
