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
 * @defgroup rtc_example_main main.c
 * @{
 * @ingroup rtc_example
 * @brief Real Time Counter Example Application main file.
 *
 * This file contains the source code for a sample application using the Real Time Counter (RTC).
 *
 */

#include "nrf.h"
#include "nrf_gpio.h"
#include "nrf_drv_rtc.h"
#include "nrf_drv_clock.h"
#include "boards.h"
#include "app_error.h"
#include <stdint.h>
#include <stdbool.h>

#include "nrf_log.h"
#include "nrf_log_ctrl.h"
#include "nrf_log_default_backends.h"

#include "rtc.h"

const nrf_drv_rtc_t rtc = NRF_DRV_RTC_INSTANCE(2); /**< Declaring an instance of nrf_drv_rtc for RTC0. */

static time_t second = 0;

char *times[6]= { NULL, };
static struct tm time_struct;



/** @brief: Function for handling the RTC0 interrupts.
 * Triggered on TICK and COMPARE0 match.
 */
static void rtc_handler(nrf_drv_rtc_int_type_t int_type)
{
	
    if (int_type == NRF_DRV_RTC_INT_COMPARE0)
    {
    }
    else if (int_type == NRF_DRV_RTC_INT_TICK)
    {
    }
}

/** @brief Function starting the internal LFCLK XTAL oscillator.
 */
static void lfclk_config(void)
{
    ret_code_t err_code = nrf_drv_clock_init();
    APP_ERROR_CHECK(err_code);

    nrf_drv_clock_lfclk_request(NULL);
}

/** @brief Function initialization and configuration of RTC driver instance.
 */
static void rtc_config(void)
{
    uint32_t err_code;

    //Initialize RTC instance
    nrf_drv_rtc_config_t config = NRF_DRV_RTC_DEFAULT_CONFIG;
    config.prescaler = 4095;
    err_code = nrf_drv_rtc_init(&rtc, &config, rtc_handler);
    APP_ERROR_CHECK(err_code);

    //Enable tick event & interrupt
    nrf_drv_rtc_tick_enable(&rtc,true);

    //Set compare channel to trigger interrupt after COMPARE_COUNTERTIME seconds
    err_code = nrf_drv_rtc_cc_set(&rtc,0,COMPARE_COUNTERTIME*8,true);
    APP_ERROR_CHECK(err_code);

    //Power on RTC instance
    nrf_drv_rtc_enable(&rtc);
}

int settime(char *s)
{
				int i = 0;  
				char *ptr = strtok(s, ":");      // " " ???? ?????? ???????? ???????? ????, ?????? ????

				while (ptr != NULL)               // ???? ???????? ?????? ???? ?????? ????
				{
						times[i] = ptr;             // ???????? ???? ?? ?????? ?????? ?????? ?????? ?????? ????
						i++;             
						ptr = strtok(NULL, ":");      // ???? ???????? ?????? ???????? ????
				}
				
				time_struct.tm_year = atoi(times[0]) - 1900;
				time_struct.tm_mon = atoi(times[1]) - 1;
				time_struct.tm_mday = atoi(times[2]);
				time_struct.tm_hour = atoi(times[3]);
				time_struct.tm_min = atoi(times[4]);
				time_struct.tm_sec = atoi(times[5]);
				second = mktime(&time_struct);

				nrf_drv_rtc_counter_clear(&rtc);

				NRF_LOG_INFO("settime : %s", ctime(&second));
				NRF_LOG_FLUSH();
				
				return time_struct.tm_min;
}
time_t gettime(void){
	return second+(nrf_drv_rtc_counter_get(&rtc)/8);
}
void nowtime(void)
{
	second = second + (nrf_drv_rtc_counter_get(&rtc)/8);
	NRF_LOG_INFO("Current Time : %s, nrf_drv_rtc_counter_get : %d", ctime(&second), nrf_drv_rtc_counter_get(&rtc));
	NRF_LOG_FLUSH();
}

int rtc_init(void)
{
			lfclk_config();
			rtc_config();

			time_struct.tm_year = 2018 - 1900;
			time_struct.tm_mon = 0;
			time_struct.tm_mday = 1;
			time_struct.tm_hour = 0;
			time_struct.tm_min = 0;
			time_struct.tm_sec = 0;
			second = mktime(&time_struct);

			return 1;
}


/**  @} */
