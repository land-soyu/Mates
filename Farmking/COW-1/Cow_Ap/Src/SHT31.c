#define _SHT31_C_

#include "stm32l1xx_hal.h"
#include "SHT31.h"

#define ACK_data 0
#define NACK_data 1

#define SHT31_ADDR (0x44<<1)// (if ADDR Pin is connected to VSS )

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
void SHT31_WriteCommand(uint16_t data)
{
	I2C_start();
	I2C_write(SHT31_ADDR|0);
	I2C_write(data>>8);
	I2C_write(data&0xFF);
	I2C_stop();
	_delay_us(10);
}
void SHT31_ReadSensor(uint8_t* data)
{
	I2C_start();
	I2C_write(SHT31_ADDR|1);
	HAL_Delay(100);
	
	char i = 0;
	for( i=0; i<5; i++)
	{
		*(data+i) = I2C_read(0);
	}
	*(data+i) = I2C_read(1);
	
	I2C_stop();
	_delay_us(10);
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
void SHT31_ReadStatus(uint8_t* data)
{
	I2C_start();
	I2C_write(SHT31_ADDR|0);
	I2C_write(0xF3);
	I2C_write(0x2D);

	I2C_start();
	I2C_write(SHT31_ADDR|1);
	*data = I2C_read(0);
	*(data+1) = I2C_read(0);
	*(data+2) = I2C_read(1);
	I2C_stop();
	_delay_us(10);
}
double SHT31_ReadTemp(void)
{
	uint8_t data[6] = {0};
	double temp_result = 0;
	SHT31_WriteCommand(0x2C06);
	SHT31_ReadSensor(data);
	temp_result = (((data[0] * 256) + data[1]) * 175.0) / 65535.0  - 45.0; 
	
	return temp_result;
}
double SHT31_ReadHumi(void)
{
	uint8_t data[6] = {0};
	double humi_result = 0;
	SHT31_WriteCommand(0x2C06);
	SHT31_ReadSensor(data);
	
	humi_result = (((data[3] * 256) + data[4])) * 100.0 / 65535.0;
	return humi_result;
}
void SHT31_Init(void)
{
	SHT31_WriteCommand(0x30A2);//software reset
	HAL_Delay(20);
}
