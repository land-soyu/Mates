#include "bma250.h"
#include "app_util_platform.h"
#include "nrf_gpio.h"
#include "nrf_delay.h"
#include "boards.h"
#include "app_error.h"
#include <string.h>
#include "nrf_log.h"
#include "nrf_log_ctrl.h"
#include "nrf_log_default_backends.h"

#include "nrf_drv_twi.h"


/* TWI instance. */
static const nrf_drv_twi_t m_twi = NRF_DRV_TWI_INSTANCE(1);

/* Indicates if operation on TWI has ended. */
static volatile bool m_xfer_done = false;

void twi_handler_bma(nrf_drv_twi_evt_t const * p_event, void * p_context)
{
	switch (p_event->type)
    {
        case NRF_DRV_TWI_EVT_DONE:
            m_xfer_done = true;
            break;
        case NRF_DRV_TWI_EVT_ADDRESS_NACK:            
            break;
        case NRF_DRV_TWI_EVT_DATA_NACK:
            break;
        default:
            break;   
    }
}
void twi_init_bma (void)
{
    NRF_LOG_INFO("twi_init_bma");

	ret_code_t err_code;

    const nrf_drv_twi_config_t twi_config = {
        .scl            = TWI_SCL_PIN_OLED,
        .sda            = TWI_SDA_PIN_OLED,
        .frequency      = NRF_TWI_FREQ_400K,
        .interrupt_priority = APP_IRQ_PRIORITY_HIGH
    };

    err_code = nrf_drv_twi_init(&m_twi, &twi_config, twi_handler_bma, NULL);
    APP_ERROR_CHECK(err_code);
    
    nrf_drv_twi_enable(&m_twi);
}

 ret_code_t XYZ_I2C_register_read( uint8_t reg_addr,  uint8_t * p_rx_data, uint32_t bytes)
{   
    ret_code_t ret_code;

	    uint8_t tx_data[1];
    tx_data[0] = 0x02;

  			nrf_delay_ms(150);

    ret_code = nrf_drv_twi_tx(&m_twi,BMA250_ADDRESS, tx_data, sizeof(tx_data), false);
	if(ret_code != NRF_SUCCESS)
    {
        return ret_code;
    }


		  			nrf_delay_ms(150);

    ret_code = nrf_drv_twi_rx(&m_twi, BMA250_ADDRESS, p_rx_data, bytes);
    return ret_code;
}


void bma250_init(void)
{
	uint8_t tx_data[1];
    NRF_LOG_INFO("bma250_init.");
    NRF_LOG_FLUSH();

    twi_init_bma();

	tx_data[0] = 0x0F;
    APP_ERROR_CHECK(nrf_drv_twi_tx(&m_twi, BMA250_ADDRESS,  tx_data, sizeof(tx_data), true));

  			nrf_delay_ms(150);
	
	tx_data[0] = 0x03;
    APP_ERROR_CHECK(nrf_drv_twi_tx(&m_twi, BMA250_ADDRESS,  tx_data, sizeof(tx_data), true));
 
			nrf_delay_ms(150);
	tx_data[0] = 0x10;
    APP_ERROR_CHECK(nrf_drv_twi_tx(&m_twi, BMA250_ADDRESS,  tx_data, sizeof(tx_data), true));

			nrf_delay_ms(150);

  tx_data[0] = 0x08;
    APP_ERROR_CHECK(nrf_drv_twi_tx(&m_twi, BMA250_ADDRESS,  tx_data, sizeof(tx_data), true));

			nrf_delay_ms(150);

  tx_data[0] = 0x11;
    APP_ERROR_CHECK(nrf_drv_twi_tx(&m_twi, BMA250_ADDRESS,  tx_data, sizeof(tx_data), true));

      NRF_LOG_INFO("init complite");
      NRF_LOG_FLUSH();
	
uint32_t i=0;
    while (true)
    {
      NRF_LOG_INFO("while start.");
      NRF_LOG_FLUSH();
			
			ret_code_t err_code;
			uint8_t val[6];
	    err_code = XYZ_I2C_register_read(BMA250_ADDRESS,val,sizeof(val));
    APP_ERROR_CHECK(err_code);
float xAccl = ((val[1] * 256.0) + (val[0] & 0xC0)) / 64;
  if (xAccl > 511)
  {
   xAccl -= 1024;
  }
  float yAccl = ((val[3] * 256.0) + (val[2] & 0xC0)) / 64;
  if (yAccl > 511)
  {
    yAccl -= 1024;
  }
  float zAccl = ((val[5] * 256.0) + (val[4] & 0xC0)) / 64;
  if (zAccl > 511)
  {
    zAccl -= 1024;
  }

	// Output data to the serial monitor
  NRF_LOG_INFO(" Acceleration in X-Axis := %i", xAccl);
  NRF_LOG_INFO(" Acceleration in Y-Axis := %i", yAccl);
  NRF_LOG_INFO(" Acceleration in Z-Axis := %i", zAccl);

	nrf_delay_ms(1000);

    NRF_LOG_INFO("\r\nTWI sensor example = %i", i);
    NRF_LOG_FLUSH();

i++;
if ( i > 60 ) { i =0; }
    }
}
