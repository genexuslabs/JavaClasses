package com.genexus.eo.mqtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.sql.Timestamp;

class MqttConnection {

    private static final Logger log = LogManager.getLogger(MqttConnection.class);

    private MqttClient 			client;
    private String 				brokerUrl;
    private MqttConnectOptions conOpt;

    public boolean allowWildcardsInTopicFilters;

    /**
     * Constructs an instance of the sample client wrapper
     * @param clientId the client id to connect with
     * @param brokerUrl the url of the server to connect to
     * @param options the connection configuration
     * @param clientListener the listener for handling MQTT callbacks
     * @param allowWildcardsInTopicFilters true if wildcards are allowed in topic filters
     * @throws MqttException
     */
    public MqttConnection(String clientId, String brokerUrl, MqttConnectOptions options, MqttClientListener clientListener, boolean allowWildcardsInTopicFilters, int waitTimeout) throws MqttException {

        try {
            this.brokerUrl = brokerUrl;
            this.conOpt = options;
            this.allowWildcardsInTopicFilters = allowWildcardsInTopicFilters;
            this.client = new MqttClient(this.brokerUrl, clientId, new MemoryPersistence());
            this.client.setTimeToWait(waitTimeout * 1000);
            this.client.setCallback(clientListener);

        } catch (MqttException e) {
            e.printStackTrace();
            log.error("Unable to set up client: "+e.toString());
            throw e;
        }
    }

    /**
     * @return true if the client is connected, false otherwhise
     * @throws MqttException
     */
    public boolean isConnected() throws MqttException {
        return client.isConnected();
    }


    /**
     * Connects to the MQTT server using current connection settings
     * @throws MqttException
     */
    public void connect() throws MqttException {
        log.debug("Connecting to "+brokerUrl + " with client ID "+client.getClientId());
        client.connect(conOpt);
        log.debug("Connected");
    }


    /**
     * Disconnects from the MQTT server
     * @throws MqttException
     */
    public void disconnect() throws MqttException {
        client.disconnect();
        log.debug("Disconnected");
    }

    /**
     * Publish / send a message to an MQTT server
     * @param topicName the name of the topic to publish to
     * @param qos the quality of service to delivery the message at (0,1,2)
     * @param payload the set of bytes to send to the MQTT server
     * @throws MqttException
     */
    public void publish(String topicName, int qos, byte[] payload, boolean retainMessage, int messageExpiryInterval) throws MqttException {

        String time = new Timestamp(System.currentTimeMillis()).toString();
        log.debug("Publishing at: "+time+ " to topic \""+topicName+"\" qos "+qos);
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(retainMessage);
        // Send the message to the server, control is not returned until
        // it has been delivered to the server meeting the specified quality of service.
        client.publish(topicName, message);
    }

    /**
     * Subscribe to a topic on an MQTT server
     * Once subscribed this method waits for the messages to arrive from the server
     * that match the subscription.
     * The QoS specified is the maximum level that messages will be sent to the client at.
     * For instance if QoS 1 is specified, any messages originally published at QoS 2 will
     * be downgraded to 1 when delivering to the client but messages published at 1 and 0
     * will be received at the same level they were published at..
     * @param topicName to subscribe to (can be wild carded)
     * @param qos the maximum quality of service to receive messages at for this subscription
     * @throws MqttException
     */
    public void subscribe(String topicName, int qos) throws MqttException {
        log.debug("Subscribing to topic \""+topicName+"\" qos "+qos);
        client.subscribe(topicName, qos);
    }

    /**
     * Unsubscribe from a topic on an MQTT server
     * @param topicName to subscribe to (can be wild carded)
     * @throws MqttException
     */
    public void unsubscribe(String topicName) throws MqttException {
        log.debug("Unsubscribing from topic \""+topicName);
        client.unsubscribe(topicName);
    }

}
