/**
  ******************************************************************************
  * File Name          : main.h
  * Description        : This file contains the common defines of the application
  ******************************************************************************
  *
  * COPYRIGHT(c) 2017 STMicroelectronics
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *   1. Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *   2. Redistributions in binary form must reproduce the above copyright notice,
  *      this list of conditions and the following disclaimer in the documentation
  *      and/or other materials provided with the distribution.
  *   3. Neither the name of STMicroelectronics nor the names of its contributors
  *      may be used to endorse or promote products derived from this software
  *      without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  ******************************************************************************
  */
/* Define to prevent recursive inclusion -------------------------------------*/
#ifndef __MAIN_H
#define __MAIN_H
  /* Includes ------------------------------------------------------------------*/

/* USER CODE BEGIN Includes */

/* USER CODE END Includes */

/* Private define ------------------------------------------------------------*/

#define V_TEMP_Pin GPIO_PIN_0
#define V_TEMP_GPIO_Port GPIOA
#define LED_Pin GPIO_PIN_3
#define LED_GPIO_Port GPIOA
#define GYRO_SPI_SDO_Pin GPIO_PIN_5
#define GYRO_SPI_SDO_GPIO_Port GPIOA
#define GYRO_SPI_SDI_Pin GPIO_PIN_6
#define GYRO_SPI_SDI_GPIO_Port GPIOA
#define GYRO_SPI_SCL_Pin GPIO_PIN_7
#define GYRO_SPI_SCL_GPIO_Port GPIOA
#define RF_SWITCH_Pin GPIO_PIN_0
#define RF_SWITCH_GPIO_Port GPIOB
#define RF_NRESET_Pin GPIO_PIN_1
#define RF_NRESET_GPIO_Port GPIOB
#define RF_NSS_Pin GPIO_PIN_12
#define RF_NSS_GPIO_Port GPIOB
#define RF_SCK_Pin GPIO_PIN_13
#define RF_SCK_GPIO_Port GPIOB
#define RF_MISO_Pin GPIO_PIN_14
#define RF_MISO_GPIO_Port GPIOB
#define RF_MOSI_Pin GPIO_PIN_15
#define RF_MOSI_GPIO_Port GPIOB
#define GYRO_SPI_SC_Pin GPIO_PIN_8
#define GYRO_SPI_SC_GPIO_Port GPIOA
#define GYRO_INT2_Pin GPIO_PIN_9
#define GYRO_INT2_GPIO_Port GPIOA
#define GYRO_INT1_Pin GPIO_PIN_10
#define GYRO_INT1_GPIO_Port GPIOA
#define DIO1_Pin GPIO_PIN_11
#define DIO1_GPIO_Port GPIOA
#define DIO0_Pin GPIO_PIN_12
#define DIO0_GPIO_Port GPIOA
#define DIO4_Pin GPIO_PIN_3
#define DIO4_GPIO_Port GPIOB
#define DIO5_Pin GPIO_PIN_4
#define DIO5_GPIO_Port GPIOB
#define DIO6_Pin GPIO_PIN_5
#define DIO6_GPIO_Port GPIOB

/* USER CODE BEGIN Private defines */

/* USER CODE END Private defines */

/**
  * @}
  */ 

/**
  * @}
*/ 

#endif /* __MAIN_H */
/************************ (C) COPYRIGHT STMicroelectronics *****END OF FILE****/
