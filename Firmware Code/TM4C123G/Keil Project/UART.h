// standard ASCII symbols
#define CR   0x0D
#define LF   0x0A
#define BS   0x08
#define ESC  0x1B
#define SP   0x20
#define DEL  0x7F
#define ACK  0x06
#define ENQ  0x05

void UART0_Init(void);
void UART1_Init(void);

unsigned char UART0_GetChar(void);
unsigned char UART1_GetChar(void);

void UART0_GetString(unsigned char *s_ptr, unsigned short max);
void UART1_GetString(unsigned char *s_ptr);

void UART0_SendChar(unsigned char c);
void UART1_SendChar(unsigned char c);

void UART0_SendString(unsigned char *s_ptr);
void UART1_SendString(unsigned char *s_ptr);

void UART0_CRLF(void);
void UART1_CRLF(void);

unsigned long UART0_GetUDec(void);
unsigned long UART1_GetUDec(void);

void UART0_SendUDec(unsigned long n);
void UART1_SendUDec(unsigned long n);
