#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>
#include "Tlc5940.h"

#define NUM_ROWS 3
#define NUM_COLS 4

#define FIRST_PIN 22
#define LAST_PIN 44

#define COMMAND_LED 0x0
#define COMMAND_SWITCH 0x1
#define COMMAND_REFRESH 0x2

#define INTENSITY 2

AndroidAccessory acc("Navi", "ESN", "Empty Seat Navigator",
                     "1.0", "www.uta.edu", "");

typedef struct {
  byte pin;
  byte red;
  byte green;
  byte blue;
  byte isAvailable;
} Seat;

byte message[6];

Seat seats[NUM_ROWS][NUM_COLS];

void sendMessage(byte command, byte row, byte col, byte value) {
  if (acc.isConnected()) {
    message[0] = command;
    message[1] = row;
    message[2] = col;
    message[3] = value;
    
    acc.write(message, 4);
  }
}

void setPins(int i, int j, int pin, int rPin, int gPin, int bPin) {
      pinMode(pin, INPUT_PULLUP);
      seats[i][j].pin = pin;
      seats[i][j].red = rPin;
      seats[i][j].green = gPin;
      seats[i][j].blue = bPin;
}

void setup() {
  //Start the android accessory
  acc.powerOn();
  
  //Initialize the LED driver(s)
  Tlc.init();
  
  //Assign seat pins and LED channels to seats
  setPins(0,0,29,36,37,38);
  setPins(0,1,25,17,18,19);
  setPins(0,2,23,29,30,31);
  setPins(0,3,22,7,8,9);
 
  setPins(1,0,31,39,40,41);
  setPins(1,1,26,20,21,22);
  setPins(1,2,27,1,2,3);
  setPins(1,3,24,10,11,12);
 
  setPins(2,0,32,45,46,47);
  setPins(2,1,30,26,27,28);
  setPins(2,2,33,4,5,6);
  setPins(2,3,28,13,14,15);
 
  //Set all LEDs to Green, isAvailable to true
  for (int i=0; i<NUM_ROWS; i++) {
    for (int j=0; j<NUM_COLS; j++) {
        Tlc.set(seats[i][j].green, 255 * INTENSITY);
        seats[i][j].isAvailable = 1;
    }
  }
}

void loop() {
  
 //Check state of each switch
  for (int i = 0; i < NUM_ROWS; i++) {
    for (int j = 0; j < NUM_COLS; j++) {
      int pin = seats[i][j].pin;
      int prevValue = seats[i][j].isAvailable;
      int curValue = digitalRead(pin);
      delay(25);
      if (prevValue != curValue) { 
        if (curValue == HIGH) {
          seats[i][j].isAvailable = 1;
          Tlc.set(seats[i][j].red, 0);
          Tlc.set(seats[i][j].green, 255 * INTENSITY);
          Tlc.set(seats[i][j].blue, 0);
        } else {
          seats[i][j].isAvailable = 0;
          Tlc.set(seats[i][j].red, 0);
          Tlc.set(seats[i][j].green, 0);
          Tlc.set(seats[i][j].blue, 0);
        }
        sendMessage(COMMAND_SWITCH, i, j, curValue); 
      }
    }
  }
  
  if (acc.isConnected()) {
    //Read received messages
    int len = acc.read(message, sizeof(message), 1);
    if (len > 0) {
      if (message[0] == COMMAND_LED) {
        int row = message[1];
        int col = message[2];
        Serial.write(message[1]); Serial.write(message[2]);
        Tlc.set(seats[row][col].red, message[3] * INTENSITY);
        Tlc.set(seats[row][col].green, message[4] * INTENSITY);
        Tlc.set(seats[row][col].blue, message[5] * INTENSITY);
      } else if (message[0] == COMMAND_REFRESH) {          
        //Check state of each switch
        for (int i = 0; i < NUM_ROWS; i++) {
          for (int j = 0; j < NUM_COLS; j++) {
            int pin = seats[i][j].pin;
            int value = digitalRead(pin);
            delay(25);
            sendMessage(COMMAND_SWITCH, i, j, value); 
          }
        }        
      }
    }
  }

  Tlc.update();
} 
