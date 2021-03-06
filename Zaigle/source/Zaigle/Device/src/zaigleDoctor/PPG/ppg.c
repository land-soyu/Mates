#include "ppg.h"
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
#include "algorithm.h"

uint32_t aun_ir_buffer[500]; //IR LED sensor data
int32_t n_ir_buffer_length;    //data length
uint32_t aun_red_buffer[500];    //Red LED sensor data
int32_t n_sp02; //SPO2 value
int8_t ch_spo2_valid;   //indicator to show if the SP02 calculation is valid
int32_t n_heart_rate;   //heart rate value
int8_t  ch_hr_valid;    //indicator to show if the heart rate calculation is valid
uint8_t uch_dummy;


/* TWI instance ID. */
#define TWI_INSTANCE_ID     1
/* TWI instance. */
static const nrf_drv_twi_t m_twi = NRF_DRV_TWI_INSTANCE(TWI_INSTANCE_ID);

/* Indicates if operation on TWI has ended. */
static volatile bool m_xfer_done = false;

void twi_handler(nrf_drv_twi_evt_t const * p_event, void * p_context)
{
	switch (p_event->type)
    {
        case NRF_DRV_TWI_EVT_DONE:
/*            if (p_event->xfer_desc.type == NRF_DRV_TWI_XFER_RX)
            {
                data_handler(m_sample);
            }
*/            m_xfer_done = true;
            break;
        case NRF_DRV_TWI_EVT_ADDRESS_NACK:            
            break;
        case NRF_DRV_TWI_EVT_DATA_NACK:
            break;
        default:
            break;   
    }
}

void twi_init (void)
{
    NRF_LOG_INFO("twi_init");

	ret_code_t err_code;

    const nrf_drv_twi_config_t twi_config = {
        .scl            = TWI_SCL_PIN,
        .sda            = TWI_SDA_PIN,
        .frequency      = NRF_TWI_FREQ_400K,
        .interrupt_priority = APP_IRQ_PRIORITY_HIGH
    };

    err_code = nrf_drv_twi_init(&m_twi, &twi_config, twi_handler, NULL);
    APP_ERROR_CHECK(err_code);
    
    nrf_drv_twi_enable(&m_twi);
}

ret_code_t max30102_I2C_register_write(uint8_t reg_addr, uint8_t * p_tx_data, uint8_t bytes)
{
    ret_code_t ret_code;
    uint8_t tx_data[bytes+1];
    tx_data[0] = reg_addr;
    
    for(uint8_t i = 0 ; i<bytes ; i++) 
    {
        tx_data[i+1] = p_tx_data[i];
    }   
    ret_code = nrf_drv_twi_tx(&m_twi, MAX30102_ADDRESS, tx_data, sizeof(tx_data), true);

		m_xfer_done = false;
		while(!m_xfer_done); 

    return ret_code;
}

ret_code_t max30102_I2C_data_read( uint8_t reg_addr,  uint8_t * p_rx_data, uint32_t bytes)
{   
    ret_code_t ret_code;
    ret_code = nrf_drv_twi_tx(&m_twi,MAX30102_ADDRESS, &reg_addr, 1, true);
    if(ret_code != NRF_SUCCESS)
    {
        return ret_code;
    } else {
			m_xfer_done = false;
		while(!m_xfer_done);
    ret_code = nrf_drv_twi_rx(&m_twi, MAX30102_ADDRESS, p_rx_data, bytes);
    return ret_code;
		}
}


bool maxim_max30102_read_fifo(uint32_t *pun_red_led, uint32_t *pun_ir_led)
/**
* \brief        Read a set of samples from the MAX30102 FIFO register
* \par          Details
*               This function reads a set of samples from the MAX30102 FIFO register
*
* \param[out]   *pun_red_led   - pointer that stores the red LED reading data
* \param[out]   *pun_ir_led    - pointer that stores the IR LED reading data
*
* \retval       true on success
*/
{
  uint32_t un_temp;
  uint8_t uch_temp;
  *pun_red_led=0;
  *pun_ir_led=0;
  uint8_t ach_i2c_data[6];

  //read and clear status register
  max30102_I2C_data_read(REG_INTR_STATUS_1, &uch_temp, 1);
  max30102_I2C_data_read(REG_INTR_STATUS_2, &uch_temp, 1);


    ach_i2c_data[0] = REG_FIFO_DATA;
    nrf_drv_twi_tx(&m_twi,MAX30102_ADDRESS, ach_i2c_data, 1, true);
    m_xfer_done = false;
    while(!m_xfer_done);
    nrf_drv_twi_rx(&m_twi, MAX30102_ADDRESS, ach_i2c_data, 6);

  un_temp=(unsigned char) ach_i2c_data[0];
  un_temp<<=16;
  *pun_red_led+=un_temp;
  un_temp=(unsigned char) ach_i2c_data[1];
  un_temp<<=8;
  *pun_red_led+=un_temp;
  un_temp=(unsigned char) ach_i2c_data[2];
  *pun_red_led+=un_temp;
  
  un_temp=(unsigned char) ach_i2c_data[3];
  un_temp<<=16;
  *pun_ir_led+=un_temp;
  un_temp=(unsigned char) ach_i2c_data[4];
  un_temp<<=8;
  *pun_ir_led+=un_temp;
  un_temp=(unsigned char) ach_i2c_data[5];
  *pun_ir_led+=un_temp;
  *pun_red_led&=0x03FFFF;  //Mask MSB [23:18]
  *pun_ir_led&=0x03FFFF;  //Mask MSB [23:18]
  return true;
}

void ppg_init(uint32_t *hb, uint32_t *sp)
{
  uint32_t un_min, un_max, un_prev_data;  //variables to calculate the on-board LED brightness that reflects the heartbeats
	uint32_t red, ir, red_pre, ir_pre, red_count, ir_count;
  int32_t un_brightness;
  float f_temp;
	int i;

  NRF_LOG_INFO("I2C PPG.");
  NRF_LOG_FLUSH();

	twi_init();

	ret_code_t err_code;
	uint8_t data_write;

  NRF_LOG_INFO("init start");
  NRF_LOG_FLUSH();

	data_write = 0x40;
	err_code = max30102_I2C_register_write(REG_MODE_CONFIG, &data_write, 1);
  APP_ERROR_CHECK(err_code);
	err_code = max30102_I2C_data_read(REG_FIFO_DATA, 0, 0);
  data_write = 0xc0;
	err_code = max30102_I2C_register_write(REG_INTR_ENABLE_1, &data_write, 1);
  APP_ERROR_CHECK(err_code);
  data_write = 0x00;
	err_code = max30102_I2C_register_write(REG_INTR_ENABLE_2, &data_write, 1);
  APP_ERROR_CHECK(err_code);

  data_write = 0x00;
	err_code = max30102_I2C_register_write(REG_FIFO_WR_PTR, &data_write, 1);
  APP_ERROR_CHECK(err_code);
  data_write = 0x00;
	err_code = max30102_I2C_register_write(REG_OVF_COUNTER, &data_write, 1);
  APP_ERROR_CHECK(err_code);
  data_write = 0x00;
	err_code = max30102_I2C_register_write(REG_FIFO_RD_PTR, &data_write, 1);
  APP_ERROR_CHECK(err_code);
  data_write = 0x4f;
	err_code = max30102_I2C_register_write(REG_FIFO_CONFIG, &data_write, 1);
  APP_ERROR_CHECK(err_code);
  data_write = 0x03;
	err_code = max30102_I2C_register_write(REG_MODE_CONFIG, &data_write, 1);
  APP_ERROR_CHECK(err_code);
  data_write = 0x27;
	err_code = max30102_I2C_register_write(REG_SPO2_CONFIG, &data_write, 1);
  APP_ERROR_CHECK(err_code);
  data_write = 0x24;
	err_code = max30102_I2C_register_write(REG_LED1_PA, &data_write, 1);
  APP_ERROR_CHECK(err_code);
  data_write = 0x24;
	err_code = max30102_I2C_register_write(REG_LED2_PA, &data_write, 1);
  APP_ERROR_CHECK(err_code);
  data_write = 0x7f;
	err_code = max30102_I2C_register_write(REG_PILOT_PA, &data_write, 1);
  APP_ERROR_CHECK(err_code);

			
	  un_brightness=0;
    un_min=0x3FFFF;
    un_max=0;
		
		red = 0;
		ir = 0;
		red_pre = 0;
		ir_pre = 0;
		red_count = 0;
		ir_count = 0;
  
    n_ir_buffer_length=500; //buffer length of 100 stores 5 seconds of samples running at 100sps
    
    //read the first 500 samples, and determine the signal range
    for(i=0;i<n_ir_buffer_length;i++)
    {
        
        maxim_max30102_read_fifo((aun_red_buffer+i), (aun_ir_buffer+i));  //read from MAX30102 FIFO
            
        if(un_min>aun_red_buffer[i])
            un_min=aun_red_buffer[i];    //update signal min
        if(un_max<aun_red_buffer[i])
            un_max=aun_red_buffer[i];    //update signal max
    }

    maxim_heart_rate_and_oxygen_saturation(aun_ir_buffer, n_ir_buffer_length, aun_red_buffer, &n_sp02, &ch_spo2_valid, &n_heart_rate, &ch_hr_valid); 
   
uint32_t a=0;
    while (true)
    {
        //dumping the first 100 sets of samples in the memory and shift the last 400 sets of samples to the top
        for(i=100;i<500;i++)
        {
            aun_red_buffer[i-100]=aun_red_buffer[i];
            aun_ir_buffer[i-100]=aun_ir_buffer[i];
            
            //update the signal min and max
            if(un_min>aun_red_buffer[i])
            un_min=aun_red_buffer[i];
            if(un_max<aun_red_buffer[i])
            un_max=aun_red_buffer[i];
        }
        
        //take 100 sets of samples before calculating the heart rate.
        for(i=400;i<500;i++)
        {
            un_prev_data=aun_red_buffer[i-1];
            maxim_max30102_read_fifo((aun_red_buffer+i), (aun_ir_buffer+i));
        
            if(aun_red_buffer[i]>un_prev_data)
            {
                f_temp=aun_red_buffer[i]-un_prev_data;
                f_temp/=(un_max-un_min);
                f_temp*=MAX_BRIGHTNESS;
							  f_temp=un_brightness-f_temp;
                if(f_temp<0)
                  un_brightness=0;
                else
                  un_brightness=(int)f_temp;
            }
            else
            {
                f_temp=un_prev_data-aun_red_buffer[i];
                f_temp/=(un_max-un_min);
                f_temp*=MAX_BRIGHTNESS;
                un_brightness+=(int)f_temp;
                if(un_brightness>MAX_BRIGHTNESS)
                    un_brightness=MAX_BRIGHTNESS;
            }
        }

				if ( ch_spo2_valid == 1 && ch_hr_valid == 1 ) {
					if ( n_heart_rate < 140  && n_heart_rate > 40 ) {
						
						if ( n_heart_rate != red_pre ) {
						  red = red+n_heart_rate;
						  red_count++;
						}
						red_pre = n_heart_rate;
					}
					if ( n_sp02 < 101 && n_sp02 > 90 ) {
						if ( n_sp02 != ir_pre ) {
							ir = ir+n_sp02;
							ir_count++;
						}
						ir_pre = n_sp02;
					}
	NRF_LOG_INFO(" HR=%i , SpO2=%i   ",n_heart_rate, n_sp02);
    NRF_LOG_FLUSH();
				}
		    maxim_heart_rate_and_oxygen_saturation(aun_ir_buffer, n_ir_buffer_length, aun_red_buffer, &n_sp02, &ch_spo2_valid, &n_heart_rate, &ch_hr_valid); 
					
        a++;
        if ( a > 600 ) {
	NRF_LOG_INFO(" red_count=%i , ir_count=%i   ",red_count, ir_count);
    NRF_LOG_FLUSH();
					*hb = red / red_count;
					*sp = ir / ir_count;
					return;
				}

    }





}

