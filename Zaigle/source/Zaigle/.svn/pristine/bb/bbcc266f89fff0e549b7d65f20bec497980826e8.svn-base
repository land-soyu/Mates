#include <stdint.h>

#include "calculator.h"


int getHeartRateLevel(int hr, int new_age)
{
        int heart = 0;
        if (new_age <= 1) {
            if (hr > 180) {
                heart = BODY_HIGH;
            } else if (hr < 101) {
                heart = BODY_LOW;
            } else {
                heart = BODY_STAND;
            }
        } else if (new_age < 4) {
            if (hr > 150) {
                heart = BODY_HIGH;
            } else if (hr < 90) {
                heart = BODY_LOW;
            } else {
                heart = BODY_STAND;
            }
        } else if (new_age < 18) {
            if (hr > 110) {
                heart = BODY_HIGH;
            } else if (hr < 60) {
                heart = BODY_LOW;
            } else {
                heart = BODY_STAND;
            }
        } else if (new_age < 60) {
            if (hr > 80) {
                heart = BODY_HIGH;
            } else if (hr < 65) {
                heart = BODY_LOW;
            } else {
                heart = BODY_STAND;
            }
        } else {
            if (hr > 85) {
                heart = BODY_HIGH;
            } else if (hr < 62) {
                heart = BODY_LOW;
            } else {
                heart = BODY_STAND;
            }
        }
				return heart;
}


int getSpo2Level(int spo2)
{
        int oxygen = 0;
        if (spo2 == 100) {
            oxygen = BODY_HIGH;
        } else if (spo2 < 85) {
            oxygen = BODY_LOW;
        } else {
            oxygen = BODY_STAND;
        }	
				return oxygen;
}


int getBMILevel(int b)
{
        int bmi = 0;
        if (b < 18) {
            bmi = BODY_LOW;
        } else if (b > 23) {
            bmi = BODY_HIGH;
        } else {
            bmi = BODY_STAND;
        }
				return bmi;
}


int getKcalLevel(int k, int new_age, int gender)
{
        int kcal = 0;
        if (gender == 0) {    //  man
            if (new_age < 8) {
                if (k > 1200) {
                    kcal = BODY_HIGH;
                } else if (k < 1000) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            } else if (new_age < 14) {
                if (k > 1400) {
                    kcal = BODY_HIGH;
                } else if (k < 1200) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            } else if (new_age < 18) {
                if (k > 1710) {
                    kcal = BODY_HIGH;
                } else if (k < 1500) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            } else if (new_age < 30) {
                if (k > 2000) {
                    kcal = BODY_HIGH;
                } else if (k < 1400) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            } else if (new_age < 50) {
                if (k > 1800) {
                    kcal = BODY_HIGH;
                } else if (k < 1350) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            } else if (new_age < 60) {
                if (k > 1700) {
                    kcal = BODY_HIGH;
                } else if (k < 1200) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            } else {
                if (k > 1300) {
                    kcal = BODY_HIGH;
                } else if (k < 1100) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            }
        } else {    //  woman
            if (new_age < 8) {
                if (k > 1100) {
                    kcal = BODY_HIGH;
                } else if (k < 900) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            } else if (new_age < 14) {
                if (k > 1280) {
                    kcal = BODY_HIGH;
                } else if (k < 1080) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            } else if (new_age < 18) {
                if (k > 1400) {
                    kcal = BODY_HIGH;
                } else if (k < 1200) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            } else if (new_age < 30) {
                if (k > 1500) {
                    kcal = BODY_HIGH;
                } else if (k < 1100) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            } else if (new_age < 50) {
                if (k > 1450) {
                    kcal = BODY_HIGH;
                } else if (k < 1050) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            } else if (new_age < 60) {
                if (k > 1350) {
                    kcal = BODY_HIGH;
                } else if (k < 1000) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            } else {
                if (k > 1100) {
                    kcal = BODY_HIGH;
                } else if (k < 900) {
                    kcal = BODY_LOW;
                } else {
                    kcal = BODY_STAND;
                }
            }
        }	
				return kcal;
}


int getFatLevel(int p, int new_age, int gender)
{
        int percent = 0;
        if (gender == 0) {    //  man
            if (new_age < 18) {
                if (p > 20) {
                    percent = BODY_HIGH;
                } else if (p < 8) {
                    percent = BODY_LOW;
                } else {
                    percent = BODY_STAND;
                }
            } else if (new_age < 40) {
                if (p > 22) {
                    percent = BODY_HIGH;
                } else if (p < 11) {
                    percent = BODY_LOW;
                } else {
                    percent = BODY_STAND;
                }
            } else if (new_age < 60) {
                if (p > 25) {
                    percent = BODY_HIGH;
                } else if (p < 13) {
                    percent = BODY_LOW;
                } else {
                    percent = BODY_STAND;
                }
            }
        } else {    //  woman
            if (new_age < 18) {
                if (p > 33) {
                    percent = BODY_HIGH;
                } else if (p < 20) {
                    percent = BODY_LOW;
                } else {
                    percent = BODY_STAND;
                }
            } else if (new_age < 40) {
                if (p > 34) {
                    percent = BODY_HIGH;
                } else if (p < 22) {
                    percent = BODY_LOW;
                } else {
                    percent = BODY_STAND;
                }
            } else if (new_age < 60) {
                if (p > 36) {
                    percent = BODY_HIGH;
                } else if (p < 23) {
                    percent = BODY_LOW;
                } else {
                    percent = BODY_STAND;
                }
            }
        }
				return percent;
}

