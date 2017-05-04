package com.horchat.horchat.model;

import com.horchat.horchat.exception.ServerValidationException;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

public class Server implements Serializable {
    public static final int MAX_PORT = 65535;
    public static final int MIN_PORT = 1;
    public static final int E_HOSTNOTVALID = 11;
    public static final int E_PORTNOTINT = 12;
    public static final int E_PORTRANGE = 13;

    private String mHost;
    private int mPort;
    private String mPassword;
    // TODO: Implement
    private String charset;
    private boolean mSSL;
    private Session mSession;

    public Server(CharSequence host, CharSequence port, CharSequence password)
            throws ServerValidationException {
        setHost(host.toString());
        setPort(port.toString());
        setPassword(password.toString());
    }
    public Server(String host, String port, String password) throws ServerValidationException {
        setHost(host);
        setPort(port);
        setPassword(password);
    }
    public void setHost(String host) throws ServerValidationException {
        // Perform host validation
        // http://stackoverflow.com/questions/3114595/java-regex-for-accepting-a-valid-hostname-ipv4-or-ipv6-address
        boolean isValid = true;
        if (host.contains("/")) {
            isValid = false;
        }
        try {
            if (null == (new URI("my://userinfo@" + host + ":80").getHost())) {
                isValid = false;
            }
        } catch (URISyntaxException e) {
            isValid = false;
        }
        if (isValid) {
            mHost = host;
        } else {
            throw new ServerValidationException(E_HOSTNOTVALID);
        }
    }
    public String getHost() {
        return mHost;
    }
    public void setPort(String port) throws ServerValidationException {
        try {
            setPort(Integer.parseInt(port));
        } catch (NumberFormatException e) {
            throw new ServerValidationException(E_PORTRANGE);
        }
    }
    public void setPort(int port) throws ServerValidationException {
        if (port < MIN_PORT || port > MAX_PORT) {
            throw new ServerValidationException(E_PORTNOTINT);
        }
        mPort = port;
    }
    public int getPort() {
        return mPort;
    }
    public void setPassword(String password) throws ServerValidationException {
        // TODO: Add validations
        mPassword = password;
    }
    public String getPassword() {
        return mPassword;
    }
    public void setSSL(boolean ssl) {
        mSSL = ssl;
    }
    public boolean getSSL() {
        return mSSL;
    }
    public void setSession(Session session) {
        mSession = session;
    }
    public Session getSession() {
        return mSession;
    }
}
