#ifndef _MEMS
#define _MEMS

#include "i2cspm.h"

////////////////////////////////////////////////////////////////////////////
// MEMS sensor
typedef struct
{
	float x, y, z;
} MEMS_Value;

typedef enum
{
	Gravity_2G,
	Gravity_4G
} MEMS_Gravity;

typedef enum {
	Power_Down      = 0x00,
	Nomal_Low_1Hz   = 0x10,
	Nomal_Low_10Hz  = 0x20,
	Nomal_Low_25Hz  = 0x30,
	Nomal_Low_50Hz  = 0x40,
	Nomal_Low_100Hz = 0x50,
	Nomal_Low_200Hz = 0x60,
	Nomal_Low_400Hz = 0x70,
	Low_400Hz       = 0x80,
} MEMSODR;

extern MEMS_Value cur;
void		MEMS_init(MEMS_Gravity gravity);
extern void MEMS_Power_Mode(MEMSODR modr);
uint8_t MEMS_whoAmI(void);
bool MEMS_read(MEMS_Value* value);
bool MEMS_gravity(MEMS_Value* value);

/*
////////////////////////////////////////////////////////////////////////////
// Shake detector
void initShake(void);
void checkShake(void);
*/


#endif
