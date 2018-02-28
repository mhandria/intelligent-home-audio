#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <FS.h>  // SPIFFS library

// Constants //
const char* ssid     = "Evol";
const char* password = "turtlebutt";

IPAddress IHA_Server(192,168,1,248);
WiFiClient client;

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
    result = client.connect(IHA_Server, 14124);
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


