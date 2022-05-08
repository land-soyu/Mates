#define _UART1_C_

/* USER CODE BEGIN */
#include "main.h"	
/* USER CODE END */
#include "stm32l1xx_hal.h"
#include "uart1.h"
#include "sx1276.h"

extern UART_HandleTypeDef huart1;

//두개의 틱을 비교하여 틱 차이를 반환한다 (단위 ms)
uint32_t TimeOut_Tick(uint32_t Now_Tick, uint32_t Prev_Tick)
{
	uint32_t Past_Tick = 0;
			
	if( Now_Tick == Prev_Tick ){ return 0; }
	if( Now_Tick > Prev_Tick ){ Past_Tick = Now_Tick - Prev_Tick; }
	else { Past_Tick = 0xFFFFFFFF - Prev_Tick + Now_Tick; }

	return Past_Tick;
}
uint8_t Uart1_PutChar(char c)
{
	return HAL_UART_Transmit(&huart1,(uint8_t *)&c,1,50);
}
//데이터 반환 성공시 1
uint8_t Uart1_GetRx(uint8_t *data)
{
	if( Uart1.Rx.Rear != Uart1.Rx.Front )
	{
		*data = Uart1.Rx.Buf[Uart1.Rx.Front++];
		if( Uart1.Rx.Front >= UART1_RX_BUF_SIZE ){ Uart1.Rx.Front = 0; }
		return 1;
	}
	return 0;
}
uint8_t Uart1_GetTx(uint8_t *data)
{
	if( Uart1.Tx.Rear != Uart1.Tx.Front )
	{
		*data = Uart1.Tx.Buf[Uart1.Tx.Front++];
		if( Uart1.Tx.Front >= UART1_TX_BUF_SIZE ){ Uart1.Tx.Front = 0; }
		return 1;
	}
	return 0;
}
#define MAX_TEST_BUF_SIZE 256
uint8_t TestBuf[MAX_TEST_BUF_SIZE] = {0};
uint16_t TestRear = 0;
uint16_t TestFront = 0;

void Uart1_RxProcess(uint8_t c)
{	TestBuf[TestRear] = c;
	TestRear++;
	if( TestRear >= MAX_TEST_BUF_SIZE ){ TestRear = 0; }
	
	// Uart1_PutChar(c);
	Uart1.Rx.Buf[Uart1.Rx.Rear++] = c;
	if(Uart1.Rx.Rear >= UART1_RX_BUF_SIZE ){ Uart1.Rx.Rear = 0; }
	Uart1.Rx.Tick = HAL_GetTick();
}

void Uart1_Init(void)
{
	memset(&Uart1,0,sizeof(Uart1_Struct));
	__HAL_UART_ENABLE_IT(&huart1, UART_IT_RXNE);
}
#define LORA_TX_MS 50 //LORA_TX_WAIT_MS
#define LORA_TX_LEN 250
void Uart1_To_Lora(void)
{
	uint32_t now_tick = 0;
	uint8_t buffer[UART1_RX_BUF_SIZE];
	uint16_t pos = 0;

	if( SX1276->State == STATE_TX_RUNNING ) return;
	now_tick = HAL_GetTick();
	if( now_tick - Uart1.Rx.Tick <= LORA_TX_MS ) return;
	while(Uart1.Rx.Rear != Uart1.Rx.Front )	
	{
		buffer[pos++] = Uart1.Rx.Buf[Uart1.Rx.Front] ;
		Uart1.Rx.Front++;
		if( Uart1.Rx.Front >= UART1_RX_BUF_SIZE ) Uart1.Rx.Front = 0;
		if( pos >= LORA_TX_LEN ) break;
	}
	
	if( pos != 0  )
	SX1276StartTx(buffer, pos);
}
void Lora_RxProcess(uint8_t c)
{	
	Uart1.Tx.Buf[Uart1.Tx.Rear] = c;
	Uart1.Tx.Rear++;
	if(Uart1.Tx.Rear >= UART1_TX_BUF_SIZE ){ Uart1.Tx.Rear = 0; }
}
void Lora_To_Uart1(void)
{
	uint8_t TxData;
	while(Uart1.Tx.Rear != Uart1.Tx.Front )	
	{
		TxData = Uart1.Tx.Buf[Uart1.Tx.Front] ;
		Uart1.Tx.Front++;
		Uart1_PutChar(TxData);
		if( Uart1.Tx.Front >= UART1_TX_BUF_SIZE ) Uart1.Tx.Front = 0;
	}
}


