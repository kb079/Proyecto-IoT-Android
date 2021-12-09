package com.myfridge.app.comunication;

public class Mqtt {
    public static final String TAG = "MQTT";
    public static final String topicRoot = "myfridge/sensor/";
    public static final int qos = 1;
    public static final String broker = "tcp://broker.hivemq.com:1883";
    public static final String clientId = "MyFridge";

    //TOPICS
    public static final String topicTemperature = "temperatura";
    public static final String topicDoorState = "magnetico";
    public static final String topicRFID = "rfid";
}