#include "app_util_platform.h"

// COMMON
#define TWI_SCL_PIN     27
#define TWI_SDA_PIN     26
#define HIGH	1
#define LOW 0
#define TWI_INSTANCE  1

/* BMA250 I2C-ADDRESS */
#define BMA250_ADDRESS   0x18

// MAX30102
#define MAX30102_ADDRESS   0x57
/* MAX30100 Register Map */
#define REG_INTR_STATUS_1 0x00
#define REG_INTR_STATUS_2 0x01
#define REG_INTR_ENABLE_1 0x02
#define REG_INTR_ENABLE_2 0x03
#define REG_FIFO_WR_PTR 0x04
#define REG_OVF_COUNTER 0x05
#define REG_FIFO_RD_PTR 0x06
#define REG_FIFO_DATA 0x07
#define REG_FIFO_CONFIG 0x08
#define REG_MODE_CONFIG 0x09
#define REG_SPO2_CONFIG 0x0A
#define REG_LED1_PA 0x0C
#define REG_LED2_PA 0x0D
#define REG_PILOT_PA 0x10
#define REG_MULTI_LED_CTRL1 0x11
#define REG_MULTI_LED_CTRL2 0x12
#define REG_TEMP_INTR 0x1F
#define REG_TEMP_FRAC 0x20
#define REG_TEMP_CONFIG 0x21
#define REG_PROX_INT_THRESH 0x30
#define REG_REV_ID 0xFE
#define REG_PART_ID 0xFF
#define MAX_BRIGHTNESS 255

// OLED
#define OLED_PW  11  //pw on/off
#define OLED_RST  15  //res
/* OLED I2C-ADDRESS */
#define OLED_ADDRESS   0x3C


void twi_init (void);
void bma250_init(void);
void ppg_init(uint32_t *hb, uint32_t *sp);
void oled_init_i2c(void);
void buzzer_on(void);

ret_code_t bma250_I2C_data_read( uint8_t reg_addr,  uint8_t * p_rx_data, uint32_t bytes);
ret_code_t OLED_I2C_data_write(uint8_t p_tx_data, uint8_t bytes);
ret_code_t max30102_I2C_register_write(uint8_t reg_addr, uint8_t * p_tx_data, uint8_t bytes);
ret_code_t max30102_I2C_data_read( uint8_t reg_addr,  uint8_t * p_rx_data, uint32_t bytes);
bool maxim_max30102_read_fifo(uint32_t *pun_red_led, uint32_t *pun_ir_led);

void fill_i2c(uint8_t dat1,uint8_t dat2)	;
void fillchar(uint8_t dat1)	;
void show_zaigle();
void pane();
void showpic() ;
void GpsImage();
void	show_zaigle();
void Write_Command(uint8_t Data);
void Write_Data(uint8_t Data);
void battery_init(void);
  

void OLED_Set_Pos(uint8_t x, uint8_t y) ;

void GpsImage_1();
void GpsImage_2();

ret_code_t OLED_I2C_register_write(uint8_t * p_tx_data, uint8_t bytes);
ret_code_t OLED_I2C_data_write(uint8_t p_tx_data, uint8_t bytes);
