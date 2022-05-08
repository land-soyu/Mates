#ifndef _TIMER_H_
#define _TIMER_H_

#ifdef _TIMER_C_
#define _TIMER_E_
#else
#define _TIMER_E_ extern
#endif

#define NUM_TIMERS 			(16)

typedef struct Software_Content_t
{
	uint8_t en;
	uint32_t tick;
	uint32_t reset_tick;
	void  (*expire)(void);
}Software_Content_s;

typedef struct Software_Timer_t
{
	uint32_t (*tick)(void);		
	uint32_t prev_tick;
	uint32_t usoftick;				// 1tick이 몇 us인지
	Software_Content_s pro[NUM_TIMERS];
}SW_Timer_s;

	
_TIMER_E_ void stm_init(SW_Timer_s* pT, uint32_t (*tick)(void), uint32_t usOfTick);
_TIMER_E_ void stm_clean(SW_Timer_s* pT);
_TIMER_E_ uint8_t stm_add(SW_Timer_s* pT, uint32_t duration, uint8_t restart, void  (*expire)(void));
_TIMER_E_ uint8_t stm_add_id(SW_Timer_s* pT, uint8_t id, uint32_t duration, uint8_t restart, void  (*expire)(void));
_TIMER_E_ uint8_t stm_del_id(SW_Timer_s* pT, uint8_t id);
_TIMER_E_ uint8_t stm_get_status(SW_Timer_s* pT, uint8_t id);
_TIMER_E_ uint32_t stm_get_tick(SW_Timer_s* pT, uint8_t id);
_TIMER_E_ uint8_t stm_add_us(SW_Timer_s* pT, uint32_t duration, uint8_t restart, void  (*expire)(void));
_TIMER_E_ uint8_t stm_add_ms(SW_Timer_s* pT, uint32_t duration, uint8_t restart, void  (*expire)(void));
_TIMER_E_ uint8_t stm_add_sec(SW_Timer_s* pT, uint16_t duration, uint8_t restart, void  (*expire)(void));
_TIMER_E_ uint8_t stm_add_min(SW_Timer_s* pT, uint8_t duration, uint8_t restart, void  (*expire)(void));
_TIMER_E_ uint8_t stm_add_id_us(SW_Timer_s* pT,uint8_t id, uint32_t duration, uint8_t restart, void  (*expire)(void));
_TIMER_E_ uint8_t stm_add_id_ms(SW_Timer_s* pT,uint8_t id, uint32_t duration, uint8_t restart, void  (*expire)(void));
_TIMER_E_ uint8_t stm_add_id_sec(SW_Timer_s* pT,uint8_t id, uint16_t duration, uint8_t restart, void  (*expire)(void));
_TIMER_E_ uint8_t stm_add_id_min(SW_Timer_s* pT,uint8_t id, uint8_t duration, uint8_t restart, void  (*expire)(void));
_TIMER_E_ uint8_t stm_start_checktime(SW_Timer_s *pT,uint8_t id);
_TIMER_E_ uint32_t stm_stop_checktime(SW_Timer_s *pT,uint8_t id);
_TIMER_E_ uint32_t stm_stop_checktime_us(SW_Timer_s *pT,uint8_t id);
_TIMER_E_ uint32_t stm_stop_checktime_ms(SW_Timer_s *pT,uint8_t id);
_TIMER_E_ uint32_t stm_stop_checktime_sec(SW_Timer_s *pT,uint8_t id);
_TIMER_E_ uint32_t stm_stop_checktime_min(SW_Timer_s *pT,uint8_t id);
_TIMER_E_ void stm_run(SW_Timer_s* pT);

#endif 	//_TIMER_H_

