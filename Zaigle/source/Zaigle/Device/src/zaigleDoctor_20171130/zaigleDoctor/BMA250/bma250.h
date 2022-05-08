#include "app_util_platform.h"


/* BMA250 I2C-ADDRESS */
#define BMA250_ADDRESS   0x18
/* TWI PINS */
#define TWI_SCL_PIN_OLED     27
#define TWI_SDA_PIN_OLED     26


void twi_init_bma (void);
void bma250_init(void);

