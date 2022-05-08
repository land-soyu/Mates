#define _PROTOCOL_IOT_C_


#include "stm32l1xx_hal.h"
#include "protocol_iot.h"
#include "uart1.h"
#include "sx1276.h"

protocol_parse_state_e Uart_State = STX;
protocol_parse_state_e Lora_State = STX;
protocol_iot_packet_s Uart_Packet;
protocol_iot_packet_s Lora_Packet;

#define STX_BYTE 0xFB
void Uart_Parse(void)
{
	uint8_t RxData = 0;
	static uint8_t lenFlag = 0;
	static uint32_t 
	if( !Uart1_GetRx(&RxData) ) return;
	switch(Uart_State)
	{
		case STX:
			if( RxData == STX_BYTE )
			{
				Uart_Packet.stx = RxData;
				Uart_State = LEN;
			}
			break;
		case LEN:
			if( lenFlag == 0 )
			{
				Uart_Packet.len = (RxData<<8);
				lenFlag = 1;
			}
			else
			{
				Uart_Packet.len |= RxData;
				lenFlag = 0;
				Uart_State = COMMAND;
			}
			
			break;
		case COMMAND:
			break;
		case CHECK:
			break;
		default:
			break;
	}
}
void Lora_Parse(void)
{
	switch(Lora_State)
	{
		case STX:
			break;
		case LEN:
			break;
		case COMMAND:
			break;
		case CHECK:
			break;
		default:
			break;
	}
}
