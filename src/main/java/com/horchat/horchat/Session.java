package com.horchat.horchat;

import java.io.Serializable;

public class Session implements Serializable {
    private Account account;
    public Session(Account account) {
        this.account = account;
    }
    public Account getAccount() {
        return account;
    }
}
