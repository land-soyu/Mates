#define _SYSTEM_C_

#include <stdio.h>
#include <string.h>
#include "em_cmu.h"
#include "em_emu.h"
#include "em_chip.h"
#include "em_rtcc.h"
#include "bspconfig.h"
#include "system.h"
#include "sx1276.h"
#include "MEMS.h"
#include "api.h"

#define TESTPACKET	"STR|C0000020|+00.00|+00.00 +01.00 +02.00 +03.00 +04.00 +05.00 +06.00 +07.00 +08.00 +09.00|+00.00 +01.00 +02.00 +03.00 +04.00 +05.00 +06.00 +07.00 +08.00 +09.00|+00.00 +01.00 +02.00 +03.00 +04.00 +05.00 +06.00 +07.00 +08.00 +09.00|END"
#define ID_INDEX					4
#define TEMP_INDEX				12
#define GRAVITY_X_INDEX		20
#define GRAVITY_Y_INDEX		90
#define GRAVITY_Z_INDEX		160
#define END_INDEX					230
#define TxPacket_size		(sizeof(TESTPACKET)-1)
char TxBuf[sizeof(TESTPACKET)] = {0};
#define DEVICE_ID (0xc0000005)
int16_t TMP116_Value[5];
static uint8_t gravity_num;
uint32_t deviceId;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				System Tasking 																																							 ////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void Sys_Task_Init(void)
{
	rtccSetup();
#ifndef USE_RF_PWR_SWITCH
	SX1276->Init();
	SX1276SetOpMode(RFLR_OPMODE_SLEEP);
#endif
	memcpy(TxBuf,TESTPACKET,sizeof(TESTPACKET)-1);
	deviceId = SYSTEM_GetUnique();
	sprintf(&TxBuf[ID_INDEX],"%08x",deviceId);
}

void Temp_Sensor_Task(void)
{

	uint16_t temp_value;
	i2c_write_word(TMP116_ADDR, TMP116_REG_CONFIG, TMP116_MOD_CC);
  HAL_Delay(200);
	i2c_read_word(TMP116_ADDR, TMP116_REG_TEMP, (uint16_t*)&TMP116_Value[0]);
	if(TMP116_LIMIT <= TMP116_Value[0]) TMP116_Value[0] = TMP116_LIMIT;			// +99.99 까지만 표시
	temp_value = (float)TMP116_Value[0]*0.7812+0.5 + 9999;									// raw data * 0.007812 = real temp -> real temp X 100
	TEMPERATURE = temp_value;
	i2c_write_word(TMP116_ADDR, TMP116_REG_CONFIG, TMP116_MOD_SD);

}

void Gravity_Sensor_Task(void)
{
  uint32_t gtemp[3];
	MEMS_Power_Mode(Nomal_Low_10Hz);
	HAL_Delay(1);
	LETIMER_setup();
	SysTick_Disable();

	for(gravity_num = 0;gravity_num < 10;gravity_num++)
	{
		MEMS_gravity(&cur);
		gtemp[0] = cur.x*100+0.5 + 9999;
		gtemp[1] = cur.y*100+0.5 + 9999;
		gtemp[2] = cur.z*100+0.5 + 9999;
		GRAVITY_X(gravity_num) = gtemp[0];
		GRAVITY_Y(gravity_num) = gtemp[1];//cur.y*100+0.5;
		GRAVITY_Z(gravity_num) = gtemp[2];GPIO_PinOutToggle(USART1_RX_Port, USART1_RX_Pin);
		if(gravity_num == 9) break;
		EMU_EnterEM2(true);
	}
	MEMS_Power_Mode(Power_Down);
	SysTick_Enable();
}

void Rola_Send_Task(void)
{
	GPIO_PinModeSet(gpioPortF, 7, gpioModePushPull, 0);
	HAL_Delay(100);
	GPIO_PinModeSet(gpioPortF, 7, gpioModePushPull, 1);
	HAL_Delay(100);
	GPIO_PinModeSet(gpioPortF, 7, gpioModePushPull, 0);
	HAL_Delay(100);
	GPIO_PinModeSet(gpioPortF, 7, gpioModePushPull, 1);
	HAL_Delay(100);
	GPIO_PinModeSet(gpioPortF, 7, gpioModePushPull, 0);
	HAL_Delay(100);
	GPIO_PinModeSet(gpioPortF, 7, gpioModePushPull, 1);
	HAL_Delay(100);
	GPIO_PinModeSet(gpioPortF, 7, gpioModePushPull, 0);
	HAL_Delay(100);

#ifdef TEST_MODE
	//sprintf(TxBuf,"STR|1%07d|%+06.2f|",MAIN_COUNT,((float)TEMPERATURE-9999)/100);
	sprintf(TxBuf,"STR|%08d|%+06.2f|",MAIN_COUNT,((float)TEMPERATURE-9999)/100);
#else
	//sprintf(TxBuf,"STR|%08x|%+06.2f|",DEVICE_ID,((float)TEMPERATURE-9999)/100);
	sprintf(&TxBuf[TEMP_INDEX],"|%+06.2f|",((float)TEMPERATURE-9999)/100);
#endif

	/*
	for(uint8_t i=0;i<10;i++)
	{
		sprintf(&TxBuf[GRAVITY_X_INDEX+i*7],"%+06.2f ",((float)GRAVITY_X(i)-9999)/100);
	}
	TxBuf[GRAVITY_Y_INDEX-1] = '|';
	for(uint8_t i=0;i<10;i++)
	{
		sprintf(&TxBuf[GRAVITY_Y_INDEX+i*7],"%+06.2f ",((float)GRAVITY_Y(i)-9999)/100);
	}
	TxBuf[GRAVITY_Z_INDEX-1] = '|';
	for(uint8_t i=0;i<10;i++)
	{
		sprintf(&TxBuf[GRAVITY_Z_INDEX+i*7],"%+06.2f ",((float)GRAVITY_Z(i)-9999)/100);
	}
	//*/
	sprintf(&TxBuf[END_INDEX-1],"|END");

#ifdef USE_RF_PWR_SWITCH
	SX1276->Init();
#endif
	SX1276->StartTx((uint8_t *)TxBuf,sizeof(TxBuf)-1);
	while(1)
	{
		if(GPIO_PinInGet(DIO0_GPIO_Port,DIO0_Pin))	 // wait TxDone
		{
			// Clear Irq
			SX1276Write( REG_LR_IRQFLAGS, RFLR_IRQFLAGS_TXDONE );
			SX1276->State = STATE_TX_DONE;
			break;
		}
	}
	SX1276_Pin_PowerOff();

}

void Sys_Task(void)
{
	Temp_Sensor_Task();
//	Gravity_Sensor_Task();
	GPIO_PinOutSet(USART1_TX_Port, USART1_TX_Pin);
	Rola_Send_Task();
	GPIO_PinOutClear(USART1_TX_Port, USART1_TX_Pin);
//	HAL_Delay(1000);
	em_EM4H_LfxoRTCC();
}
