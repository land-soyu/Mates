#ifndef _UART1_H_ 
#define _UART1_H_			

#ifdef _UART1_C_
#define _UART1_EXT_	
#else
#define _UART1_EXT_ extern
#endif

#define UART1_RX_BUF_SIZE	(512)
#define UART1_TX_BUF_SIZE	(512)

_UART1_EXT_ void Uart1_RxProcess(uint8_t c);
_UART1_EXT_ uint8_t Uart1_PutChar(char c);
_UART1_EXT_ uint8_t Uart1_GetRx(uint8_t *data);
_UART1_EXT_ uint8_t Uart1_GetTx(uint8_t *data);
_UART1_EXT_ void Uart1_RxProcess(uint8_t c);
_UART1_EXT_ void Uart1_To_Lora(void);
_UART1_EXT_ void Lora_RxProcess(uint8_t c);
_UART1_EXT_ void Lora_To_Uart1(void);
_UART1_EXT_ void Uart1_Init(void);
_UART1_EXT_ void Uart1_Process(void);

typedef struct
{
	uint8_t Buf[UART1_RX_BUF_SIZE];
	uint8_t Front;
	uint8_t Rear;
	uint32_t Tick;
}Uart1_Rx;
typedef struct
{
	uint8_t Buf[UART1_TX_BUF_SIZE];
	uint8_t Front;
	uint8_t Rear;
	uint32_t Tick;
}Uart1_Tx;
typedef struct
{
	Uart1_Rx Rx;
	Uart1_Tx Tx;
}Uart1_Struct;


_UART1_EXT_ Uart1_Struct Uart1;
#endif //_UART1_H_

