#ifndef _SHT31_H_ 
#define _SHT31_H_		
#include <stdint.h>
#ifdef _SHT31_C_
#define _SHT31_E_	
#else
#define _SHT31_E_ extern
#endif

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

_SHT31_E_ void SHT31_WriteCommand(uint16_t data);
_SHT31_E_ void SHT31_ReadSensor(uint8_t* data);
_SHT31_E_ void SHT31_ReadStatus(uint8_t* data);
_SHT31_E_ double SHT31_ReadTemp(void);
_SHT31_E_ double SHT31_ReadHumi(void);
_SHT31_E_ void SHT31_Init(void);

#endif //_SHT31_E_

