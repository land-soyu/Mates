#include "spi.h"
#include "nrf_drv_spi.h"
#include "app_util_platform.h"
#include "nrf_gpio.h"
#include "nrf_delay.h"
#include "boards.h"
#include "app_error.h"
#include <string.h>
#include "nrf_log.h"
#include "nrf_log_ctrl.h"
#include "nrf_log_default_backends.h"

const nrf_drv_spi_t spi = NRF_DRV_SPI_INSTANCE(SPI_INSTANCE);  /**< SPI instance. */
volatile bool spi_xfer_done;  /**< Flag used to indicate that SPI instance completed the transfer. */

//*	AFE4300	
double resistance = 0;
//BCM variables
int voltageCodeCounter = 0; //Count the number of time voltage is measured
long voltageCode = 0; //Code in ADC register indicating voltage
long voltageCodeArray[8]; //Array to store successive samples of data

//Slope and Y-intercept of calibration data (BCM FW)
double fwSlope = 13.484;
double fwYint = 108.349;
//Calibration resistances
double caliResistance1 = CALI_RESISTANCE1;
double caliResistance2 = CALI_RESISTANCE2;
//Voltage codes for calibration resistors
long caliVoltageCode1 = CALI_CODE1;
long caliVoltageCode2 = CALI_CODE2;


void spi_event_handler(nrf_drv_spi_evt_t const * p_event,
                       void *                    p_context)
{
    //NRF_LOG_INFO("spi_event_handler... ");
		spi_xfer_done = true;
    if (am_rx_buf[0] != 0)
    {
        //NRF_LOG_INFO(" Received: %d", am_rx_buf[0]);
        //NRF_LOG_HEXDUMP_INFO(am_rx_buf, strlen((const char *)am_rx_buf));
    }
    //NRF_LOG_FLUSH();
}

void spi_init(void)
{
    nrf_drv_spi_config_t spi_config = NRF_DRV_SPI_DEFAULT_CONFIG;
		spi_config.orc =0xCC;
		spi_config.frequency = NRF_DRV_SPI_FREQ_1M;
		spi_config.mode =NRF_DRV_SPI_MODE_3;
		spi_config.bit_order =NRF_DRV_SPI_BIT_ORDER_MSB_FIRST;
		spi_config.irq_priority =APP_IRQ_PRIORITY_LOW;

    spi_config.ss_pin   = WSBC_CS;
    spi_config.mosi_pin = WSBC_SDIN;
    spi_config.miso_pin = WSBC_SDOUT;
    spi_config.sck_pin  = WSBC_SCLK;
	
    APP_ERROR_CHECK(nrf_drv_spi_init(&spi, &spi_config, spi_event_handler, NULL));
}

void spi_pin_clear(void)
{
		nrf_drv_gpiote_out_clear(WSBC_RST);

		nrf_drv_spi_uninit(&spi);
	
		nrf_drv_gpiote_out_clear(WSBC_CS);
		nrf_drv_gpiote_out_clear(WSBC_SDIN);
		nrf_drv_gpiote_out_clear(WSBC_SDOUT);
		nrf_drv_gpiote_out_clear(WSBC_SCLK);	
}



//*	AFE4300 INIT START
/**
* @brief  Resets the AFE4300 device
*
* @param  None
*
* @return  None
*/
void resetAFE4300()
{

  //P6.0 is RESET and is pulled low (device is reset)
		nrf_gpio_pin_write(WSBC_RST, LOW);
	  nrf_delay_ms(20);

   //P6.0 is RESET and is pulled high (device in normal mode again)
		nrf_gpio_pin_write(WSBC_RST, HIGH);
	  nrf_delay_ms(20);
}
/**
* @brief  Toggles Chips Select (CS)
*
* @param  None
*
* @return  None
*/
void toggleChipSelect()
{
  //Pull CS high to block MISO
		nrf_gpio_pin_write(WSBC_CS, HIGH);

   //Pull CS low for next instruction
		nrf_gpio_pin_write(WSBC_CS, LOW);
}
/**
* @brief  Writes to a register on the AFE4300
*
* @param  address an unsigned character
* @param  data an unsigned integer
*
* @return  None
*/
void writeRegister(unsigned char address, unsigned int data)
{
  //unsigned char firstByte = (unsigned char)(data >> 8);
  //unsigned char secondByte = (unsigned char)data;
  //address = address & 0x1F; //Last 5 bits specify address, first 3 bits need to be 0 for write opcode
	
  unsigned char firstByte = (unsigned char)(data >> 8);
  unsigned char secondByte = (unsigned char)(data & 0xFF);
  address = address & 0xDF; //Last 5 bits specify address, first 3 bits need to be 0 for write opcode

   memset(am_rx_buf, 0, am_length);
	
  //Specify address of register to be written to
		spi_xfer_done = false;
    APP_ERROR_CHECK(nrf_drv_spi_transfer(&spi, &address, am_length, am_rx_buf, am_length));
//		nrf_delay_ms(100);
//		nrf_delay_ms(1);
		while(!spi_xfer_done); 

  //Send 2 bytes to be written
		spi_xfer_done = false;
    APP_ERROR_CHECK(nrf_drv_spi_transfer(&spi, &firstByte, am_length, am_rx_buf, am_length));
//		nrf_delay_ms(100);
//		nrf_delay_ms(1);
		while(!spi_xfer_done); 
		spi_xfer_done = false;
    APP_ERROR_CHECK(nrf_drv_spi_transfer(&spi, &secondByte, am_length, am_rx_buf, am_length));
//		nrf_delay_ms(100);
//		nrf_delay_ms(1);
		while(!spi_xfer_done); 

}       
/**
* @brief  Reads from a register on the AFE4300
*
* @param  address an unsigned character
*
* @return  None
*/
int readRegister(unsigned char address)
{
  uint16_t spiReceive = 0;
  uint8_t spiReceiveFirst = 0;
  uint8_t spiReceiveSecond = 0;

//*  
  //address = address & 0x1F; //Last 5 bits specify address
  //address = address | 0x20; //First 3 bits need to be 001 for read opcode

  address = 0x20 | address; //First 3 bits need to be 001 for read opcode

  memset(am_rx_buf, 0, am_length);

  //Specify address of register to be written to
  //SPI_transmitData(__MSP430_BASEADDRESS_USCI_B1__, address);
		spi_xfer_done = false;
  APP_ERROR_CHECK(nrf_drv_spi_transfer(&spi, &address, am_length, am_rx_buf, am_length));
//	nrf_delay_ms(100);
//		nrf_delay_ms(1);
		while(!spi_xfer_done); 

  //Send 2 dummy bytes to read back
  //SPI_transmitData(__MSP430_BASEADDRESS_USCI_B1__, 0x00);
  //spiReceiveFirst = SPI_receiveData(__MSP430_BASEADDRESS_USCI_B1__);
  //SPI_transmitData(__MSP430_BASEADDRESS_USCI_B1__, 0x00);
  //spiReceiveSecond = SPI_receiveData(__MSP430_BASEADDRESS_USCI_B1__);
		spi_xfer_done = false;
	APP_ERROR_CHECK(nrf_drv_spi_transfer(&spi, 0x00, am_length, &spiReceiveFirst, am_length));
//		nrf_delay_ms(100);
//		nrf_delay_ms(1);
		while(!spi_xfer_done); 

		spi_xfer_done = false;
  APP_ERROR_CHECK(nrf_drv_spi_transfer(&spi, 0x00, am_length, &spiReceiveSecond, am_length));

		while(!spi_xfer_done); 
//		nrf_delay_ms(1);

  //Combine the two received bytes into a signed int
				NRF_LOG_INFO("readRegister start spiReceiveFirst = %d", spiReceiveFirst);
				NRF_LOG_INFO("readRegister start spiReceiveSecond = %d", spiReceiveSecond);
  spiReceive = (spiReceiveFirst << 8);
  spiReceive |= spiReceiveSecond;
  //*/
	
//  toggleChipSelect();

				NRF_LOG_INFO("readRegister start spiReceive = %d", spiReceive);
				NRF_LOG_FLUSH();

  return spiReceive;
}
/**
* @brief  Initializes the Weigh Scale Module
*
* @param  None
*
* @return  None
void initWeighScale()
{
  writeRegister(ADC_CONTROL_REGISTER,0x4120); //Differential measurement mode, 32 SPS
  toggleChipSelect();
  writeRegister(DEVICE_CONTROL_1,0x0005); //Power up weigh scale signal chain
  toggleChipSelect();
  writeRegister(ADC_CONTROL_REGISTER_2,0x0000); //ADC selects output of weigh scale
  toggleChipSelect();
  writeRegister(WEIGHT_SCALE_CONTROL,0x003F); //Gain = 1 DAC Offset = -1
  toggleChipSelect();
  writeRegister(BCM_DAC_FREQ,0x0040); //Frequency = default
  toggleChipSelect();
  writeRegister(IQ_MODE_ENABLE,0x0000); //Disable IQ mode
  toggleChipSelect();
  writeRegister(ISW_MATRIX,0x0000); //Channels IOUTP1 and IOUTN0
  toggleChipSelect();
  writeRegister(VSW_MATRIX,0x0000); //Channels VSENSEP1 and VSENSEN0
  toggleChipSelect();
}
*/

/**
* @brief  Initializes the BCM Module
*
* @param  None
*
* @return  None
*/
void initBCM()
{
  writeRegister(ADC_CONTROL_REGISTER,0x4143); //Differential measurement mode, 32 SPS
  toggleChipSelect();
  writeRegister(DEVICE_CONTROL_1,0x6006); //Power up BCM signal chain
  toggleChipSelect();
  writeRegister(ISW_MATRIX,0x0408); //Channels IOUTP1 and IOUTN0
  toggleChipSelect();
  writeRegister(VSW_MATRIX,0x0408); //Channels VSENSEP1 and VSENSEN0
  toggleChipSelect();
/*
  writeRegister(ADC_CONTROL_REGISTER_2,0x0063); //ADC selects output of BCM-I output
  toggleChipSelect();
  writeRegister(WEIGHT_SCALE_CONTROL,0x0000); //Gain = 1 DAC Offset = 0
  toggleChipSelect();
//*/
}

/**
* @brief  Initializes the BCM Module for FW mode
*
* @param  None
*
* @return  None
*/
void initFW()
{
  writeRegister(BCM_DAC_FREQ,0x0032); //Frequency = 50Khz
  toggleChipSelect();
  writeRegister(IQ_MODE_ENABLE,0x0000); //Disable IQ mode
  toggleChipSelect();
}

/**
* @brief  Initializes the BCM Module for measuring user resistance
*
* @param  None
*
* @return  None
void initBCMMeasure()
{
  writeRegister(ISW_MATRIX,0x0804); //Channels IOUTP1 and IOUTN0
  toggleChipSelect();
  writeRegister(VSW_MATRIX,0x0804); //Channels VSENSEP1 and VSENSEN0
  toggleChipSelect();
}
*/
        
/**
* @brief  Initializes the BCM Module for 100 ohm reference calibration
*
* @param  None
*
* @return  None
void initBCMCalibrate1()
{
  writeRegister(ISW_MATRIX,0x0101); //Reference resistors RP0 and RN0
  toggleChipSelect();
  writeRegister(VSW_MATRIX,0x0101); //Reference resistors RP0 and RN0
  toggleChipSelect();
}
*/
/**
* @brief  Initializes the BCM Module for 1000 ohm reference calibration
*
* @param  None
*
* @return  None
void initBCMCalibrate2()
{
  writeRegister(ISW_MATRIX,0x0202); //Reference resistors RP1 and RN1
  toggleChipSelect();
  writeRegister(VSW_MATRIX,0x0202); //Reference resistors RP1 and RN1
  toggleChipSelect();
}
*/
/**
* @brief  Initializes the AFE4300 device
*
* @param  None
*
* @return  None
*/
void initAFE4300()
{
    NRF_LOG_INFO("initAFE4300.");
    NRF_LOG_FLUSH();
	
  writeRegister(ADC_CONTROL_REGISTER,0x5140);
  toggleChipSelect();
  writeRegister(MISC1_REGISTER,0x0000);
  toggleChipSelect();
  writeRegister(MISC2_REGISTER,0xFFFF);
  toggleChipSelect();
  writeRegister(DEVICE_CONTROL_1,0x0006); //Power down both signal chains
  toggleChipSelect();
  writeRegister(ISW_MATRIX,0x0000);
  toggleChipSelect();
  writeRegister(VSW_MATRIX,0x0000);
  toggleChipSelect();
	/*
  writeRegister(IQ_MODE_ENABLE,0x0000);
  toggleChipSelect();
  writeRegister(WEIGHT_SCALE_CONTROL,0x0000);
  toggleChipSelect();
  writeRegister(BCM_DAC_FREQ,0x0040);
  toggleChipSelect();
  writeRegister(DEVICE_CONTROL_2,0x0000);
  toggleChipSelect();
  writeRegister(ADC_CONTROL_REGISTER_2,0x0011);
  toggleChipSelect();
  writeRegister(MISC3_REGISTER,0x00C0);
  toggleChipSelect();
	//*/
}
void initAFE4300_()
{
    NRF_LOG_INFO("initAFE4300_");
    NRF_LOG_FLUSH();
	/*
  writeRegister(ADC_CONTROL_REGISTER,0x5140);
  toggleChipSelect();
  writeRegister(MISC1_REGISTER,0x0000);
  toggleChipSelect();
  writeRegister(MISC2_REGISTER,0xFFFF);
  toggleChipSelect();
  writeRegister(DEVICE_CONTROL_1,0x0004); //Power down both signal chains
  toggleChipSelect();
  writeRegister(ISW_MATRIX,0x0000);
  toggleChipSelect();
  writeRegister(VSW_MATRIX,0x0000);
  toggleChipSelect();
	//*/
  writeRegister(IQ_MODE_ENABLE,0x0000);
  toggleChipSelect();
  writeRegister(WEIGHT_SCALE_CONTROL,0x0000);
  toggleChipSelect();
  writeRegister(BCM_DAC_FREQ,0x0090);	//	Sets the frequency of the BCM excitation current source
  toggleChipSelect();
  writeRegister(DEVICE_CONTROL_2,0x0000);
  toggleChipSelect();
  writeRegister(ADC_CONTROL_REGISTER_2,0x0011);
  toggleChipSelect();
  writeRegister(MISC3_REGISTER,0x0030);
  toggleChipSelect();
}
void initAFE4300_g()
{
    NRF_LOG_INFO("initAFE4300.");
    NRF_LOG_FLUSH();
	
  writeRegister(MISC1_REGISTER,0x0000);
  toggleChipSelect();
  writeRegister(MISC2_REGISTER,0xFFFF);
  toggleChipSelect();
  writeRegister(MISC3_REGISTER,0x0030);
  toggleChipSelect();

  writeRegister(ADC_CONTROL_REGISTER,0x4143);
  toggleChipSelect();
  writeRegister(BCM_DAC_FREQ,0x0033);
  toggleChipSelect();
  writeRegister(ADC_CONTROL_REGISTER_2,0x0063);
  toggleChipSelect();
  writeRegister(DEVICE_CONTROL_2,0x0000);
  toggleChipSelect();
  writeRegister(DEVICE_CONTROL_1,0x6006); //Power down both signal chains
  toggleChipSelect();

}
//*/	AFE4300 INIT END


/*
void afe4300_init(void)
{
		spi_init();

	  nrf_delay_ms(500);
		nrf_gpio_cfg_output(WSBC_RST);
		nrf_gpio_pin_write(WSBC_RST, HIGH);
	  nrf_delay_ms(500);

	  resetAFE4300();

  	toggleChipSelect();

    initAFE4300();

//  if(deviceCode == 0x30) initWeighScale(); //If device is Foot BCM
//  else if(deviceCode == 0x31) //If device is hand BCM
//  {
    NRF_LOG_INFO("initBCM.");
    NRF_LOG_FLUSH();
     initBCM();
     //Initialize BCM to measure calibration resistors
     //initBCMCalibrate1();
     //initBCMCalibrate2();
    NRF_LOG_INFO("initFW.");
    NRF_LOG_FLUSH();
     initFW();
//  }

    NRF_LOG_INFO("toggleChipSelect.");
    NRF_LOG_FLUSH();
	  toggleChipSelect();
}
//*/








/**
* @brief  Reads the ADC data register
*
* @param  None
*
* @return  integer
*/
uint16_t readData()
{
    nrf_delay_ms(50);
  writeRegister(ADC_CONTROL_REGISTER,0xc140);
    nrf_delay_ms(250); //250

	uint16_t register_value = readRegister(ADC_DATA_RESULT);
	NRF_LOG_INFO("readData ==========================   register_value = %d.", register_value);
  return register_value;
}
int afe4300_check (int height, int weight, int age, int gender, double *totalbodywater, double *extracellwater, double *ffm, double *bodyfat)
{
	
	NRF_LOG_INFO("weight = %d, height = %d, age = %d, gender = %d.", weight, height, age, gender);

	for ( voltageCodeCounter=0; voltageCodeCounter<34; voltageCodeCounter++ ) {	//Update counter for number of samples
		nrf_delay_ms(10);

    //Wait approx. 0.75 seconds for samples to settle and then take 8 measurements
    if((voltageCodeCounter > 24) && (voltageCodeCounter <= 32))
    {
       
       voltageCodeArray[voltageCodeCounter - 25] = readData();
       toggleChipSelect();
    }
	
    //After taking 8 samples, calculate average code
    else if(voltageCodeCounter > 32)
    {
       double voltageCodeSum = 0; 
       //Loop through array of data calculating total sum
       int i;
       for(i=0; i<8; i++)
       {
         voltageCodeSum += voltageCodeArray[i];
       }
       
       voltageCode = (long)(voltageCodeSum/8);
       //voltageCodeCounter = 0; //Reset counter
    NRF_LOG_INFO("voltageCode = %d.", voltageCode);
    NRF_LOG_FLUSH();
       
			 //	calcSlope();
				fwSlope = (caliVoltageCode2 - caliVoltageCode1)/(caliResistance2 - caliResistance1);
				fwYint = caliVoltageCode1 - (fwSlope * caliResistance1);

			  //	calcResistance();
				resistance = (voltageCode - fwYint)/fwSlope;

			 // calcBodyFat();
				weight /= 10.0; //If hand BCM, weight input was formatted over BLE
				
				*totalbodywater = 0.3674*((pow(height,2))/resistance) + 0.1753*weight - (0.11 * age) + 2.83*gender + 6.53;
				*extracellwater =  0.1890*((pow(height,2))/resistance)+ 0.0675*weight - (0.02 * age) + 2.53;
				*ffm = *totalbodywater/0.73;
				*bodyfat = ((weight - *ffm)/weight)*100;
				*bodyfat *= HAND_FACTOR;
				*ffm = weight - ((*bodyfat/100)*weight);
				*totalbodywater = *ffm*0.73;

    } else {
//	NRF_LOG_INFO("readData() = %d.", readData());
		}
	}

	
	return 1;
}

