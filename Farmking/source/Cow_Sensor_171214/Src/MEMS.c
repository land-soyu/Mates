#include "bspconfig.h"
#include "MEMS.h"


////////////////////////////////////////////////////////////////////////////
// MEMS sensor i2c interface
#define DEVICE_ID							(0x18)
#define SA0_CONNECTED_TO_VCC	(1)
#define SA0_CONNECTED_TO_GND	(0)
#define SA0_CONFIG						(SA0_CONNECTED_TO_VCC)//(SA0_CONNECTED_TO_GND)
#define SLAVE_ADDRESS					(DEVICE_ID | SA0_CONFIG)

// register definition
#define WHO_AM_I				0x0F
#define CTRL_REG1				0x20
#define CTRL_REG2				0x21
#define CTRL_REG3				0x22
#define CTRL_REG4				0x23
#define CTRL_REG5				0x24
//#define HP_FILTER_RESET	0x25
#define STATUS_REG			0x27
#define OUT_X_L					0x28
#define OUT_X_H					0x29
#define OUT_Y_L					0x2A
#define OUT_Y_H					0x2B
#define OUT_Z_L					0x2C
#define OUT_Z_H					0x2D
#define INT1_CFG				0x30
#define INT1_SOURCE			0x31
#define INT1_THS				0x32
#define INT1_DURATION		0x33
#define INT2_CFG				0x34
#define INT2_SOURCE			0x35
#define INT2_THS				0x36
#define INT2_DURATION		0x37
#define DIV_VALUE_2G		(16384.0f) // 2G
#define DIV_VALUE_4G		(8192) // 4G

#define CTRL_REG6	0x25
#define REFEREMCE	0x26

////////////////////////////////////////////////////////////////////////////
// MEMS sensor
typedef enum {
	X_AXIS,
	Y_AXIS,
	Z_AXIS
} MEMSItem;

// Shake gesture definition
#define MIN_FORCE 					1 		// Minimum movement force to consider
#define MIN_DIRECTION_CHANGE 		2 		// Minimum times in a shake gesture that the direction of movement needs to change
#define MAX_TOTAL_DURATION_OF_SHAKE 400 	// Maximum allowed time for shake gesture
#define MAX_SLEEP					400

typedef enum {
	SH_WAIT_SHAKE,
	SH_NEXT_SLEEP,
} ShakeState;

extern I2CSPM_Init_TypeDef fkI2CHandle;
//static MEMS_Value last;
//static ShakeState m_shakeState = SH_WAIT_SHAKE;
//static int        m_shakeCount = 0;
//static T_Timer    m_shakeTimer;
static uint32_t   m_divValue = DIV_VALUE_2G;
MEMS_Value cur;

/*Function for i2c bus read */
bool i2c_read_byte(uint8_t dev_addr, uint8_t reg_addr, uint8_t *reg_data)
{
	I2C_TransferSeq_TypeDef    seq;
	I2C_TransferReturn_TypeDef ret;

  seq.addr  = dev_addr<<1;
  seq.flags = I2C_FLAG_WRITE_READ;

	seq.buf[0].data = &reg_addr;
	seq.buf[0].len  = 1;
	seq.buf[1].data = reg_data;
	seq.buf[1].len  = 1;

  ret = I2CSPM_Transfer(fkI2CHandle.port, &seq);
  if (ret != i2cTransferDone)
  {
    return false;
  }
  return true;
}

/*Function for i2c bus write byte */
bool i2c_write_byte(uint8_t dev_addr, uint8_t reg_addr, uint8_t reg_data)
{
  I2C_TransferSeq_TypeDef    seq;
  I2C_TransferReturn_TypeDef ret;
	uint8_t i2c_write_data[1];
	i2c_write_data[0] = reg_data;

  seq.addr  = dev_addr<<1;
  seq.flags = I2C_FLAG_WRITE_WRITE;

	seq.buf[0].data = &reg_addr;
	seq.buf[0].len  = 1;
	seq.buf[1].data = i2c_write_data;
	seq.buf[1].len  = 1;

  ret = I2CSPM_Transfer(fkI2CHandle.port, &seq);
  if (ret != i2cTransferDone)
  {
    return false;
  }
  return true;
}

void MEMS_init(MEMS_Gravity gravity)
{
	/* 
	i2c_write_byte(SLAVE_ADDRESS, CTRL_REG1, 0x27); // REG1 normal low power mode(10hz) xyz enable
	i2c_write_byte(SLAVE_ADDRESS, CTRL_REG2, 0x00); // REG2 
	i2c_write_byte(SLAVE_ADDRESS, CTRL_REG3, 0x00); // REG3
	i2c_write_byte(SLAVE_ADDRESS, INT1_THS,  0x00); // THS1
	
	uint8_t value = 0;
	switch (gravity)
	{
		case Gravity_4G: 
			value = 0x10; 
			m_divValue = DIV_VALUE_4G;
			break;
		case Gravity_2G:
		default : 
			value = 0x00; 
			m_divValue = DIV_VALUE_2G;
			break;
	}
	
	i2c_write_byte(SLAVE_ADDRESS, CTRL_REG4, 	   value); // FS1-FS0 
	i2c_write_byte(SLAVE_ADDRESS, INT2_THS, 	   0x00);  // THS2
	i2c_write_byte(SLAVE_ADDRESS, INT2_DURATION, 0x00);  // DUR2
	*/
	i2c_write_byte(SLAVE_ADDRESS, CTRL_REG1, 0x27); // REG1 normal low power mode(10hz) xyz enable
	i2c_write_byte(SLAVE_ADDRESS, CTRL_REG2, 0x00); // REG2 
	i2c_write_byte(SLAVE_ADDRESS, CTRL_REG3, 0x00); // REG3
	i2c_write_byte(SLAVE_ADDRESS, INT1_THS,  0x00); // THS1
	
	uint8_t value = 0;
	switch (gravity)
	{
		case Gravity_4G: 
			value = 0x10; 
			m_divValue = DIV_VALUE_4G;
			break;
		case Gravity_2G:
		default : 
			value = 0x00; 
			m_divValue = DIV_VALUE_2G;
			break;
	}
	
	i2c_write_byte(SLAVE_ADDRESS, CTRL_REG4, 	   value); // FS1-FS0 
	i2c_write_byte(SLAVE_ADDRESS, INT2_THS, 	   0x00);  // THS2
	i2c_write_byte(SLAVE_ADDRESS, INT2_DURATION, 0x00);  // DUR2

	/*
	  i2c_write_byte(SLAVE_ADDRESS, CTRL_REG5, 	   0x00); 
  	i2c_write_byte(SLAVE_ADDRESS, CTRL_REG6, 	   0x00); 
		i2c_write_byte(SLAVE_ADDRESS, REFEREMCE, 	   0x00); 
		i2c_write_byte(SLAVE_ADDRESS, INT1_DURATION, 	   0x00); 
		i2c_write_byte(SLAVE_ADDRESS, INT1_CFG, 	   0x00); 
		i2c_write_byte(SLAVE_ADDRESS, INT2_CFG, 	   0x00); 
	  i2c_write_byte(SLAVE_ADDRESS, CTRL_REG5, 	   0x00); 
//*/
	

}

void MEMS_Power_Mode(MEMSODR modr)
{
	i2c_write_byte(SLAVE_ADDRESS, CTRL_REG1, modr|0x07); // REG1 normal low power mode(10hz) xyz enable
}

uint8_t MEMS_whoAmI(void)
{
	uint8_t value;
	i2c_read_byte(SLAVE_ADDRESS, WHO_AM_I, &value);
	return value;
}

static bool MEMS_readInternal(MEMSItem item, int16_t* value)
{
	uint8_t high, low;
	
	switch (item)
	{
		case X_AXIS : high = OUT_X_H; low = OUT_X_L; break;
		case Y_AXIS : high = OUT_Y_H; low = OUT_Y_L; break;
		case Z_AXIS : high = OUT_Z_H; low = OUT_Z_L; break;
	}
	
	if (!i2c_read_byte(SLAVE_ADDRESS, low, &low))   return false;
	if (!i2c_read_byte(SLAVE_ADDRESS, high, &high)) return false;
	
	*value = (high << 8) | low;
	
	return true;
}

bool MEMS_gravity(MEMS_Value* value)
{
	if (!MEMS_read(value)) return false;
	
	value->x /= m_divValue;
	value->y /= m_divValue;
	value->z /= m_divValue;
	
	return true;
}

bool MEMS_read(MEMS_Value* value)
{
	int16_t item;
	if (!MEMS_readInternal(X_AXIS, &item)) return false;
	value->x = item;
	if (!MEMS_readInternal(Y_AXIS, &item)) return false;
	value->y = item;
	if (!MEMS_readInternal(Z_AXIS, &item)) return false;
	value->z = item;
	
	return true;
}
/*
////////////////////////////////////////////////////////////////////////////
// shake detector
void initShake(void)
{
	m_shakeState = SH_WAIT_SHAKE;
	m_shakeCount = 0;
	initTimer(&m_shakeTimer);
}

void checkShake(void)
{
	MEMS_Value cur;	
	if (!MEMS_gravity(config, &cur))
		return;
	
	// calculate movement
    float totalMovement = cur.x-last.x + cur.y-last.y + cur.z-last.z;
	if (totalMovement < 0) totalMovement = -totalMovement;
	
	switch (m_shakeState)
	{
		case SH_WAIT_SHAKE :
			if (totalMovement >= MIN_FORCE)
			{
				m_shakeCount++;
				if (m_shakeCount == MIN_DIRECTION_CHANGE)
				{
					BT_trace("Shake");
					pushEvent(EVT_Shake, true);
					m_shakeState = SH_NEXT_SLEEP;
					initTimer(&m_shakeTimer);
				}
			}
			if (isTimeout(&m_shakeTimer, MAX_TOTAL_DURATION_OF_SHAKE))
				initShake();
			break;
		case SH_NEXT_SLEEP :
			if (isTimeout(&m_shakeTimer, MAX_SLEEP))
				initShake();
			break;
		default:
			initShake();
			break;
	}
	
	last = cur;
}
*/

