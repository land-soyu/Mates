
#define _API_C_

#include "em_cmu.h"
#include "em_emu.h"
#include "em_rtcc.h"
#include "em_letimer.h"
#include "bspconfig.h"
#include "MEMS.h"
#include "system.h"
#include "api.h"
#include	<stdio.h>
#include "SEGGER_RTT.h"
#define	RTT_PRINTF(...)	\
	do	{	\
					char	str[512];\
					sprintf(str,	__VA_ARGS__);\
					SEGGER_RTT_WriteString(0,	str);\
					}	while(0)
#define	printf	RTT_PRINTF		

#define START_TIME      0x00235945      /* 23:59:45 */
#define START_DATE      0x06171024      /* 2017 Oct 24 */

I2CSPM_Init_TypeDef fkI2CHandle = I2CSPM_INIT_DEFAULT;
uint16_t tmp116_config;
volatile uint32_t msTicks; /* counts 1ms timeTicks */


/**************************************************************************//**
 * @brief SysTick_Handler
 * Interrupt Service Routine for system tick counter
 *****************************************************************************/
void SysTick_Handler(void)
{
  msTicks++;       /* increment counter necessary in Delay()*/
}

/**************************************************************************//**
 * @brief Delays number of msTick Systicks (typically 1 ms)
 * @param dlyTicks Number of ticks to delay
 *****************************************************************************/
void HAL_Delay(uint32_t dlyTicks)
{
  uint32_t curTicks;

  curTicks = msTicks;
  while ((msTicks - curTicks) < dlyTicks) ;
}

void SysTick_Enable(void)
{
	SysTick->CTRL |= SysTick_CTRL_ENABLE_Msk;
}

void SysTick_Disable(void)
{
	SysTick->CTRL &= ~SysTick_CTRL_ENABLE_Msk;
}

void SystemClock_Config(void)
{
  /* Use the 7 MHz frequency in order to decrease time spent awake. */
  CMU_ClockSelectSet(cmuClock_HF, cmuSelect_HFRCO);
  CMU_HFRCOFreqSet(cmuHFRCOFreq_7M0Hz);
//  /* Switch HFCLK to HFXO and disable HFRCO */
//  CMU_ClockSelectSet(cmuClock_HF, cmuSelect_HFXO);
//  CMU_OscillatorEnable(cmuOsc_HFRCO, false, false);

  /* Setup SysTick Timer for 1 msec interrupts  */
  if (SysTick_Config(CMU_ClockFreqGet(cmuClock_CORE) / 1000)) while (1) ;
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				Timer																																											///////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**************************************************************************//**
 * @brief RTCC Interrupt Handler.
 *        Updates minutes and hours.
 *****************************************************************************/
void RTCC_IRQHandler(void)
{
  /* Clear interrupt source */
  RTCC_IntClear(RTCC_IF_CC1);
}

/**************************************************************************//**
 * @brief Enables LFECLK and selects clock source for RTCC
 *        Sets up the RTCC to generate an interrupt every second.
 *****************************************************************************/
void rtccSetup(void)
{
  RTCC_Init_TypeDef rtccInit = RTCC_INIT_DEFAULT;
	

  // Route the LFXO clock to RTCC.
  CMU_ClockSelectSet(cmuClock_LFE, cmuSelect_LFXO);
  CMU_ClockEnable(cmuClock_RTCC, true);

  // Enable clock to the interface with low energy modules.
  CMU_ClockEnable(cmuClock_HFLE, true);

  rtccInit.enable   = false;
  rtccInit.presc = rtccCntPresc_32768;		// 32768/32768 = 1 sec 
	rtccInit.cntWrapOnCCV1 = true;
  RTCC_Init(&rtccInit);

  /* Interrupt at given frequency. */
  RTCC_CCChConf_TypeDef ccchConf = RTCC_CH_INIT_COMPARE_DEFAULT;
  ccchConf.compMatchOutAction = rtccCompMatchOutActionToggle;
  RTCC_ChannelInit(1, &ccchConf);

	if(RTCC_CounterGet() < 1)	// RTCC RAM INIT
	{
	  for(uint8_t i=0;i<32;i++)
		{
			RTCC->RET[i].REG = 0;
		}
	}

	MAIN_COUNT += SLEEP_TIME;
	RTCC_ChannelCCVSet(1, MAIN_COUNT);

	/* Enable EM4 wakeup */
	RTCC_EM4WakeupEnable(true);
    
  /* Enable required interrupt */
  RTCC_IntEnable(RTCC_IEN_CC1);

  /* Enable RTCC interrupt */
  NVIC_ClearPendingIRQ(RTCC_IRQn);
  NVIC_EnableIRQ(RTCC_IRQn);

  /* Start Counter */
  RTCC_Enable(true);
}

/**************************************************************************//**
 * @brief LETIMER0_IRQHandler
 * Interrupt Service Routine for LETIMER
 *****************************************************************************/
void LETIMER0_IRQHandler(void)
{ 
  /* Clear LETIMER0 Compare Match 0 interrupt flag */
  LETIMER_IntClear(LETIMER0, LETIMER_IF_COMP0);
}

/**************************************************************************//**
 * @brief  LETIMER_setup
 * Configures and starts the LETIMER0
 *****************************************************************************/
void LETIMER_setup(void)
{
  LETIMER_Init_TypeDef letimerInit = LETIMER_INIT_DEFAULT;
  letimerInit.comp0Top = true;                  /* Reload CNT from TOP on underflow */

  /* Use LFRCO as LFA clock for LETIMER */
  CMU_ClockSelectSet(cmuClock_LFA, cmuSelect_LFXO);
  CMU_ClockEnable(cmuClock_CORELE, true);
  CMU_ClockEnable(cmuClock_LETIMER0, true);

  LETIMER_Init(LETIMER0, &letimerInit);
  LETIMER_CompareSet(LETIMER0, 0, 32768);   		/* every 1s */
//  LETIMER_RepeatSet(LETIMER0, 0, 1);            /* Set REP0 to a non-zero value to generate output */
  LETIMER_Enable(LETIMER0, true);

  /* Enable Compare Match 0 interrupt */  
  LETIMER_IntEnable(LETIMER0, LETIMER_IF_COMP0);  
  
  /* Enable LETIMER0 interrupt vector in NVIC*/
  NVIC_EnableIRQ(LETIMER0_IRQn);
}

/**************************************************************************//**
 * @brief   Disable high frequency clocks
 *****************************************************************************/
static void disableHFClocks(void)
{
  // Disable High Frequency Peripheral Clocks
  CMU_ClockEnable(cmuClock_HFPER, false);
  CMU_ClockEnable(cmuClock_USART0, false);
  CMU_ClockEnable(cmuClock_USART1, false);
  CMU_ClockEnable(cmuClock_TIMER0, false);
  CMU_ClockEnable(cmuClock_TIMER1, false);
  CMU_ClockEnable(cmuClock_CRYOTIMER, false);
  CMU_ClockEnable(cmuClock_ACMP0, false);
  CMU_ClockEnable(cmuClock_ACMP1, false);
  CMU_ClockEnable(cmuClock_IDAC0, false);
  CMU_ClockEnable(cmuClock_ADC0, false);
  CMU_ClockEnable(cmuClock_I2C0, false);

  // Disable High Frequency Bus Clocks
  CMU_ClockEnable(cmuClock_CRYPTO, false);
  CMU_ClockEnable(cmuClock_LDMA, false);
  CMU_ClockEnable(cmuClock_GPCRC, false);
  CMU_ClockEnable(cmuClock_GPIO, false);
  CMU_ClockEnable(cmuClock_HFLE, false);
  CMU_ClockEnable(cmuClock_PRS, false);
}

/**************************************************************************//**
 * @brief   Disable low frequency clocks
 *****************************************************************************/
static void disableLFClocks(void)
{
  // Enable LFXO for Low Frequency Clock Disables
//  CMU_OscillatorEnable(cmuOsc_LFXO, true, true);

  // Disable Low Frequency A Peripheral Clocks
  // Note: LFA clock must be sourced before modifying peripheral clock enables
  CMU_ClockSelectSet(cmuClock_LFA, cmuSelect_LFXO);
  CMU_ClockEnable(cmuClock_LETIMER0, false);
  CMU_ClockEnable(cmuClock_PCNT0, false);
  CMU_ClockSelectSet(cmuClock_LFA, cmuSelect_Disabled);

  // Disable Low Frequency B Peripheral Clocks
  // Note: LFB clock must be sourced before modifying peripheral clock enables
  CMU_ClockSelectSet(cmuClock_LFB, cmuSelect_LFXO);
  CMU_ClockEnable(cmuClock_LEUART0, false);
  CMU_ClockSelectSet(cmuClock_LFB, cmuSelect_Disabled);

  // Disable Low Frequency E Peripheral Clocks
  // Note: LFE clock must be sourced before modifying peripheral clock enables
//  CMU_ClockSelectSet(cmuClock_LFE, cmuSelect_LFXO);
//  CMU_ClockEnable(cmuClock_RTCC, false);
//  CMU_ClockSelectSet(cmuClock_LFE, cmuSelect_Disabled);

  // Disable Low Frequency Oscillator
//  CMU_OscillatorEnable(cmuOsc_LFXO, false, true);
}

/**************************************************************************//**
 * @brief   Disable all clocks to achieve lowest current consumption numbers.
 *****************************************************************************/
static void disableClocks(void)
{
  // Disable High Frequency Clocks
  disableHFClocks();

  // Disable Low Frequency Clocks
  disableLFClocks();
}

/***************************************************************************//**
 * @brief
 *   Enter EM4H with RTCC running on a LFXO.
 *
 * @details
 *   Parameter:
 *     EM4H. Hibernate Mode.@n
 *   Condition:
       RTCC, 128 byte RAM, 32.768 kHz LFXO.@n
 *
 * @note
 *   To better understand disabling clocks and oscillators for specific modes,
 *   see Reference Manual section EMU-Energy Management Unit and Table 9.2.
 ******************************************************************************/
void em_EM4H_LfxoRTCC(void)
{
  // Make sure clocks are disabled.
  disableClocks();
//  rtccSetup();
	
  // Route the LFXO clock to RTCC.
//  CMU_ClockSelectSet(cmuClock_LFE, cmuSelect_LFXO);
//  CMU_ClockEnable(cmuClock_RTCC, true);

  // Enable clock to the interface with low energy modules.
//  CMU_ClockEnable(cmuClock_HFLE, true);

  // Setup RTC parameters.
//  RTCC_Init_TypeDef rtccInit = RTCC_INIT_DEFAULT;
//  rtccInit.presc = rtccCntPresc_1;
//  rtccInit.cntWrapOnCCV1 = true;
//  rtccInit.debugRun = true;

  // Initialize RTCC. Configure RTCC with prescaler 1.
//  RTCC_Init(&rtccInit);

  // Make sure unwanted oscillators are disabled specifically for EM4H and LFXO.
//  CMU_OscillatorEnable(cmuOsc_LFRCO, false, true);

  // EM4H retains 128 byte RAM through RTCC by default.

//RTCC_EM4WakeupEnable(true);
  // Enter EM4H.
  EMU_EM4Init_TypeDef em4Init = EMU_EM4INIT_DEFAULT;
  em4Init.em4State = emuEM4Hibernate;
  em4Init.retainLfxo = true;
  em4Init.pinRetentionMode = emuPinRetentionLatch;//emuPinRetentionDisable;
  EMU_EM4Init(&em4Init);
  EMU_EnterEM4();
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				I2C 																																											///////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*Function for i2c bus read word*/
bool i2c_read_word(uint8_t dev_addr, uint8_t reg_addr, uint16_t *reg_data)
{
	I2C_TransferSeq_TypeDef    seq;
	I2C_TransferReturn_TypeDef ret;
	uint8_t i2c_read_data[2];

  seq.addr  = dev_addr<<1;
  seq.flags = I2C_FLAG_WRITE_READ;

	seq.buf[0].data = &reg_addr;
	seq.buf[0].len  = 1;
	seq.buf[1].data = i2c_read_data;
	seq.buf[1].len  = 2;

  ret = I2CSPM_Transfer(fkI2CHandle.port, &seq);
  if (ret != i2cTransferDone)
  {
		*reg_data = 0;
    return false;
  }

	*reg_data = ((uint16_t) i2c_read_data[0] << 8) + i2c_read_data[1];
  return true;
}

/*Function for i2c bus write word */
bool i2c_write_word(uint8_t dev_addr, uint8_t reg_addr, uint16_t reg_data)
{
  I2C_TransferSeq_TypeDef    seq;
  I2C_TransferReturn_TypeDef ret;
	uint8_t i2c_write_data[2];
	i2c_write_data[0] = reg_data>>8;
	i2c_write_data[1] = reg_data;

  seq.addr  = dev_addr<<1;
  seq.flags = I2C_FLAG_WRITE_WRITE;

	seq.buf[0].data = &reg_addr;
	seq.buf[0].len  = 1;
	seq.buf[1].data = i2c_write_data;
	seq.buf[1].len  = 2;

  ret = I2CSPM_Transfer(fkI2CHandle.port, &seq);
  if (ret != i2cTransferDone)
  {
    return false;
  }
  return true;
}

void Sensor_Init(void)
{
	CMU_ClockEnable( cmuClock_GPIO, true );
  /* I2C driver config */
	I2CSPM_Init(&fkI2CHandle);

	MEMS_init(Gravity_2G);	// motion sensor 초기화
	i2c_read_word(TMP116_ADDR, TMP116_REG_CONFIG, &tmp116_config);	// TMP116 config read

	MEMS_Power_Mode(Power_Down);
	i2c_write_word(TMP116_ADDR, TMP116_REG_CONFIG, TMP116_MOD_SD);
}

/*
extern ADC_HandleTypeDef hadc;
void ADC_Init(void)
{
	int cnt = 0;
	HAL_ADC_Start_DMA(&hadc,(uint32_t *)ADC_Value,ADC_NUM);
	ADC_Avrage_Cnt = 0;
	for(cnt = 0;cnt<ADC_NUM;cnt++)
	{
		ADC_Avrage[cnt] = 0;
		ADC_Result[cnt] = 0;
	}
}

void ADC_Task(void)
{
	uint8_t cnt = 0;
	ADC_Avrage_Cnt++;
	for(cnt = 0;cnt<ADC_NUM;cnt++)
	{
		ADC_Avrage[cnt] += ADC_Value[cnt];
		if(ADC_Avrage_Cnt>=256)
		{
			ADC_Result[cnt] = (uint16_t)(ADC_Avrage[cnt]/256);
			ADC_Avrage[cnt] = 0;
		}			
	}
	if(ADC_Avrage_Cnt >= 256) ADC_Avrage_Cnt = 0;
}
*/
