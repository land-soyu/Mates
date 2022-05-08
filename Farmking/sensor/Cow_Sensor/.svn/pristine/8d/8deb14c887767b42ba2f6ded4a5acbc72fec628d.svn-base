/**************************************************************************//**
 * @file
 * @brief Simple LED Blink Demo for SLSTK3401A
 * @version 5.1.2
 ******************************************************************************
 * @section License
 * <b>Copyright 2015 Silicon Labs, Inc. http://www.silabs.com</b>
 *******************************************************************************
 *
 * This file is licensed under the Silabs License Agreement. See the file
 * "Silabs_License_Agreement.txt" for details. Before using this software for
 * any purpose, you must agree to the terms of that agreement.
 *
 ******************************************************************************/

#include <stdint.h>
#include <stdbool.h>
#include "em_device.h"
#include "em_chip.h"
#include "em_cmu.h"
#include "em_emu.h"
#include "bspconfig.h"
#include "api.h"
#include "system.h"


/**************************************************************************//**
 * @brief  Main function
 *****************************************************************************/
int main(void)
{
  /* Chip errata */
  CHIP_Init();
  /* Unlatch EM4 pin retention */
  EMU_UnlatchPinRetention();

	
	SystemClock_Config();
#ifdef	DEBUG	
	SEGGER_RTT_ConfigUpBuffer(0, NULL, NULL, 0, SEGGER_RTT_MODE_BLOCK_IF_FIFO_FULL);
	//SEGGER_RTT_WriteString(0, "SEGGER Real-Time-Terminal Sample\r\n\r\n");
	//SEGGER_RTT_printf(0, "printf Test: %%c,         'S' : %c.\r\n", 'S');
#endif
	
	Sensor_Init();
	Sys_Task_Init();

  /* Infinite blink loop */
  while (1)
  {
		Sys_Task();
  }
}
