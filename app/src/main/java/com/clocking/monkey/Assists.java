package com.clocking.monkey;

import com.google.firebase.Timestamp;

public class Assists {

    Timestamp date;
    String email;
    boolean fail;
    boolean type;


    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isFail() {
        return fail;
    }

    public void setFail(boolean fail) {
        this.fail = fail;
    }

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return "Assists{" +
                "date=" + date +
                ", email='" + email + '\'' +
                ", fail=" + fail +
                ", type=" + type +
                '}';
    }
}
