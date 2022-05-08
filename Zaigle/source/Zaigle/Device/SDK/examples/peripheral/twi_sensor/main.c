/**
 * Copyright (c) 2015 - 2017, Nordic Semiconductor ASA
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form, except as embedded into a Nordic
 *    Semiconductor ASA integrated circuit in a product or a software update for
 *    such product, must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 * 
 * 3. Neither the name of Nordic Semiconductor ASA nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * 4. This software, with or without modification, must only be used with a
 *    Nordic Semiconductor ASA integrated circuit.
 * 
 * 5. Any software provided in binary form under this license must not be reverse
 *    engineered, decompiled, modified and/or disassembled.
 * 
 * THIS SOFTWARE IS PROVIDED BY NORDIC SEMICONDUCTOR ASA "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY, NONINFRINGEMENT, AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NORDIC SEMICONDUCTOR ASA OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
/** @file
 * @defgroup tw_sensor_example main.c
 * @{
 * @ingroup nrf_twi_example
 * @brief TWI Sensor Example main file.
 *
 * This file contains the source code for a sample application using TWI.
 *
 */

#include <stdio.h>
#include "boards.h"
#include "app_util_platform.h"
#include "app_error.h"
#include "nrf_drv_twi.h"
#include "nrf_delay.h"


#include "nrf_log.h"
#include "nrf_log_ctrl.h"
#include "nrf_log_default_backends.h"

/* TWI instance ID. */
#define TWI_INSTANCE_ID     0

/* TWI instance. */
static const nrf_drv_twi_t m_twi = NRF_DRV_TWI_INSTANCE(TWI_INSTANCE_ID);

/* TWI PINS */
#define TWI_SCL_PIN     27
#define TWI_SDA_PIN     26

#define xyzAddr 0x1C



/* Indicates if operation on TWI has ended. */
static volatile bool m_xfer_done = false;

/* Buffer for samples read from temperature sensor. */
static uint8_t m_sample;

/**
 * @brief Function for handling data from temperature sensor.
 *
 * @param[in] temp          Temperature in Celsius degrees read from sensor.
 */
__STATIC_INLINE void data_handler(uint8_t temp)
{
    NRF_LOG_INFO("Temperature: %d Celsius degrees.", temp);
}

/**
 * @brief TWI events handler.
 */
void twi_handler(nrf_drv_twi_evt_t const * p_event, void * p_context)
{
    switch (p_event->type)
    {
        case NRF_DRV_TWI_EVT_DONE:
            if (p_event->xfer_desc.type == NRF_DRV_TWI_XFER_RX)
            {
                data_handler(m_sample);
            }
            m_xfer_done = true;
            break;
        default:
            break;
    }
}

/**
 * @brief UART initialization.
 */
void twi_init (void)
{
    NRF_LOG_INFO("\r\n  22twi_init");

	ret_code_t err_code;

    const nrf_drv_twi_config_t twi_max30100_config = {
        .scl            = TWI_SCL_PIN,
        .sda            = TWI_SDA_PIN,
        .frequency      = NRF_TWI_FREQ_400K,
        .interrupt_priority = APP_IRQ_PRIORITY_HIGH
    };
    const nrf_drv_twi_config_t twi_config = {
       .scl                = TWI_SCL_PIN,
       .sda                = TWI_SDA_PIN,
       .frequency          = NRF_TWI_FREQ_100K,
       .interrupt_priority = APP_IRQ_PRIORITY_HIGH,
       .clear_bus_init     = false
    };
    err_code = nrf_drv_twi_init(&m_twi, &twi_max30100_config, NULL, NULL);
    APP_ERROR_CHECK(err_code);
    
    nrf_drv_twi_enable(&m_twi);
}

ret_code_t XYZ_I2C_register_write(uint8_t reg_addr, uint8_t p_tx_data, uint8_t bytes)
{
    NRF_LOG_INFO("\r\n   -----------------   XYZ_I2C_register_write start %i", xyzAddr);

	ret_code_t ret_code;
    uint8_t tx_data[bytes+1];
    tx_data[0] = reg_addr;
    
    for(uint8_t i = 0 ; i<bytes ; i++) 
    {
        tx_data[i+1] = p_tx_data;
    }   
    ret_code = nrf_drv_twi_tx(&m_twi, xyzAddr, tx_data, sizeof(tx_data), true);
    return ret_code;
}
 ret_code_t XYZ_I2C_register_read( uint8_t reg_addr,  uint8_t * p_rx_data, uint32_t bytes)
{   
    ret_code_t ret_code;

	    uint8_t tx_data[2];
    tx_data[0] = 0x02;
    tx_data[1] = 0x01;

    ret_code = nrf_drv_twi_tx(&m_twi,xyzAddr, tx_data, sizeof(tx_data), false);
  	if(ret_code != NRF_SUCCESS)
    {
        return ret_code;
    }
    ret_code = nrf_drv_twi_rx(&m_twi, xyzAddr, p_rx_data, bytes);
    return ret_code;
}

/**
 * @brief Function for main application entry.
 */
int main(void)
{
			ret_code_t err_code;
	uint8_t data_write;
	
    APP_ERROR_CHECK(NRF_LOG_INIT(NULL));
    NRF_LOG_DEFAULT_BACKENDS_INIT();

    NRF_LOG_INFO("\r\n 1111 TWI sensor example");
    NRF_LOG_FLUSH();
    twi_init();

    NRF_LOG_INFO("\r\n   9 -----------------   init start ");


uint32_t i=0;
    while (true)
    {

	NRF_LOG_INFO(" while start ");
       NRF_LOG_FLUSH();


			ret_code_t err_code;
    
    uint8_t a_M[1];
    uint8_t x_M[1];
    uint8_t y_M[1];
    uint8_t z_M[1];


			err_code = XYZ_I2C_register_read(xyzAddr,a_M,sizeof(x_M));
			nrf_delay_ms(4);
	  err_code = XYZ_I2C_register_read(xyzAddr,x_M,sizeof(x_M));
			nrf_delay_ms(4);
	  err_code = XYZ_I2C_register_read(xyzAddr,y_M,sizeof(y_M));
			nrf_delay_ms(4);
	  err_code = XYZ_I2C_register_read(xyzAddr,z_M,sizeof(z_M));
			nrf_delay_ms(4);
    APP_ERROR_CHECK(err_code);


  
  // Output data to the serial monitor
  NRF_LOG_INFO("\r\n Acceleration in X-Axis := %i", a_M);
  NRF_LOG_INFO("\r\n Acceleration in Y-Axis := %i", x_M);
  NRF_LOG_INFO("\r\n Acceleration in Z-Axis := %i", y_M);
  NRF_LOG_INFO("\r\n Acceleration in Z-Axis := %i", z_M);

			nrf_delay_ms(1000);

    }
}

/** @} */
