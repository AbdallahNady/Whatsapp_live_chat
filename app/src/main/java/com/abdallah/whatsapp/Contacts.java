package com.abdallah.whatsapp;

public class Contacts {
    public String status, name, image;


    public Contacts() {
    }

    public Contacts(String status, String name, String image) {
        this.status = status;
        this.name = name;
        this.image = image;

    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


}