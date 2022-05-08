#include "nrf.h"
#include "boards.h"

#include <time.h>


#define COMPARE_COUNTERTIME  (3UL)                                        /**< Get Compare event COMPARE_TIME seconds after the counter starts from 0. */


int settime(char *s);

time_t gettime(void);
void nowtime(void);
int rtc_init(void);

