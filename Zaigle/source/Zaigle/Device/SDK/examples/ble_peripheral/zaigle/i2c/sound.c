#include <stdio.h>
#include <string.h>
#include "nrf_drv_pwm.h"
#include "app_util_platform.h"
#include "app_error.h"
#include "boards.h"
#include "bsp.h"
#include "nrf_drv_clock.h"
#include "nrf_delay.h"
#include "nrf_log.h"
#include "nrf_log_ctrl.h"
#include "nrf_log_default_backends.h"

#define OUTPUT_PIN 10

static nrf_drv_pwm_t m_pwm0 = NRF_DRV_PWM_INSTANCE(0);

// Declare variables holding PWM sequence values. In this example only one channel is used 
nrf_pwm_values_individual_t seq_values[] = {0, 100, 200, 400,	600,	800,	1000,	1200,	1400,	1600,	1800,	2000,	4000,	8000};
nrf_pwm_sequence_t const seq =
{
    .values.p_individual = seq_values,
    .length          = NRF_PWM_VALUES_LENGTH(seq_values),
    .repeats         = 0,
    .end_delay       = 0
};


static void pwm_init(void)
{
		
    
    nrf_drv_pwm_config_t const config0 =
    {
        .output_pins =
        {
            OUTPUT_PIN, // channel 0
            NRF_DRV_PWM_PIN_NOT_USED,             // channel 1
            NRF_DRV_PWM_PIN_NOT_USED,             // channel 2
            NRF_DRV_PWM_PIN_NOT_USED,             // channel 3
        },
        .irq_priority = APP_IRQ_PRIORITY_LOWEST,
        .base_clock   = NRF_PWM_CLK_250kHz,
        .count_mode   = NRF_PWM_MODE_UP,
        .top_value    = 62,
        .load_mode    = NRF_PWM_LOAD_COMMON,
        .step_mode    = NRF_PWM_STEP_AUTO
    };
    // Init PWM without error handler
    APP_ERROR_CHECK(nrf_drv_pwm_init(&m_pwm0, &config0, NULL));

    seq_values->channel_0 = 15;
    nrf_drv_pwm_simple_playback(&m_pwm0, &seq, 1, NRF_DRV_PWM_FLAG_LOOP);
		NRF_LOG_INFO("buzzer end");
    NRF_LOG_FLUSH();
	
}


void buzzer_on(void)
{
		NRF_LOG_INFO("buzzer init");
    NRF_LOG_FLUSH();

    // Start clock for accurate frequencies
    //NRF_CLOCK->TASKS_HFCLKSTART = 1; 
    // Wait for clock to start
    //while(NRF_CLOCK->EVENTS_HFCLKSTARTED == 0) ;

    pwm_init();

    //for (;;)
    //{

    //}
}