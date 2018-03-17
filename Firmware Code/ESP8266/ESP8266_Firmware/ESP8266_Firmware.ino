#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include "ESP8266Ping\src\ESP8266Ping.h"
#include <EEPROM.h>
#include <Ticker.h>

// yield()          - lets the processor run background tasks (networking)
// ESP.wdtDisable() - disables only the software watchdog timer
// ESP.wdtFeed()    - resets the software and hardware watchdog timers

// Constants //
const char* ssid     = "wigglewiggle";
const char* password = "I|\\|s+@|\\|+_R@m3|\\|_|\\|00d13s";
const int CHUNK_SIZE = 2048;
// const int BAUD_RATE = 921600;
const int BAUD_RATE = 115200;

// Set these three constants to speed up testing //
const bool manualConnect = true;
const bool writeManualConnectToEEPROM = true;
const IPAddress IHA_SERVER(192,168,1,103);

// Global Variables //
bool debug_enable;
WiFiClient client;
IPAddress IHA_Server;
Ticker sampleTick;
byte sampleArray0 [CHUNK_SIZE];
// byte sampleArray1 [CHUNK_SIZE];
int sampleIndex;
int chunkLength;

// Runs after reset //
void setup()
{
  sampleIndex = 0;
  ESP.wdtDisable(); // disable the watch dog timer simply because I am a bad programmer
  UART_init();
  wifi_init();
  client_init();
  
  unsigned char in = ' ';
  while(in != 's')
  {
    in = getMCUChar();
  }
}

// Main program loop, runs after setup //
void loop()
{
  unsigned char in;
  ESP.wdtFeed();
  
  getSong(); // TODO: make this return
  
  if(!client.connected())
  {
    debugLine("The connection with the server has been lost. Reconnecting...");
    Serial.write('!');
    // If we're disconnected try to reconnect forever
    clientReconnect();
    debugLine("The connection with the server has been restored");
    Serial.write('y');
  }
  
  // TODO: Check if Wi-Fi disconnected, if so, reconnect and update MCU LED
}

//                          //
//                          //
// Initialization Functions //
//                          //
//                          //
void UART_init()
{
  unsigned char in = ' ';
  // Start the Serial communication to send messages to the MCU
  Serial.begin(BAUD_RATE);
  
  debug_enable = 0; // Debug is off by default
  while(in != 'c')
  {
    in = getMCUChar();
    if(in == 'a')
    {
      Serial.write('b');
      in = getMCUChar();
    }
    else if(in == 'd' && debug_enable == 0)
    {
      debug_enable = 1;
      debugLine(" ");
      debugLine("Debug mode enabled");
    }
  }
  
  debugLine(" ");
  debugLine("Handshake completed");
  debugLine(" ");
}

void wifi_init()
{
  // Connect to the Wi-Fi network
  debugLine("Connecting to:  " + String(ssid));
  debugLine("Using password: " + String(password));
  
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED)
  { // Wait for the Wi-Fi to connect
    delay(1000);
    debugStr(".");
  }
  
  Serial.print("Y");
  debugLine(" ");
  debugLine("Connected!");
}

void client_init()
{
  uint8_t defaultFlag;
  ESP.wdtFeed();
  if(manualConnect)
  {
    // Debug code to speed up testing //
    IHA_Server = IHA_SERVER;
    defaultFlag = 255;
    if(writeManualConnectToEEPROM)
    { // write it to EEPROM so that in the future life is easy
      EEPROM.begin(128);
      for(int i = 0; i < 4; i++)
      { // The first 4 bytes in EEPROM are the 4 octets of the IHA_Server IP Address
        EEPROM.write(i, IHA_SERVER[i]);
      }
      EEPROM.write(4,255);
      EEPROM.commit();
      EEPROM.end();
    }
    debugLine(" ");
    debugLine("Using constant address set by programmer: " + IHA_Server.toString());
  }
  else
  {
    // Fetch the local IP address
    EEPROM.begin(128);
    for(int i = 0; i < 4; i++)
    { // The first 4 bytes in EEPROM are the 4 octets of the IHA_Server IP Address
      IHA_Server[i] = EEPROM.read(i);
    }
    // The fourth EEPROM byte is non-zero if the IHA_Server address is valid
    defaultFlag = EEPROM.read(4);
    EEPROM.end();
  }
  debugLine(" ");
  if(defaultFlag != 255) debugLine("No stored server address found.");
  
  ESP.wdtFeed();
  
  bool result = false;
  int i = 0;
  while(result == false && i < 5)
  {
    ESP.wdtFeed();
    if(defaultFlag == 255)
    { // if it's not the default EEPROM address
      // then attempt to connect to it 5 times
      if(i == 0) debugLine("Connnecting to IHA_Server @ IP address: " + IHA_Server.toString());
      result = client.connect(IHA_Server, 14124);
      ESP.wdtFeed();
      if(result == false) debugStr(".");
      delay(3000); // try again in 3 seconds
    }
    i++;
  }
  
  if(result == false)
  { // If it could not connect in under 5 attempts, search the network for the new IP address
    findServerIP();
  }
  else
  { // Otherwise this speaker is connected to the network
    // Get the server status //
    unsigned char in = ' ';
    while(in != 'y')
    {
      ESP.wdtFeed();
      client.write("?");
      delay(100);
      if(client.available() > 0)
      {
        in = client.read();
      }
    }
  }
  debugLine("Connected!");
  Serial.print("Y");
}

//                          //
//                          //
// IP Calculation Functions //
//                          //
//                          //
void findServerIP()
{
  ESP.wdtFeed();
  debugLine(" ");
  debugLine("Could not connect to the server");
  debugLine("Finding the IHA_Server Address");
  
  // Get the first and last usable IP Addresses for the current network //
  IPAddress local  = WiFi.localIP();
  IPAddress mask   = WiFi.subnetMask();
  debugLine("Local IP Address:     " + local.toString());
  debugLine("Subnet Mask:          " + mask.toString());
  
  // Calculate the network address
  // [0] is the most significant octet
  uint8_t netAddr [4];
  for(int i = 0; i < 4; i++)
  {
    netAddr[i] = local[i] & mask[i];
  }
  
  debugStr("Network Address:      ");
  debugPrintAddr(netAddr);
  debugLine(" ");
  
  // Calculate first address (network address + 1)
  uint8_t firstAddr[4];
  IPAddr_set(firstAddr, netAddr);
  IPAddr_increment(firstAddr);
  
  debugStr("First Usable Address: ");
  debugPrintAddr(firstAddr);
  debugLine(" ");
  ESP.wdtFeed();
  
  // Calculate last address
  // The the set all bits that aren't part of the mask
  // to get the broadcast address
  uint8_t lastAddr[4];
  for (int i = 0; i < 4; i++)
  {
    lastAddr[i] = (~mask[i] | netAddr[i]);
  }
  // Sub 1 from the broadcast address to get the last address
  IPAddr_decrement(lastAddr);
  
  debugStr("Last Usable Address:  ");
  debugPrintAddr(lastAddr);
  debugLine(" ");
  
  // Now that the first and Last usable addresses have   //
  // been calculated, ping all addresses on the network. //
  // The ones which are responsive, check if they are open on port 14123. //
  // If they are, check if it's the IHA server with the "stat" command    //
  // Finally, save the server address in nonvolitile memory for next time //
  
  ESP.wdtFeed();
  debugLine(" ");
  debugLine("Checking addresses for IHA_Server:");
  
  uint8_t currAddr[4];
  IPAddr_set(currAddr, firstAddr);
  bool search = true;
  while(search)
  {
    ESP.wdtFeed();
    if(Ping.ping(currAddr, 2)) // ping the current address
    { // If the ping is sucessfuld
      debugLine(" ");
      debugPrintAddr(currAddr);
      debugStr(" - ");
      debugStr(String(Ping.averageTime()));
      debugLine("ms");

      IPAddress server = currAddr;
      if(client.connect(server, 14124))
      { // check if the IHA port is open
        debugLine(" ");
        debugStr("Connected to ");
        debugPrintAddr(currAddr);
        debugLine(":14124");
        // If the port is open check the response of the "stat" command
        client.print("?");
        
        delay(500);
        while(client.available() > 0)
        {
          ESP.wdtFeed();
          if(client.read() == 'y' && search == true)
          { // If the server responds correctly, it's the IHA server
            debugLine("It's the IHA_Server");
            search = false;
            IHA_Server = server;

            // Store the new IP in EEPROM for next time
            EEPROM.begin(128);
            for(int i = 0; i < 4; i++)
            { // The first 4 bytes in EEPROM are the 4 octets of the IHA_Server IP Address
              EEPROM.write(i, IHA_Server[i]);
            }
            EEPROM.write(4,255);
            EEPROM.commit();
            EEPROM.end();
          }
        }
        if(search)
        {
          debugLine("It's NOT the IHA_Server");
          client.stop();
        }
      }
    }
    
    ESP.wdtFeed();
    if(IPAddr_isEqual(currAddr, lastAddr))
    { // If that was the last address, start over at the first address in 10 seconds
      debugLine("IHA_Server not found on network, trying again in 10 seconds...");
      IPAddr_set(currAddr, firstAddr);
      delay(10000);
      debugLine("Checking addresses for IHA_Server:");
    }
    else
    { // Otherwise try the next address in the network
      IPAddr_increment(currAddr);
      debugStr(".");
    }
  }
}

// Increments the byte array IP address by one //
void IPAddr_increment(uint8_t addr[4])
{
  // Increment the least significant octet,
  // and if it overflows increment the next one etc.

  addr[3] = addr[3] + 1;
  if (addr[3] == 0)
  {
    addr[2] = addr[2] + 1;
    if (addr[2] == 0)
    {
      addr[1] = addr[1] + 1;
      if (addr[1] == 0)
      {
        addr[0] = addr[0] + 1;
      }
    }
  }
  return;
}

// Increments the byte array IP address by one //
void IPAddr_decrement(uint8_t addr[4])
{
  // Increment the least significant octet,
  // and if it overflows increment the next one etc.

  addr[3] = addr[3] - 1;
  if (addr[3] == 255)
  {
    addr[2] = addr[2] - 1;
    if (addr[2] == 255)
    {
      addr[1] = addr[1] - 1;
      if (addr[1] == 255)
      {
        addr[0] = addr[0] - 1;
      }
    }
  }
  return;
}

// Sets address 0 to address 1
void IPAddr_set(uint8_t addr0[4], uint8_t addr1[4])
{
  for (int i = 0; i < 4; i++)
  {
    addr0[i] = addr1[i];
  }
}

// Checks if two 4-wide uint8_t arrays are equal //
bool IPAddr_isEqual(uint8_t addr0[4], uint8_t addr1[4])
{
  for(int i = 0; i < 4; i++)
  {
    if(addr0[i] != addr1[i]) return false;
  }
  return true;
}

// Runs if the ESP8266 main loop detects the client is //
// no longer connected to the server //
void clientReconnect()
{
  bool result = false;
  while(result == false)
  {
    result = client.connect(IHA_Server, 14124);
    delay(3000); // try again in 3 seconds
  }
}
//                          //
//                          //
//      I/O Functions       //
//                          //
//                          //
unsigned char getMCUChar()
{
  unsigned char in;
  while(Serial.available() < 1)
  {
    ESP.wdtFeed();
  }
  in = Serial.read();
  return in;
}

unsigned char getServerChar()
{
  unsigned char in;
  while(client.available() < 1)
  {
    ESP.wdtFeed();
  }
  in = client.read();
  return in;
}

// gets a song chunk from the server
void getSong()
{
  while(true)
  {
    ESP.wdtFeed();
    client.flush();
    client.write("s"); // Prompt for song data

    // wait for the chunk
    while(client.available() < 1)
    {
      ESP.wdtFeed();
    }
    
    int i = 0;
    while(client.available() > 0 && i < CHUNK_SIZE)
    {
      ESP.wdtFeed();
      sampleArray0[i] = client.read();
      i++;
    }
    
    ESP.wdtFeed();
    chunkLength = i;
    // sendChunk();
  }
}

// sends a single 16-bit sample to the TM4C123 to be immediately "played"
void sendChunk()
{
  for(int i = 0; i < CHUNK_SIZE && i < chunkLength; i++)
  {
    delayMicroseconds(500);
    Serial.write(sampleArray0[i]);
    ESP.wdtFeed();
  }
}

//                          //
//                          //
//      Debug Functions     //
//                          //
//                          //
void debugLine(String str)
{
  if(debug_enable)
  {
    Serial.println(str);
  }
}

void debugStr(String str)
{
  if(debug_enable)
  {
    Serial.print(str);
  }
}

void debugWrite(byte b)
{
  if(debug_enable)
  {
    Serial.write(b);
  }
}


void debugPrintAddr(uint8_t* addr)
{
  for (int i = 0; i < 4; i++)
  {
    debugStr(String(addr[i]));
    if (i != 3) debugStr(".");
  }
}
