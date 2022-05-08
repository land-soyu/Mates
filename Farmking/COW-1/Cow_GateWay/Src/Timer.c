
/************************************************************************************************
*
*@Brief						: 	����Ʈ���� Ÿ�̸�
*@Note 	
*			Ÿ�̸� �ν��Ͻ����� NUM_TIMERS ���� ��ŭ�� ����Ʈ���� Ÿ�̸Ӹ� ������.
*			�ν��Ͻ��� ����Ʈ���� Ÿ�̸Ӵ� �ڵ��߰�, ID���� �߰��� ������ ���� ������ ����ؾ߸� �Ѵ�.
*			(�Ѵ� ����Ϸ��� Ÿ�̸� �ν��Ͻ��� �ΰ� ������ �Ѵ�.)
*			ƽ�� ���� �ִ� �ð��� ������ 1us~1ms ƽ�� �� 1�ð�~49��
*
*@Version 			: 	1.0.1 Beta
*@MadeBy 			: 	�� ����
*@MadeDay			: 	2016.04.04
*
*************************************************************************************************/

#define _TIMER_C_

/* USER CODE BEGIN */
#include "stm32l1xx_hal.h"			// type ���� ���
/* USER CODE END */

#include "timer.h"
#include "main.h"

/************************************************************************************************
*
*@Brief	Ÿ�̸� �ʱ�ȭ
*@Note 	
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param tick 					: 	ƽ�� ���� �ݹ��Լ�
*@Param usOfTick 	: 	ƽ�� ����ũ���� 
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
*@Brief	Ÿ�̸� û��
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
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
*@Brief	Ÿ�̸Ӹ� �ڵ����� �߰�
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param duration 	: 	Ÿ�̸� �ð�(ƽ ����)
*@Param restart			: 	Ÿ�̸� ����� ����(0 : ������, 1 : �����)
*@Param expire			: 	Ÿ�̸Ӱ� �Ϸ�� ��� ������ �ݹ��Լ�
*@Retval 											�߰��� ����Ʈ���� Ÿ�̸� ID(0 : �߰� ����, 1~NUM_TIMERS : �߰��� ID
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
*@Brief	Ÿ�̸� ID�� �����Ͽ� �߰�
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Param duration 	: 	Ÿ�̸� �ð�(ƽ ����)
*@Param restart			: 	Ÿ�̸� ����� ����(0 : ������, 1 : �����)
*@Param expire			: 	Ÿ�̸Ӱ� �Ϸ�� ��� ������ �ݹ��Լ�
*@Retval 											�߰��� ����Ʈ���� Ÿ�̸� ID(0 : �߰� ����, 1~NUM_TIMERS : �߰��� ID
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
*@Brief	Ÿ�̸� ID�� �����Ͽ� ����
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Retval 											0 : ���� ����, 1 : ���� ����
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
*@Brief	Ÿ�̸� ID�� ���� �б�
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Retval 											0 : ID ����, 1 : ����, 2 : ������, 3 : �̻��
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
*@Brief	Ÿ�̸� ID�� Tick �б�
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Retval 											0 : ID ����, 1~n : tick
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
*@Brief	����ũ�� Ÿ�̸� �ڵ� �߰�
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param duration 	: 	Ÿ�̸� �ð�(����ũ�� ����)
*@Param restart			: 	Ÿ�̸� ����� ����(0 : ������, 1 : �����)
*@Param expire			: 	Ÿ�̸Ӱ� �Ϸ�� ��� ������ �ݹ��Լ�
*@Retval 											�߰��� ����Ʈ���� Ÿ�̸� ID(0 : �߰� ����, 1~NUM_TIMERS : �߰��� ID
*
*************************************************************************************************/
uint8_t stm_add_us(SW_Timer_s* pT, uint32_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add(pT, (duration/pT->usoftick),restart,expire);
}


/************************************************************************************************
*
*@Brief	�и� Ÿ�̸� �ڵ� �߰�
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param duration 	: 	Ÿ�̸� �ð�(�и� ����)
*@Param restart			: 	Ÿ�̸� ����� ����(0 : ������, 1 : �����)
*@Param expire			: 	Ÿ�̸Ӱ� �Ϸ�� ��� ������ �ݹ��Լ�
*@Retval 											�߰��� ����Ʈ���� Ÿ�̸� ID(0 : �߰� ����, 1~NUM_TIMERS : �߰��� ID
*
*************************************************************************************************/
uint8_t stm_add_ms(SW_Timer_s* pT, uint32_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_us(pT, (duration*1000),restart,expire);
}


/************************************************************************************************
*
*@Brief	�� Ÿ�̸� �ڵ� �߰�
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param duration 	: 	Ÿ�̸� �ð�(�� ����)
*@Param restart			: 	Ÿ�̸� ����� ����(0 : ������, 1 : �����)
*@Param expire			: 	Ÿ�̸Ӱ� �Ϸ�� ��� ������ �ݹ��Լ�
*@Retval 											�߰��� ����Ʈ���� Ÿ�̸� ID(0 : �߰� ����, 1~NUM_TIMERS : �߰��� ID
*
*************************************************************************************************/
uint8_t stm_add_sec(SW_Timer_s* pT, uint16_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_ms(pT, (duration*1000),restart,expire);
}


/************************************************************************************************
*
*@Brief	�� Ÿ�̸� �ڵ� �߰�
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param duration 	: 	Ÿ�̸� �ð�(�� ����)
*@Param restart			: 	Ÿ�̸� ����� ����(0 : ������, 1 : �����)
*@Param expire			: 	Ÿ�̸Ӱ� �Ϸ�� ��� ������ �ݹ��Լ�
*@Retval 											�߰��� ����Ʈ���� Ÿ�̸� ID(0 : �߰� ����, 1~NUM_TIMERS : �߰��� ID
*
*************************************************************************************************/
uint8_t stm_add_min(SW_Timer_s* pT, uint8_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_sec(pT, (duration*60),restart,expire);
}


/************************************************************************************************
*
*@Brief	����ũ�� Ÿ�̸� ID���� �߰�
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Param duration 	: 	Ÿ�̸� �ð�(����ũ�� ����)
*@Param restart			: 	Ÿ�̸� ����� ����(0 : ������, 1 : �����)
*@Param expire			: 	Ÿ�̸Ӱ� �Ϸ�� ��� ������ �ݹ��Լ�
*@Retval 											�߰��� ����Ʈ���� Ÿ�̸� ID(0 : �߰� ����, 1~NUM_TIMERS : �߰��� ID
*
*************************************************************************************************/
uint8_t stm_add_id_us(SW_Timer_s* pT,uint8_t id, uint32_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_id(pT,id, (duration/pT->usoftick),restart,expire);
}


/************************************************************************************************
*
*@Brief	�и� Ÿ�̸� ID���� �߰�
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Param duration 	: 	Ÿ�̸� �ð�(�и� ����)
*@Param restart			: 	Ÿ�̸� ����� ����(0 : ������, 1 : �����)
*@Param expire			: 	Ÿ�̸Ӱ� �Ϸ�� ��� ������ �ݹ��Լ�
*@Retval 											�߰��� ����Ʈ���� Ÿ�̸� ID(0 : �߰� ����, 1~NUM_TIMERS : �߰��� ID
*
*************************************************************************************************/
uint8_t stm_add_id_ms(SW_Timer_s* pT,uint8_t id, uint32_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_id_us(pT,id, (duration*1000),restart,expire);
}


/************************************************************************************************
*
*@Brief	�� Ÿ�̸� ID���� �߰�
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Param duration 	: 	Ÿ�̸� �ð�(�� ����)
*@Param restart			: 	Ÿ�̸� ����� ����(0 : ������, 1 : �����)
*@Param expire			: 	Ÿ�̸Ӱ� �Ϸ�� ��� ������ �ݹ��Լ�
*@Retval 											�߰��� ����Ʈ���� Ÿ�̸� ID(0 : �߰� ����, 1~NUM_TIMERS : �߰��� ID
*
*************************************************************************************************/
uint8_t stm_add_id_sec(SW_Timer_s* pT,uint8_t id, uint16_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_id_ms(pT,id, (duration*1000),restart,expire);
}


/************************************************************************************************
*
*@Brief	�� Ÿ�̸� ID���� �߰�
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Param duration 	: 	Ÿ�̸� �ð�(�� ����)
*@Param restart			: 	Ÿ�̸� ����� ����(0 : ������, 1 : �����)
*@Param expire			: 	Ÿ�̸Ӱ� �Ϸ�� ��� ������ �ݹ��Լ�
*@Retval 											�߰��� ����Ʈ���� Ÿ�̸� ID(0 : �߰� ����, 1~NUM_TIMERS : �߰��� ID
*
*************************************************************************************************/
uint8_t stm_add_id_min(SW_Timer_s* pT,uint8_t id, uint8_t duration, uint8_t restart, void  (*expire)(void))
{
	return stm_add_id_sec(pT,id, (duration*60),restart,expire);
}


/************************************************************************************************
*
*@Brief	Ÿ�� üũ ����
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Retval 											�߰��� ����Ʈ���� Ÿ�̸� ID(0 : �߰� ����, 1~NUM_TIMERS : �߰��� ID
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
*@Brief	Ÿ�� üũ ����
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Retval 											üũ�� ƽ
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
*@Brief	Ÿ�� üũ ����(us)
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Retval 											üũ�� ����ũ����
*
*************************************************************************************************/
uint32_t stm_stop_checktime_us(SW_Timer_s *pT,uint8_t id)
{
	return (stm_stop_checktime(pT,id)*pT->usoftick);
}


/************************************************************************************************
*
*@Brief	Ÿ�� üũ ����(ms)
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Retval 											üũ�� �и���
*
*************************************************************************************************/
uint32_t stm_stop_checktime_ms(SW_Timer_s *pT,uint8_t id)
{
	return (stm_stop_checktime_us(pT,id)/1000);
}


/************************************************************************************************
*
*@Brief	Ÿ�� üũ ����(sec)
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Retval 											üũ�� ��
*
*************************************************************************************************/
uint32_t stm_stop_checktime_sec(SW_Timer_s *pT,uint8_t id)
{
	return (stm_stop_checktime_ms(pT,id)/1000);
}


/************************************************************************************************
*
*@Brief	Ÿ�� üũ ����(min)
*@Note				
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
*@Param id						 	: 	�ν��Ͻ� ���� �߰��� ����Ʈ���� Ÿ�̸� ID
*@Retval 											üũ�� ��
*
*************************************************************************************************/
uint32_t stm_stop_checktime_min(SW_Timer_s *pT,uint8_t id)
{
	return (stm_stop_checktime_sec(pT,id)/60);
}

/************************************************************************************************
*
*@Brief	Ÿ�̸� ����
*@Note			
*			tick�� �������� Ÿ�̸� �ν��Ͻ��� ����Ʈ���� Ÿ�̸Ӹ� Ȯ��
*
*@Param pT 						: 	Ÿ�̸� �ν��Ͻ�
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
				else// restart == 0�� ��� ó�� 
				{
					pT->pro[i].en = 0;
					pT->pro[i].tick = 0;
					pT->pro[i].expire = 0;
				}
				
			}
		}
	}

}
