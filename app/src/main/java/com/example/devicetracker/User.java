package com.example.devicetracker;

import java.util.Objects;

public class User
{
    private String phoneNumber;
    private String Name;

    public User(String phoneNumber, String name) {
        this.phoneNumber = phoneNumber;
        Name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", Name='" + Name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getPhoneNumber().equals(user.getPhoneNumber()) &&
                getName().equals(user.getName());
    }
}
