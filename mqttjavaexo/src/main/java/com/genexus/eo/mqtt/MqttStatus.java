package com.genexus.eo.mqtt;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

public class MqttStatus {

    public UUID key;

    public boolean error = false;

    public String errorMessage = "";

    public MqttStatus(UUID key, boolean error, String errorMessage) {
        this.key = key;
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public MqttStatus() {};

    public MqttStatus(UUID key) {
        this.key = key;
    }

    public static MqttStatus Success(UUID key)
    {
        return new MqttStatus(key);
    }

    public static MqttStatus Fail(UUID key, Exception ex)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String sStackTrace = sw.toString();
        return new MqttStatus(key, true, sStackTrace);
    }
}
