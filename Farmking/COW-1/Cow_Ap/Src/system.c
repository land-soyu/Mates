#define _SYSTEM_C_

#include "stm32l1xx_hal.h"
#include "system.h"
#include "Timer.h"
#include "sx1276.h"
#include "api.h"
#include "L3G4200D.h"
#include <stdio.h>

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
uint8_t who_am_i = 0;
uint16_t Gyro_Buf[3] = {0};

uint8_t TxBuf[30] = {0};
#define DEVICE_ID (0xC0000020)
void testTask(void)
{
	//Frequency = SX1276GetFreq();
   // L3G4200D_ReadBuffer(0x0F,&who_am_i,1);//0xD3 is correct response
    Get_Gyro(&Gyro_Buf[0]);
   //sprintf(TxBuf,"%x %x %x %x",ADC_Value[0],Gyro_Buf[0],Gyro_Buf[1],Gyro_Buf[2]);
   
   sprintf(TxBuf,"%x:%x %x %x %x\r\n",DEVICE_ID,ADC_Value[0],Gyro_Buf[0],Gyro_Buf[1],Gyro_Buf[2]);
	SX1276->StartTx(TxBuf,sizeof(TxBuf));
}
uint32_t test_freq;
void ledToggleTask(void)
{
	HAL_GPIO_TogglePin(LED_GPIO_Port,LED_Pin);
}

void Sys_Task_Init(void)
{
	SoftwareTimer_Init();
	ADC_Init();
	stm_add_ms(&Timer1,500,1,ledToggleTask);
	stm_add_ms(&Timer1,10,1,ADC_Task);
    stm_add_ms(&Timer1,1000*10*6*30,1,testTask);
	
	L3G4200D_Init();
	L3G4200D_Write(L3G4200D_CTRL_REG1,0x0F);
	L3G4200D_Write(L3G4200D_CTRL_REG4,0x80);
		
	SX1276->Init();
}
void Sys_Task(void)
{
	stm_run(&Timer1);
	SX1276->Process();
}


