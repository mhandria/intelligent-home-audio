// CSULB - CECS490B
// Team IHA Senior Project
// blake.spangenberg1@gmail.com
// Target Device: TM4C123GXL

// This program is the software which runs on the MCU
// dedicated to the speakers in the network.
// It is connected to the network via an ESP2866 network interface
// on UART0

// UART with 115,200 baud rate (assuming 50 MHz UART clock),
// 8 bit word length, no parity bits, one stop bit, FIFOs enabled

// I/O
// UART0 - USB Serial Debug
// PA0 - Tx Output
// PA1 - Rx Input

// UART 1 - ESP8266 Serial connection
// PB0 - Tx Output
// PB1 - Rx Input

// PD7 - RST for ESP8266
// PF1-PF3 - RGB LEDs Output

#include "PLL.h"
#include "SysTick.h"
#include "UART.h"
#include "tm4c123gh6pm.h"
#include "stdbool.h"
#include <stdio.h>

// Data Structure Section //


// Function Prototype Section // 

void DisableInterrupts(void);
void EnableInterrupts(void);
void init(void);
void PortF_Init(void);
void PortD_Init(void);
void ESP_Init(void);    // Initializes the ESP8266
void delay_ms(int t);

// Global Variables Section //

unsigned char returnChar;
unsigned char ESPString [256];

// Function Implementation Section //

int main(void)
{
	unsigned char in;
	init();
	
  UART1_SendChar('s'); // MCU is ready to recieve song data
	while(1)
	{
		
		
		
		if(in == '!')
		{
			GPIO_PORTF_DATA_R &= ~0x0E;
			GPIO_PORTF_DATA_R |=  0x04; // BLUE LED = Connected to Wi-Fi Network
			
			in = UART1_GetChar(); // Should send 'y', that would mean reconnected
			UART0_SendChar(in);
			
			GPIO_PORTF_DATA_R &= ~0x0E;
			GPIO_PORTF_DATA_R |=  0x08; // Green LED = ESP8266 Serial Comm OK
		}
	}
}

// Initialize clock, I/O, and variables
void init(void)
{
	DisableInterrupts();
	PLL_Init();     // 80Mhz clock
	SysTick_Init(); // For 
	UART0_Init();   // UART0 initialization - USB
	UART1_Init();   // UART1 initialization - PB0 Rx - PB1 Tx
	PortF_Init();   // Initalize RGB LEDs
	PortD_Init();   // Initialize ESP8266 reset
	
	UART0_SendString("USB UART Connection OK");
	UART0_CRLF();
	
	GPIO_PORTF_DATA_R &= ~0x0E; // Turn off LEDs
	
	ESP_Init();
	
	EnableInterrupts();
}

// Configure PF1-PF3 as GPIO Output
void PortF_Init(void)
{
  unsigned long volatile delay;
  SYSCTL_RCGC2_R |= 0x00000020;      // Activate clock for port F
  delay = SYSCTL_RCGC2_R;
  GPIO_PORTF_LOCK_R   =  0x4C4F434B; // unlock GPIO Port F
  GPIO_PORTF_CR_R     =  0x0E;       // Allow changes to PF1-PF3
  GPIO_PORTF_DIR_R   |=  0x0E;       // Setup PF1-PF3 as output
  GPIO_PORTF_AFSEL_R &= ~0x0E;       // Disable alt funct on PF1-PF3
  GPIO_PORTF_DEN_R   |=  0x0E;       // Enable PF1-PF3
  GPIO_PORTF_PCTL_R  &= ~0x0000FFF0; // Configure PF1-PF3 as GPIO
  GPIO_PORTF_AMSEL_R &= ~0x0E;       // Disable analog functionality on PF1-PF3
  GPIO_PORTF_PUR_R   &= ~0x0E;       // Enable weak pull-up on PF1-PF3
}

// PD7 GPIO Output
void PortD_Init(void)
{
  unsigned long volatile delay;
  SYSCTL_RCGC2_R |= 0x00000008;     // activate clock for port D
  delay = SYSCTL_RCGC2_R;
  GPIO_PORTD_AMSEL_R &= ~0x80;       // disable analog functionality on PD7
  GPIO_PORTD_PCTL_R  &= ~0xF0000000; // configure PD7 as GPIO
  GPIO_PORTD_DIR_R   |= ~0x80;       // make PD74 input
  // GPIO_PORTD_DR8R_R  |=  0x80;       // enable 8 mA drive on PD7
  GPIO_PORTD_AFSEL_R &= ~0x80;       // disable alt funct on PD7
  GPIO_PORTD_DEN_R   |=  0x80;       // enable digital I/O on PD7
}

// Reset ESP8266, verify connection to network
void ESP_Init(void)
{
	unsigned char in;
	in = ' ';
	
	// TODO: Attatch this to mosfet so it resets properly
	// GPIO_PORTD_DATA_R &= ~0x80; // Assert ESP8266 Reset
	// GPIO_PORTD_DATA_R |=  0x80; // Deassert ESP8266 Reset
	
	UART0_SendString("Reset ESP8266");
	UART0_CRLF();
	
	// Handshake
	
	while(in != 'b')
	{
		delay_ms(500); // Every half second try the handshake
		UART1_SendChar('a');
		UART0_SendChar('a');
		
		if((UART1_FR_R&UART_FR_RXFE) == 0)
		{ // If we recieved data back, get  it
			in = (unsigned char)(UART1_DR_R&0xFF);
			UART0_SendChar(in);
		}
	}
	
	delay_ms(100);
	UART1_SendChar('c');
	
	UART0_SendString("ESP8266 Serial Handshake Completed");
	UART0_CRLF();
	
	GPIO_PORTF_DATA_R &= ~0x0E; // Turn off LEDs
	GPIO_PORTF_DATA_R |=  0x02; // RED LED = Serial Connection Established
	
	// Wi-Fi connected char
	returnChar = ' ';
	while(returnChar != 'Y')
	{
		returnChar = UART1_GetChar();
		// UART0_SendChar(returnChar);
	}
	
	UART0_SendString("ESP8266 Wi-Fi Connection OK?: ");
	UART0_SendChar(returnChar);
	UART0_CRLF();
	
	GPIO_PORTF_DATA_R &= ~0x0E; // Turn off LEDs
	GPIO_PORTF_DATA_R |=  0x04; // BLUE LED = Connected to Wi-Fi Network
	
	// Server connected char
	returnChar = ' ';
	while(returnChar != 'Y')
	{
		returnChar = UART1_GetChar();
		// UART0_SendChar(returnChar);
	}
	
	UART0_SendString("ESP8266 Server-Client Connection OK?: ");
	UART0_SendChar(returnChar);
	UART0_CRLF();
	
	GPIO_PORTF_DATA_R &= ~0x0E; // Turn off LEDs
	GPIO_PORTF_DATA_R |=  0x08; // Green LED = ESP8266 Connection to IHA Server
	
	UART0_SendString("Entering Main Loop");
	UART0_CRLF();
}

// 80Mhz = 12.5ns period
// 1ms = 80xperiod
// 80 periods/3periodsperloop = 27 per ms
void delay_ms(int t)
{
	int ulCount;
	
	int i;
	for(i = 0; i <  t; i++)
	{
		// Delay 1 ms //
		ulCount = 26;
		
		do
		{
			ulCount--;
		}
		while(ulCount);
	}
}
