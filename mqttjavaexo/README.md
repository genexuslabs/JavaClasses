# MQTT External Object  

This repository holds the implementation of a GeneXus [External Object](https://wiki.genexus.com/commwiki/servlet/wiki?6148) that allows you to include MQTT events in your Knowledge Base.

=== This implementation is only for the Java generator ===

## How to use it

First, add to the Knowledge Base the dependencies needed for the external object, provided in the [assets](./assets/) folder of this repository. Unzip the [MQTTLib.zip](./assets/MQTTLib.zip) file and add the two contained files (`genexus-mqtt-*.jar`, `eclipse-paho-mqttv3-1.2.5.jar`) into your Knowledge Base. Make sure you set the property [Extract for Java Generator](https://wiki.genexus.com/commwiki/servlet/wiki?39499,Extract+for+Java+Generator+property) to **True** for all of these files.

In order to be able to import the external object, you need to add both jar files to your [classpath property](https://wiki.genexus.com/commwiki/servlet/wiki?9248,Classpath+property,). 

You also need to import the [MQTT_EXO.xpz](./assets/MQTT_EXO.xpz) file provided in this repository. This will import 3 different External Objects and a Domain.

The MQTT External Object is the one holds the method for publishing and subscribing to MQTT events. 

![](./res/MQTT_Exo.png)

The implementation is based on [Eclipse Paho](https://www.eclipse.org/paho/index.php?page=clients/java/index.php), *"an MQTT client library written in Java for developing applications."* 

### Connecting to an MQTT broker

The first thing you need to do, either for publishing or subscribing is to connect to the desired broker.

```genexus
&mqttStatus = MQTT.Connect(&url, &mqttConfig)
```

Where `&url` is the URL of the broker and `&mqttConfig` is an instance of the also provided `MqttConfig` External Object.  
The `MqttConfig` External Object has the needed properties to connect to your broker, whether you're using user and password or a certificate. Here are the properties that hold a default value.

Name|Default value
---|---
Port|1883
KeepAlive|0
ConnectionTimeout|5
MQTTConnectionName|mqtt_connection1
SSLConnection|false
ProtocolVersion|0
CleanSession|true
AllowWildcardsInTopicFilters|false
AutoReconnectDelay|5
SessionExpiryInterval|0

![](./res/MqttConfig.png)

### Publishing messages

The publish method receives a GUID, which is the Key of the `MqttStatus` after connecting, a topic, the message itself, the [Quality of Service](https://assetwolf.com/learn/mqtt-qos-understanding-quality-of-service) (there's a domain called MqttQoS), a boolean indicating if the message should be retained and a numeric stating the expiry of the message (in seconds)

```genexus
&mqttStatus = MQTT.Publish(&mqttguid,&topic, &message, MqttQoS.AtMostOnce, true,30)
```

Again, you'll get an instance of `MqttStatus` where you can check if everything went well.

### Subscribing to topics

To subscribe you need to call the Subscribe method which also receives the GUID of the connection, the topic you wish to subscribe to, the name of the Procedure that will be called once a message arrives, and again, the Quality of Service.

```genexus
&mqttStatus = MQTT.Subscribe(&mqttguid,&topic,"SaveMessage",MqttQoS.AtLeastOnce)
```

The GeneXus procedure to be called when a message arrives must comply with the following requirements.
1) Its [Main program property](https://wiki.genexus.com/commwiki/servlet/wiki?7407) must be set to **True**
2) The parm rule must be exactly as follows. First, a Varchar that will be the topic, the second parameter (also Varchar) will be the message body itself, and a third parameter (DateTime) that's the timestamp when the message was received by the client. This DateTime is generated on the subscriber.

```genexus
parm(in:&topic,in:&message,in:&dateTime);
```

Once a message for the subscribed topic is received, the Procedure will be called asynchronously. You will not have a status back from the execution unless you write the code yourself in your procedure.

### Unsubscribing

You can also unsubscribe from a specific topic. All you need is the GUID of the connection and the topic you wish to unsubscribe to.

```genexus
&mqttStatus = MQTT.Unsubscribe(&mqttguid,&topic)
```

### Disconnecting

To disconnect an established connection all you have to do is sending the connection GUID.

```genexus
&mqttStatus = MQTT.Disconnect(&mqttguid)
```

There's also an `IsConnected` method that receives the GUID of the connection and a Boolean as an out parameter that will hold whether the connection is active or not.

```genexus
&mqttStatus = MQTT.IsConnected(&mqttguid,&connected)
```

In every case, the returned `&mqttStatus` is an instance of the `MqttStatus` External Object that holds a `Key` (GUID), an `Error` (Boolean) and an `ErrorMessage` (VarChar) in case Error is True.

![](./res/MqttStatus.png)


## Testing

There are a couple of unit tests that can be used for testing purposes. If you wish to test the implementation in your favourite Java IDE, make sure you copy from your GeneXus installation the gxclassR.jar, gxcommon.jar, gxwrapperjavax.jar, and gxwrappercommon.jar files to the [lib](./MQTTLib/lib) folder.

Also, there's an application called [MQTT Box](https://www.microsoft.com/en-us/p/mqttbox/9nblggh55jzg) which you can install from the Windows Store to test your solution.

## Running the client from a command line procedure

When using the MQTT client from a command line main procedure, the JVM will not end until all MQTT clients are disconnected. If you run your command line procedure and it never finishes, double check if you are leaving open connections to the MQTT server.

## Broker compatibility / support

Depending on the broker used, some specific configurations may be done.


### Connecting to a AWS IoT Core broker

When connecting to an [AWS Iot Core](https://docs.aws.amazon.com/iot/latest/developerguide/mqtt.html) broker, some considerations should be taken into account:

- Only [MQTT 3.1.1](https://docs.aws.amazon.com/iot/latest/developerguide/mqtt.html) version is supported.
- The [port for connection](https://docs.aws.amazon.com/iot/latest/developerguide/protocols.html) is 8883, using SSL.
- When connecting the AWS broker, a CA certificate, a client certificate, and a client private key must be obtained from AWS and provided in the `MqttConfig` options.
- Only [QoS levels 0 and 1](https://docs.aws.amazon.com/iot/latest/developerguide/mqtt.html#mqtt-qos) are supported.
- Many times an action (connect, publish, subscribe) is rejected because of missing permissions in the AWS policies related to the thing or the authenticated role.
