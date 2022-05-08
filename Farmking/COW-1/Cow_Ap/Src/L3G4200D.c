#define _L3G4200D_C_

#include <string.h>
#include "stm32l1xx_hal.h"
#include "L3G4200D.h"
#include <stdint.h>

#define HIGH (1)
#define LOW (0)

void L3G4200D_Init(void)
{
	//pDriver->CS(HIGH);
	//pDriver->SCK(HIGH);
	HAL_GPIO_WritePin(GYRO_SPI_SC_GPIO_Port, GYRO_SPI_SC_Pin,(GPIO_PinState)1);
	HAL_GPIO_WritePin(GYRO_SPI_SCL_GPIO_Port, GYRO_SPI_SCL_Pin,(GPIO_PinState)1);
	HAL_Delay(10);

}
void L3G4200D_Write(uint8_t addr, uint8_t data )
{
	L3G4200D_WriteBuffer(addr, &data, 1 );
}

uint8_t L3G4200D_Read(uint8_t addr )
{
    uint8_t data;
    L3G4200D_ReadBuffer(addr, &data, 1 );
    return data;
}
//BIT 0 : R/W 0: write
//			  1: read
//BIT 1 : MS  1: address is auto-incremented 
//BIT 2-7 	   : register address

void L3G4200D_WriteBuffer(uint8_t addr, uint8_t *buffer, uint8_t size )
{
	uint8_t i;
	//pDriver->CS(LOW);
	HAL_GPIO_WritePin(GYRO_SPI_SC_GPIO_Port, GYRO_SPI_SC_Pin,(GPIO_PinState)0);
	HAL_Delay(1);
	addr &= 0x7F; //0x0111 1111 
	//addr &= 0x3F; //0x0011 1111 
	L3G4200D_SpiOut(addr);
    for( i = 0; i < size; i++ )
    {
        L3G4200D_SpiOut(buffer[i] );
    }
	HAL_Delay(1);
	HAL_GPIO_WritePin(GYRO_SPI_SC_GPIO_Port, GYRO_SPI_SC_Pin,(GPIO_PinState)1);
}

void L3G4200D_ReadBuffer(uint8_t addr, uint8_t *buffer, uint8_t size )
{
    uint8_t i;
	HAL_GPIO_WritePin(GYRO_SPI_SC_GPIO_Port, GYRO_SPI_SC_Pin,(GPIO_PinState)0);
	HAL_Delay(1);
	//addr &= 0xFF; //0x1111 1111
	addr |= 0x80; 
	L3G4200D_SpiOut(addr);
	for( i = 0; i < size; i++ )
	{
		L3G4200D_SpiIn(&buffer[i] );
	}
	HAL_Delay(1);
	HAL_GPIO_WritePin(GYRO_SPI_SC_GPIO_Port, GYRO_SPI_SC_Pin,(GPIO_PinState)1);
}
void L3G4200D_SpiOut(uint8_t data )
{
	uint8_t bit_mask;
	HAL_GPIO_WritePin(GYRO_SPI_SCL_GPIO_Port, GYRO_SPI_SCL_Pin,(GPIO_PinState)0);
	HAL_Delay(1);
	for(bit_mask = 0x80; bit_mask > 0x00 ; bit_mask >>=1)
	{
		if((bit_mask & data) == bit_mask)		{ HAL_GPIO_WritePin(GYRO_SPI_SDI_GPIO_Port, GYRO_SPI_SDI_Pin,(GPIO_PinState)1); }	//Data_H
		else									{ HAL_GPIO_WritePin(GYRO_SPI_SDI_GPIO_Port, GYRO_SPI_SDI_Pin,(GPIO_PinState)0); }	//Data_L
		HAL_GPIO_WritePin(GYRO_SPI_SCL_GPIO_Port, GYRO_SPI_SCL_Pin,(GPIO_PinState)1);
		HAL_Delay(1);
		HAL_GPIO_WritePin(GYRO_SPI_SCL_GPIO_Port, GYRO_SPI_SCL_Pin,(GPIO_PinState)0);
		HAL_Delay(1);
	}
	//HAL_GPIO_WritePin(GYRO_SPI_SCL_GPIO_Port, GYRO_SPI_SCL_Pin,(GPIO_PinState)1);
}
void L3G4200D_SpiIn(uint8_t *data )
{
	uint8_t bit_mask;
	HAL_GPIO_WritePin(GYRO_SPI_SCL_GPIO_Port, GYRO_SPI_SCL_Pin,(GPIO_PinState)0);
	HAL_Delay(1);
	*data = 0;
	for(bit_mask = 0x80; bit_mask > 0x00 ; bit_mask >>=1)
	{
		
		HAL_GPIO_WritePin(GYRO_SPI_SCL_GPIO_Port, GYRO_SPI_SCL_Pin,(GPIO_PinState)1);
		HAL_Delay(1);
		if( HAL_GPIO_ReadPin(GYRO_SPI_SDO_GPIO_Port,GYRO_SPI_SDO_Pin ) == 1)
		{
			*data |= bit_mask;
		}	//Data_H
		HAL_GPIO_WritePin(GYRO_SPI_SCL_GPIO_Port, GYRO_SPI_SCL_Pin,(GPIO_PinState)0);
		HAL_Delay(1);
	}
	//HAL_GPIO_WritePin(GYRO_SPI_SCL_GPIO_Port, GYRO_SPI_SCL_Pin,(GPIO_PinState)1);
}
void Get_Gyro(uint16_t *Data)
{
	uint8_t Buf[6] = {0};
	L3G4200D_ReadBuffer(L3G4200D_OUT_X_L,&Buf[0],6);
	
	Data[0] = (Buf[1]<<8) | Buf[0] ;
	Data[1] = (Buf[3]<<8) | Buf[2] ;
	Data[2] = (Buf[5]<<8) | Buf[4] ;
	#if 0
	 raw.x = (((int)_buff[1]) << 8) | _buff[0];
  raw.y = (((int)_buff[3]) << 8) | _buff[2];
  raw.z = (((int)_buff[5]) << 8) | _buff[4];
  #endif
	
}


