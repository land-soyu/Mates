
/************************************************************************************************
*
*@Brief						: 	소프트웨어 타이머
*@Note 	
*			타이머 인스턴스마다 NUM_TIMERS 갯수 만큼의 소프트웨어 타이머를 가진다.
*			인스턴스의 소프트웨어 타이머는 자동추가, ID지정 추가가 있으며 같은 종류로 사용해야만 한다.
*			(둘다 사용하려면 타이머 인스턴스를 두개 만들어야 한다.)
*			틱에 따라 최대 시간이 결정됨 1us~1ms 틱은 약 1시간~49일
*
*@Version 			: 	1.0.1 Beta
*@MadeBy 			: 	가 귀현
*@MadeDay			: 	2016.04.04
*
*************************************************************************************************/

#define _TIMER_C_

/* USER CODE BEGIN */
#include "stm32l1xx_hal.h"			// type 정의 헤더
/* USER CODE END */

#include "timer.h"
#include "main.h"

/************************************************************************************************
*
*@Brief	타이머 초기화
*@Note 	
*@Param pT 						: 	타이머 인스턴스
*@Param tick 					: 	틱에 대한 콜백함수
*@Param usOfTick 	: 	틱당 마이크로초 
*@Retval 
*
*************************************************************************************************/
void stm_init(SW_Timer_s* pT, uint32_t (*tick)(void), uint32_t usOfTick)
{
	uint8_t i;
	pT->tick = tick;
	pT->prev_tick = pT->tick();
	pT->usoftick = usOfTick;
	for(i=0;i<NUM_TIMERS;i++)
	{
		pT->pro[i].en = 0;
		pT->pro[i].tick = 0xFFFFFFFF;
		pT->pro[i].reset_tick = 0;
		pT->pro[i].expire = 0;
	}
}


/************************************************************************************************
*
*@Brief	타이머 청소
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Retval 									
*
*************************************************************************************************/
void stm_clean(SW_Timer_s* pT)
{
	uint8_t i;
	pT->prev_tick = pT->tick();
	for(i = 0;i<NUM_TIMERS;i++)
	{
		pT->pro[i].en = 0; 
		pT->pro[i].tick = 0;
		pT->pro[i].reset_tick = 0;
		pT->pro[i].expire = 0;
	}
}


/************************************************************************************************
*
*@Brief	타이머를 자동으로 추가
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param duration 	: 	타이머 시간(틱 기준)
*@Param restart			: 	타이머 재시작 여부(0 : 사용안함, 1 : 사용함)
*@Param expire			: 	타이머가 완료될 경우 실행할 콜백함수
*@Retval 											추가된 소프트웨어 타이머 ID(0 : 추가 실패, 1~NUM_TIMERS : 추가된 ID
*
*************************************************************************************************/
uint8_t stm_add(SW_Timer_s* pT, uint32_t duration, uint8_t restart, void  (*expire)(void))
{
	uint8_t i;
	for(i=0;i<NUM_TIMERS;i++)
	{
		if(pT->pro[i].en == 0)
		{
			pT->pro[i].en  = 1;
			pT->pro[i].tick = duration;
			if(restart != 0)
			{
					if( pT->pro[i].tick == 0)  pT->pro[i].tick = 1;
					pT->pro[i].reset_tick = pT->pro[i].tick;
			}
			else pT->pro[i].reset_tick = 0;
			pT->pro[i].expire = expire;
			return (i+1);
		}
	}
	return 0;
}


/************************************************************************************************
*
*@Brief	타이머 ID를 지정하여 추가
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Param duration 	: 	타이머 시간(틱 기준)
*@Param restart			: 	타이머 재시작 여부(0 : 사용안함, 1 : 사용함)
*@Param expire			: 	타이머가 완료될 경우 실행할 콜백함수
*@Retval 											추가된 소프트웨어 타이머 ID(0 : 추가 실패, 1~NUM_TIMERS : 추가된 ID
*
*************************************************************************************************/
uint8_t stm_add_id(SW_Timer_s* pT, uint8_t id, uint32_t duration, uint8_t restart, void  (*expire)(void))
{
	uint8_t i;
	i = id-1;
	if(i >= NUM_TIMERS) return 0;
	if(pT->pro[i].en == 0)
	{
		pT->pro[i].en  = 1;
		pT->pro[i].tick = duration;
		if(restart != 0)
		{
				if( pT->pro[i].tick == 0)  pT->pro[i].tick = 1;
				pT->pro[i].reset_tick = pT->pro[i].tick;
		}
		else pT->pro[i].reset_tick = 0;
		pT->pro[i].expire = expire;
		return (i+1);
	}
	return 0;
}


/************************************************************************************************
*
*@Brief	타이머 ID를 지정하여 삭제
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Retval 											0 : 삭제 실패, 1 : 삭제 성공
*
*************************************************************************************************/
uint8_t stm_del_id(SW_Timer_s* pT, uint8_t id)
{
	uint8_t i;
	i = id-1;
	if(i >= NUM_TIMERS) return 0;
	pT->pro[i].en = 0; 
	pT->pro[i].tick = 0;
	pT->pro[i].expire = 0;
	return 1;
}



/************************************************************************************************
*
*@Brief	타이머 ID의 상태 읽기
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Retval 											0 : ID 에러, 1 : 종료, 2 : 동작중, 3 : 미사용
*
*************************************************************************************************/
uint8_t stm_get_status(SW_Timer_s* pT, uint8_t id)
{
	uint8_t i;
	i = id-1;
	if(i >= NUM_TIMERS) return 0;				// Over Num of Timer
	if(pT->pro[i].en == 1)
	{
		if(pT->pro[i].tick == 0) return 1;	// Time end
		else return 2; 														// Time process
	}	
	 else
	 {
		 return 3;																		// not use Timer
	 }
}


/************************************************************************************************
*
*@Brief	타이머 ID의 Tick 읽기
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Retval 											0 : ID 에러, 1~n : tick
*
*************************************************************************************************/
uint32_t stm_get_tick(SW_Timer_s* pT, uint8_t id)
{
	uint8_t i;
	i = id-1;
	if(i >= NUM_TIMERS) return 0;				// Over Num of Timer
	return pT->pro[i].tick;
}


/************************************************************************************************
*
*@Brief	마이크로 타이머 자동 추가
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param duration 	: 	타이머 시간(마이크로 기준)
*@Param restart			: 	타이머 재시작 여부(0 : 사용안함, 1 : 사용함)
*@Param expire			: 	타이머가 완료될 경우 실행할 콜백함수
*@Retval 											추가된 소프트웨어 타이머 ID(0 : 추가 실패, 1~NUM_TIMERS : 추가된 ID
*
*************************************************************************************************/
uint8_t stm_add_us(SW_Timer_s* pT, uint32_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add(pT, (duration/pT->usoftick),restart,expire);
}


/************************************************************************************************
*
*@Brief	밀리 타이머 자동 추가
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param duration 	: 	타이머 시간(밀리 기준)
*@Param restart			: 	타이머 재시작 여부(0 : 사용안함, 1 : 사용함)
*@Param expire			: 	타이머가 완료될 경우 실행할 콜백함수
*@Retval 											추가된 소프트웨어 타이머 ID(0 : 추가 실패, 1~NUM_TIMERS : 추가된 ID
*
*************************************************************************************************/
uint8_t stm_add_ms(SW_Timer_s* pT, uint32_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_us(pT, (duration*1000),restart,expire);
}


/************************************************************************************************
*
*@Brief	초 타이머 자동 추가
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param duration 	: 	타이머 시간(초 기준)
*@Param restart			: 	타이머 재시작 여부(0 : 사용안함, 1 : 사용함)
*@Param expire			: 	타이머가 완료될 경우 실행할 콜백함수
*@Retval 											추가된 소프트웨어 타이머 ID(0 : 추가 실패, 1~NUM_TIMERS : 추가된 ID
*
*************************************************************************************************/
uint8_t stm_add_sec(SW_Timer_s* pT, uint16_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_ms(pT, (duration*1000),restart,expire);
}


/************************************************************************************************
*
*@Brief	분 타이머 자동 추가
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param duration 	: 	타이머 시간(분 기준)
*@Param restart			: 	타이머 재시작 여부(0 : 사용안함, 1 : 사용함)
*@Param expire			: 	타이머가 완료될 경우 실행할 콜백함수
*@Retval 											추가된 소프트웨어 타이머 ID(0 : 추가 실패, 1~NUM_TIMERS : 추가된 ID
*
*************************************************************************************************/
uint8_t stm_add_min(SW_Timer_s* pT, uint8_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_sec(pT, (duration*60),restart,expire);
}


/************************************************************************************************
*
*@Brief	마이크로 타이머 ID지정 추가
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Param duration 	: 	타이머 시간(마이크로 기준)
*@Param restart			: 	타이머 재시작 여부(0 : 사용안함, 1 : 사용함)
*@Param expire			: 	타이머가 완료될 경우 실행할 콜백함수
*@Retval 											추가된 소프트웨어 타이머 ID(0 : 추가 실패, 1~NUM_TIMERS : 추가된 ID
*
*************************************************************************************************/
uint8_t stm_add_id_us(SW_Timer_s* pT,uint8_t id, uint32_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_id(pT,id, (duration/pT->usoftick),restart,expire);
}


/************************************************************************************************
*
*@Brief	밀리 타이머 ID지정 추가
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Param duration 	: 	타이머 시간(밀리 기준)
*@Param restart			: 	타이머 재시작 여부(0 : 사용안함, 1 : 사용함)
*@Param expire			: 	타이머가 완료될 경우 실행할 콜백함수
*@Retval 											추가된 소프트웨어 타이머 ID(0 : 추가 실패, 1~NUM_TIMERS : 추가된 ID
*
*************************************************************************************************/
uint8_t stm_add_id_ms(SW_Timer_s* pT,uint8_t id, uint32_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_id_us(pT,id, (duration*1000),restart,expire);
}


/************************************************************************************************
*
*@Brief	초 타이머 ID지정 추가
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Param duration 	: 	타이머 시간(초 기준)
*@Param restart			: 	타이머 재시작 여부(0 : 사용안함, 1 : 사용함)
*@Param expire			: 	타이머가 완료될 경우 실행할 콜백함수
*@Retval 											추가된 소프트웨어 타이머 ID(0 : 추가 실패, 1~NUM_TIMERS : 추가된 ID
*
*************************************************************************************************/
uint8_t stm_add_id_sec(SW_Timer_s* pT,uint8_t id, uint16_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_id_ms(pT,id, (duration*1000),restart,expire);
}


/************************************************************************************************
*
*@Brief	분 타이머 ID지정 추가
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Param duration 	: 	타이머 시간(분 기준)
*@Param restart			: 	타이머 재시작 여부(0 : 사용안함, 1 : 사용함)
*@Param expire			: 	타이머가 완료될 경우 실행할 콜백함수
*@Retval 											추가된 소프트웨어 타이머 ID(0 : 추가 실패, 1~NUM_TIMERS : 추가된 ID
*
*************************************************************************************************/
uint8_t stm_add_id_min(SW_Timer_s* pT,uint8_t id, uint8_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_id_sec(pT,id, (duration*60),restart,expire);
}


/************************************************************************************************
*
*@Brief	타임 체크 시작
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Retval 											추가된 소프트웨어 타이머 ID(0 : 추가 실패, 1~NUM_TIMERS : 추가된 ID
*
*************************************************************************************************/
uint8_t stm_start_checktime(SW_Timer_s *pT, uint8_t id)
{	
	uint8_t ret = 1;
	stm_stop_checktime(pT,id);
	ret = stm_add_id(pT, id, 0xFFFFFFFF,0,0);
	return ret;
}


/************************************************************************************************
*
*@Brief	타임 체크 종료
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Retval 											체크된 틱
*
*************************************************************************************************/
uint32_t stm_stop_checktime(SW_Timer_s *pT,uint8_t id)
{
	uint32_t ret;
	uint32_t timer_tick;
	stm_run(pT);
	timer_tick = (0xFFFFFFFF) - (stm_get_tick(pT,id));
	stm_del_id(pT,id);

	ret = timer_tick;
	return ret;
}


/************************************************************************************************
*
*@Brief	타임 체크 종료(us)
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Retval 											체크된 마이크로초
*
*************************************************************************************************/
uint32_t stm_stop_checktime_us(SW_Timer_s *pT,uint8_t id)
{
	return (stm_stop_checktime(pT,id)*pT->usoftick);
}


/************************************************************************************************
*
*@Brief	타임 체크 종료(ms)
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Retval 											체크된 밀리초
*
*************************************************************************************************/
uint32_t stm_stop_checktime_ms(SW_Timer_s *pT,uint8_t id)
{
	return (stm_stop_checktime_us(pT,id)/1000);
}


/************************************************************************************************
*
*@Brief	타임 체크 종료(sec)
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Retval 											체크된 초
*
*************************************************************************************************/
uint32_t stm_stop_checktime_sec(SW_Timer_s *pT,uint8_t id)
{
	return (stm_stop_checktime_ms(pT,id)/1000);
}


/************************************************************************************************
*
*@Brief	타임 체크 종료(min)
*@Note				
*@Param pT 						: 	타이머 인스턴스
*@Param id						 	: 	인스턴스 내의 추가할 소프트웨어 타이머 ID
*@Retval 											체크된 분
*
*************************************************************************************************/
uint32_t stm_stop_checktime_min(SW_Timer_s *pT,uint8_t id)
{
	return (stm_stop_checktime_sec(pT,id)/60);
}

/************************************************************************************************
*
*@Brief	타이머 동작
*@Note			
*			tick을 기준으로 타이머 인스턴스의 소프트웨어 타이머를 확인
*
*@Param pT 						: 	타이머 인스턴스
*@Retval 											
*
*************************************************************************************************/
void stm_run(SW_Timer_s* pT)
{
	uint32_t i,current_count,past_count;
	current_count = pT->tick();

	if(current_count == pT->prev_tick)		return ;

	if(current_count > pT->prev_tick)		past_count = current_count - pT->prev_tick;
	else									past_count = 0xFFFFFFFF - pT->prev_tick + current_count ;
	pT->prev_tick = current_count;
	for(i=0;i<NUM_TIMERS;i++)
	{
		if(pT->pro[i].en != 0)
		{
			if(past_count < (pT->pro[i].tick))
			{
				pT->pro[i].tick -= past_count;
			}
			else
			{
			
				if(pT->pro[i].expire != 0)
				{
					(*(pT->pro[i].expire))();
				}

				if(pT->pro[i].reset_tick != 0)
				{
					pT->pro[i].tick = pT->pro[i].reset_tick;
				}
				else// restart == 0일 경우 처리 
				{
					pT->pro[i].en = 0;
					pT->pro[i].tick = 0;
					pT->pro[i].expire = 0;
				}
				
			}
		}
	}

}
