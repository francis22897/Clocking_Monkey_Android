package com.clocking.monkey;

import android.provider.ContactsContract;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Assistance {

    private Timestamp date;
    private String email;
    private Boolean fail;
    private Boolean type;
    private String comment;

    public Assistance(Timestamp date, String email, Boolean fail, Boolean type, String comment) {
        this.date = date;
        this.email = email;
        this.fail = fail;
        this.type = type;
        this.comment = comment;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Assistance{" +
                "date=" + date +
                ", email='" + email + '\'' +
                ", fail=" + fail +
                ", type=" + type +
                ", comment='" + comment + '\'' +
                '}';
    }
}
