package com.concurrent.rabbitmq;

import java.io.Serializable;

public class UserResponse {

    private String message;
    private User result;

    public UserResponse(String message, User result) {
        this.message = message;
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return result;
    }

    public void setUser(User user) {
        this.result = user;
    }
}

class User implements Serializable {
    private int idCliente;
    private int Cuenta;

    public User(int idCliente, int cuenta) {
        this.idCliente = idCliente;
        Cuenta = cuenta;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getCuenta() {
        return Cuenta;
    }

    public void setCuenta(int cuenta) {
        Cuenta = cuenta;
    }
}
