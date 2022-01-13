

#include <M5Stack.h>
// Incluimos librería
#include <DHT.h>


int countX = 0;

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
int value = 0;
float t = 0;

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
#include <esp_task_wdt.h>
#define TWDT_TIMEOUT_S 1000
#define TASK_RESET_PERIOD_S 2

//Declaracion manejadores de los semaforos binarios
SemaphoreHandle_t xSemaphore1 = NULL;
SemaphoreHandle_t xSemaphore2 = NULL;
SemaphoreHandle_t xSemaphore3 = NULL;

//factor de conversion de microsegundos a segundos
#define uS_TO_S_FACTOR 1000000 //se salva como 32bits
//tiempo que el ESP32 estara dormido (en segundos)
#define TIME_TO_SLEEP 60
//#define uS_TO_S_FACTOR 1000000ULL //se salva como 64bits
//#define TIME_TO_SLEEP 5ULL

#define BUTTON_PIN_BITMASK 0x1000000004 // IO 2 y 36 activas

//Tomar datos de la temperatura
void tarea1(void *pvParameter)
{
  while (1) {
    Serial.println("Ejecutando tarea 1");
    //Codigo para sensor de temperatura

    t = dht.readTemperature();
    jsonBuffer["Temperatura"] = t;
    Serial.println(t);
    if (t > 25 && aux == 0) {
      M5.Speaker.tone(500, 10000);
      aux++;
    } else {
      M5.Speaker.end();
    }

    if (M5.BtnA.wasPressed()) {
      M5.Speaker.end();

    }
    xSemaphoreGive(xSemaphore1);
    vTaskDelay(3000 / portTICK_PERIOD_MS);
  }
}
//Tomar datos del estado de la puerta
void tarea2(void *pvParameter)
{
  while (1) {
    if (xSemaphoreTake( xSemaphore1, portMAX_DELAY) == pdTRUE) {
      Serial.println("Ejecutando        tarea 2");

      value = digitalRead(pinMagnetic);

      if (value == LOW && value != preview) {
        M5.Lcd.print("Cerrado");
        jsonBuffer["Puerta"] = "Cerrada";
        M5.Lcd.clear();


        mqttClient.beginMessage(topic_foto);
        mqttClient.print("Tomar foto");
        mqttClient.endMessage();


      } else if (value == HIGH && value != preview) {
        M5.Lcd.print("Abierto");
        jsonBuffer["Puerta"] = "Abierta";
        M5.Lcd.clear();


      }
      preview = value;

      char texto[200];
      serializeJson(jsonBuffer, texto); //paso del objeto “jsonbuffer" a texto para
      //transmitirlo
      udp.broadcastTo(texto, 1234); //se envía por el puerto 1234 el JSON
      
      xSemaphoreGive(xSemaphore2);
      vTaskDelay(3000 / portTICK_PERIOD_MS);
    }

  }
}

//Mandar los datos por MQTT
void tarea3(void *pvParameter)
{
  while (1) {
    if (xSemaphoreTake( xSemaphore2, portMAX_DELAY) == pdTRUE) {
      Serial.println("Ejecutando                tarea 3");

      mqttClient.poll();
      unsigned long currentMillis = millis();
      if (currentMillis - previousMillis >= interval) {
        // save the last time a message was sent
        previousMillis = currentMillis;

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
      xSemaphoreGive(xSemaphore3);
      vTaskDelay(3000 / portTICK_PERIOD_MS);
    }

  }
}
//Tomar los datos del sensor de vibracion
void tarea4(void *pvParameter)
{
  while (1) {
    if (xSemaphoreTake( xSemaphore3, portMAX_DELAY) == pdTRUE) {
      Serial.println("Ejecutando                       tarea 4");


      int vibraADC = analogRead(VIBRA_PIN);
      float vibraV = vibraADC / 1023.0 * 5.0;
      Serial.println(vibraV); // Print the voltage.

      digitalWrite(23, HIGH);
      if (countervibra == 0) {
        if (vibraV > 5) {
          //digitalWrite(pinLEDMagnetic, HIGH);
          countervibra = 10;

        } else {
          //digitalWrite(pinLEDMagnetic, LOW);
        }
      }
      if (countervibra != 0) {
        countervibra--;
      }
      custom_turnOff(value, vibraV);
      vTaskDelay(3000 / portTICK_PERIOD_MS);
    }

  }
}

void setup() {
  //Inicializar el M5Stack y el sensor DHT11
  dht.begin();
  M5.begin();

  esp_task_wdt_init(TWDT_TIMEOUT_S, false);

  //configurar pin como entrada con resistencia pull-up interna
  pinMode(pinMagnetic, INPUT_PULLUP);
  pinMode(pinLEDMagnetic, OUTPUT);
  pinMode(pinLEDVibra, OUTPUT);

  //Encendemos permanentmente el LED de vibracion
  digitalWrite(pinLEDMagnetic, HIGH);

  //Configurar el wifi y la comunicación UDP
  setTime (9, 15, 0, 7, 10, 2018); //hora minuto segundo dia mes año
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  if (WiFi.waitForConnectResult() != WL_CONNECTED) {
    while (1) {
      delay(1000);
    }
  }
  if (udp.listen(1234)) {
    udp.onPacket([](AsyncUDPPacket packet) {
    });
  }

  //Configurar el cliente MQTT
  mqttClient.setId("123479567"); // Obligatorio cambiarlo
  Serial.print("Attempting to connect to the MQTT broker: ");
  Serial.println(broker);
  if (!mqttClient.connect(broker, port)) {
    Serial.print("MQTT connection failed! Error code = ");
    Serial.println(mqttClient.connectError());
    while (1);
  }
  Serial.println("You're connected to the MQTT broker!");
  Serial.println();

  //Creación de semáforos
  xSemaphore1 = xSemaphoreCreateBinary();
  xSemaphore2 = xSemaphoreCreateBinary();
  xSemaphore3 = xSemaphoreCreateBinary();
  // Creacion de tareas
  xTaskCreatePinnedToCore(&tarea1, "tarea1", 2048, NULL, 1, NULL, 0);
  xTaskCreatePinnedToCore(&tarea2, "tarea2", 2048, NULL, 1, NULL, 0);
  xTaskCreatePinnedToCore(&tarea3, "tarea3", 2048, NULL, 1, NULL, 0);
  xTaskCreatePinnedToCore(&tarea4, "tarea4", 2048, NULL, 1, NULL, 0);

  print_wakeup_reason();

  //programacion del temporizador del RTC para que despierte

  esp_sleep_enable_timer_wakeup(TIME_TO_SLEEP * uS_TO_S_FACTOR);

  esp_sleep_enable_ext1_wakeup(BUTTON_PIN_BITMASK,
                               ESP_EXT1_WAKEUP_ANY_HIGH);

  //delay(10000);
  //esp_deep_sleep_start(); //duerme al ESP32 (modo SLEEP)







}

void print_wakeup_reason() {
  esp_sleep_wakeup_cause_t wakeup_reason;
  wakeup_reason = esp_sleep_get_wakeup_cause();
  Serial.println("");
  Serial.println("");
  Serial.println("EXT1 Test");
  switch (wakeup_reason)
  {
    case ESP_SLEEP_WAKEUP_EXT0 : Serial.println("Wakeup caused by external signal using RTC_IO"); break;
    case ESP_SLEEP_WAKEUP_EXT1 : {
        Serial.print("Wakeup caused by external signal using RTC_CNTL ");
        uint64_t a = esp_sleep_get_ext1_wakeup_status();
        if (a == 0x1000000000) {
          Serial.println("Despertado por el sensor de vibración");

        }
        else if (a == 4)Serial.println("Despertado por el sensor magnético");

        Serial.println();
        break;
      }
    case ESP_SLEEP_WAKEUP_TIMER : Serial.println("Wakeup caused by timer"); break;
    case ESP_SLEEP_WAKEUP_TOUCHPAD : Serial.println("Wakeup caused by touchpad"); break;
    case ESP_SLEEP_WAKEUP_ULP : Serial.println("Wakeup caused by ULP program"); break;
    case ESP_SLEEP_WAKEUP_UNDEFINED : Serial.println("Causa de despertar no producida por deep sleep"); break;
    default : Serial.println("Wakeup was not caused by deep sleep"); break;
  }
}

void custom_turnOff(int doorState, float vibration) {

  if (doorState == 0 && vibration < 1) {
    countX++;
  }

  if (countX == 3) {
    esp_deep_sleep_start(); //duerme al ESP32 (modo SLEEP)
  }

  Serial.println(countX);
}


void loop() {}
