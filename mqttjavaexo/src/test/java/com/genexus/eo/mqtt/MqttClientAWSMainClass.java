package com.genexus.eo.mqtt;

public class MqttClientAWSMainClass {

    public static void main( String args[] )
    {
        MqttClient mqttClient = new MqttClient();
        MqttConfig config = getDefaultConfig();
        String brokerUrl = "a1ij4bvp7hpdei-ats.iot.us-east-1.amazonaws.com";
        MqttStatus status = mqttClient.connect(brokerUrl, config);
        System.out.println("Error:" + status.error);
        System.out.println("Message:" +  status.errorMessage);
        System.out.println("Key:" + status.key);
        mqttClient.disconnect(status.key);

        config.clientId="holanda2";
        status = mqttClient.connect(brokerUrl, config);
        System.out.println("Error2:" + status.error);
        System.out.println("Message2:" +  status.errorMessage);
        System.out.println("Key2:" + status.key);

        mqttClient.disconnect(status.key);
    }


    private static MqttConfig getDefaultConfig() {
        MqttConfig config = new MqttConfig();
        config.allowWildcardsInTopicFilters = true;
        config.port = 8883;
        config.sessionExpiryInterval = 20;
        config.autoReconnectDelay = 0;
        config.connectionTimeout = 5;
        config.cleanSession = true;
        config.clientId = "BigCheese-2021";
        config.sslConnection = true;
        config.privateKey = "C:\\temp\\mqtttest-private.pem.key";
        config.caCertificate = "C:\\temp\\AmazonRootCA1.pem";
        config.clientCertificate = "C:\\temp\\mqtttest-certificate.pem.crt";
        return config;
    }
}
