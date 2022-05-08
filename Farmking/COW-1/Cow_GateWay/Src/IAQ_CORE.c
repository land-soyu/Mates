#define _IAQ_CORE_C_

#include "stm32l1xx_hal.h"
#include "IAQ_CORE.h"
#include "SHT31.h"

#define ACK_data 0
#define NACK_data 1

#define IAQ_CORE_ADDR (0xB5) //read

#if 0
void _delay_us(uint32_t us);
void SDA_Set(void);
void SDA_Reset(void);
void SCL_Set(void);
void SCL_Reset(void);
void I2C_clock(void);
void I2C_start(void);
void I2C_stop(void);
void I2C_nack(void);
void I2C_ack(void);
void I2C_write(char d);
uint8_t I2C_read(char ack);
#endif

#if 0
void _delay_us(uint32_t us)
{
	uint32_t count=us*8-2;
    while(count--);
}
void SDA_Set(void)
{
	HAL_GPIO_WritePin(I2C1_SDA_GPIO_Port,I2C1_SDA_Pin,(GPIO_PinState)1);
}
 
void SDA_Reset(void)
{
	HAL_GPIO_WritePin(I2C1_SDA_GPIO_Port,I2C1_SDA_Pin,(GPIO_PinState)0);
}
 
void SCL_Set(void)
{
	HAL_GPIO_WritePin(I2C1_SCL_GPIO_Port,I2C1_SCL_Pin,(GPIO_PinState)1);
}
void SCL_Reset(void)
{
	HAL_GPIO_WritePin(I2C1_SCL_GPIO_Port,I2C1_SCL_Pin,(GPIO_PinState)0);
}


void I2C_clock(void)
{
	SCL_Set();
	_delay_us(10);
	SCL_Reset();
	_delay_us(10);
}
void I2C_start(void)
{
	SDA_Set();
	SCL_Set();
	_delay_us(10);
	SDA_Reset();
	_delay_us(10);
	SCL_Reset();
	_delay_us(10);
}
void I2C_stop(void)
{
	SDA_Reset();
	_delay_us(10);
	SCL_Set();
	_delay_us(10);
	SDA_Set();
	_delay_us(10);
}
void I2C_nack(void)
{
	SDA_Set();
	I2C_clock();
}
void I2C_ack(void)
{
	SDA_Reset();
	I2C_clock();
}
void I2C_write(char d)
{
	char i;
	for(i=0;i<8;i++)
	{
		if(d&(0x80>>i))
			SDA_Set();
		else 
			SDA_Reset();
	I2C_clock();
	}
	I2C_nack();
}
uint8_t I2C_read(char ack)
{
	uint8_t i,data=0;
	SDA_Set();

	GPIO_InitTypeDef GPIO_InitStruct;

	GPIO_InitStruct.Pin = I2C1_SDA_Pin ;
	GPIO_InitStruct.Mode = GPIO_MODE_INPUT;
	GPIO_InitStruct.Pull = GPIO_NOPULL;
	HAL_GPIO_Init(GPIOB, &GPIO_InitStruct);

	for(i=0;i<8;i++)
	{
		_delay_us(10);
		SCL_Set();
		_delay_us(10);
		
		if(HAL_GPIO_ReadPin(I2C1_SDA_GPIO_Port, I2C1_SDA_Pin))
		{
			data |= 0x80>>i;
		}
		SCL_Reset();
	}

	GPIO_InitStruct.Pin = I2C1_SDA_Pin;
	GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_OD;
	GPIO_InitStruct.Pull = GPIO_NOPULL;
	GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_LOW;
	HAL_GPIO_Init(GPIOB, &GPIO_InitStruct);
	
	if(ack==ACK_data)	I2C_ack();
	else 				I2C_nack();
	return data;
}
#endif
void IAQ_CORE_Read(uint8_t *data)
{
	I2C_start();
	I2C_write(IAQ_CORE_ADDR);
	
	char i = 0;
	for( i=0; i<8; i++)
	{
		*(data+i) = I2C_read(0);
	}
	*(data+i) = I2C_read(1);
	
	I2C_stop();
	_delay_us(10);
}
