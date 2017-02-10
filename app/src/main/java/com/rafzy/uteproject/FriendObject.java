package com.rafzy.uteproject;

/**
 * Created by root on 2/9/17.
 */
public class FriendObject {
    public FriendObject(String name, String number) {
        this.number = number;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    private String name;
    private String number;
    @Override
    public String toString(){
        return name + " " +number;
    }
}
