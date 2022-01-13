#include <M5Stack.h>


#include "WiFi.h"
#include "AsyncUDP.h"
#include <ArduinoJson.h>

const char * ssid = "TP-LINK_6CAE";
const char * password = "41422915";
char texto[200]; //array para recibir los datos como texto
int hora;
boolean rec = 0;
AsyncUDP udp;

#include <esp_task_wdt.h>
#define TWDT_TIMEOUT_S 1000
#define TASK_RESET_PERIOD_S 2

bool pressed = false;

void tarea1(void *pvParameter)
{
  while (1) {
    if (rec) {
      rec = 0;
      udp.broadcastTo("Recibido", 1234); //Confirmación
      udp.broadcastTo(texto, 1234); //reenvía lo recibido
      hora = atol(texto); //paso de texto a int
      M5.Lcd.setCursor(0, 0);
      M5.Lcd.print(texto);
      StaticJsonDocument<200> jsonBufferRecv; //definición buffer para almacenar el objeto JSON, 200 máximo
      DeserializationError error = deserializeJson(jsonBufferRecv, texto); //paso de texto a formato JSON
      if (error)
        return;
      //serializeJson(jsonBufferRecv, Serial); //envío por el puerto serie el objeto "jsonBufferRecv"
      //Serial.println(); //nueva línea
      int segundo = jsonBufferRecv ["Segundo"]; //extraigo el dato "Segundo" del objeto " jsonBufferRecv " y lo
      //almaceno en la variable "segundo"
      //Serial.println(segundo); //envío por el puerto serie la variable segundo
    }
    if(M5.BtnB.read()) pressed = true;
    if(pressed == 1) esp_deep_sleep_start(); //duerme al ESP32 (modo SLEEP)
    //vTaskDelay(3000 / portTICK_PERIOD_MS);

  }
}
void setup()
{
  M5.begin();
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
      int i = 200;
      while (i--) {
        *(texto + i) = *(packet.data() + i);
      }
      rec = 1; //recepcion de un mensaje
    });
  }

  xTaskCreatePinnedToCore(&tarea1, "tarea1", 4096, NULL, 1, NULL, 0);

  esp_sleep_enable_ext0_wakeup(GPIO_NUM_39, 0); //1 = High, 0 = Low
}
void loop()
{

}
