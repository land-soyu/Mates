#define BODY_HIGH 0
#define BODY_STAND 1
#define BODY_LOW 2


int getHeartRateLevel(int hr, int new_age);
int getSpo2Level(int spo2);
int getBMILevel(int b);
int getKcalLevel(int k, int new_age, int gender);
int getFatLevel(int p, int new_age, int gender);
