#include "app_util_platform.h"


#define HIGH	1
#define LOW 0

#define TX_RX_BUF_LENGTH        254                 /**< SPI transaction buffer length. */

#define WSBC_RST  23  //res
#define WSBC_CS   17
#define WSBC_SCLK 13 //D0
#define WSBC_SDIN  12 // MOSI

#define WSBC_SDOUT  18 // MISO
#define WSBC_RDY  24
#define WSBC_CLOCK  25

//* AFE4300 register address definitions
#define ADC_DATA_RESULT         0x00
#define ADC_CONTROL_REGISTER    0x01
#define MISC1_REGISTER          0x02
#define MISC2_REGISTER          0x03
#define DEVICE_CONTROL_1        0x09
#define ISW_MATRIX              0x0A
#define VSW_MATRIX              0x0B
#define IQ_MODE_ENABLE          0x0C
#define WEIGHT_SCALE_CONTROL    0x0D
#define BCM_DAC_FREQ            0x0E
#define DEVICE_CONTROL_2        0x0F
#define ADC_CONTROL_REGISTER_2  0x10
#define MISC3_REGISTER          0x1A
//*/

//*
#define SPICLK           2000000        //Frequency of SPI Clock
#define BAUD_RATE        115200         //Baud rate for UART
#define ADDRESS_START    (0x1900)      //Address to write calibration values to flash

#define deviceCode 0x31
#define CALI_RESISTANCE1 100.46
#define CALI_RESISTANCE2 998.9
#define HAND_FACTOR 0.43

#define CALI_CODE1 1147
#define CALI_CODE2 10538
#define thisDevice "atsn,HandBCM002\r\0"
#define SLOPE 128.8
//BCM Definitions
#define MALE    0
#define FEMALE  1
#define DB      1.05 //Body Density
//*/

#define SPI_INSTANCE  0 /**< SPI instance index. */


static uint8_t       am_tx_buf;           /**< TX buffer. */
static uint8_t       am_rx_buf[2];    /**< RX buffer. */
static uint8_t am_length = sizeof(am_tx_buf);        /**< Transfer length. */

void spi_init(void);
void spi_pin_clear(void);

void write(uint8_t Data);
int readRegister(unsigned char address);

void resetAFE4300(void);
void afe4300_init(void);
int afe4300_check (int weight, int height, int age, int gender, double *totalbodywater, double *extracellwater, double *ffm, double *bodyfat);


void toggleChipSelect(void);
void initAFE4300(void);
void initAFE4300_(void);
void initBCM(void);
void initFW(void);


void writeRegister(unsigned char address, unsigned int data);
int readRegister(unsigned char address);
