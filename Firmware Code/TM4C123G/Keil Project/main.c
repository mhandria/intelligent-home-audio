// CSULB - CECS490B
// Team IHA Senior Project
// blake.spangenberg1@gmail.com
// Target Device: TM4C123GXL

// This program is the software which runs on the MCU
// dedicated to the speakers in the network.
// It is connected to the network via an ESP2866 network interface
// on UART0

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
#include "UART.h"
#include "tm4c123gh6pm.h"
#include "stdbool.h"
#include <stdio.h>

// Data Structure Section //


// Function Prototype Section // 

void DisableInterrupts(void);
void EnableInterrupts(void);

void init(void);
void PortB_Init(void);
void PortC_Init(void);
void PortE_Init(void);
void PortF_Init(void);
void ESP_Init(void);    // Initializes the ESP8266

void pulseLEDs(void);   // Flashy sequence to show the board reset
void delay_ms(int t);
void writeBuff(unsigned char in);
unsigned char readBuff(void);
void wait_for_procede(void);
unsigned int getPtrDifference(void);
void writeDAC(unsigned char upper, unsigned char lower);

void SysTick_Init(void);           // Executes SysTick_Handler ever x periods
void SysTick_Handler(void);        // Sends a sample to the the DAC

// Constants Section //
const unsigned long BUFFER_SIZE = 30000;
const double SAMPLE_FREQ = 44100;

// Global Variables Section //

unsigned char sampleBuffer[BUFFER_SIZE];
unsigned char *sampleReadPtr;
unsigned char *sampleWritePtr;
bool arePtrsMisaligned;

unsigned int ticksSinceLastRequest;
  
// Function Implementation Section //

int main(void)
{
	init();
	
	delay_ms(500);
  UART1_SendChar('s'); // MCU is ready to recieve song data
	
	delay_ms(500);
  UART1_SendChar('s'); // MCU is ready to recieve song data
	
	UART0_SendString("Entering Main Loop");
	UART0_CRLF();
	
	while(1)
	{
		while((UART1_FR_R&UART_FR_RXFE) == 0) 
		{ // when a sample comes, store it in the buffer
			writeBuff((unsigned char)(UART1_DR_R&0xFF));
		}
	}
}

// Initialize clock, I/O, and variables
void init(void)
{
	sampleReadPtr  = &sampleBuffer[0];
	sampleWritePtr = &sampleBuffer[0];
	ticksSinceLastRequest = 0;
	arePtrsMisaligned = false;
	ticksSinceLastRequest = 0;
	
	DisableInterrupts();
	PLL_Init();     // 80Mhz clock
	UART0_Init();   // UART0 initialization - USB
	UART1_Init();   // UART1 initialization - PB0 Rx - PB1 Tx
	
	PortB_Init();
	PortC_Init();
	PortE_Init();
	PortF_Init();   // Initalize RGB LEDs
	
	UART0_SendString("USB UART Connection OK");
	UART0_CRLF();
	
	SysTick_Init(); // Setup timer for playing samples
	
	pulseLEDs();
	
	ESP_Init();
	
	EnableInterrupts();
}

void pulseLEDs(void)
{
	int i;
	delay_ms(100);
	for(i = 0; i < 2; i++)
	{
		GPIO_PORTF_DATA_R &= ~0x0E; // Turn off LEDs
		GPIO_PORTF_DATA_R |=  0x02; // RED
		delay_ms(20);
		GPIO_PORTF_DATA_R &= ~0x0E; // Turn off LEDs
		GPIO_PORTF_DATA_R |=  0x04; // BLUE
		delay_ms(20);
		GPIO_PORTF_DATA_R &= ~0x0E; // Turn off LEDs
		GPIO_PORTF_DATA_R |=  0x08; // GREEN
		delay_ms(20);
		GPIO_PORTF_DATA_R &= ~0x0E; // Turn off LEDs
		delay_ms(50);
	}
}

// PB2-PB7 GPIO Output
void PortB_Init(void)
{
  unsigned long volatile delay;
  SYSCTL_RCGC2_R |= 0x00000002;     // activate clock for port B
  delay = SYSCTL_RCGC2_R;
  GPIO_PORTB_AMSEL_R &= ~0xFC;       // disable analog functionality on PB2-PB7
  GPIO_PORTB_PCTL_R  &= ~0xFFFFFF00; // configure PB2-PB7 as GPIO
  GPIO_PORTB_DIR_R   |=  0xFC;       // make PB2-PB7 output
	GPIO_PORTB_DR8R_R  |=  0xFC;       // enable 8 mA drive on PB2 - PB7
  GPIO_PORTB_AFSEL_R &= ~0xFC;       // disable alt funct on PB2-PB7
  GPIO_PORTB_DEN_R   |=  0xFC;       // enable digital I/O on PB2-PB7
	GPIO_PORTB_DATA_R  &= ~0xFC;       // Set to zero
}

// PC7 - PC4 GPIO Output
void PortC_Init(void)
{
  unsigned long volatile delay;
  SYSCTL_RCGC2_R |= 0x00000004;     // activate clock for port C
  delay = SYSCTL_RCGC2_R;
  GPIO_PORTC_AMSEL_R &= ~0xF0;       // disable analog functionality on PC7 - PC4
  GPIO_PORTC_PCTL_R  &= ~0xFFFF0000; // configure PC7 - PC4 as GPIO
  GPIO_PORTC_DIR_R   |=  0xF0;       // make PC7 - PC4 output
	// GPIO_PORTC_DR8R_R  |=  0xF0;       // enable 8 mA drive on PC7 - PC4
  GPIO_PORTC_AFSEL_R &= ~0xF0;       // disable alt funct on PC7 - PC4
  GPIO_PORTC_DEN_R   |=  0xF0;       // enable digital I/O on PC7 - PC4
	GPIO_PORTC_DATA_R  &= ~0xF0;       // Set to zero
}

// PE5 - PE0 GPIO Output
// 0011 1111
// 3    F
void PortE_Init(void)
{
  unsigned long volatile delay;
  SYSCTL_RCGC2_R |= 0x00000010;     // activate clock for port E
  delay = SYSCTL_RCGC2_R;
  GPIO_PORTE_AMSEL_R &= ~0x3F;       // disable analog functionality on PD7, PD6, PD1, PD0
  GPIO_PORTE_PCTL_R  &= ~0x00FFFFFF; // configure PD7, PD6, PD1, PD0 as GPIO
  GPIO_PORTE_DIR_R   |=  0x3F;       // make PD7, PD6, PD1, PD0 output
	// GPIO_PORTE_DR8R_R  |=  0x3F;       // enable 8 mA drive on PD7, PD6, PD1, PD0
  GPIO_PORTE_AFSEL_R &= ~0x3F;       // disable alt funct on PD7, PD6, PD1, PD0
  GPIO_PORTE_DEN_R   |=  0x3F;       // enable digital I/O on PD7, PD6, PD1, PD0
	GPIO_PORTE_DATA_R  &= ~0x3F;       // Set to zero
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

// Reset ESP8266, verify connection to network
void ESP_Init(void)
{
	unsigned char in;
	in = ' ';
	
	// Handshake
	
	while(in != 'b')
	{
		delay_ms(100); // Every half second try the handshake
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
	
	UART0_CRLF();
	UART0_SendString("ESP8266 Serial Handshake Completed");
	UART0_CRLF();
	
	GPIO_PORTF_DATA_R &= ~0x0E; // Turn off LEDs
	GPIO_PORTF_DATA_R |=  0x02; // RED LED = Serial Connection Established
	
	// Wi-Fi connected char
	wait_for_procede();
	
	UART0_SendString("ESP8266 Wi-Fi Connection OK");
	UART0_CRLF();
	
	GPIO_PORTF_DATA_R &= ~0x0E; // Turn off LEDs
	GPIO_PORTF_DATA_R |=  0x04; // BLUE LED = Connected to Wi-Fi Network
	
	// Server connected char
	wait_for_procede();
	
	UART0_SendString("ESP8266 Server-Client Connection OK");
	UART0_CRLF();
	
	GPIO_PORTF_DATA_R &= ~0x0E; // Turn off LEDs
	GPIO_PORTF_DATA_R |=  0x08; // Green LED = ESP8266 Connection to IHA Server
}

// Initialize SysTick periodic interrupts
// Assumes 80Mhz clk
void SysTick_Init(void)
{
	// Calculate how many clock cycles between interrupts
	unsigned long period = 80000000 / SAMPLE_FREQ;
	// 44,100Hz = 22.67574us period
	// 80,000,000 = 0.0125us period
	// 22.675 / 0.0125 = 1814 clk ticks
  
	UART0_SendString("Using sample rate of: ");
	UART0_SendUDec(SAMPLE_FREQ);
	UART0_CRLF();
	UART0_SendString("# of clk ticks between interrupts: ");
	UART0_SendUDec(period);
	UART0_CRLF();
	
  NVIC_ST_CTRL_R = 0;         // disable SysTick during setup
  NVIC_ST_RELOAD_R = period-1;// reload value
  NVIC_ST_CURRENT_R = 0;      // any write to current clears it
  NVIC_SYS_PRI3_R = (NVIC_SYS_PRI3_R&0x00FFFFFF)|0x40000000; // priority 2
                              // enable SysTick with core clock and interrupts
  NVIC_ST_CTRL_R = 0x07;
}

// Interrupt service routine
unsigned char value = 0;
bool decreasing = 0;
void TriangleWave(void)
{
	writeDAC(value, 0x00);
	if(decreasing)
	{
		if(value == 0)
		{
			decreasing = false;
			value++;
		}
		else
		{
			value--;
		}
	}
	else
	{
		if(value == 255)
		{
			decreasing = true;
			value--;
		}
		else
		{
			value++;
		}
	}
}

unsigned int ticksSinceLastRequest;
const unsigned char temp = 0x03;
void SysTick_Handler(void)
{
	unsigned char upper, lower;
	
	// TriangleWave();
	
	ticksSinceLastRequest++;
	
	// Update the DAC to match the current sample //
	upper = readBuff();
	// lower = readBuff();
	writeDAC(upper, lower);
	
	// If we're running low on samples, request some more //
	if(getPtrDifference() < 25000 && ticksSinceLastRequest > 1300)
	{ 
		UART1_SendChar('s');
		ticksSinceLastRequest = 0;
	}
}

unsigned int tickSinceLastComplain = 0;
void writeBuff(unsigned char in)
{
	
	if(sampleWritePtr == sampleReadPtr && arePtrsMisaligned)
	{ // if the write pointer caught up to the read pointer, 
		// then drop the sample
		
		UART0_SendString("Samples came in too fast");
		UART0_CRLF();
	}
	else
	{ // otherwise write to the buffer and update the pointer
			*sampleWritePtr = in;
		  
			if(sampleWritePtr - &sampleBuffer[0] < BUFFER_SIZE-1)
			{ // if the pointer isn't at the end, increment
				sampleWritePtr++;
			}
			else
			{ // Otherwise it loops back to zero
				sampleWritePtr = &sampleBuffer[0];
				// If the write ptr loops back to zero, it means that the write ptr will be a greater value than it
				arePtrsMisaligned = true;
			}
	}
}

bool songPlaying = false;
unsigned char readBuff(void)
{
	unsigned char out = 127;
	
	if(sampleWritePtr == sampleReadPtr && !arePtrsMisaligned)
	{ // if the read pointer caught up to the write pointer, 
		// then play silence
		
		if(songPlaying && tickSinceLastComplain > 10000)
		{
			tickSinceLastComplain = 0;
			UART0_SendString("Samples came in too slow");
			UART0_CRLF();
		}
		else if(songPlaying)
		{
			tickSinceLastComplain++;
		}
	}
	else
	{ // otherwise read the buffer and update the pointer
			songPlaying = true;
		
			out = *sampleReadPtr;
			
			if(sampleReadPtr - &sampleBuffer[0] < BUFFER_SIZE-1)
			{ // if the pointer isn't at the end, increment
				sampleReadPtr++;
			}
			else
			{ // Otherwise it loops back to zero
				sampleReadPtr = &sampleBuffer[0];
				// If the read ptr loops back to zero, it means that the write ptr will be a greater value than it
				arePtrsMisaligned = false;
			}
	}
	
	return out;
}

unsigned int getPtrDifference(void)
{
	if(arePtrsMisaligned)
	{
		return ((BUFFER_SIZE - (sampleReadPtr - &sampleBuffer[0])) + (sampleWritePtr - &sampleBuffer[0]));
	}
	else
	{ // Otherwise it's simple the difference
		return (sampleWritePtr - sampleReadPtr);
	}
}

void writeDAC(unsigned char upper, unsigned char lower)
{
	// 15 - 10
	GPIO_PORTB_DATA_R = upper&0xFC;
	
	// 9 - 6
	GPIO_PORTC_DATA_R = ((upper << 4)&0x30) | (lower&0xC0);
	
	// 5 - 0
	GPIO_PORTE_DATA_R = lower&0x3F;
}

void wait_for_procede(void)
{
	unsigned char in = ' ';
	while(in != ENQ)
	{
		in = UART1_GetChar();
	}
	UART1_SendChar(ACK);
}

// 80Mhz = 12.5ns period
// 1us = 80xperiod
// 80 periods/3periodsperloop = 27 per uss
void delay_ms(int t)
{
	int ulCount;
	
	int i, j;
	
	// t ms
	for(j = 0; j < t; j++)
	{	
		// 1ms
		for(i = 0; i <  900; i++)
		{
			// Delay 1us //
			ulCount = 26;
			
			do
			{
				ulCount--;
			}
			while(ulCount);
		}
	}
	
}

