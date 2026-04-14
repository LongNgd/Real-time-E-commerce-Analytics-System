package com.vieguys.authservice.model;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String email;
    private String name;
    private String role;

}