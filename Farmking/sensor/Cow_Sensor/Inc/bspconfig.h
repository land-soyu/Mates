/***************************************************************************//**
 * @file
 * @brief Provide BSP (board support package) configuration parameters.
 * @version 5.1.1
 *******************************************************************************
 * @section License
 * <b>Copyright 2015 Silicon Labs, Inc. http://www.silabs.com</b>
 *******************************************************************************
 *
 * This file is licensed under the Silabs License Agreement. See the file
 * "Silabs_License_Agreement.txt" for details. Before using this software for
 * any purpose, you must agree to the terms of that agreement.
 *
 ******************************************************************************/

#ifndef __SILICON_LABS_BSPCONFIG_H__
#define __SILICON_LABS_BSPCONFIG_H__

//#define USE_RF_PWR_SWITCH				1

#ifdef USE_RF_PWR_SWITCH
#define RF_PWR_Pin 							3
#define RF_PWR_GPIO_Port 				gpioPortF
#endif
#define RF_SWITCH_V1_Pin 				6
#define RF_SWITCH_V1_GPIO_Port 	gpioPortC
#define RF_SWITCH_V2_Pin 				7
#define RF_SWITCH_V2_GPIO_Port 	gpioPortC
#define RF_NRESET_Pin 					14
#define RF_NRESET_GPIO_Port 		gpioPortD
#define RF_NSS_Pin 							10
#define RF_NSS_GPIO_Port 				gpioPortD
#define RF_SCK_Pin 							13
#define RF_SCK_GPIO_Port 				gpioPortD
#define RF_MISO_Pin 						12
#define RF_MISO_GPIO_Port 			gpioPortD
#define RF_MOSI_Pin 						11
#define RF_MOSI_GPIO_Port 			gpioPortD
#define DIO0_Pin 								5
#define DIO0_GPIO_Port 					gpioPortA
#define DIO1_Pin 								4
#define DIO1_GPIO_Port 					gpioPortA
#define DIO2_Pin 								3
#define DIO2_GPIO_Port 					gpioPortA
#define DIO3_Pin 								2
#define DIO3_GPIO_Port 					gpioPortA
#define DIO4_Pin 								1
#define DIO4_GPIO_Port 					gpioPortA
#define DIO5_Pin 								0
#define DIO5_GPIO_Port 					gpioPortA
#define USART1_TX_Pin 					11
#define USART1_TX_Port 					gpioPortB
#define USART1_RX_Pin 					12
#define USART1_RX_Port 					gpioPortB


#define RF_SPI_USART 			    			(USART0)
#define RF_SPI_CLK									(cmuClock_USART0)
#define RF_SPI_USART_LOCATION_TX 		(USART_ROUTELOC0_TXLOC_LOC19)
#define RF_SPI_USART_LOCATION_RX		(USART_ROUTELOC0_RXLOC_LOC19)
#define RF_SPI_USART_LOCATION_SCLK 	(USART_ROUTELOC0_CLKLOC_LOC19)
#define RF_SPI_BAUDRATE   		    	(250000)//(115200)

/// Configuration data for SPI master using USART0.
#define SPI_MASTER_USART0                                              \
{                                                                         \
  USART0,                       /* USART port                       */    \
  _USART_ROUTELOC0_TXLOC_LOC19,  /* USART Tx pin location number     */    \
  _USART_ROUTELOC0_RXLOC_LOC19,  /* USART Rx pin location number     */    \
  _USART_ROUTELOC0_CLKLOC_LOC19, /* USART Clk pin location number    */    \
  _USART_ROUTELOC0_CSLOC_LOC19,  /* USART Cs pin location number     */    \
  8000000,                      /* Bitrate                          */    \
  8,                            /* Frame length                     */    \
  0,                            /* Dummy tx value for rx only funcs */    \
  spidrvMaster,                 /* SPI mode                         */    \
  spidrvBitOrderMsbFirst,       /* Bit order on bus                 */    \
  spidrvClockMode0,             /* SPI clock/phase mode             */    \
  spidrvCsControlApplication,   /* CS controlled by the application */    \
  spidrvSlaveStartImmediate     /* Slave start transfers immediately*/    \
}

#if !defined( EMU_DCDCINIT_STK_DEFAULT )
/* Use emlib defaults */
#define EMU_DCDCINIT_STK_DEFAULT          EMU_DCDCINIT_DEFAULT
#endif

#if !defined(CMU_HFXOINIT_STK_DEFAULT)
#define CMU_HFXOINIT_STK_DEFAULT                                                \
{                                                                               \
  true,         /* Low-power mode for EFM32 */                                  \
  false,        /* Disable auto-start on EM0/1 entry */                         \
  false,        /* Disable auto-select on EM0/1 entry */                        \
  false,        /* Disable auto-start and select on RAC wakeup */               \
  _CMU_HFXOSTARTUPCTRL_CTUNE_DEFAULT,                                           \
  0x142,        /* Steady-state CTUNE for STK boards without load caps */       \
  _CMU_HFXOSTEADYSTATECTRL_REGISH_DEFAULT,                                      \
  0x20,         /* Matching errata fix in CHIP_Init() */                        \
  0x7,          /* Recommended steady-state osc core bias current */            \
  0x6,          /* Recommended peak detection threshold */                      \
  _CMU_HFXOTIMEOUTCTRL_SHUNTOPTTIMEOUT_DEFAULT,                                 \
  0xA,          /* Recommended peak detection timeout  */                       \
  0x4,          /* Recommended steady timeout */                                \
  _CMU_HFXOTIMEOUTCTRL_STARTUPTIMEOUT_DEFAULT,                                  \
  cmuOscMode_Crystal,                                                           \
}
#endif

#define BSP_BCP_VERSION 2
//#include "bsp_bcp.h"

#endif /* __SILICON_LABS_BSPCONFIG_H__ */
