
#define _API_C_

#include "main.h"
#include "system.h"
#include "api.h"
#include "stm32l1xx.h"



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				ADC 																																																														///////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
extern ADC_HandleTypeDef hadc;
void ADC_Init(void)
{
	int cnt = 0;
	HAL_ADC_Start_DMA(&hadc,(uint32_t *)ADC_Value,ADC_NUM);
	ADC_Avrage_Cnt = 0;
	for(cnt = 0;cnt<ADC_NUM;cnt++)
	{
		ADC_Avrage[cnt] = 0;
		ADC_Result[cnt] = 0;
	}
}

void ADC_Task(void)
{
	uint8_t cnt = 0;
	ADC_Avrage_Cnt++;
	for(cnt = 0;cnt<ADC_NUM;cnt++)
	{
		ADC_Avrage[cnt] += ADC_Value[cnt];
		if(ADC_Avrage_Cnt>=256)
		{
			ADC_Result[cnt] = (uint16_t)(ADC_Avrage[cnt]/256);
			ADC_Avrage[cnt] = 0;
		}			
	}
	if(ADC_Avrage_Cnt >= 256) ADC_Avrage_Cnt = 0;
}
