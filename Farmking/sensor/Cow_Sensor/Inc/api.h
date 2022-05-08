#ifndef _API_H_ 
#define _API_H_			(1)

#ifdef _API_C_
#define _API_EXT_	
#else
#define _API_EXT_ extern
#endif

#include <stdint.h>
#include "i2cspm.h"

//#define	DEBUG		1
#ifdef		DEBUG
#include <stdio.h>
#include "SEGGER_RTT.h"
#define	RTT_PRINTF(...)	\
	do	{	\
					char	str[256];\
					sprintf(str,	__VA_ARGS__);\
					SEGGER_RTT_WriteString(0,	str);\
					}	while(0)
#define	printf	RTT_PRINTF	
#endif // DEBUG

//#define TEST_MODE							1

#ifdef TEST_MODE
#define SLEEP_TIME						20	// SEC
#else
#define SLEEP_TIME						600	// SEC
#endif

#define TMP116_ADDR						0x48			//ADD0-GND
#define TMP116_RESOLUTION			0.0078125

#define TMP116_REG_TEMP				0x00
#define TMP116_REG_CONFIG			0x01

#define TMP116_MOD_DEFAULT		0x0220			//default
#define TMP116_MOD_CC					0x0220			//Continuous conversion
#define TMP116_MOD_SD					0x0620			//Shutdown

#define TMP116_LIMIT					12799				// 99.99 까지만 측정 -> 12799*0.0078125 = 99.9921875

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				RTCC RAM DATA      																																								///////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

#define MAIN_COUNT		RTCC->RET[0].REG		// main counter
#define TEMPERATURE		RTCC->RET[1].REG		// Temperature 
#define GRAVITY_X0		RTCC->RET[2].REG		// Gravity x0
#define GRAVITY_X1		RTCC->RET[3].REG		// Gravity x1
#define GRAVITY_X2		RTCC->RET[4].REG		// Gravity x2
#define GRAVITY_X3		RTCC->RET[5].REG		// Gravity x3
#define GRAVITY_X4		RTCC->RET[6].REG		// Gravity x4
#define GRAVITY_X5		RTCC->RET[7].REG		// Gravity x5
#define GRAVITY_X6		RTCC->RET[8].REG		// Gravity x6
#define GRAVITY_X7		RTCC->RET[9].REG		// Gravity x7
#define GRAVITY_X8		RTCC->RET[10].REG		// Gravity x8
#define GRAVITY_X9		RTCC->RET[11].REG		// Gravity x9
#define GRAVITY_y0		RTCC->RET[12].REG		// Gravity y0
#define GRAVITY_y1		RTCC->RET[13].REG		// Gravity y1
#define GRAVITY_y2		RTCC->RET[14].REG		// Gravity y2
#define GRAVITY_y3		RTCC->RET[15].REG		// Gravity y3
#define GRAVITY_y4		RTCC->RET[16].REG		// Gravity y4
#define GRAVITY_y5		RTCC->RET[17].REG		// Gravity y5
#define GRAVITY_y6		RTCC->RET[18].REG		// Gravity y6
#define GRAVITY_y7		RTCC->RET[19].REG		// Gravity y7
#define GRAVITY_y8		RTCC->RET[20].REG		// Gravity y8
#define GRAVITY_y9		RTCC->RET[21].REG		// Gravity y9
#define GRAVITY_z0		RTCC->RET[22].REG		// Gravity z0
#define GRAVITY_z1		RTCC->RET[23].REG		// Gravity z1
#define GRAVITY_z2		RTCC->RET[24].REG		// Gravity z2
#define GRAVITY_z3		RTCC->RET[25].REG		// Gravity z3
#define GRAVITY_z4		RTCC->RET[26].REG		// Gravity z4
#define GRAVITY_z5		RTCC->RET[27].REG		// Gravity z5
#define GRAVITY_z6		RTCC->RET[28].REG		// Gravity z6
#define GRAVITY_z7		RTCC->RET[29].REG		// Gravity z7
#define GRAVITY_z8		RTCC->RET[30].REG		// Gravity z8
#define GRAVITY_z9		RTCC->RET[31].REG		// Gravity z9

#define GRAVITY_X(num)		RTCC->RET[num+2].REG		// Gravity x
#define GRAVITY_Y(num)		RTCC->RET[num+12].REG		// Gravity y
#define GRAVITY_Z(num)		RTCC->RET[num+22].REG		// Gravity z

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				API       																																								///////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
_API_EXT_ uint16_t tmp116_config;


void HAL_Delay(uint32_t dlyTicks);
_API_EXT_ void SysTick_Enable(void);
_API_EXT_ void SysTick_Disable(void);
_API_EXT_ void SystemClock_Config(void);
_API_EXT_ void rtccSetup(void);
_API_EXT_ void LETIMER_setup(void);
_API_EXT_ void em_EM4H_LfxoRTCC(void);

_API_EXT_ bool i2c_read_word(uint8_t dev_addr, uint8_t reg_addr, uint16_t *reg_data);
_API_EXT_ bool i2c_write_word(uint8_t dev_addr, uint8_t reg_addr, uint16_t reg_data);
_API_EXT_ void Sensor_Init(void);


#endif	//_API_H_


