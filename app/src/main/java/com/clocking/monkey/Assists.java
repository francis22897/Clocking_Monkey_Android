package com.clocking.monkey;

import java.util.ArrayList;
import java.util.List;

public class Assists {

    private List<Assistance> assists;

    public Assists(){
        this.assists = new ArrayList<>();
    }

    public Assists(List<Assistance> assists){
        this.assists = assists;
    }

    public List<Assistance> getAssists(){
        return assists;
    }

    public void addAssistance(Assistance assistance){
        assists.add(assistance);
    }

    public void removeAssistance(Assistance assistance){
        assists.remove(assistance);
    }

    @Override
    public String toString() {
        return "Assists{" +
                "assists=" + assists +
                '}';
    }
}
