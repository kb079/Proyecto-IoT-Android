#define BLANCO 0XFFFF
#define NEGRO 0
#define ROJO 0xF800
#define VERDE 0x07E0
#define AZUL 0x001F
#include <SPI.h>
#include <MFRC522.h>
#include <M5Stack.h>

int value = 0;
bool pressed = false;

#define RST_PIN 2 //Pin 9 para el reset del RC522 no es necesario conctarlo
#define SS_PIN 21 //Pin 10 para el SS (SDA) del RC522
#define WAKEUP_PIN 5 // Pin 36 para despertar a la placa cuando se encienda la nevera

MFRC522 mfrc522(SS_PIN, RST_PIN); ///Creamos el objeto para el RC522
MFRC522::StatusCode status; //variable to get card status

#include <ArduinoMqttClient.h>
#include <WiFi.h>
#if defined(ARDUINO_SAMD_MKRWIFI1010) || defined(ARDUINO_SAMD_NANO_33_IOT) || defined(ARDUINO_AVR_UNO_WIFI_REV2)
#include <WiFiNINA.h>
#elif defined(ARDUINO_SAMD_MKR1000)
#include <WiFi101.h>
#elif defined(ARDUINO_ESP8266_ESP12)
#include <ESP8266WiFi.h>
#endif

//#include "arduino_secrets.h"
///////please enter your sensitive data in the Secret tab/arduino_secrets.h
char ssid[] = "TP-LINK_6CAE";        // your network SSID (name)
char pass[] = "41422915";    // your network password (use for WPA, or use as key for WEP)

// To connect with SSL/TLS:
// 1) Change WiFiClient to WiFiSSLClient.
// 2) Change port value from 1883 to 8883.
// 3) Change broker value to a server with a known SSL/TLS root certificate
//    flashed in the WiFi module.

WiFiClient wifiClient;
MqttClient mqttClient(wifiClient);

const char broker[] = "broker.hivemq.com";
int        port     = 1883;
const char topic[]  = "myfridge/sensor/rfid";

const long interval = 1000;
unsigned long previousMillis = 0;

int count = 0;

byte ActualUID[7]; //almacenará el código del Tag leído
byte Alimento1[7] = {0x04, 0x62, 0x49, 0x22, 0xEE, 0x64, 0x80} ; //código del usuario 1
byte Alimento2[7] = {0x04, 0x69, 0x48, 0x22, 0xEE, 0x64, 0x80} ; //código del usuario 2
byte Alimento3[7] = {0x04, 0x6D, 0x48, 0x22, 0xEE, 0x64, 0x80} ; //código del usuario 1
byte Alimento4[7] = {0x04, 0x71, 0x48, 0x22, 0xEE, 0x64, 0x80} ; //código del usuario 1

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

#define BUTTON_PIN_BITMASK 0x1000000000 // IO 36 activa

//-----------------------------------------------------------------------------------------------------------

void tarea1(void *pvParameter)
{
  while (1) {

    // call poll() regularly to allow the library to send MQTT keep alives which
    // avoids being disconnected by the broker
    mqttClient.poll();

    // avoid having delays in loop, we'll use the strategy from BlinkWithoutDelay
    // see: File -> Examples -> 02.Digital -> BlinkWithoutDelay for more info
    unsigned long currentMillis = millis();


    // Revisamos si hay nuevas tarjetas presentes
    if ( mfrc522.PICC_IsNewCardPresent())
    {
      //Seleccionamos una tarjeta
      if ( mfrc522.PICC_ReadCardSerial())
      {
        // Enviamos serialemente su UID
        Serial.println();
        Serial.print(F("ALIMENTO:"));
        M5.Lcd.setCursor(0, 30);
        M5.Lcd.fillScreen(NEGRO);
        M5.Lcd.setTextColor(AZUL);
        M5.Lcd.print(F("Codigo:"));

        for (byte i = 0; i < mfrc522.uid.size; i++) {
          MFRC522::PICC_Type piccType = mfrc522.PICC_GetType(mfrc522.uid.sak);
          Serial.println(mfrc522.PICC_GetTypeName(piccType));
          Serial.println(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");
          M5.Lcd.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");
          Serial.println(mfrc522.uid.uidByte[i], HEX);
          M5.Lcd.print(mfrc522.uid.uidByte[i], HEX);

          ActualUID[i] = mfrc522.uid.uidByte[i];
        }

        Serial.print(" ");
        M5.Lcd.print(" ");
        //comparamos los UID para determinar si es uno de nuestros usuarios

        if (compareArray(ActualUID, Alimento1, 7) || compareArray(ActualUID, Alimento2, 7) || compareArray(ActualUID, Alimento3, 7) || compareArray(ActualUID, Alimento4, 7)) {
          Serial.println("Alimento reconocido");
          M5.Lcd.setCursor(0, 60);
          M5.Lcd.setTextColor(VERDE);
          M5.Lcd.println("Alimento reconocido");
          si();

          mqttClient.beginMessage(topic);
          for (byte i = 0; i < mfrc522.uid.size; i++) {

            mqttClient.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : "");
            mqttClient.print(mfrc522.uid.uidByte[i], HEX);
          }


          if (currentMillis - previousMillis >= interval) {
            // save the last time a message was sent
            previousMillis = currentMillis;

            Serial.print("Sending message to topic: ");
            Serial.println(topic);
            Serial.print("Alimento");
            Serial.println(count);
            Serial.println();
          }
        }
        else
        { Serial.println("Alimento no reconocido");
          M5.Lcd.setCursor(0, 60);
          M5.Lcd.setTextColor(ROJO);
          M5.Lcd.println("No reconocido, escanea codigo con la app");
          no();
        }
        if (compareArray(ActualUID, Alimento1, 7))
        {
          mqttClient.print(";");
          mqttClient.print(1639436400000);
          mqttClient.print(";");
          mqttClient.print(3033490004521 );
          mqttClient.endMessage();
        }
        else if (compareArray(ActualUID, Alimento2, 7))
        {
          mqttClient.print(";");
          mqttClient.print(1645743600000);
          mqttClient.print(";");
          mqttClient.print(5410188031072);
          mqttClient.endMessage();
        }
        else if (compareArray(ActualUID, Alimento3, 7))
        {
          mqttClient.print(";");
          mqttClient.print(1651442400000);
          mqttClient.print(";");
          mqttClient.print(87157215);
          mqttClient.endMessage();
        }
        else if (compareArray(ActualUID, Alimento4, 7))
        {
          mqttClient.print(";");
          mqttClient.print(1705273200000);
          mqttClient.print(";");
          mqttClient.print(8076809513722 );
          mqttClient.endMessage();
        }

        // Terminamos la lectura de la tarjeta tarjeta actual
        mfrc522.PICC_HaltA();
        M5.Lcd.setCursor(30, 140);
        M5.Lcd.setTextColor(BLANCO);
        M5.Lcd.println("PASE OTRO ALIMENTO");
      }
    }
    if(M5.BtnB.read()) pressed = true;
    if(pressed == 1) esp_deep_sleep_start(); //duerme al ESP32 (modo SLEEP)
    //vTaskDelay(3000 / portTICK_PERIOD_MS);

  }
}

void setup() {

  M5.begin();
  //Initialize serial and wait for port to open:
  //Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }

  // attempt to connect to Wifi network:
  Serial.print("Attempting to connect to WPA SSID: ");
  Serial.println(ssid);
  while (WiFi.begin(ssid, pass) != WL_CONNECTED) {
    // failed, retry
    Serial.print(".");
    delay(5000);
  }

  Serial.println("You're connected to the network");
  Serial.println();
  mqttClient.setId("1234795625765769");

  // You can provide a unique client ID, if not set the library uses Arduino-millis()
  // Each client must have a unique client ID
  // mqttClient.setId("clientId");

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



  M5.Lcd.setTextSize(2);
  //Serial.begin(9600); //Iniciamos La comunicacion serial
  SPI.begin(); //Iniciamos el Bus SPI
  mfrc522.PCD_Init(); // Iniciamos el MFRC522
  Serial.println("PASE ALIMENTO");
  M5.Lcd.setCursor(30, 10);
  M5.Lcd.setTextColor(BLANCO);
  M5.Lcd.println("PASE ALIMENTO");



  xTaskCreatePinnedToCore(&tarea1, "tarea1", 4096, NULL, 1, NULL, 0);

  esp_sleep_enable_ext0_wakeup(GPIO_NUM_39, 0); //1 = High, 0 = Low
  
    
  


}




//-----------------------------------------------------------------------------------------------------------

void loop() {}

//-----------------------------------------------------------------------------------------------------------

boolean compareArray(byte array1[], byte array2[], int n_byte)
{
  for (int i = 0; i < n_byte; i++)
  {
    if (array1[i] != array2[i])return (false);
  }
  return (true);
}

//-----------------------------------------------------------------------------------------------------------

void si ()
{
  M5.Lcd.setTextSize(4);
  M5.Lcd.setCursor(150, 90);
  M5.Lcd.setTextColor(VERDE);
  M5.Lcd.println("SI");
  M5.Lcd.setTextSize(2);
}
void no ()
{
  M5.Lcd.setTextSize(4);
  M5.Lcd.setCursor(150, 90);
  M5.Lcd.setTextColor(ROJO);
  M5.Lcd.println("NO");
  M5.Lcd.setTextSize(2);
}

//-----------------------------------------------------------------------------------------------------------

void lectura_datos()
{
  byte buffer_1[18]; //buffer intermedio para leer 16 bytes
  byte buffer[66]; //data transfer buffer (64+2 bytes data+CRC)
  byte tam = sizeof(buffer);
  byte tam1 = sizeof(buffer_1);
  uint8_t pageAddr = 0x04; //In this example we will write/read 64 bytes (page 6,7,8 hasta la 21).
  //Ultraligth mem = 16 pages. 4 bytes per page.
  //Pages 0 to 4 are for special functions.
  // Read data ***************************************************
  //En esta función los datos se leen de 16 bytes en 16 y se almacenan en buffer_1 (de 16+2 bytes)
  //para despues transferirlos a buffer que tiene un tamaño mayor
  for (int i = 0; i < (tam - 2) / 16; i++)
  {
    //data in 4 block is readed at once 4 bloques de 4 bytes total 16 bytes en cada lectura.
    status = (MFRC522::StatusCode) mfrc522.MIFARE_Read(pageAddr + i * 4, buffer_1, &tam1);

    //copio los datos leidos en buffer_1 a la posición correspondiente del buffer
    for (int j = 0; j < 16; j++)
    {
      buffer[j + i * 16] = buffer_1[j];
    }
  }
  //Presentacion de los datos ledidos por el puerto serie y por el M5Stack
  Serial.print(F("Readed data: "));
  //Dump a byte array to Serial
  for (byte i = 0; i < (tam - 2); i++) {
    Serial.write(buffer[i]);
  }
  M5.Lcd.setTextSize(2);
  M5.Lcd.setCursor(0, 160);
  M5.Lcd.setTextColor(VERDE);
  for (byte i = 0; i < (tam - 2); i++) {
    M5.Lcd.print((char)buffer[i]);
  }
}

//-----------------------------------------------------------------------------------------------------------
