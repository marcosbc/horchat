package com.horchat.horchat.irc;

import org.jibble.pircbot.PircBot;

public class IRCClient extends PircBot {
    /* Constants */
    /* Class constructor */
    public IRCClient() {
        // If nick already exists, change to a different one automatically
        this.setAutoNickChange(true);
    }
}
