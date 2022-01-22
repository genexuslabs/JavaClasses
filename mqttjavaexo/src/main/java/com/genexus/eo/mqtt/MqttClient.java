package com.genexus.eo.mqtt;

import java.util.HashMap;
import java.util.UUID;

import com.genexus.GXRuntime;
import com.genexus.eo.utils.CertificateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import javax.net.ssl.SSLSocketFactory;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.*;


public class MqttClient {

    private static final Logger log = LogManager.getLogger(MqttClient.class);

    private static HashMap<UUID, MqttConnection> connections = new HashMap<UUID, MqttConnection>();
    private static HashMap<String, String> subscribedGXProcs = new HashMap<String, String>();

    public static MqttStatus connect(String url, MqttConfig config) {

        UUID key = UUID.randomUUID();
        MqttStatus status = new MqttStatus(key);

        try {
            MqttConnectOptions options = buildMqttConnectOptions(config);
            MqttClientListener clientListener = new MqttClientListener(subscribedGXProcs);

            String protocol = getConnectionProtocol(config);
            String brokerUrl = protocol + url + ":" + config.port;

            MqttConnection connection = new MqttConnection(config.clientId, brokerUrl, options, clientListener, config.allowWildcardsInTopicFilters, config.getWaitTimeout());
            connection.connect();
            connections.put(key, connection);
            status.error = false;
        } catch (Exception e) {
            e.printStackTrace();
            status.errorMessage = e.getMessage();
            status.error = true;
            GXRuntime.setExitCode(1);
        }
        return status;
    }

    public static MqttStatus disconnect(UUID key) {
        MqttStatus status = new MqttStatus(key);
        MqttConnection connection = getConnection(key);
        if (connection==null) {
            status.errorMessage = "Connection key not found";
            status.error = true;
            return status;
        }
        try {
            connection.disconnect();
            connections.remove(key);
            status.error = false;
        } catch (MqttException e) {
            e.printStackTrace();
            status.errorMessage = e.getMessage();
            status.error = true;
        }
        return status;
    }

    public static MqttStatus subscribe(UUID key, String topic, String gxproc, int qos) {
        MqttStatus status = new MqttStatus(key);
        MqttConnection connection = getConnection(key);
        if (connection==null) {
            status.errorMessage = "Connection key not found";
            status.error = true;
            return status;
        }
        if (!connection.allowWildcardsInTopicFilters && (topic.contains("*") || topic.contains("+") || topic.contains("#"))) {
            status.errorMessage = "Wildcards not allowed for this instance";
            status.error = true;
            return status;
        }
        try {
            connection.subscribe(topic, qos);
            subscribedGXProcs.put(topic, gxproc);
            status.error = false;
        } catch (MqttException e) {
            e.printStackTrace();
            status.errorMessage = e.getMessage();
            status.error = true;
        }
        return status;
    }

    public static MqttStatus unsubscribe(UUID key, String topic) {
        MqttStatus status = new MqttStatus(key);
        MqttConnection connection = getConnection(key);
        if (connection==null) {
            status.errorMessage = "Connection key not found";
            status.error = true;
            return status;
        }
        try {
            if (subscribedGXProcs.containsKey(topic)) {
                connection.unsubscribe(topic);
                subscribedGXProcs.remove(topic);
                status.error = false;
            } else {
                status.errorMessage = "Not subscribed to that topic";
                status.error = true;
            }

        } catch (MqttException e) {
            e.printStackTrace();
            status.errorMessage = e.getMessage();
            status.error = true;
        }
        return status;
    }

    public static MqttStatus publish(UUID key, String topic, String payload, int qos, boolean retainMessage, int messageExpiryInterval) {
        MqttStatus status = new MqttStatus(key);
        MqttConnection connection = getConnection(key);
        if (connection==null) {
            status.errorMessage = "Connection key not found";
            status.error = true;
            return status;
        }
        try {
            connection.publish(topic, qos, payload.getBytes(), retainMessage, messageExpiryInterval);
        } catch (MqttException e) {
            e.printStackTrace();
            status.errorMessage = e.getMessage();
            status.error = true;
        }
        return status;
    }

    public static  MqttStatus isConnected(UUID key, boolean[] connected) {
        MqttStatus status = new MqttStatus(key);
        MqttConnection connection = getConnection(key);
        if (connection==null) {
            status.errorMessage = "Connection key not found";
            status.error = true;
            connected[0] = false;
            return status;
        }
        try {
            connected[0] = connection.isConnected();
            status.error = false;
        } catch (MqttException e) {
            e.printStackTrace();
            status.errorMessage = e.getMessage();
            status.error = true;
        }
        return status;
    }

    private static String getConnectionProtocol(MqttConfig config) {
        if (config.isSslConnection())
            return "ssl://";
        else
            return "tcp://";
    }

    private static MqttConnection getConnection(UUID key){
        return connections.get(key);
    }

    private static MqttConnectOptions buildMqttConnectOptions(MqttConfig config) throws Exception {
        MqttConnectOptions options = new MqttConnectOptions();

        options.setConnectionTimeout(config.connectionTimeout);
        options.setMaxReconnectDelay(config.autoReconnectDelay);
        options.setCleanSession(config.cleanSession);

        switch (config.protocolVersion){
            case 0:     options.setMqttVersion(MQTT_VERSION_DEFAULT); break;
            case 310:   options.setMqttVersion(MQTT_VERSION_3_1); break;
            case 311:   options.setMqttVersion(MQTT_VERSION_3_1_1); break;
            default:
                log.warn("Invalid or unsupported MQTT protocol version " + config.protocolVersion + ", using default value");
        }
        //options.setKeepAliveInterval(config.keepAlive); This one breaks Mosquitto integration

        if(config.password != null )
            options.setPassword(config.password.toCharArray());
        if(config.userName != null)
            options.setUserName(config.userName);
        if (config.autoReconnectDelay>0)
            options.setAutomaticReconnect(true);

        if (config.isSslConnection()) {
            SSLSocketFactory socketFactory = CertificateUtils.getSSLSocketFactory(
                    config.caCertificate, config.clientCertificate, config.privateKey, config.clientCertificatePassphrase);
            options.setSocketFactory(socketFactory);
        }
        return options;
    }
}
