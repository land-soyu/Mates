#ifndef _API_H_ 
#define _API_H_			(1)

#ifdef _API_C_
#define _API_EXT_	
#else
#define _API_EXT_ extern
#endif

#include <stdint.h>
#include "timer.h"

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				ADC Get																																																														///////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#define ADC_NUM			(1)
typedef enum{
	_TEMP_,
}adc_index_t;

_API_EXT_ volatile uint16_t ADC_Value[ADC_NUM];				
_API_EXT_ volatile uint16_t ADC_Result[ADC_NUM];	
_API_EXT_ volatile uint16_t ADC_Avrage_Cnt;	
_API_EXT_ volatile uint32_t ADC_Avrage[ADC_NUM];	
_API_EXT_ void ADC_Init(void);
_API_EXT_ void ADC_Task(void);


#endif	//_API_H_


