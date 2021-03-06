// Transmits data from the 5 back sensors 
// to RPi
// Numbering of sensors follow the diagram
#include <NewPing.h>

#define TRIGGER_PIN_4 11
#define ECHO_PIN_4 12
#define TRIGGER_PIN_5 10
#define ECHO_PIN_5 13
#define TRIGGER_PIN_6 9
#define ECHO_PIN_6 8
#define TRIGGER_PIN_7 6 
#define ECHO_PIN_7 7
#define TRIGGER_PIN_8 5 
#define ECHO_PIN_8 4

#define SENSOR_NUM 5
#define MAX_DISTANCE 200

// NewPing setup of pins and maximum distance
NewPing sonar_4(TRIGGER_PIN_4, ECHO_PIN_4, MAX_DISTANCE); 
NewPing sonar_5(TRIGGER_PIN_5, ECHO_PIN_5, MAX_DISTANCE); 
NewPing sonar_6(TRIGGER_PIN_6, ECHO_PIN_6, MAX_DISTANCE); 
NewPing sonar_7(TRIGGER_PIN_7, ECHO_PIN_7, MAX_DISTANCE);
NewPing sonar_8(TRIGGER_PIN_8, ECHO_PIN_8, MAX_DISTANCE);

void setup() {
  Serial.begin(9600);
}

void loop() {
   delay(200);
   unsigned int distance_4 = sonar_4.convert_cm(sonar_4.ping_median());
   unsigned int distance_5 = sonar_5.convert_cm(sonar_5.ping_median());
   unsigned int distance_6 = sonar_6.convert_cm(sonar_6.ping_median());
   unsigned int distance_7 = sonar_7.convert_cm(sonar_7.ping_median());
   unsigned int distance_8 = sonar_8.convert_cm(sonar_8.ping_median());

   char *readings = (char*)malloc((SENSOR_NUM+1) * sizeof(int));
   sprintf(readings, "%u, %u, %u, %u, %u", distance_4, distance_5, distance_6, distance_7, distance_8);
   Serial.println(readings);
}
