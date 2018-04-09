#include <Arduino.h>
#line 1 "D:\\Libraries\\Google Drive\\Computer Engineering\\Projects\\Senior Project\\ESP8266\\ESP8266_Firmware\\ESP8266_Firmware.ino"
#line 1 "D:\\Libraries\\Google Drive\\Computer Engineering\\Projects\\Senior Project\\ESP8266\\ESP8266_Firmware\\ESP8266_Firmware.ino"
#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <FS.h>  // SPIFFS library

// Constants //
const char* ssid     = "wigglewiggle";
const char* password = "I|\\|s+@|\\|+_R@m3|\\|_|\\|00d13s";

IPAddress IHA_Server(192,168,1,103);
WiFiClient client;

#line 12 "D:\\Libraries\\Google Drive\\Computer Engineering\\Projects\\Senior Project\\ESP8266\\ESP8266_Firmware\\ESP8266_Firmware.ino"
void setup();
#line 19 "D:\\Libraries\\Google Drive\\Computer Engineering\\Projects\\Senior Project\\ESP8266\\ESP8266_Firmware\\ESP8266_Firmware.ino"
void loop();
#line 38 "D:\\Libraries\\Google Drive\\Computer Engineering\\Projects\\Senior Project\\ESP8266\\ESP8266_Firmware\\ESP8266_Firmware.ino"
void UART_init();
#line 53 "D:\\Libraries\\Google Drive\\Computer Engineering\\Projects\\Senior Project\\ESP8266\\ESP8266_Firmware\\ESP8266_Firmware.ino"
void wifi_init();
#line 66 "D:\\Libraries\\Google Drive\\Computer Engineering\\Projects\\Senior Project\\ESP8266\\ESP8266_Firmware\\ESP8266_Firmware.ino"
void client_init();
#line 78 "D:\\Libraries\\Google Drive\\Computer Engineering\\Projects\\Senior Project\\ESP8266\\ESP8266_Firmware\\ESP8266_Firmware.ino"
unsigned char getChar();
#line 12 "D:\\Libraries\\Google Drive\\Computer Engineering\\Projects\\Senior Project\\ESP8266\\ESP8266_Firmware\\ESP8266_Firmware.ino"
void setup()
{
  UART_init();
  wifi_init();
  client_init();
}

void loop()
{
  unsigned char in;

  // Pass data from server to MCU
  if(client.available() > 0)
  {
    in = client.read();
    Serial.write(in);
  }

  // Pass data from MCU to server
  if(Serial.available() > 0)
  {
    in = getChar();
    client.write(in);
  }
}

void UART_init()
{
  // Start the Serial communication to send messages to the MCU
  Serial.begin(115200); 
  delay(100);
  
  // Rudementary handshake with the MCU to let it know the program is ready
  // TODO: improve making it 2-way after input is figured out
  Serial.print('a');
  delay(100);
  Serial.print('b');
  delay(100);
  Serial.print('c');
}

void wifi_init()
{
  // Connect to the Wi-Fi network
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED)
  { // Wait for the Wi-Fi to connect
    delay(1000);
  }

  Serial.print("Y");
}

void client_init()
{
  bool result = false;
  while(result == false)
  {
    result = client.connect(IHA_Server, 14123);
    delay(5000); // try again in 5 seconds
  }

  Serial.print("Y");
}

unsigned char getChar()
{
  unsigned char in;
  while(Serial.available() < 1) {};
  in = Serial.read();
  return in;
}



