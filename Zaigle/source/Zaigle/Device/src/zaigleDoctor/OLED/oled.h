#include "app_util_platform.h"


#define HIGH	1
#define LOW 0

#define TX_RX_BUF_LENGTH        254                 /**< SPI transaction buffer length. */

#define OLED_PW  11  //pw on/off
#define OLED_RST  15  //res
#define OLED_DC   14
#define OLED_CS   16
//#define OLED_MISO 12 //D1
#define OLED_SCLK 13 //D0
#define OLED_SDIN  12 // MOSI

#define SPI_INSTANCE  0 /**< SPI instance index. */

#define OLED_TWI_INSTANCE  1 /**< SPI instance index. */

#define OLED_CMD	0
#define OLED_DATA	1

/* OLED I2C-ADDRESS */
#define OLED_ADDRESS   0x3C
//#define OLED_ADDRESS   0x1E
/* TWI PINS */
#define TWI_SCL_PIN_OLED     27
#define TWI_SDA_PIN_OLED     26


static uint8_t       am_tx_buf;           /**< TX buffer. */
static uint8_t       am_rx_buf[2];    /**< RX buffer. */
static uint8_t am_length = sizeof(am_tx_buf);        /**< Transfer length. */


void fill(uint8_t dat1,uint8_t dat2)	;
void fill_i2c(uint8_t dat1,uint8_t dat2)	;
void fillchar(uint8_t dat1)	;
void pane();
void showpic() ;
void GpsImage();


void twi_init_oled (void);
void oled_init(void);
void oled_init_i2c(void);

void Write_Command(uint8_t Data);
void Write_Data(uint8_t Data);


void OLED_Set_Pos(uint8_t x, uint8_t y) ;

void GpsImage_1();
void GpsImage_2();

ret_code_t OLED_I2C_register_write(uint8_t * p_tx_data, uint8_t bytes);
ret_code_t OLED_I2C_data_write(uint8_t p_tx_data, uint8_t bytes);
