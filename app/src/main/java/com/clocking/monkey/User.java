package com.clocking.monkey;

import com.google.gson.Gson;

public class User {
    String email;
    String name;
    String first_lastname;
    String second_lastname;
    Boolean active;

    public User(String email, String name, String first_lastname, String second_lastname) {
        this.email = email;
        this.name = name;
        this.first_lastname = first_lastname;
        this.second_lastname = second_lastname;
        this.active = false;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirst_lastname() {
        return first_lastname;
    }

    public void setFirst_lastname(String first_lastname) {
        this.first_lastname = first_lastname;
    }

    public String getSecond_lastname() {
        return second_lastname;
    }

    public void setSecond_lastname(String second_lastname) {
        this.second_lastname = second_lastname;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String toJson(){
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }

    public static User fromJSON(String json){
        Gson gson = new Gson();
        return gson.fromJson(json, User.class);
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", first_lastname='" + first_lastname + '\'' +
                ", second_lastname='" + second_lastname + '\'' +
                '}';
    }
}
