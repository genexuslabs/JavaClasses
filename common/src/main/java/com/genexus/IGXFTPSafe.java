package com.genexus;

public interface IGXFTPSafe {

    void lastError(int[] lastError);
    int getLastError();
    void connect(final String host, final String user, final String password);
    void disconnect();
    void status(String[] status);
    String getStatus();
    void get(final String source, final String target, final String mode);
    void put(final String source, final String target, final String mode);
    void delete(final String source);
    void mkdir(final String mkpath);
    void command(final String cmd);
    void setPassive(boolean passive);
}
