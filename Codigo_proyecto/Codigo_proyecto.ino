#include <M5Stack.h>
// Incluimos librería
#include <DHT.h>

#include "WiFi.h"
#include "AsyncUDP.h"
#include <TimeLib.h>
#include <ArduinoJson.h>
#include <ArduinoMqttClient.h>
//#include <WiFi.h> // Añade esta línea
//char pass[] = "Reemplaza_por_contraseña";
WiFiClient wifiClient;
MqttClient mqttClient(wifiClient);
const char broker[] = "broker.hivemq.com";
int port = 1883;
const char topic_temp[] = "myfridge/sensor/temperatura";
const char topic_magn[] = "myfridge/sensor/magnetico";
const char topic_foto[] = "myfridge/sensor/foto";
const long interval = 1000;
unsigned long previousMillis = 0;
int count = 0;
int aux = 0;

const char * ssid = "TP-LINK_6CAE";
const char * password = "41422915";
//const char * ssid = "vodafoneBA2375";
//const char * password = "C7T453DHKNY46RNY";
AsyncUDP udp;
StaticJsonDocument<200> jsonBuffer; //tamaño maximo de los datos

// Definimos el pin digital donde se conecta el sensor
#define DHTPIN 26
#define pinLEDVibra 23
const int VIBRA_PIN = 36; // Piezo output

// Dependiendo del tipo de sensor
#define DHTTYPE DHT11

// Inicializamos el sensor DHT11
DHT dht(DHTPIN, DHTTYPE);

const int pinMagnetic = 2;
const int pinLEDMagnetic = 5;

int preview = LOW;
int preview2 = LOW;

int countervibra = 0;

void setup() {

  // Inicializamos comunicación serie
  //Serial.begin(115200);

  // Comenzamos el sensor DHT
  dht.begin();

  M5.begin();  //Init M5Core.  初始化 M5Core
  M5.Power.begin(); //Init Power module.  初始化电源模块
  /* Power chip connected to gpio21, gpio22, I2C device
    Set battery charging voltage and current
    If used battery, please call this function in your project */

  //configurar pin como entrada con resistencia pull-up interna
  pinMode(pinMagnetic, INPUT_PULLUP);
  pinMode(pinLEDMagnetic, OUTPUT);
  pinMode(pinLEDVibra, OUTPUT);

  setTime (9, 15, 0, 7, 10, 2018); //hora minuto segundo dia mes año
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  if (WiFi.waitForConnectResult() != WL_CONNECTED) {
    //Serial.println("WiFi Failed");
    while (1) {
      delay(1000);
    }
  }
  if (udp.listen(1234)) {
    //Serial.print("UDP Listening on IP: ");
    //Serial.println(WiFi.localIP());
    udp.onPacket([](AsyncUDPPacket packet) {
      //Serial.write(packet.data(), packet.length());
      //Serial.println();
    });
  }

  // attempt to connect to Wifi network:
  //You can provide a unique ID, if not set the library uses Arduino-millis()
  //Each client must have a unique client ID
  mqttClient.setId("123479567"); // Obligatorio cambiarlo
  // You can provide a username and password for authentication
  // mqttClient.setUsernamePassword("username", "password");
  Serial.print("Attempting to connect to the MQTT broker: ");
  Serial.println(broker);
  if (!mqttClient.connect(broker, port)) {
    Serial.print("MQTT connection failed! Error code = ");
    Serial.println(mqttClient.connectError());
    while (1);
  }
  Serial.println("You're connected to the MQTT broker!");
  Serial.println();


}

void loop() {
  //Codigo para sensor de temperatura

  //digitalWrite(pinLEDTemperature, HIGH);
  // Leemos la humedad relativa
  float h = dht.readHumidity();
  // Leemos la temperatura en grados centígrados (por defecto)
  float t = dht.readTemperature();
  Serial.println(t);
  // Leemos la temperatura en grados Fahreheit
  float f = dht.readTemperature(true);
  

  if(t > 25 && aux == 0){
    M5.Speaker.tone(500, 10000);
    aux++;
  } else{
    M5.Speaker.end();
  }

  if(M5.BtnA.wasPressed()){
     M5.Speaker.end();
     
  }


  // Read Piezo ADC value in, and convert it to a voltage
  int vibraADC = analogRead(VIBRA_PIN);
  float vibraV = vibraADC / 1023.0 * 5.0;
  Serial.println(vibraV); // Print the voltage.

digitalWrite(23, HIGH);
  if (countervibra == 0) {
    if (vibraV > 5) {
      digitalWrite(pinLEDMagnetic, HIGH);
      countervibra = 10;

    } else {
      digitalWrite(pinLEDMagnetic, LOW);
    }
  }
  if (countervibra != 0) {
    countervibra--;
  }

  float hif = dht.computeHeatIndex(f, h);
  // Calcular el índice de calor en grados centígrados
  float hic = dht.computeHeatIndex(t, h, false);

  M5.Lcd.setCursor(0, 0);
  M5.Lcd.println(t);
  jsonBuffer["Temperatura"] = t;

  int value = digitalRead(pinMagnetic);

  if (value == LOW && value != preview) {

    //M5.Lcd.setCursor(0,0);
    M5.Lcd.print("Cerrado");
    jsonBuffer["Puerta"] = "Cerrada";
    //digitalWrite(pinLEDMagnetic, LOW);
    M5.Lcd.clear();

    mqttClient.beginMessage(topic_foto);
    mqttClient.print("Tomar foto");
    mqttClient.endMessage();


  } else if (value == HIGH && value != preview) {

    //M5.Lcd.setCursor(0,0);
    M5.Lcd.print("Abierto");
    jsonBuffer["Puerta"] = "Abierta";
    //digitalWrite(pinLEDMagnetic, HIGH);
    M5.Lcd.clear();

  }
  preview = value;
  //delay(1000);
  char texto[200];
  serializeJson(jsonBuffer, texto); //paso del objeto “jsonbuffer" a texto para
  //transmitirlo
  udp.broadcastTo(texto, 1234); //se envía por el puerto 1234 el JSON

  //-----------------------------------------------------------------------------------
  //-----------------------------------------------------------------------------------
  //CODIGO DEL PROTOCOLO MQTT
  //call poll() regularly to allow the library to receive MQTT messages and
  //send MQTT keep alives which avoids being disconnected by the broker
  mqttClient.poll();
  //avoid having delays in loop, we'll use the strategy from BlinkWithoutDelay
  //see: File -> Examples -> 02.Digital -> BlinkWithoutDelay for more info
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= interval) {
    // save the last time a message was sent
    previousMillis = currentMillis;
    /*Serial.print("Sending message to topic: ");
      Serial.println(topic);
      Serial.print("echo ");
      Serial.println("Bye");*/

    //send message, the Print interface can be used to set the message content
    mqttClient.beginMessage(topic_temp);
    mqttClient.print(t);
    mqttClient.endMessage();
    Serial.println();

    mqttClient.beginMessage(topic_magn);

    mqttClient.print(value);
    mqttClient.endMessage();
    Serial.println();
    count++;
  }

  delay(1000);
}
