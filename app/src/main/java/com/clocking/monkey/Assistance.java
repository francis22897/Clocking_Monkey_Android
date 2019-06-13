package com.clocking.monkey;

import android.provider.ContactsContract;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Assistance {

    Timestamp date;
    String email;
    Boolean fail;
    Boolean type;

    public Assistance(Timestamp date, String email, Boolean fail, Boolean type) {
        this.date = date;
        this.email = email;
        this.fail = fail;
        this.type = type;
    }

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

    public Boolean getFail() {
        return fail;
    }

    public void setFail(Boolean fail) {
        this.fail = fail;
    }

    public Boolean getType() {
        return type;
    }

    public void setType(Boolean type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Assistance{" +
                "date=" + date +
                ", email='" + email + '\'' +
                ", fail=" + fail +
                ", type=" + type +
                '}';
    }
}
