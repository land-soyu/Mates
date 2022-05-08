#ifndef _IAQ_CORE_H_ 
#define _IAQ_CORE_H_		
#include <stdint.h>
#ifdef _IAQ_CORE_C_
#define _IAQ_CORE_E_	
#else
#define _IAQ_CORE_E_ extern
#endif



_IAQ_CORE_E_ void IAQ_CORE_Read(uint8_t *data);



#endif //_IAQ_CORE_E_

