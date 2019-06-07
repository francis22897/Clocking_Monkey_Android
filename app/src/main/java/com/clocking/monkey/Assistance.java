package com.clocking.monkey;

import android.provider.ContactsContract;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Assistance {

    boolean fail;
    Timestamp in;
    Timestamp out;
    String user;

    public Assistance(boolean fail, Timestamp in, Timestamp out, String user) {
        this.fail = fail;
        this.in = in;
        this.out = out;
        this.user = user;
    }

    public boolean isFail() {
        return fail;
    }

    public void setFail(boolean fail) {
        this.fail = fail;
    }

    public Timestamp getIn() {
        return in;
    }

    public void setIn(Timestamp in) {
        this.in = in;
    }

    public Timestamp getOut() {
        return out;
    }

    public void setOut(Timestamp out) {
        this.out = out;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Assistance{" +
                "fail=" + fail +
                ", in=" + in +
                ", out=" + out +
                ", user='" + user + '\'' +
                '}';
    }
}
