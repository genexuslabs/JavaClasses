package com.genexus.eo.mqtt;

import com.genexus.eo.utils.GeneXusConfigUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class MqttClientListener implements MqttCallback {

    private static final Logger log = LogManager.getLogger(MqttClientListener.class);
    private HashMap<String, String> subscribedGXProcs = new HashMap<String, String>();

    public MqttClientListener(HashMap<String, String> subscribedGXProcs) {
        this.subscribedGXProcs = subscribedGXProcs;
    }

    /**
     * Called when connection to MQTT server is lost
     * @see MqttCallback#connectionLost(Throwable)
     */
    public void connectionLost(Throwable cause) {
        log.warn("Connection to server lost!\n" + cause);
        cause.printStackTrace();
    }

    /**
     * Called when a message has been delivered to the
     * server. The token passed in here is the same one
     * that was passed to or returned from the original call to publish.
     * This allows applications to perform asynchronous
     * delivery without blocking until delivery completes.
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.debug("Message delivery completed: " + token.toString());
    }

    /**
     * Called when a message arrives from the server that matches any subscription made by the client
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    public void messageArrived(String topic, MqttMessage message) throws MqttException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Date timestamp = new Date(System.currentTimeMillis());
        String body = new String(message.getPayload());
        log.debug("Message arrived at time:\t" +timestamp + "  Topic:\t" + topic + "  Message:\t" + body + "  QoS:\t" + message.getQos());
        for (String gxprocToCall : getGXProcsUsingTopicFilters(topic)) {
            invoke(gxprocToCall, topic, body, timestamp);
        }
    }

    private void invoke(String gxproc, String topic, String message, Date timestamp) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        if (gxproc == null || gxproc.isEmpty()) {
            log.warn("No gxproc defined for callback, ignoring invoke function");
            return;
        }
        String className = getBasePackageNamePath() + gxproc.toLowerCase();
        String methodName = "execute";
        Class<?> c = Class.forName(className);
        Constructor<?> ctor = c.getConstructor();
        Object objectInstance = ctor.newInstance(new Object[] {});
        Class[] parameterTypes = { String.class, String.class, java.util.Date.class };
        Method method = c.getDeclaredMethod(methodName, parameterTypes);
        if (method==null)
            log.error("Method " + methodName + " not found in class " + className);
        else {
            log.debug("Invoking " + className + "." + methodName + "() with parameters [ " + topic + " , " + message + " , " + timestamp + " ] ");
            method.invoke(objectInstance, new Object[]{topic, message, timestamp});
        }
    }

    private String getBasePackageNamePath() {
        String packageName = GeneXusConfigUtils.getDefaultPackageName();
        return packageName.isEmpty()? "" : packageName + ".";
    }

    private List<String> getGXProcsUsingTopicFilters(String topic) {
        List<String> list = new ArrayList<String>();
        for (String key : subscribedGXProcs.keySet()) {
            String regexPattern = key;
            if (key.endsWith("#")) {
                regexPattern = regexPattern.replace("#", ".*");
            }
            if (key.contains("+")) {
                regexPattern = regexPattern.replace("+", "[a-zA-Z0-9]*");
            }
            Pattern regex = Pattern.compile(regexPattern);
            if (regex.matcher(topic).matches())
                list.add(subscribedGXProcs.get(key));
        }
        return list;
    }

}
