package com.genexus.eo.mqtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class MqttClientMosquittoServerTest {

    private static final Logger log = LogManager.getLogger(MqttClientMosquittoServerTest.class);

    private static String brokerUrl = "test.mosquitto.org";
    private static String topic = "BigCheese/topic1";
    private static String payload = "This is a test";
    private static String gxproc = "";
    private static int qos = 2;

    @Test
    public void MosquittoServer_SingleConnection() {
        log.debug("Starting MosquittoServer_SingleConnection");

        MqttConfig config = getDefaultConfig();

        MqttStatus status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.disconnect(status.key);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);
    }

    @Test
    public void MosquittoServer_SingleConnection_Publish() {
        log.debug("Starting MosquittoServer_SingleConnection_Publish");

        MqttConfig config = getDefaultConfig();

        MqttStatus status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.publish(status.key, topic, payload, qos, false, 0);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.disconnect(status.key);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);
    }

    @Test
    public void MosquittoServer_IsConnected() {
        log.debug("Starting MosquittoServer_IsConnected");

        MqttConfig config = getDefaultConfig();

        boolean[] isConnected = new boolean[1];
        // Check a not existing client
        MqttStatus status = MqttClient.isConnected(UUID.randomUUID(), isConnected);
        assertFalse(isConnected[0]);
        assertTrue(status.error);
        assertEquals("Connection key not found", status.errorMessage);
        assertNotNull(status.key);

        // Connect
        status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        // Test if connected
        status = MqttClient.isConnected(status.key,isConnected);
        assertTrue(isConnected[0]);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        // Disconnect
        status = MqttClient.disconnect(status.key);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        // Test if not connected
        status = MqttClient.isConnected(status.key,isConnected);
        assertFalse(isConnected[0]);
        assertTrue(status.error);   // This is because after an explicit disconnect action, the connection key is removed from the connection pool.
        assertEquals("Connection key not found", status.errorMessage);
        assertNotNull(status.key);

    }

    @Test
    public void MosquittoServer_SingleConnection_Subscribe() {
        log.debug("Starting MosquittoServer_SingleConnection_Subscribe");

        MqttConfig config = getDefaultConfig();

        MqttStatus status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.subscribe(status.key, topic, gxproc, qos);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.disconnect(status.key);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

    }

    @Test
    public void MosquittoServer_SingleConnection_Subscribe_Unsubscribe() {
        log.debug("Starting MosquittoServer_SingleConnection_Subscribe_Unsubscribe");

        MqttConfig config = getDefaultConfig();

        MqttStatus status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.subscribe(status.key, topic, gxproc, qos);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.unsubscribe(status.key, topic);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.disconnect(status.key);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

    }

    @Test
    public void MosquittoServer_SingleConnection_Subscribe_Publish() throws InterruptedException {
        log.debug("Starting MosquittoServer_SingleConnection_Subscribe_Publish");

        MqttConfig config = getDefaultConfig();

        MqttStatus status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.subscribe(status.key, topic, gxproc, qos);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);
        log.debug("Subscribed to Mosquito server with key " + status.key.toString());

        Thread.sleep(2000);

        status = MqttClient.publish(status.key, topic, payload, qos, false, 0);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);
        log.debug("Published message to Mosquito server with key " + status.key.toString());

        log.debug("Waiting for message to arrive");
        Thread.sleep(2000);

        status = MqttClient.disconnect(status.key);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

    }

    private MqttConfig getDefaultConfig() {
        MqttConfig config = new MqttConfig();
        config.allowWildcardsInTopicFilters = true;
        config.port = 1883;
        config.sessionExpiryInterval = 3600;
        config.autoReconnectDelay = 0;
        config.connectionTimeout = 5;
        config.cleanSession = true;
        config.clientId = "BigCheese2021";
        config.sslConnection = false;
        return config;
    }

}
