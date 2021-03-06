/**
 * Copyright (c) 2014 - 2017, Nordic Semiconductor ASA
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
 *
 * @defgroup ble_sdk_uart_over_ble_main main.c
 * @{
 * @ingroup  ble_sdk_app_nus_eval
 * @brief    UART over BLE application main file.
 *
 * This file contains the source code for a sample application that uses the Nordic UART service.
 * This application uses the @ref srvlib_conn_params module.
 */



#include <stdint.h>
#include <string.h>
#include "nordic_common.h"
#include "nrf.h"
#include "ble_hci.h"
#include "ble_advdata.h"
#include "ble_advertising.h"
#include "ble_conn_params.h"
#include "nrf_sdh.h"
#include "nrf_sdh_soc.h"
#include "nrf_sdh_ble.h"
#include "nrf_ble_gatt.h"
#include "app_timer.h"
#include "ble_nus.h"
#include "app_uart.h"
#include "app_util_platform.h"
#include "bsp_btn_ble.h"
#include "nrf_delay.h"
#include "i2c.h"
#include "nrf_fstorage.h"
#include "nrf_fstorage_sd.h"

#include "nrf_log.h"
#include "nrf_log_ctrl.h"
#include "nrf_log_default_backends.h"

#include "spi.h"
#include "afe4300.h"
#include "rtc.h"
#include "calculator.h"

#include "nrf_drv_gpiote.h"
#include "app_gpiote.h"

#include "nrf_drv_timer.h"

#include "nrf_drv_saadc.h"
#define SAMPLES_IN_BUFFER 4


#define APP_BLE_CONN_CFG_TAG            1                                           /**< A tag identifying the SoftDevice BLE configuration. */

#define APP_FEATURE_NOT_SUPPORTED       BLE_GATT_STATUS_ATTERR_APP_BEGIN + 2        /**< Reply when unsupported features are requested. */

#define DEVICE_NAME                     "S-TECH "                               /**< Name of device. Will be included in the advertising data. */
#define NUS_SERVICE_UUID_TYPE           BLE_UUID_TYPE_VENDOR_BEGIN                  /**< UUID type for the Nordic UART Service (vendor specific). */

#define APP_BLE_OBSERVER_PRIO           3                                           /**< Application's BLE observer priority. You shouldn't need to modify this value. */

#define APP_ADV_INTERVAL                40                                          /**< The advertising interval (in units of 0.625 ms. This value corresponds to 40 ms). */
#define APP_ADV_TIMEOUT_IN_SECONDS      180                                         /**< The advertising timeout (in units of seconds). */

#define MIN_CONN_INTERVAL               MSEC_TO_UNITS(500, UNIT_1_25_MS)             /**< Minimum acceptable connection interval (20 ms), Connection interval uses 1.25 ms units. */
#define MAX_CONN_INTERVAL               MSEC_TO_UNITS(1000, UNIT_1_25_MS)             /**< Maximum acceptable connection interval (75 ms), Connection interval uses 1.25 ms units. */
#define SLAVE_LATENCY                   0                                           /**< Slave latency. */
#define CONN_SUP_TIMEOUT                MSEC_TO_UNITS(4000, UNIT_10_MS)             /**< Connection supervisory timeout (4 seconds), Supervision Timeout uses 10 ms units. */
#define FIRST_CONN_PARAMS_UPDATE_DELAY  APP_TIMER_TICKS(5000)                       /**< Time from initiating event (connect or start of notification) to first time sd_ble_gap_conn_param_update is called (5 seconds). */
#define NEXT_CONN_PARAMS_UPDATE_DELAY   APP_TIMER_TICKS(30000)                      /**< Time between each call to sd_ble_gap_conn_param_update after the first call (30 seconds). */
#define MAX_CONN_PARAMS_UPDATE_COUNT    3                                           /**< Number of attempts before giving up the connection parameter negotiation. */

#define DEAD_BEEF                       0xDEADBEEF                                  /**< Value used as error code on stack dump, can be used to identify stack location on stack unwind. */

// SW_PWR_HOLD
#define SW_PWR_HOLD  3  //power on/off
#define SW_PWR_SLEEP  4  //power button
#define LED_D_4  9  //	D4 battery


BLE_NUS_DEF(m_nus);                                                                 /**< BLE NUS service instance. */
NRF_BLE_GATT_DEF(m_gatt);                                                           /**< GATT module instance. */
BLE_ADVERTISING_DEF(m_advertising);                                                 /**< Advertising module instance. */

static uint16_t   m_conn_handle          = BLE_CONN_HANDLE_INVALID;                 /**< Handle of the current connection. */
static uint16_t   m_ble_nus_max_data_len = BLE_GATT_ATT_MTU_DEFAULT - 3;            /**< Maximum length of data (in bytes) that can be transmitted to the peer by the Nordic UART service module. */
static ble_uuid_t m_adv_uuids[]          =                                          /**< Universally unique service identifier. */
{
    {BLE_UUID_NUS_SERVICE, NUS_SERVICE_UUID_TYPE}
};

//uint8_t result_data[6] = {0, 0, 0, 0, 0, 0}; // 0: hr, 1:sp02, 2:bmi, 3:kcal, 4:fat, 5:stress_level
//*	BCM Parameters
//double BCM_result[4] = {0, 0, 0, 0};	//	0:totalbodywater,	1:extracellwater,	2:ffm(fat-free mass),	3:bodyfat
//int order_data[4] = {0, 0, 0, MALE};	// 0:height, 1:weight, 2:age, 3:gender
//int 		step_data[8] = {0, 0, 0, 0, 0, 0, 0, 0};
//*/


uint8_t screen_flag = 1;	//	1:time, 2:(HR/STEP), 3:(SPO2/KCAL), 4:STRESS, 5:FAT ....
uint8_t ble_connect = 0;	// 0:disconnect, 1:connect
uint8_t battery_value_pre=0;

uint8_t screen_view_flag = 0;	// screen on:0/off:1 flag
uint8_t stepcount = 0;

char *strs[13]= { NULL, };
bool check_flag = false;

//* fstorage start
char						write_data[] = "";
char						send_data[] = "STEP|0";
char						ok_data[] = "ORDER|OK|00000";
bool 					write_data_change_flag = false;
bool 					step_data_send_flag = false;
uint8_t    		read_data[1] = {0};
void 						fstorage_write(void);
bool 					fstorage_read(void);
// fstorage end */ 
void 						screen_print(void);

#define FPU_EXCEPTION_MASK 0x0000009F
static void sleep_on(void)
{
		int	i;
     SCB->SCR |= SCB_SCR_SEVONPEND_Msk;


    /* Clear exceptions and PendingIRQ from the FPU unit */
    __set_FPSCR(__get_FPSCR()  & ~(FPU_EXCEPTION_MASK));
    (void) __get_FPSCR();
    NVIC_ClearPendingIRQ(FPU_IRQn);
		
		    sd_power_mode_set(NRF_POWER_MODE_LOWPWR);
        sd_app_evt_wait();
				
saadc_sampling_event_disable();
//NRF_SPI0->ENABLE = 0;
NRF_SAADC->ENABLE = 0;
nrf_gpio_pin_write(22, 0); //battery 

//NRF_TWI1->ENABLE = 0; 2mA
sd_ble_gap_adv_stop();

app_button_disable();
nrf_delay_ms(500);
app_button_enable();
//for(i=1;i>=10000;i++)

    // Make sure any pending events are cleared
    __SEV();
		
    __WFE();
    // Enter System ON sleep mode
    __WFE();

}

static void sleep_off(void)
{
//NRF_SPI0->ENABLE = 1;
NRF_SAADC->ENABLE = 1;
nrf_gpio_pin_write(22, 1); //battery 
saadc_sampling_event_enable();
ble_advertising_start(&m_advertising, BLE_ADV_MODE_FAST);


}
void screen_on(bool flag)
{
		if ( flag ) {
				show_check_ready(5);
		} else {
				screen_flag = 0;
				screen_print();
		}

		nrf_gpio_cfg_output(OLED_PW);
		nrf_gpio_pin_write(OLED_PW, HIGH);
		uint8_t tx_on_data[1] = {0xAF};
	  OLED_I2C_register_write(tx_on_data, 1);
		nrf_delay_ms(100);

		enable_battery_timer();

		screen_view_flag = 0;
		sleep_off();
}
void screen_off(void)
{
		uint8_t tx_on_data[1] = {0xAE};
	  OLED_I2C_register_write(tx_on_data, 1);
		nrf_gpio_cfg_output(OLED_PW);
		nrf_gpio_pin_write(OLED_PW, LOW);
		//nrf_delay_ms(100);


		disable_battery_timer();

		screen_view_flag = 1;
		sleep_on();
}

void screen_sleep(void)
{
	if ( screen_view_flag == 0 ) {
			screen_off();
	} else {
			screen_on(false);
	}
}



uint8_t timer_count = 0;
struct tm *timeinfo;
time_t date_hour_seconds;
int now_min = 0;
void timer_led_event_handler()
{
	if ( timer_count == 100 ) {
		timer_count = 0;
		screen_flag = 0;
		screen_print();
	}
	timer_count++;

		if ( timer_count > 30 && timer_count < 90 ) { 
			screen_off(); 
			timer_count = 0;
		} else {
				if ( screen_flag == 1 ) {
						date_hour_seconds = gettime();
						timeinfo = localtime(&date_hour_seconds);

						if ( timeinfo->tm_min != now_min ) {
							if ( timer_count != 1 ) {
								screen_flag = 0;
								screen_print();
							}
							now_min = timeinfo->tm_min;
						}
				}
		}
}


void screen_default() {
//		NRF_LOG_INFO("screen_default ble_connect = %d, battery_value_pre = %d", ble_connect, battery_value_pre);
//		NRF_LOG_FLUSH();
		show_battery(battery_value_pre);
		show_bt(ble_connect);
}

int screen_view(int count) {
	/*
  	switch (count) {
				case 0:
					date_hour_seconds = gettime();
					timeinfo = localtime(&date_hour_seconds);

					screen_default();
					show_time(timeinfo->tm_mon+1, timeinfo->tm_mday, timeinfo->tm_hour, timeinfo->tm_min, timeinfo->tm_wday);
		    	return 1;
    		case 1:
					screen_default();
				  if ( result_data[0] > 0 ) {
						show_heartrate(result_data[0]);
					} else {
						show_stepcount(stepcount);
					}
					return 2;
    		case 2:
					screen_default();
					if ( result_data[1] > 0 ) {
						 show_spo2(result_data[1]);
							return 3;
					} else {
							show_stepcal(stepcount);
							return 0;
					}
    		case 3:
					screen_default();
							if ( result_data[5] > 0 ) {
								show_stress(result_data[5]);
							} else {
								return -1;
							}
    			  return 4;
		    case 4:
					screen_default();
							if ( BCM_result[3] > 0 ) {
								show_bodyfat_weight(result_data[4], order_data[1], getFatLevel(result_data[4], order_data[2], order_data[3]));
							} else {
								return -1;
							}
    			return 5;
		    case 5:
					screen_default();
							if ( BCM_result[3] > 0 ) {
								show_bodyfat(result_data[4], getFatLevel(result_data[4], order_data[2], order_data[3]));
							} else {
								return -1;
							}
    			return 6;
		    case 6:
					screen_default();
							if ( result_data[2] > 0 ) {
//								show_bmi(bmi, getBMILevel(bmi));
								show_bmi(result_data[2], getFatLevel(result_data[4], order_data[2], order_data[3]));
							} else {
								return -1;
							}
    			return 7;
		    case 7:
					screen_default();
					show_stepcount(stepcount);
    			return 8;
		    case 8:
					screen_default();
					show_stepcal(stepcount);
			    return 0;
		    case 9:
			    return -1;
		    default:
			    return -2;
	}
		
	*/
}

void screen_print(void) {
	int flag = -1;
	while ( flag == -1 ) {
		flag = screen_view(screen_flag++);
		if ( flag == -1 ) screen_flag = 0;
	}
}

void afe4300_init(void)
{
		show_check_ready(4);
		spi_init();

		nrf_delay_ms(500);
		nrf_gpio_cfg_output(WSBC_RST);
		nrf_gpio_pin_write(WSBC_RST, HIGH);
		nrf_delay_ms(500);

		show_check_ready_count(3);
		resetAFE4300();

		toggleChipSelect();

		initAFE4300_g();
		show_check_ready_count(2);
		initBCM();

		show_check_ready_count(1);
		//initFW();

		NRF_LOG_INFO("toggleChipSelect.");
		NRF_LOG_FLUSH();
		toggleChipSelect();
}
void check_function(int checking_flag) {
		NRF_LOG_INFO("check_function.");
		NRF_LOG_FLUSH();
//				disable_battery_timer();

				check_flag = true;
  			uint32_t err_code;
  			char data[] = "00|00|00|00";
  			uint16_t dlvr_data[4];

				if ( checking_flag == 0 ) {
		NRF_LOG_INFO("ble_connect = %d .", ble_connect);
		NRF_LOG_FLUSH();
				}	else	{
					  dlvr(&dlvr_data[0], &dlvr_data[1], &dlvr_data[2], &dlvr_data[3]);
		NRF_LOG_INFO("dlvr read = %d, %d, %d, %d", dlvr_data[0], dlvr_data[1], dlvr_data[2], dlvr_data[3]);
						sprintf(data, "%x|%x|%x|%x", dlvr_data[0], dlvr_data[1], dlvr_data[2], dlvr_data[3]);

						NRF_LOG_INFO("Send button state change data = %s", data);

  					uint16_t data_len = strlen(data);
						err_code = ble_nus_string_send(&m_nus, data, &data_len);
					
						NRF_LOG_INFO("err_code = %d", err_code);
						if ( err_code != NRF_SUCCESS ) {
						} else {
							APP_ERROR_CHECK(err_code);
						}
				}
				


/*				
		show_check_ready(4);
		spi_init();

//		nrf_delay_ms(500);
		nrf_delay_ms(10);
		nrf_gpio_cfg_output(WSBC_RST);
//		nrf_gpio_pin_write(WSBC_RST, HIGH);
//		nrf_delay_ms(500);

				
		show_check_ready_count(3);
//		resetAFE4300();

				//afe4300_init();
                afe4k3_init();
                afe4k3_getcrt();
                afe4k3_enable(false);

				show_check(0);

				
				    nrf_delay_ms(1000);

				if ( checking_flag == 0 ) {
					fstorage_read();
				} 

      	show_check_progress(1);
	
//				int afe4300_result = afe4300_check(order_data[0], order_data[1], order_data[2], order_data[3], &BCM_result[0], &BCM_result[1], &BCM_result[2], &BCM_result[3]);
        afe4k3_enable(true);
				uint16_t afe4300_result = get_calodi();
        afe4k3_enable(false);
				//readRegister(ADC_DATA_RESULT);

      	show_check_progress(2);

//				NRF_LOG_INFO("BCM_result[0] = "NRF_LOG_FLOAT_MARKER, NRF_LOG_FLOAT(BCM_result[0]));
//				NRF_LOG_INFO("BCM_result[0] = "NRF_LOG_FLOAT_MARKER_, NRF_LOG_FLOAT_(BCM_result[0]));

 				sprintf(afe4300_data, ""NRF_LOG_FLOAT_MARKER_"|"NRF_LOG_FLOAT_MARKER_"|"NRF_LOG_FLOAT_MARKER_"|"NRF_LOG_FLOAT_MARKER_"", NRF_LOG_FLOAT_(BCM_result[0]), NRF_LOG_FLOAT_(BCM_result[1]), NRF_LOG_FLOAT_(BCM_result[2]), NRF_LOG_FLOAT_(BCM_result[3]));
				NRF_LOG_INFO("afe4300_data = %s", afe4300_data);
				NRF_LOG_INFO("afe4300_result : %d", afe4300_result);

      	show_check_progress(4);
				
				uint32_t result = ppg_init(&result_data[0], &result_data[1]);


				if ( result == 1 ) {
  				NRF_LOG_INFO("ppg_init is true");

					if ( order_data[0] != 0 && order_data[1] != 0 ) {
						result_data[2] = order_data[1] / ( (order_data[0]*0.01 ) * (order_data[0]*0.01 ) );
						if ( order_data[3] == 0 ) {
							result_data[3] = 66.47 + (13.75 * order_data[1]) + (5 * order_data[0]) - (6.76 * order_data[2]);
						} else {
							result_data[3] = 655.1 + (9.56 * order_data[1]) + (1.85 * order_data[0]) - (4.68* order_data[2]);
						}
					}

/*					
				  if ( hartrate != 0 ) {
  				  hartrate = hartrate - 20;
	  			  if ( hartrate < 50 ) hartrate = hartrate + 10;
				  }
//
  				sprintf(data, "%d|%d|%d|%d|%s", result_data[0], result_data[1], result_data[2], result_data[3], afe4300_data);
					
				} else {
						NRF_LOG_INFO("ppg_init is error hartrate = %d, spo2 = %d", result_data[0], result_data[1]);
						result_data[0] = 0;
						sprintf(data, "00|00|00|00|%s", afe4300_data);
				}
				
			NRF_LOG_FLUSH();
			uint16_t len = strlen(data);


			NRF_LOG_INFO("len = %i", len);
			NRF_LOG_INFO("Send button state change data = %s", data);
			NRF_LOG_FLUSH();

				
		if ( ble_connect == 0 ) {
		
		}	else	{
				err_code = ble_nus_string_send(&m_nus, data, &len);
				APP_ERROR_CHECK(err_code);
		}


		if ( result == 1 ) {
			int hr_level = getHeartRateLevel(result_data[0], order_data[2]);
			int spo2_level = getSpo2Level(result_data[1]);
			int bmi_level = getBMILevel(result_data[2]);
			int kcal_level = getKcalLevel(result_data[3], order_data[2], order_data[3]);
			int bodyfat_level = getFatLevel(result_data[4], order_data[2], order_data[3]);
			
			result_data[5] = 0;
			
			if ( hr_level == BODY_HIGH ) {
				result_data[5] = result_data[5] + 5;
			} else if ( hr_level == BODY_STAND ) {
				result_data[5] = result_data[5] + 10;
			} else {
				result_data[5] = result_data[5] + 20;
			}
			if ( spo2_level == BODY_HIGH ) {
				result_data[5] = result_data[5] + 20;
			} else if ( spo2_level == BODY_STAND ) {
				result_data[5] = result_data[5] + 10;
			} else {
				result_data[5] = result_data[5] + 5;
			}
			if ( bmi_level == BODY_HIGH ) {
				result_data[5] = result_data[5] + 5;
			} else if ( bmi_level == BODY_STAND ) {
				result_data[5] = result_data[5] + 10;
			} else {
				result_data[5] = result_data[5] + 20;
			}
			if ( kcal_level == BODY_HIGH ) {
				result_data[5] = result_data[5] + 20;
			} else if ( kcal_level == BODY_STAND ) {
				result_data[5] = result_data[5] + 10;
			} else {
				result_data[5] = result_data[5] + 5;
			}
			if ( bodyfat_level == BODY_HIGH ) {
				result_data[5] = result_data[5] + 5;
			} else if ( bodyfat_level == BODY_STAND ) {
				result_data[5] = result_data[5] + 10;
			} else {
				result_data[5] = result_data[5] + 20;
			}

			screen_flag = 1;
			screen_print();
			timer_count = 0;
		} else {
			timer_count = 97;
			screen_flag = 0;
			show_check_fail();
		}
*/
		check_flag = false;
		
//		spi_pin_clear();

//		enable_battery_timer();

}


/**@brief Function for assert macro callback.
 *
 * @details This function will be called in case of an assert in the SoftDevice.
 *
 * @warning This handler is an example only and does not fit a final product. You need to analyse
 *          how your product is supposed to react in case of Assert.
 * @warning On assert from the SoftDevice, the system can only recover on reset.
 *
 * @param[in] line_num    Line number of the failing ASSERT call.
 * @param[in] p_file_name File name of the failing ASSERT call.
 */
void assert_nrf_callback(uint16_t line_num, const uint8_t * p_file_name)
{
    app_error_handler(DEAD_BEEF, line_num, p_file_name);
}


/**@brief Function for the GAP initialization.
 *
 * @details This function will set up all the necessary GAP (Generic Access Profile) parameters of
 *          the device. It also sets the permissions and appearance.
 */
static void gap_params_init(void)
{
    uint32_t                err_code;
    ble_gap_conn_params_t   gap_conn_params;
    ble_gap_conn_sec_mode_t sec_mode;

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&sec_mode);

    err_code = sd_ble_gap_device_name_set(&sec_mode,
                                          (const uint8_t *) DEVICE_NAME,
                                          strlen(DEVICE_NAME));
    APP_ERROR_CHECK(err_code);

    memset(&gap_conn_params, 0, sizeof(gap_conn_params));

    gap_conn_params.min_conn_interval = MIN_CONN_INTERVAL;
    gap_conn_params.max_conn_interval = MAX_CONN_INTERVAL;
    gap_conn_params.slave_latency     = SLAVE_LATENCY;
    gap_conn_params.conn_sup_timeout  = CONN_SUP_TIMEOUT;

    err_code = sd_ble_gap_ppcp_set(&gap_conn_params);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for handling the data from the Nordic UART Service.
 *
 * @details This function will process the data received from the Nordic UART BLE Service and send
 *          it to the UART module.
 *
 * @param[in] p_nus    Nordic UART Service structure.
 * @param[in] p_data   Data to be send to UART module.
 * @param[in] length   Length of the data.
 */
/**@snippet [Handling the data received over BLE] */
void nus_data_handler(ble_nus_evt_t * p_evt)
{
    if (p_evt->type == BLE_NUS_EVT_RX_DATA)
    {
 				int8_t data[1];
 				sprintf(data, "%s", p_evt->params.rx_data.p_data);
				
  			int i = 0;  
				char *ptr = strtok(data, "|");      // " " ???? ?????? ???????? ???????? ????, ?????? ????

				while (ptr != NULL)               // ???? ???????? ?????? ???? ?????? ????
				{
						strs[i] = ptr;             // ???????? ???? ?? ?????? ?????? ?????? ?????? ?????? ????
						i++;             
						ptr = strtok(NULL, "|");      // ???? ???????? ?????? ???????? ????
				}

			if ( strncmp( strs[0], "ORDER", 5 ) == 0 ) {
						check_function(1);

//				sprintf(ok_data, "ORDER|OK|%d", stepcount);
// 				sprintf(write_data, "%s|%s|%s|%s|%s|%d|%d|%d|%d|%d|%d|%d", "OD", strs[1],strs[2],strs[3],strs[4], stepcount, step_data[1], step_data[2], step_data[3], step_data[4], step_data[5], step_data[6]);
//				sprintf(send_data, "STEP|%d|%d|%d|%d|%d|%d|%d", stepcount, step_data[1], step_data[2], step_data[3], step_data[4], step_data[5], step_data[6]);

//				if ( screen_view_flag == 1 ) {
//					screen_on(false);
//				}
//				write_data_change_flag = true;
			} else if ( strncmp( strs[0], "TIME", 4 ) == 0 ){
				now_min = settime(strs[1]);
						NRF_LOG_INFO("TIME receive  now_min = %d", now_min);
						NRF_LOG_FLUSH();

				if ( screen_view_flag == 1 ) {
					screen_on(false);
				}
				step_data_send_flag = true;
			} else {
				if ( check_flag ) {

						int8_t checkingdata[1];
							
						sprintf(checkingdata, "checking");
						uint16_t len = strlen(checkingdata);
						NRF_LOG_INFO("check_flag is true len = %i", len);
						NRF_LOG_INFO("check_flag is true  data = %s", checkingdata);
						NRF_LOG_FLUSH();
						APP_ERROR_CHECK(ble_nus_string_send(&m_nus, checkingdata, &len));
							
				} else {
						if ( screen_view_flag == 1 ) {
							screen_on(true);
						}
						check_function(1);
				}
			}				
    }

						NRF_LOG_FLUSH();
}
/**@snippet [Handling the data received over BLE] */


/**@brief Function for initializing services that will be used by the application.
 */
static void services_init(void)
{
    uint32_t       err_code;
    ble_nus_init_t nus_init;

    memset(&nus_init, 0, sizeof(nus_init));

    nus_init.data_handler = nus_data_handler;

    err_code = ble_nus_init(&m_nus, &nus_init);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for handling an event from the Connection Parameters Module.
 *
 * @details This function will be called for all events in the Connection Parameters Module
 *          which are passed to the application.
 *
 * @note All this function does is to disconnect. This could have been done by simply setting
 *       the disconnect_on_fail config parameter, but instead we use the event handler
 *       mechanism to demonstrate its use.
 *
 * @param[in] p_evt  Event received from the Connection Parameters Module.
 */
static void on_conn_params_evt(ble_conn_params_evt_t * p_evt)
{
    uint32_t err_code;

    if (p_evt->evt_type == BLE_CONN_PARAMS_EVT_FAILED)
    {
        err_code = sd_ble_gap_disconnect(m_conn_handle, BLE_HCI_CONN_INTERVAL_UNACCEPTABLE);
        APP_ERROR_CHECK(err_code);
    }
}


/**@brief Function for handling errors from the Connection Parameters module.
 *
 * @param[in] nrf_error  Error code containing information about what went wrong.
 */
static void conn_params_error_handler(uint32_t nrf_error)
{
    APP_ERROR_HANDLER(nrf_error);
}


/**@brief Function for initializing the Connection Parameters module.
 */
static void conn_params_init(void)
{
    uint32_t               err_code;
    ble_conn_params_init_t cp_init;

    memset(&cp_init, 0, sizeof(cp_init));

    cp_init.p_conn_params                  = NULL;
    cp_init.first_conn_params_update_delay = FIRST_CONN_PARAMS_UPDATE_DELAY;
    cp_init.next_conn_params_update_delay  = NEXT_CONN_PARAMS_UPDATE_DELAY;
    cp_init.max_conn_params_update_count   = MAX_CONN_PARAMS_UPDATE_COUNT;
    cp_init.start_on_notify_cccd_handle    = BLE_GATT_HANDLE_INVALID;
    cp_init.disconnect_on_fail             = false;
    cp_init.evt_handler                    = on_conn_params_evt;
    cp_init.error_handler                  = conn_params_error_handler;

    err_code = ble_conn_params_init(&cp_init);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for putting the chip into sleep mode.
 *
 * @note This function will not return.
 */
static void sleep_mode_enter(void)
{
    uint32_t err_code = bsp_indication_set(BSP_INDICATE_IDLE);
    APP_ERROR_CHECK(err_code);

    // Prepare wakeup buttons.
    err_code = bsp_btn_ble_sleep_mode_prepare();
    APP_ERROR_CHECK(err_code);

    // Go to system-off mode (this function will not return; wakeup will cause a reset).
    err_code = sd_power_system_off();
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for handling advertising events.
 *
 * @details This function will be called for advertising events which are passed to the application.
 *
 * @param[in] ble_adv_evt  Advertising event.
 */
static void on_adv_evt(ble_adv_evt_t ble_adv_evt)
{
    uint32_t err_code;

    switch (ble_adv_evt)
    {
        case BLE_ADV_EVT_FAST:
            err_code = bsp_indication_set(BSP_INDICATE_ADVERTISING);
            APP_ERROR_CHECK(err_code);
            break;
        case BLE_ADV_EVT_IDLE:
            //sleep_mode_enter();
            break;
        default:
            break;
    }
}


/**@brief Function for handling BLE events.
 *
 * @param[in]   p_ble_evt   Bluetooth stack event.
 * @param[in]   p_context   Unused.
 */
static void ble_evt_handler(ble_evt_t const * p_ble_evt, void * p_context)
{
    uint32_t err_code;

    switch (p_ble_evt->header.evt_id)
    {
        case BLE_GAP_EVT_CONNECTED:
            NRF_LOG_INFO("Connected");
						err_code = bsp_indication_set(BSP_INDICATE_CONNECTED);
            APP_ERROR_CHECK(err_code);
            m_conn_handle = p_ble_evt->evt.gap_evt.conn_handle;
            break;

        case BLE_GAP_EVT_DISCONNECTED:
            NRF_LOG_INFO("Disconnected");
				    ble_connect = 0;
            // LED indication will be changed when advertising starts.
            m_conn_handle = BLE_CONN_HANDLE_INVALID;
				
						show_bt(ble_connect);
						screen_view(screen_flag-1);
            break;

#ifndef S140
        case BLE_GAP_EVT_PHY_UPDATE_REQUEST:
        {
            NRF_LOG_INFO("PHY update request.");
            ble_gap_phys_t const phys =
            {
                .rx_phys = BLE_GAP_PHY_AUTO,
                .tx_phys = BLE_GAP_PHY_AUTO,
            };
            err_code = sd_ble_gap_phy_update(p_ble_evt->evt.gap_evt.conn_handle, &phys);
            APP_ERROR_CHECK(err_code);
        } break;
#endif

        case BLE_GAP_EVT_SEC_PARAMS_REQUEST:
            NRF_LOG_INFO("BLE_GAP_EVT_SEC_PARAMS_REQUEST");
            // Pairing not supported
            err_code = sd_ble_gap_sec_params_reply(m_conn_handle, BLE_GAP_SEC_STATUS_PAIRING_NOT_SUPP, NULL, NULL);
            APP_ERROR_CHECK(err_code);
            break;
#if !defined (S112)
         case BLE_GAP_EVT_DATA_LENGTH_UPDATE_REQUEST:
        {
            NRF_LOG_INFO("BLE_GAP_EVT_DATA_LENGTH_UPDATE_REQUEST");
            ble_gap_data_length_params_t dl_params;

            // Clearing the struct will effectivly set members to @ref BLE_GAP_DATA_LENGTH_AUTO
            memset(&dl_params, 0, sizeof(ble_gap_data_length_params_t));
            err_code = sd_ble_gap_data_length_update(p_ble_evt->evt.gap_evt.conn_handle, &dl_params, NULL);
            APP_ERROR_CHECK(err_code);
        } break;
#endif //!defined (S112)
        case BLE_GATTS_EVT_SYS_ATTR_MISSING:
            NRF_LOG_INFO("BLE_GATTS_EVT_SYS_ATTR_MISSING");
            // No system attributes have been stored.
            err_code = sd_ble_gatts_sys_attr_set(m_conn_handle, NULL, 0, 0);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GATTC_EVT_TIMEOUT:
            NRF_LOG_INFO("BLE_GATTC_EVT_TIMEOUT");
            // Disconnect on GATT Client timeout event.
            err_code = sd_ble_gap_disconnect(p_ble_evt->evt.gattc_evt.conn_handle,
                                             BLE_HCI_REMOTE_USER_TERMINATED_CONNECTION);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GATTS_EVT_TIMEOUT:
            NRF_LOG_INFO("BLE_GATTS_EVT_TIMEOUT");
            // Disconnect on GATT Server timeout event.
            err_code = sd_ble_gap_disconnect(p_ble_evt->evt.gatts_evt.conn_handle,
                                             BLE_HCI_REMOTE_USER_TERMINATED_CONNECTION);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_EVT_USER_MEM_REQUEST:
            NRF_LOG_INFO("BLE_EVT_USER_MEM_REQUEST");
            err_code = sd_ble_user_mem_reply(p_ble_evt->evt.gattc_evt.conn_handle, NULL);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GATTS_EVT_RW_AUTHORIZE_REQUEST:
        {
            NRF_LOG_INFO("BLE_GATTS_EVT_RW_AUTHORIZE_REQUEST");
            ble_gatts_evt_rw_authorize_request_t  req;
            ble_gatts_rw_authorize_reply_params_t auth_reply;

            req = p_ble_evt->evt.gatts_evt.params.authorize_request;

            if (req.type != BLE_GATTS_AUTHORIZE_TYPE_INVALID)
            {
                if ((req.request.write.op == BLE_GATTS_OP_PREP_WRITE_REQ)     ||
                    (req.request.write.op == BLE_GATTS_OP_EXEC_WRITE_REQ_NOW) ||
                    (req.request.write.op == BLE_GATTS_OP_EXEC_WRITE_REQ_CANCEL))
                {
                    if (req.type == BLE_GATTS_AUTHORIZE_TYPE_WRITE)
                    {
                        auth_reply.type = BLE_GATTS_AUTHORIZE_TYPE_WRITE;
                    }
                    else
                    {
                        auth_reply.type = BLE_GATTS_AUTHORIZE_TYPE_READ;
                    }
                    auth_reply.params.write.gatt_status = APP_FEATURE_NOT_SUPPORTED;
                    err_code = sd_ble_gatts_rw_authorize_reply(p_ble_evt->evt.gatts_evt.conn_handle,
                                                               &auth_reply);
                    APP_ERROR_CHECK(err_code);
                }
            }
        } break; // BLE_GATTS_EVT_RW_AUTHORIZE_REQUEST

        default:
            // No implementation needed.
            break;
    }
}


/**@brief Function for the SoftDevice initialization.
 *
 * @details This function initializes the SoftDevice and the BLE event interrupt.
 */
static void ble_stack_init(void)
{
    ret_code_t err_code;

    err_code = nrf_sdh_enable_request();
    APP_ERROR_CHECK(err_code);

    // Configure the BLE stack using the default settings.
    // Fetch the start address of the application RAM.
    uint32_t ram_start = 0;
    err_code = nrf_sdh_ble_default_cfg_set(APP_BLE_CONN_CFG_TAG, &ram_start);
    APP_ERROR_CHECK(err_code);

    // Enable BLE stack.
    err_code = nrf_sdh_ble_enable(&ram_start);
    APP_ERROR_CHECK(err_code);

    // Register a handler for BLE events.
    NRF_SDH_BLE_OBSERVER(m_ble_observer, APP_BLE_OBSERVER_PRIO, ble_evt_handler, NULL);
}


/**@brief Function for handling events from the GATT library. */
void gatt_evt_handler(nrf_ble_gatt_t * p_gatt, nrf_ble_gatt_evt_t const * p_evt)
{
    if ((m_conn_handle == p_evt->conn_handle) && (p_evt->evt_id == NRF_BLE_GATT_EVT_ATT_MTU_UPDATED))
    {
        m_ble_nus_max_data_len = p_evt->params.att_mtu_effective - OPCODE_LENGTH - HANDLE_LENGTH;
        NRF_LOG_INFO("Data len is set to 0x%X(%d)", m_ble_nus_max_data_len, m_ble_nus_max_data_len);
    }
    NRF_LOG_INFO("ATT MTU exchange completed. central 0x%x peripheral 0x%x",
                  p_gatt->att_mtu_desired_central,
                  p_gatt->att_mtu_desired_periph);
}


/**@brief Function for initializing the GATT library. */
void gatt_init(void)
{
    ret_code_t err_code;

    err_code = nrf_ble_gatt_init(&m_gatt, gatt_evt_handler);
    APP_ERROR_CHECK(err_code);

    err_code = nrf_ble_gatt_att_mtu_periph_set(&m_gatt, 64);
    APP_ERROR_CHECK(err_code);
}



/**@brief Function for handling events from the BSP module.
 *
 * @param[in]   event   Event generated by button press.
 */
void bsp_event_handler(bsp_event_t event)
{
  	timer_count = 0;	//	screen sleep timer init
    uint32_t err_code;
    switch (event)
    {
        case BSP_EVENT_KEY_0:	//	screen on/off
							if ( !check_flag ) {
								screen_sleep();
							}
              break;
        case BSP_EVENT_KEY_1:	// scroll
				      if ( screen_view_flag == 0 ) {
								screen_print();
							}
							break;
        case BSP_EVENT_KEY_2:	//	check sensor
				      if ( screen_view_flag == 1 ) {
								screen_on(true);
							}
							if ( check_flag ) {
							} else {
								check_function(0);
							}
           break;
        case BSP_EVENT_KEY_7:	// power off
							screen_off();
							nrf_gpio_pin_write(SW_PWR_HOLD, LOW);
					break;
        default:
            break;
    }
}



/**@brief Function for initializing the Advertising functionality.
 */
static void advertising_init(void)
{
    uint32_t               err_code;
    ble_advertising_init_t init;

    memset(&init, 0, sizeof(init));

    init.advdata.name_type          = BLE_ADVDATA_FULL_NAME;
    init.advdata.include_appearance = false;
//    init.advdata.flags              = BLE_GAP_ADV_FLAGS_LE_ONLY_LIMITED_DISC_MODE;
    init.advdata.flags              = BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE ;

    init.srdata.uuids_complete.uuid_cnt = sizeof(m_adv_uuids) / sizeof(m_adv_uuids[0]);
    init.srdata.uuids_complete.p_uuids  = m_adv_uuids;

    init.config.ble_adv_fast_enabled  = true;
    init.config.ble_adv_fast_interval = APP_ADV_INTERVAL;
//    init.config.ble_adv_fast_timeout  = APP_ADV_TIMEOUT_IN_SECONDS;
    init.config.ble_adv_fast_timeout  = 0;	//	timeout setting

    init.evt_handler = on_adv_evt;

    err_code = ble_advertising_init(&m_advertising, &init);
    APP_ERROR_CHECK(err_code);

    ble_advertising_conn_cfg_tag_set(&m_advertising, APP_BLE_CONN_CFG_TAG);
}



/**@brief Function for initializing buttons and leds.
 *
 * @param[out] p_erase_bonds  Will be true if the clear bonding button was pressed to wake the application up.
 */
static void buttons_leds_init(bool * p_erase_bonds)
{
    bsp_event_t startup_event;

    uint32_t err_code = bsp_init(BSP_INIT_LED | BSP_INIT_BUTTONS, bsp_event_handler);
//    uint32_t err_code = bsp_init(BSP_INIT_BUTTONS, bsp_event_handler);
    APP_ERROR_CHECK(err_code);

//    err_code = bsp_btn_ble_init(NULL, &startup_event);
//    APP_ERROR_CHECK(err_code);

      NRF_LOG_INFO("buttons_leds_init end ");
      NRF_LOG_FLUSH();

    *p_erase_bonds = (startup_event == BSP_EVENT_CLEAR_BONDING_DATA);
}


/**@brief Function for initializing the nrf log module.*/
static void log_init(void)
{
    ret_code_t err_code = NRF_LOG_INIT(NULL);
    APP_ERROR_CHECK(err_code);

    NRF_LOG_DEFAULT_BACKENDS_INIT();
}


/**@brief Function for placing the application in low power state while waiting for events.*/
static void power_manage(void)
{
    uint32_t err_code = sd_app_evt_wait();
    APP_ERROR_CHECK(err_code);
}



//* fstorage start
void fstorage_evt_handler(nrf_fstorage_evt_t * p_evt);
void fstorage_write_step();

NRF_FSTORAGE_DEF(nrf_fstorage_t fstorage) =
{
    /* Set a handler for fstorage events. */
    .evt_handler = fstorage_evt_handler,

    /* These below are the boundaries of the flash space assigned to this instance of fstorage.
     * You must set these manually, even at runtime, before nrf_fstorage_init() is called.
     * The function nrf5_flash_end_addr_get() can be used to retrieve the last address on the
     * last page of flash available to write data. */
    .start_addr = 0x3e000,
    .end_addr   = 0x3ffff,
};
void fstorage_evt_handler(nrf_fstorage_evt_t * p_evt)
{
    if (p_evt->result != NRF_SUCCESS)
    {
        NRF_LOG_INFO("--> ERROR while executing an fstorage operation.");
        return;
    }

    switch (p_evt->id)
    {
        case NRF_FSTORAGE_EVT_WRITE_RESULT:
        {
            NRF_LOG_INFO("--> wrote %d, address 0x%x.", p_evt->len, p_evt->addr);
        } break;

        case NRF_FSTORAGE_EVT_ERASE_RESULT:
        {
            NRF_LOG_INFO("--> erased %d,  address 0x%x.", p_evt->len, p_evt->addr);
        } break;

        default:
            break;
    }
}
uint32_t nrf5_flash_end_addr_get()
{
    uint32_t const bootloader_addr = NRF_UICR->NRFFW[0];
    uint32_t const page_sz         = NRF_FICR->CODEPAGESIZE;
    uint32_t const code_sz         = NRF_FICR->CODESIZE;

    return (bootloader_addr != 0xFFFFFFFF ?
            bootloader_addr : (code_sz * page_sz));
}
void wait_for_flash_ready(nrf_fstorage_t const * p_fstorage)
{
    /* While fstorage is busy, sleep and wait for an event. */
    while (nrf_fstorage_is_busy(p_fstorage))
    {
//        power_manage();
    }
}
bool fstorage_read(void) {
/*    ret_code_t rc;

	  read_data[0] = 0;

    // Read data. 
    rc = nrf_fstorage_read(&fstorage, 0x3f000, read_data, 60);

  	int i = 0;  
		char *ptr = strtok(read_data, "|");
		while (ptr != NULL)
		{
				strs[i] = ptr;
				i++;             
				ptr = strtok(NULL, "|");
		}
	
		if ( strncmp( strs[0], "OD", 2 ) == 0 ) {
      NRF_LOG_INFO("fstorage_read nrf_fstorage_read = %s", read_data);
      NRF_LOG_FLUSH();
				order_data[0] = atoi(strs[3]);
				order_data[1] = atoi(strs[4]);
				order_data[2] = atoi(strs[1]);
				order_data[3] = atoi(strs[2]);
				step_data[0] = atoi(strs[5]);
				step_data[1] = atoi(strs[6]);
				step_data[2] = atoi(strs[7]);
				step_data[3] = atoi(strs[8]);
				step_data[4] = atoi(strs[9]);
				step_data[5] = atoi(strs[10]);
				step_data[6] = atoi(strs[11]);
				step_data[7] = atoi(strs[12]);

		  return true;
	  } else {
      NRF_LOG_INFO("fstorage_read return false ");
      NRF_LOG_FLUSH();

				order_data[0] = 0;
				order_data[1] = 0;
				order_data[2] = 0;
				order_data[3] = 0;
				step_data[0] = 0;
				step_data[1] = 0;
				step_data[2] = 0;
				step_data[3] = 0;
				step_data[4] = 0;
				step_data[5] = 0;
				step_data[6] = 0;
				step_data[7] = 0;

		  return false;
	  }
*/
		  return false;
}
void fstorage_init(void) {
	    NRF_LOG_INFO("fstorage_init");
    NRF_LOG_FLUSH();

    ret_code_t rc;
    nrf_fstorage_api_t * p_fs_api;
    p_fs_api = &nrf_fstorage_sd;

    rc = nrf_fstorage_init(&fstorage, p_fs_api, NULL);
    APP_ERROR_CHECK(rc);
				
    (void) nrf5_flash_end_addr_get();

		if ( !fstorage_read() ) {
			// Let's write to flash.
			uint32_t m_data = 0xBADC0FFE;
			rc = nrf_fstorage_write(&fstorage, 0x3e000, &m_data, sizeof(m_data), NULL);
			APP_ERROR_CHECK(rc);
			wait_for_flash_ready(&fstorage);
			m_data = 0xDEADBEEF;
			rc = nrf_fstorage_write(&fstorage, 0x3e100, &m_data, sizeof(m_data), NULL);
			APP_ERROR_CHECK(rc);
			wait_for_flash_ready(&fstorage);
			rc = nrf_fstorage_write(&fstorage, 0x3f000, write_data, sizeof(write_data), NULL);
//* 
			NRF_LOG_INFO("fstorage_init. rc = %d, %d", rc, sizeof(write_data));
			NRF_LOG_FLUSH();
//*/
			APP_ERROR_CHECK(rc);
			wait_for_flash_ready(&fstorage);
		}
}
void fstorage_write(void) {
    ret_code_t rc;
		rc = nrf_fstorage_erase(&fstorage, 0x3f000, 1, NULL);
    wait_for_flash_ready(&fstorage);

    rc = nrf_fstorage_write(&fstorage, 0x3f000, write_data, sizeof(write_data), NULL);
/*
	  NRF_LOG_INFO("fstorage_write nrf_fstorage_write rc :  %d", rc);
		NRF_LOG_FLUSH();		
//*/
    APP_ERROR_CHECK(rc);
    wait_for_flash_ready(&fstorage);
}

//fstorage end */

double mysqrt(float number) {
	unsigned int NUM_REPEAT = 16;
	unsigned int k;
	double res;
	double tmp = (double)number;
	for(k=0,res=tmp;k<NUM_REPEAT;k++) {
    if(res<1.0) break; 
    res = (res*res+tmp)/(2.0*res);
  }
  return res; 
}

float returnNumber(float a, float b) {
	float returnvalue = 0;
	if ( a > b ) returnvalue = a - b;
	else returnvalue = b - a;
	return returnvalue;
}
double returnStep(int x, int y, int z, int pre_x, int pre_y, int pre_z) {
	double sqrt = 0;
	
	if ( pre_x != 0 && pre_y != 0 && pre_z != 0 ) {
  	int xx = 0, yy=0, zz=0;
		if ( pre_x !=  x ) {
			xx = returnNumber(x,pre_x)*returnNumber(x,pre_x);
		}
		if ( pre_y != y ) {
  	  yy = returnNumber(y,pre_y)*returnNumber(y,pre_y);
		}
		if ( pre_z != z ) {
	    zz = returnNumber(z,pre_z)*returnNumber(z,pre_z);
		}
		
		if ( zz > xx+yy ) {
			sqrt = 0; 
		} else {
     	sqrt = mysqrt(xx+yy-zz);
		}
	}
	
	pre_x = x;
	pre_y = y;
	pre_z = z;
	return sqrt;
}



uint8_t day = 0;
uint8_t hour = 0;
void check_day(void) {
				NRF_LOG_INFO("----- check_day -----");
		date_hour_seconds = gettime();
		timeinfo = localtime(&date_hour_seconds);
/*
	if ( day < 1 ) {
		day = timeinfo->tm_mday;
		hour = timeinfo->tm_hour;
	} else {
		if ( timeinfo->tm_mday != day ) {
			for ( int i=(sizeof(step_data) / sizeof(int)) ;i>1; i++ ) {
				step_data[i-1] = step_data[1-2];
			}
			day = timeinfo->tm_mday;
		}
		
		if ( timeinfo->tm_hour != hour ) {
				hour = timeinfo->tm_hour;
 				sprintf(write_data, "%s|%s|%s|%s|%s|%d|%d|%d|%d|%d|%d|%d", "OD", order_data[2],order_data[3],order_data[0],order_data[1], step_data[0], step_data[1], step_data[2], step_data[3], step_data[4], step_data[5], step_data[6]);

				NRF_LOG_INFO("write_data : %s", write_data);
		}

	}
	//*/	
}
void saadc_callback_m(nrf_drv_saadc_evt_t const * p_event)
{
    if (p_event->type == NRF_DRV_SAADC_EVT_DONE)
    {
        ret_code_t err_code;
				uint16_t value;


        err_code = nrf_drv_saadc_buffer_convert(p_event->data.done.p_buffer, SAMPLES_IN_BUFFER);
        APP_ERROR_CHECK(err_code);

        int i;

        for (i = 0; i < SAMPLES_IN_BUFFER; i++)
        {
            value = p_event->data.done.p_buffer[i]*0.7;
				}
				int battery_value = value - 300;
//*
				if ( (timer_count %10) == 0 ) {
        NRF_LOG_INFO("saadc_callback_m  pre = %d, now = %d, ==========   count = %d",  battery_value_pre, battery_value, timer_count);
				NRF_LOG_FLUSH();
				}
//*/				
				if ( timer_count == 0 ) { 				
					check_day();
				}
				if ( battery_value < battery_value_pre && battery_value < 100 ) {
					battery_value_pre = battery_value;
					screen_flag--;
					screen_print();
				} else if ( battery_value_pre == 0 ) {
						battery_value_pre = battery_value;
					if ( timer_count == 0 ) {
							screen_flag--;
							screen_print();
					}
				} else if ( battery_value_pre + 3 < battery_value ) {
					battery_value_pre = battery_value;
					screen_flag--;
					screen_print();
				}
				
				if ( battery_value < 30 ) {	//	RED LED ON???
				}
				
				timer_led_event_handler();
    }
}





/**@brief Application main function.
 */
int main(void)
{
    uint32_t 	err_code;
    bool     	erase_bonds;
    int 					pre_x=0, pre_y = 0, pre_z = 0;
		
    // Initialize.
		nrf_delay_ms(2000);
		nrf_gpio_cfg_output(SW_PWR_HOLD);
	  nrf_gpio_pin_write(SW_PWR_HOLD, HIGH);

	//NRF_POWER->DCDCEN = 1;
sd_power_dcdc_mode_set(NRF_POWER_DCDC_ENABLE); 

  	err_code = app_timer_init();
    APP_ERROR_CHECK(err_code);

    log_init();
    NRF_LOG_INFO("============================    program init start testttt");
		twi_init();
		
//		oled_init_i2c();
//		show_zaigle();
//		show_time(0, 0, 0, 0, 0);		//	loading
		nrf_delay_ms(2000);
		rtc_init();
		
    ble_stack_init();
    gap_params_init();
    gatt_init();
    services_init();
    advertising_init();
    conn_params_init();

//		bma250_init();
		
		timer_count = 0;
//		battery_init(saadc_callback_m);
		//buzzer_on();
		
//    afe4300_init();

//		fstorage_init();
    // Enter main loop.
    NRF_LOG_INFO("============================    program for start");

//				hartrate = 0, spo2 = 0, fat = 0, stress = 0, bmi = 0, kcal = 0;
		timer_count = 0;
		day = 0;
		hour = 0;

		battery_value_pre=0;
		ble_connect = 0;
		stepcount = 0;

//				NRF_LOG_INFO("screen_view hb = %d, sp = %d, fat = %d, stress = %d, bmi = %d, kcal = %d", result_data[0], result_data[1], result_data[2], result_data[3]);
				NRF_LOG_FLUSH();
//		buttons_leds_init(&erase_bonds);
//		nrf_gpio_cfg_output(LED_1);
//	  nrf_gpio_pin_write(LED_1, LOW);
//		nrf_gpio_cfg_output(LED_2);
//	  nrf_gpio_pin_write(LED_2, LOW);
//		nrf_gpio_cfg_output(LED_3);
//	  nrf_gpio_pin_write(LED_3, LOW);

    NRF_LOG_INFO("UART Start!");
    err_code = ble_advertising_start(&m_advertising, BLE_ADV_MODE_FAST);
    APP_ERROR_CHECK(err_code);

		for (;;)
    {

			if ( write_data_change_flag ) {
				NRF_LOG_INFO("write_data_change_flag start  ------------------------");
// 				sprintf(write_data, "%s|%s|%s|%s|%s", "OD", strs[1],strs[2],strs[3],strs[4]);
//				sprintf(send_data, "STEP|%d|%d|%d|%d|%d|%d|%d", step_data[0], step_data[1], step_data[2], step_data[3], step_data[4], step_data[5], step_data[6]);
				
//				NRF_LOG_INFO("write_data : %s", write_data);
//				NRF_LOG_INFO("send_data : %s", send_data);
				NRF_LOG_INFO("ok_data : %s", ok_data);
				NRF_LOG_FLUSH();

				uint16_t len = strlen(ok_data);
				err_code = ble_nus_string_send(&m_nus, ok_data, &len);
				NRF_LOG_INFO("=============================    write_data_change_flag err_code = %d", err_code);
				NRF_LOG_FLUSH();
				APP_ERROR_CHECK(err_code);
				
				write_data_change_flag = false;
				step_data_send_flag = true;
			}
			if ( step_data_send_flag ) {
				NRF_LOG_INFO("step_data_send_flag start  ------------------------");
				NRF_LOG_FLUSH();

				uint16_t len = strlen(send_data);
				err_code = ble_nus_string_send(&m_nus, send_data, &len);
				NRF_LOG_INFO("=============================    step_data_send_flag err_code = %d, send_data = %s", err_code, send_data);
				NRF_LOG_FLUSH();
				APP_ERROR_CHECK(err_code);

//				fstorage_write();
//				fstorage_read();

				timer_count = 0;
				ble_connect = 1;

//				screen_view(screen_flag-1);

				step_data_send_flag = false;
			}

			/*
			uint32_t i=0;

			ret_code_t err_code;
			uint8_t val[6];
	    err_code = bma250_I2C_data_read(BMA250_ADDRESS,val,sizeof(val));
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
			
			uint32_t step = returnStep(xAccl, yAccl, zAccl, pre_x, pre_y, pre_z)/27;
			pre_x = xAccl;
			pre_y = yAccl;
			pre_z = zAccl;
//		NRF_LOG_INFO(" Acceleration step := %i", step);
//  	NRF_LOG_FLUSH();
			if ( step > 0 ) {
				NRF_LOG_INFO("returnStep   =====   step : %d", step);
				NRF_LOG_FLUSH();
				
				stepcount = stepcount + 1;
				if ( stepcount > 10000 ) stepcount = 0;
			
				if ( result_data[0] > 0 ) {
						if ( screen_flag == 8 ) {
								screen_view(7);
						}
						if ( screen_flag == 9 ) {
								screen_view(8);
						}

				} else {
						if ( screen_flag == 2 ) {
								screen_view(1);
						}
						if ( screen_flag == 3 ) {
								screen_view(2);
						}
				}
				step_data[0] = stepcount;
			}
			
//i++;
//if ( i > 60 ) { i =0; }	
        UNUSED_RETURN_VALUE(NRF_LOG_PROCESS());
		    // Make sure any pending events are cleared
*/
		nrf_delay_ms(400);
				
    }
}
