#ifndef _SYSTEM_H_ 
#define _SYSTEM_H_

#ifdef _SYSTEM_C_
#define _SYSTEM_E_	
#else
#define _SYSTEM_E_ extern
#endif




/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////				System Tasking 																																																										////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
_SYSTEM_E_ void Sys_Task_Init(void);
_SYSTEM_E_ void Sys_Task(void);

#endif //_SYSTEM_E_

