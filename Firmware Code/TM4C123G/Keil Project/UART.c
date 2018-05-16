#include "tm4c123gh6pm.h"
#include "UART.h"
#include <stdio.h>

// Functions for initializing and using UART on the TM4C123G
// Some functions are specifically for the IHA speaker network

// Initialize UART0 (Port A) - USB
// UART with 115,200 baud rate (assuming 80 MHz UART clock),
// 8 bit word length, no parity bits, one stop bit, FIFOs enabled
// Used for serial terminal debug
void UART0_Init(void)
{
  SYSCTL_RCGC1_R |= SYSCTL_RCGC1_UART0; // activate UART0
  SYSCTL_RCGC2_R |= SYSCTL_RCGC2_GPIOA; // activate port A
  UART0_CTL_R &= ~UART_CTL_UARTEN;      // disable UART
  UART0_IBRD_R = 43;                    //  IBRD = floor(80,000,000 / (16 * 115,200) = 26
  UART0_FBRD_R = 26;                    //  FBRD = floor(0.4028 * 64 + 0.5) = 26
                                        // 8 bit word length (no parity bits, one stop bit, FIFOs)
  UART0_LCRH_R = (UART_LCRH_WLEN_8|UART_LCRH_FEN);
  UART0_CTL_R |= UART_CTL_UARTEN;       // enable UART
  GPIO_PORTA_AFSEL_R |= 0x03;           // enable alt funct on PA1-0
  GPIO_PORTA_DEN_R |= 0x03;             // enable digital I/O on PA1-0
                                        // configure PA1-0 as UART
  GPIO_PORTA_PCTL_R = (GPIO_PORTA_PCTL_R&0xFFFFFF00)+0x00000011;
  GPIO_PORTA_AMSEL_R &= ~0x03;          // disable analog functionality on PA
}

// Initialize UART1 (Port B)
// UART with x baud rate (assuming 80 MHz UART clock),
// 8 bit word length, no parity bits, one stop bit, FIFOs enabled
// Used for serial terminal debug
void UART1_Init(void)
{
	SYSCTL_RCGC1_R |= 0x02;               // activate UART1
	SYSCTL_RCGC2_R |= SYSCTL_RCGC2_GPIOB; // activate port B
  SYSCTL_RCGCGPIO_R |= 0x02;  
	while((SYSCTL_PRGPIO_R&0x02) == 0){};
  UART1_CTL_R &= ~0x01;                 // disable UART
  // UART1_IBRD_R = 1;                     //  IBRD = floor(80,000,000 / (16 * 2,764,800)) = 1
  // UART1_FBRD_R = 52;                    //  FBRD = floor(0.80845 * 64 + 0.5) = 52
  UART1_IBRD_R = 4;                     //  IBRD = floor(80,000,000 / (16 * 1,203,005)) = 4
  UART1_FBRD_R = 10;                     //  FBRD = floor(fract * 64 + 0.5) = 10
                                        // 8 bit word length (no parity bits, one stop bit, FIFOs)
  UART1_LCRH_R = (UART_LCRH_WLEN_8|UART_LCRH_FEN);
  UART1_CTL_R |= UART_CTL_UARTEN;       // enable UART
  GPIO_PORTB_AFSEL_R |= 0x03;           // enable alt funct on PB1-0
  GPIO_PORTB_DEN_R |= 0x03;             // enable digital I/O on PB1-0
                                        // configure PB1-0 as UART
  GPIO_PORTB_PCTL_R = (GPIO_PORTB_PCTL_R&0xFFFFFF00)+0x00000011;
  GPIO_PORTB_AMSEL_R &= ~0x03;          // disable analog functionality on PB0, PB1
}

unsigned char UART0_GetChar(void)
{
	while((UART0_FR_R&UART_FR_RXFE) != 0){};  // Wait for a char to be sent
	return((unsigned char)(UART0_DR_R&0xFF)); // return that char
}

unsigned char UART1_GetChar(void)
{
	while((UART1_FR_R&UART_FR_RXFE) != 0){};  // Wait for a char to be sent
  return((unsigned char)(UART1_DR_R&0xFF)); // return that char
}

void UART0_GetString(unsigned char *s_ptr, unsigned short max)
{
	int length = 0;
	char character;
  character = UART0_GetChar();
  while(character != CR)
	{
    if(character == BS)
		{
      if(length)
			{
        s_ptr--;
        length--;
        UART0_SendChar(BS);
      }
    }
    else if(length < max)
		{
      *s_ptr = character;
      s_ptr++;
      length++;
      UART0_SendChar(character);
    }
    character = UART0_GetChar();
  }
  *s_ptr = 0;
}

void UART1_GetString(unsigned char *s_ptr)
{
	unsigned char a,b;
	a = ' ';
	b = ' ';
	
	while(!(a == LF && b == CR))
	{ // While the last two chars aren't CRLF, fetch the string
		b = a;
		a = UART1_GetChar();
		UART0_SendChar(a);
		*s_ptr = a;
		s_ptr++;
	}
	*s_ptr = '\0';
}

void UART0_SendChar(unsigned char c)
{
  while((UART0_FR_R&UART_FR_TXFF) != 0){};
  UART0_DR_R = c;
}

void UART1_SendChar(unsigned char c)
{
	while((UART1_FR_R&UART_FR_TXFF) != 0){};
  UART1_DR_R = c;
}

void UART0_SendString(unsigned char *s_ptr)
{
	while(*s_ptr)
	{
    UART0_SendChar(*s_ptr);
    s_ptr++;
  }
}

void UART1_SendString(unsigned char *s_ptr)
{
	while(*s_ptr)
	{
    UART1_SendChar(*s_ptr);
    s_ptr++;
  }
}

void UART0_CRLF(void)
{
  UART0_SendChar(CR);
  UART0_SendChar(LF);
}

void UART1_CRLF(void)
{
  UART1_SendChar(CR);
  UART1_SendChar(LF);
}

unsigned long UART0_GetUDec(void)
{
	unsigned long number=0, length=0;
	char character;
		character = UART0_GetChar();
		while(character != CR){ // accepts until <enter> is typed
			// The next line checks that the input is a digit, 0-9.
			// If the character is not 0-9, it is ignored and not echoed
			if((character>='0') && (character<='9')) {
				number = 10*number+(character-'0');   // this line overflows if above 4294967295
				length++;
				UART0_SendChar(character);
			}
			// If the input is a backspace, then the return number is
			// changed and a backspace is outputted to the screen
			else if((character==BS) && length){
				number /= 10;
				length--;
				UART0_SendChar(character);
			}
			character = UART0_GetChar();
		}
		return number;
}

unsigned long UART1_GetUDec(void)
{
	unsigned long number=0, length=0;
	char character;
		character = UART1_GetChar();
		while(character != CR){ // accepts until <enter> is typed
			// The next line checks that the input is a digit, 0-9.
			// If the character is not 0-9, it is ignored and not echoed
			if((character>='0') && (character<='9')) {
				number = 10*number+(character-'0');   // this line overflows if above 4294967295
				length++;
				UART1_SendChar(character);
			}
			// If the input is a backspace, then the return number is
			// changed and a backspace is outputted to the screen
			else if((character==BS) && length){
				number /= 10;
				length--;
				UART1_SendChar(character);
			}
			character = UART1_GetChar();
		}
		return number;
}

void UART0_SendUDec(unsigned long n)
{
	// This function uses recursion to convert decimal number
	//   of unspecified length as an ASCII string
  if(n >= 10)
	{
    UART0_SendUDec(n/10);
    n = n%10;
  }
  UART0_SendChar(n+'0');  // n is between 0 and 9
}

void UART1_SendUDec(unsigned long n)
{
	// This function uses recursion to convert decimal number
	//   of unspecified length as an ASCII string
  if(n >= 10)
	{
    UART1_SendUDec(n/10);
    n = n%10;
  }
  UART1_SendChar(n+'0'); // n is between 0 and 9
}
