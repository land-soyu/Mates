
#ifndef _PROTOCOL_IOT_H_ 
#define _PROTOCOL_IOT_H_

#ifdef _PROTOCOL_IOT_C_
#define _PROTOCOL_IOT_E_	
#else
#define _PROTOCOL_IOT_E_ extern
#endif

#define MAX_CMD_SIZE 255
#define MAX_SIZE 256
typedef struct
{
	uint8_t stx;
	uint16_t len;
	uint8_t command[MAX_CMD_SIZE];
}protocol_iot_packet_s;
typedef enum
{
	PRO_IOT_SERVER 		= 0x0000,
	PRO_IOT_GATEWAY 	= 0x1000,
	PRO_IOT_IOT_AP 		= 0x2000,
	PRO_IOT_SMART_WATCH = 0x3000,// ~0x30FF
	PRO_IOT_SMART_RADIO = 0x4000,// ~0x40FF
	PRO_IOT_HALMET 		= 0x5000,// ~0x50FF
	PRO_IOT_PC 			= 0x6000
	//PRO_IOT_RESERVED = 0x7000
}protocol_iot_source_id_e;
typedef enum
{
	PRO_IOT_SYSTEM_CONFIG = 0x00,
	PRO_IOT_ALARM =			0x10,
	PRO_IOT_MONITOR =		0x20,
	PRO_IOT_SETTING =		0x30,
	PRO_IOT_EVENT =			0x40,
	//RESERVE =		0x50// ~ 0x0F
}protocol_iot_order_e;
typedef enum
{
	PRO_IOT_SUCCESS 		= 0x00,
	PRO_IOT_FAILED 			= 0x01,
	PRO_IOT_ORDER_ERROR 	= 0x02,
	PRO_IOT_LENTH_ERROR 	= 0x03,
	PRO_IOT_CRC_ERROR		= 0x04,
	PRO_IOT_NOT_EXIST_ID 	= 0x05,
	PRO_IOT_NOT_EXIST_VALUE = 0x06,
	//Reserve
	PRO_IOT_UNDEFINED_ERROR = 0x0E,
	PRO_IOT_PACKET_IS_ORDER = 0x0F
}protocol_iot_ack_e;

typedef enum
{
	STX = 0x00,
	LEN,
	COMMAND,
	CHECK
}protocol_parse_state_e;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				안전모																																																										////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
typedef struct msg_00_t 
{
	uint8_t batt;
	uint8_t state;
}msg_00_s;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				안전장비(밴드&무전기)																																																								////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
typedef struct msg_10_t
{
	uint16_t helmet_id
	uint8_t helmet_distance;
	uint8_t helmet_batt;
	int writed_time;
	uint16_t ap_id;
	uint8_t ap_distance;
	uint8_t device_batt;
	uint16_t critical_level;
	uint16_t altitude;
}msg_10_s;
typedef struct msg_11_t
{
	int date;
}msg_11_s;
typedef struct msg_12_t
{
	uint8_t alarm_state;
	int writed_time;
	uint8_t setted_time;
}msg_12_s;
typedef struct msg_13_t
{
	uint16_t char_id;
	int checked_time;
}msg_13_s;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				AP																																																										////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
typedef struct msg_31_ap1_1t
{
	uint8_t batt_level;
	uint8_t sensor_cnt;
}msg_31_ap1_1s;
typedef struct msg_31_ap1_2t
{
	uint8_t batt_level;
	uint8_t sensor_cnt;
}msg_31_ap1_2s;
typedef struct msg_31_ap1_3t
{
	uint8_t batt_level;
	uint8_t sensor_cnt;
	long sensor_id;
	uint8_t sensor_batt;
	uint8_t sensor_st;
}msg_31_ap1_3s;
typedef struct msg_31_ap2t
{
	uint8_t batt_level;
	uint8_t sensor_cnt;
}msg_31_ap2;
typedef struct msg_31_ap3t
{
	uint8_t batt_level;
	uint8_t sensor_cnt;
	uint8_t sensor_type;
	uint16_t sensor_value;
}msg_31_ap3;
typedef struct msg_31_ap4t
{
	uint8_t batt_level;
	uint8_t sensor_cnt;
}msg_31_ap4;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				SERVER																																																										////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
typedef struct msg_60_t
{
	uint8_t type;
	uint8_t sub_type;
	uint8_t level1;
	uint8_t level2;
	uint8_t level3;
	uint8_t warning_level;
	uint8_t critical_level;
	uint8_t sensor_count;
	uint8_t sensor_type;
	uint8_t sensor_value_point;
	uint8_t sensor_safe_range_start_value;
	uint8_t sensor_safe_range_end_value;
	uint8_t sensor_value;
}msg_60_s;
typedef struct msg_41_t
{
	uint8_t alarm_type;
	uint8_t num_of_repetition;
}msg_41_s;
typedef struct msg_42_t
{
	uint8_t alarm_state;
	uint8_t time;
}msg_42_s;
typedef struct msg_43_t
{
	uint16_t char_id;
	uint8_t str_len_of_person;
	uint8_t person_name[MAX_SIZE];
	int time_or_date;
	uint8_t str_len_of_contents;
	uint8_t contents[MAX_SIZE];
}msg_43_s;
typedef struct msg_44_t
{
	uint8_t helmet_id
}msg_44_s;
typedef struct msg_45_t
{
	int educated_date;
}msg_45_s;
typedef struct msg_46_t
{
	uint8_t alarm_check_cycle;
	uint8_t danger_continuous_cnt;
}msg_46_s;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				AP Adv																																																									////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
typedef struct msg_adv_comm_t
{
	uint16_t service_id;
	uint8_t ap_type;
	uint8_t ap_subType;
	uint16 id;
	uint8_t tx_power;
}msg_adv_comm_s;
typedef struct msg_adv_AP1_2_t
{
	uint8_t level1;
	uint8_t level2;
	uint8_t level3;
	uint8_t warning_level;
	uint8_t critical_level;
}msg_adv_AP1_2_s;
typedef struct msg_adv_AP1_3_t
{
	uint8_t is_critical;
	uint8_t critical_distance;
}msg_adv_AP1_3_s;
typedef struct msg_adv_Helmet_t
{
	uint8_t batt;
	uint8_t st;
}msg_adv_Helmet_s;

#endif //_PROTOCOL_IOT_E_
