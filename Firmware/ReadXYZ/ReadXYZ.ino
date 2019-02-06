#include <Wire.h>
#include <ADXL345.h>

ADXL345 adxl; //variable adxl is an instance of the ADXL345 library
int colibxyz[3];
int countalarm;
#define pot A7
int sensytive;

void setup(){
  Serial.begin(9600);
  Serial.print("accelstart");  //testcode
  adxl.powerOn();
  pinMode(pot, INPUT);
  

  //set activity/ inactivity thresholds (0-255)
  adxl.setActivityThreshold(75); //62.5mg per increment
  adxl.setInactivityThreshold(75); //62.5mg per increment
  adxl.setTimeInactivity(10); // how many seconds of no activity is inactive?
 
  //look of activity movement on this axes - 1 == on; 0 == off 
  adxl.setActivityX(1);
  adxl.setActivityY(1);
  adxl.setActivityZ(1);
 
  //look of inactivity movement on this axes - 1 == on; 0 == off
  adxl.setInactivityX(1);
  adxl.setInactivityY(1);
  adxl.setInactivityZ(1);
 
  //look of tap movement on this axes - 1 == on; 0 == off
  adxl.setTapDetectionOnX(1);
  adxl.setTapDetectionOnY(1);
  adxl.setTapDetectionOnZ(1);
 
  //set values for what is a tap, and what is a double tap (0-255)
  adxl.setTapThreshold(50); //62.5mg per increment
  adxl.setTapDuration(15); //625us per increment
  adxl.setDoubleTapLatency(80); //1.25ms per increment
  adxl.setDoubleTapWindow(200); //1.25ms per increment
 
  //set values for what is considered freefall (0-255)
  adxl.setFreeFallThreshold(7); //(5 - 9) recommended - 62.5mg per increment
  adxl.setFreeFallDuration(45); //(20 - 70) recommended - 5ms per increment
 
  //setting all interrupts to take place on int pin 1
  //I had issues with int pin 2, was unable to reset it
  adxl.setInterruptMapping( ADXL345_INT_SINGLE_TAP_BIT,   ADXL345_INT1_PIN );
  adxl.setInterruptMapping( ADXL345_INT_DOUBLE_TAP_BIT,   ADXL345_INT1_PIN );
  adxl.setInterruptMapping( ADXL345_INT_FREE_FALL_BIT,    ADXL345_INT1_PIN );
  adxl.setInterruptMapping( ADXL345_INT_ACTIVITY_BIT,     ADXL345_INT1_PIN );
  adxl.setInterruptMapping( ADXL345_INT_INACTIVITY_BIT,   ADXL345_INT1_PIN );
 
  //register interrupt actions - 1 == on; 0 == off  
  adxl.setInterrupt( ADXL345_INT_SINGLE_TAP_BIT, 1);
  adxl.setInterrupt( ADXL345_INT_DOUBLE_TAP_BIT, 1);
  adxl.setInterrupt( ADXL345_INT_FREE_FALL_BIT,  1);
  adxl.setInterrupt( ADXL345_INT_ACTIVITY_BIT,   1);
  adxl.setInterrupt( ADXL345_INT_INACTIVITY_BIT, 1);
  pinMode(3,OUTPUT);
  digitalWrite(3,LOW);
  
  collebration ();

  
}

void loop(){

  delay(1000);
  int xyz[3]; 
  int x,y,z;
  adxl.readAccel(xyz);
  x =abs(xyz[0] - colibxyz[0]);
  y =abs(xyz[1] - colibxyz[1]);
  z =abs(xyz[2] - colibxyz[2]);
 // Serial.println(x+y+z);
  Serial.print(x);
  Serial.print(" ");
  Serial.print(y);
  Serial.print(" ");
  Serial.print(z);
  sensytive = map(analogRead(pot),0,1024,0,25);
  if ((x > sensytive)||(y > sensytive)||(z > sensytive)) {beep(500);countalarm++;}
  if (countalarm == 10) collebration ();
}
void beep(int j){
  for(int i = 0; i<j; i++) {
    digitalWrite(3,HIGH);
    delayMicroseconds(j); 
    digitalWrite(3,LOW);
    delayMicroseconds(j);
    
  }
}
void collebration (){
 for(int i=0;i<4;i++) {delay(100);beep(250);}
 int x,y,z;
  x=0;
  y=0;
  z=0;
  for (int i =0 ; i<100 ;i++){
    adxl.readAccel(colibxyz);
    x+=colibxyz[0];
    y+=colibxyz[1];
    z+=colibxyz[2]; 
  }
  colibxyz[0] = round(x/100);
  colibxyz[1] = round(y/100);
  colibxyz[2] = round(z/100);
  countalarm = 0;

}
