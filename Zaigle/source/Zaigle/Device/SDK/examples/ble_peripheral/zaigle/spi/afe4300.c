#include "spi.h"
#include "afe4300.h"

#include "nrf_log.h"
#include "nrf_log_ctrl.h"
#include "nrf_log_default_backends.h"

#include "nrf_gpio.h"
#include "nrf_drv_timer.h"
#include "app_pwm.h"

static const nrf_drv_timer_t ToogleTimer = NRF_DRV_TIMER_INSTANCE(3);

void toggleChipSelectCLOCK()
{
  //Pull CS high to block MISO
		nrf_gpio_pin_write(WSBC_CLOCK, HIGH);
	  nrf_delay_ms(1);
   //Pull CS low for next instruction
		nrf_gpio_pin_write(WSBC_CLOCK, LOW);
}
void timer_toogle_event_handler(nrf_timer_event_t event_type, void* p_context)
{
	toggleChipSelectCLOCK();
}


void afe4k3_init(void)
{
//		nrf_gpio_cfg_output(WSBC_CLOCK);
//    uint32_t time_ticks;
    uint32_t err_code = NRF_SUCCESS;

//	nrf_drv_timer_config_t timer_cfg = NRF_DRV_TIMER_DEFAULT_CONFIG;
//    err_code = nrf_drv_timer_init(&ToogleTimer, &timer_cfg, timer_toogle_event_handler);
//    APP_ERROR_CHECK(err_code);

//    time_ticks = nrf_drv_timer_ms_to_ticks(&ToogleTimer, 1);

//    nrf_drv_timer_extended_compare(&ToogleTimer, NRF_TIMER_CC_CHANNEL0, time_ticks, NRF_TIMER_SHORT_COMPARE0_CLEAR_MASK, true);

//    nrf_drv_timer_enable(&ToogleTimer);
	

		nrf_gpio_pin_write(WSBC_RST, LOW);
	  nrf_delay_ms(5);

   //P6.0 is RESET and is pulled high (device in normal mode again)
		nrf_gpio_pin_write(WSBC_RST, HIGH);
	  nrf_delay_ms(5);

//	toggleChipSelectCLOCK();
    uint16_t v = 0;
    v = readRegister(0x01);
 		NRF_LOG_INFO("afe4k3_init v = %d", v);

    writeRegister(0x02, 0x0000);//    delay_ms(1);
		writeRegister(0x03, 0xFFFF);//    delay_ms(1);
    writeRegister(0x1A, 0x0030);//    delay_ms(1);

    writeRegister(0x01, 0x4143);//    delay_ms(1);

		writeRegister(0x0E, 0x0033);//    delay_ms(1); //BCM_DAC_FREQ
    writeRegister(0x10, 0x0063);//    delay_ms(1); //ADC_CONTROL_REGISTER2
    writeRegister(0x0F, 0x0000);//    delay_ms(1);
    writeRegister(0x09, 0x6006);//    delay_ms(1); //DEVICE_CONTROL1  6006

    uint16_t v1 = 0;
    v1 = readRegister(0x01);
 		NRF_LOG_INFO("afe4k3_init v1 = %d", v1);

    uint16_t val = 0;
    val = readRegister(0x01);
 		NRF_LOG_INFO("afe4k3_init val = %d", val);
}

void afe4k3_enable(bool enabled)
{
    if(enabled)
    {
        writeRegister(0x01, 0x4143);//    delay_ms(1);
        writeRegister(0x09, 0x6006);//    delay_ms(1); //DEVICE_CONTROL1  6006
    }
    else
    {
        writeRegister(0x01, 0x4180);//    delay_ms(1);
        writeRegister(0x09, 0x6000);//    delay_ms(1); //DEVICE_CONTROL1  6006
    }
}

void afe4k3_isw_mux_test(uint8_t ch)
{
    uint16_t bf = 0;
    bf   = ch;
    bf <<= 8;
    bf |=  ch;
    writeRegister(0x0A, bf);
    nrf_delay_ms(100);
}

void afe4k3_vsense_mux_test(uint8_t ch)
{
    uint16_t bf = 0;
    bf   = ch;
    bf <<= 8;
    bf  |= ch;
    writeRegister(0x0B, bf);
    nrf_delay_ms(100);
}

void afe4k3_isw_mux(void)
{
    uint16_t bf = 0x0408;
    writeRegister(0x0A, bf);
    nrf_delay_ms(100);
}

void afe4k3_vsense_mux(void)
{
    uint16_t bf = 0x0408;
    writeRegister(0x0B, bf);
    nrf_delay_ms(100);
}

uint16_t bf;
float cal100ohm;
float cal1kohm;

uint16_t afe4k3_adcread(void)
{
 		NRF_LOG_INFO("afe4k3_adcread");
    uint16_t val = 0;

    writeRegister(0x01, 0xc140);
    nrf_delay_ms(5);
 		NRF_LOG_INFO("afe4k3_adcread 1 ");
    val = readRegister(0x00);
 		NRF_LOG_INFO("afe4k3_adcread val = %d", val);

    return val;
}

void afe4k3_getcrt(void)
{
 		NRF_LOG_INFO("afe4k3_getcrt start");

    afe4k3_isw_mux_test(1); //100??
 		NRF_LOG_INFO("afe4k3_getcrt start 1");
    afe4k3_vsense_mux_test(1);
 		NRF_LOG_INFO("afe4k3_getcrt start 2");
    nrf_delay_ms(250);
 		NRF_LOG_INFO("afe4k3_getcrt start 3");
    bf = afe4k3_adcread();
 		NRF_LOG_INFO("afe4k3_getcrt start 4");
    cal100ohm = (float)bf; //ad:1315
 		NRF_LOG_INFO("afe4k3_getcrt start 5");
  	nrf_delay_ms(100);

 		NRF_LOG_INFO("afe4k3_getcrt cal100ohm = %d", cal100ohm);
 		NRF_LOG_INFO("afe4k3_getcrt cal100ohm = %d.%02d", cal100ohm);

  	afe4k3_isw_mux_test(2); //1K??
    afe4k3_vsense_mux_test(2);
    nrf_delay_ms(250);
    bf = afe4k3_adcread();
    cal1kohm = (float)bf; //ad:13257
    nrf_delay_ms(1);

 		NRF_LOG_INFO("afe4k3_getcrt cal1kohm = %d", cal1kohm);
 		NRF_LOG_INFO("afe4k3_getcrt cal1kohm = %d.%02d", cal1kohm);
	
    afe4k3_isw_mux();
    afe4k3_vsense_mux();
    nrf_delay_ms(250);
}


uint16_t afe4k3_getres(void)
{
			NRF_LOG_INFO("====================    afe4k3_getres");
    uint16_t val=0;
    float fBf1,fBf2;
    nrf_delay_ms(1);
    writeRegister(0x01, 0xc140);
    nrf_delay_ms(5); //250
    val = readRegister(0x00);
    fBf1 = (float)val;
			NRF_LOG_INFO("fBf1 = %d", fBf1);
    fBf2 = (((fBf1 - cal100ohm) / (cal1kohm - cal100ohm)) * 900.0) + 100.0;
    val = (uint16_t)fBf2;



			NRF_LOG_INFO("afe4k3_getres = %d", val);
//			NRF_LOG_FLUSH();

	return val;
}

uint16_t prevVal = 0;
uint16_t calodidata[5];

uint16_t get_calodi(void)
{
				NRF_LOG_INFO("get_calodi start ==========");
			NRF_LOG_FLUSH();
    uint8_t i;
	
    uint16_t max = 0, min = 65535, val;
    uint32_t sum = 0;

//    delay_ms(150);
    for(i = 0 ; i < 5 ; i++)
    {
        calodidata[i] = afe4k3_getres();
				NRF_LOG_INFO("get_calodi calodidata[%d] : %d", i, calodidata[i]);
			NRF_LOG_FLUSH();

				sum += calodidata[i];
        if(calodidata[i] > max)
            max=calodidata[i];
        if(calodidata[i] < min)
            min=calodidata[i];
        nrf_delay_ms(5);
    }

    sum = 0;
    for(i = 0 ; i < 5 ; i++)
    {
        sum += calodidata[i];
    }
    sum -= (min + max);
    sum /= 3;
    prevVal = val = (uint16_t)sum;
    return val;
}