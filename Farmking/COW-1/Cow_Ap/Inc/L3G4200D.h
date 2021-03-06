#ifndef _L3G4200D_H_ 
#define _L3G4200D_H_			

#ifdef _L3G4200D_C_
#define _L3G4200D_E_	
#else
#define _L3G4200D_E_ extern
#endif

#define L3G4200D_WHO_AM_I      0x0F

#define L3G4200D_CTRL_REG1     0x20
#define L3G4200D_CTRL_REG2     0x21
#define L3G4200D_CTRL_REG3     0x22
#define L3G4200D_CTRL_REG4     0x23
#define L3G4200D_CTRL_REG5     0x24
#define L3G4200D_REFERENCE     0x25
#define L3G4200D_OUT_TEMP      0x26
#define L3G4200D_STATUS_REG    0x27

#define L3G4200D_OUT_X_L       0x28
#define L3G4200D_OUT_X_H       0x29
#define L3G4200D_OUT_Y_L       0x2A
#define L3G4200D_OUT_Y_H       0x2B
#define L3G4200D_OUT_Z_L       0x2C
#define L3G4200D_OUT_Z_H       0x2D

#define L3G4200D_FIFO_CTRL_REG 0x2E
#define L3G4200D_FIFO_SRC_REG  0x2F

#define L3G4200D_INT1_CFG      0x30
#define L3G4200D_INT1_SRC      0x31
#define L3G4200D_INT1_THS_XH   0x32
#define L3G4200D_INT1_THS_XL   0x33
#define L3G4200D_INT1_THS_YH   0x34
#define L3G4200D_INT1_THS_YL   0x35
#define L3G4200D_INT1_THS_ZH   0x36
#define L3G4200D_INT1_THS_ZL   0x37
#define L3G4200D_INT1_DURATION 0x38

void L3G4200D_Init(void);
void L3G4200D_Write(uint8_t addr, uint8_t data );
uint8_t L3G4200D_Read(uint8_t addr );
void L3G4200D_WriteBuffer(uint8_t addr, uint8_t *buffer, uint8_t size );
void L3G4200D_ReadBuffer(uint8_t addr, uint8_t *buffer, uint8_t size );
void L3G4200D_SpiOut(uint8_t data );
void L3G4200D_SpiIn( uint8_t *data );
void Get_Gyro(uint16_t *Data);



#endif //_SX1276_H_

