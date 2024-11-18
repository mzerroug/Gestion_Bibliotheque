package com.library.model;

public class CurrentUser {
    private static User currentUser;

    private CurrentUser() {
        // Private constructor to prevent instantiation
    }

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }
}