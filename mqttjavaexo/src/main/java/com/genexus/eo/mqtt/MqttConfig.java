package com.genexus.eo.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

public class MqttConfig {

    public int port = 1883;
    public int bufferSize = 8192;
    public int keepAlive = 0;
    public int connectionTimeout = 5;
    private int waitTimeout = 15;
    public String userName;
    public String password;
    public String mqttConnectionName = "mqtt_connection1";
    public String clientId;
    public boolean sslConnection;
    public String caCertificate;
    public String clientCertificate;
    public String privateKey;
    public String clientCertificatePassphrase;
    public int protocolVersion = 0;
    public boolean cleanSession = true;
    public boolean allowWildcardsInTopicFilters;
    public int autoReconnectDelay = 5;
    public int sessionExpiryInterval = 0;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMqttConnectionName() {
        return mqttConnectionName;
    }

    public void setMqttConnectionName(String mqttConnectionName) {
        this.mqttConnectionName = mqttConnectionName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isSslConnection() {
        return sslConnection;
    }

    public void setSslConnection(boolean sslConnection) {
        this.sslConnection = sslConnection;
    }

    public String getCaCertificate() {
        return caCertificate;
    }

    public void setCaCertificate(String caCertificate) {
        this.caCertificate = caCertificate;
    }

    public String getClientCertificate() {
        return clientCertificate;
    }

    public void setClientCertificate(String clientCertificate) {
        this.clientCertificate = clientCertificate;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getClientCertificatePassphrase() {
        return clientCertificatePassphrase;
    }

    public void setClientCertificatePassphrase(String clientCertificatePassphrase) {
        this.clientCertificatePassphrase = clientCertificatePassphrase;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public boolean isAllowWildcardsInTopicFilters() {
        return allowWildcardsInTopicFilters;
    }

    public void setAllowWildcardsInTopicFilters(boolean allowWildcardsInTopicFilters) {
        this.allowWildcardsInTopicFilters = allowWildcardsInTopicFilters;
    }

    public int getAutoReconnectDelay() {
        return autoReconnectDelay;
    }

    public void setAutoReconnectDelay(int autoReconnectDelay) {
        this.autoReconnectDelay = autoReconnectDelay;
    }

    public int getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public void setSessionExpiryInterval(int sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    public int getWaitTimeout() {
		return waitTimeout;
	}

//	public void setWaitTimeout(int waitTimeout) {
//		this.waitTimeout = waitTimeout;
//	}

	public String exportMqttConfig()
    {
        JSONObject json = new JSONObject(this);
        return json.toString();
    }

    public static MqttConfig importMqttConfig(String json) {
        try {
            return new ObjectMapper().readValue(json, MqttConfig.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new MqttConfig();
        }
    }
}
