#define _SYSTEM_C_

#include "stm32l1xx_hal.h"
#include "system.h"
#include "Timer.h"
#include "sx1276.h"
#include "uart1.h"

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				Timer																																																																///////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

SW_Timer_s Timer1;
void SoftwareTimer_Init()
{
	stm_init(&Timer1,HAL_GetTick,1000);			// 1ms Timer
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				System Tasking 																																																										////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

volatile uint32_t Frequency = 0;


#define MAX_TX_SIZE 250
double test_temp = 0;
double test_humi = 0;
void testTask(void)
{
	static uint16_t TxCnt = 0;
	Frequency = SX1276GetFreq();
	uint8_t data[MAX_TX_SIZE];
	for(int i=0; i<MAX_TX_SIZE; i++)
	{
		Uart1_RxProcess(i);
	}
}
uint32_t test_freq;
void ledToggleTask(void)
{
	HAL_GPIO_TogglePin(LED_GPIO_Port,LED_Pin);
}

void Sys_Task_Init(void)
{
	SoftwareTimer_Init();
	Uart1_Init();
	stm_add_ms(&Timer1,500,1,ledToggleTask);
 // stm_add_ms(&Timer1,3000,1,testTask);
	SX1276->Init();
	
}
void Sys_Task(void)
{
	stm_run(&Timer1);
	SX1276->Process();
	Uart1_To_Lora();
	//Lora_To_Uart1();
}


