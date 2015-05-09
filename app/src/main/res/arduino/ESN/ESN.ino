#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>
#include "Tlc5940.h"
#include <Wire.h>
#include "Adafruit_LEDBackpack.h"
#include "Adafruit_GFX.h"

#define NUM_ROWS 3
#define NUM_COLS 4

#define COMMAND_LED 0x0
#define COMMAND_SWITCH 0x1
#define COMMAND_REFRESH 0x2

#define INTENSITY 1

Adafruit_8x8matrix matrix[3] = {Adafruit_8x8matrix(), 
  Adafruit_8x8matrix(), Adafruit_8x8matrix()};
  
static const uint8_t matrixAddr[] = { 0x70, 0x71, 0x72 };

static const uint8_t PROGMEM
  numImg[][8] = {
  { B00011100,
    B00100010,
    B00100010,
    B00100010,
    B00100010,
    B00100010,
    B00100010,
    B00011100 },
  { B00001000,
    B00011000,
    B00101000,
    B00001000,
    B00001000,
    B00001000,
    B00001000,
    B00111110 },
  { B00011100,
    B00100010,
    B00000010,
    B00000100,
    B00001000,
    B00010000,
    B00100000,
    B00111110 },
  { B00011100,
    B00100010,
    B00000010,
    B00001100,
    B00000010,
    B00000010,
    B00100010,
    B00011100 },
  { B00100100,
    B00100100,
    B00100100,
    B00100100,
    B00111110,
    B00000100,
    B00000100,
    B00000100 }},
  navi[] = 
  { B00001000,
    B00010100,
    B00010100,
    B00001000,
    B01110110,
    B01111001,
    B01110110,
    B00000000 },
  sword[] = 
  { B11000000,
    B10100000,
    B01010110,
    B00101110,
    B00011100,
    B00111110,
    B00110111,
    B00000011  };

AndroidAccessory acc("Navi", "ESN", "Empty Seat Navigator",
                     "1.0", "www.uta.edu", "");

typedef struct {
  byte pin;
  byte red;
  byte green;
  byte blue;
  byte isAvailable;
} Seat;

byte message[7];

Seat seats[NUM_ROWS][NUM_COLS];

uint8_t availSeats[NUM_ROWS];
uint8_t lastSwitchState[NUM_ROWS][NUM_COLS];
long lastDebounceTime[NUM_ROWS][NUM_COLS];
long debounceDelay = 1000;

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
 
  //Set all LEDs to Green, isAvailable to true; debounce values
  for (int i=0; i<NUM_ROWS; i++) {
    for (int j=0; j<NUM_COLS; j++) {
        Tlc.set(seats[i][j].green, 255 * INTENSITY);
        seats[i][j].isAvailable = 1;
        lastDebounceTime[i][j] = 0;
        lastSwitchState[i][j] = HIGH;
    }
  }
  
  // Initialize LED Matrices
  for(uint8_t i=0; i<NUM_ROWS; i++) {
    matrix[i].begin(matrixAddr[i]);
    matrix[i].setBrightness(0);
    availSeats[i] = 4;
    matrix[i].drawBitmap(0, 0, numImg[availSeats[i]], 8, 8, LED_ON);
    matrix[i].writeDisplay();
  }
  
  //Start the android accessory
  acc.powerOn();
  
  Serial.begin(9600);
}

void loop() {
  
 //Check state of each switch
  for (int i = 0; i < NUM_ROWS; i++) {
    for (int j = 0; j < NUM_COLS; j++) {
      int pin = seats[i][j].pin;
      int prevValue = seats[i][j].isAvailable;
      int curValue = digitalRead(pin);
      delay(25);
      if (curValue != lastSwitchState[i][j]) {
        lastDebounceTime[i][j] = millis(); 
      }       
      if ((millis() - lastDebounceTime[i][j]) > debounceDelay) {
        if (prevValue != curValue) { 
          if (curValue == HIGH) {
            seats[i][j].isAvailable = 1;
            Tlc.set(seats[i][j].red, 0);
            Tlc.set(seats[i][j].green, 255 * INTENSITY);
            Tlc.set(seats[i][j].blue, 0);
            availSeats[i] = availSeats[i] + 1;
            matrix[i].clear();
            matrix[i].drawBitmap(0, 0, numImg[availSeats[i]], 8, 8, LED_ON);
            matrix[i].writeDisplay();
          } else if (curValue == LOW) {
            seats[i][j].isAvailable = 0;
            Tlc.set(seats[i][j].red, 0);
            Tlc.set(seats[i][j].green, 0);
            Tlc.set(seats[i][j].blue, 0);
            availSeats[i] = availSeats[i] - 1;
            matrix[i].clear();
            matrix[i].drawBitmap(0, 0, numImg[availSeats[i]], 8, 8, LED_ON);
            matrix[i].writeDisplay();
          }
          sendMessage(COMMAND_SWITCH, i, j, curValue); 
        }
      }
      lastSwitchState[i][j] = curValue;
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
  
  // Easter-Egg
  int easterEgg = 0;
  int badCount = 0;
  for (int i = 0; i < NUM_ROWS; i++) {
    for (int j = 0; j < NUM_COLS; j++) {
      if ((i==0 && j==0) || (i==0 && j==3) ||
          (i==2 && j==0) || (i==2 && j==3)) {
          if (seats[i][j].isAvailable==0) {
          easterEgg++;
          }
      }
      else {
        if (seats[i][j].isAvailable==0) {
          badCount++;
        }
      }
    }
  }
  
  Serial.println(easterEgg);
  Serial.println(badCount);
  
  if (easterEgg==4 && badCount==0) {
      matrix[0].setTextSize(1);
      matrix[0].setTextWrap(false);  // we dont want text to wrap so it scrolls nicely
      matrix[0].setTextColor(LED_ON);
      matrix[1].setTextSize(1);
      matrix[1].setTextWrap(false);  // we dont want text to wrap so it scrolls nicely
      matrix[1].setTextColor(LED_ON);
      matrix[2].clear();
      matrix[2].drawBitmap(0, 0, sword, 8, 8, LED_ON);
      matrix[2].writeDisplay();   
      for (int8_t x=0; x>=-127; x--) {
        matrix[0].clear();
        matrix[0].setCursor(x%64+8,0);
        matrix[0].print("Team Navi");
        matrix[0].writeDisplay();
        matrix[1].clear();
        matrix[1].setCursor(x+8,0);
        matrix[1].print("Melissa Brad & Kevin");
        matrix[1].writeDisplay();
//        if (x==0) {
//          matrix[2].clear();
//          matrix[2].drawBitmap(0, 0, navi, 8, 8, LED_ON);
//          matrix[2].writeDisplay();   
//        }
//        if (x==-64) {
//          matrix[2].clear();
//          matrix[2].drawBitmap(0, 0, sword, 8, 8, LED_ON);
//          matrix[2].writeDisplay();   
//        }
        delay(100);
      }
  }
  else {
    matrix[0].clear();
    matrix[0].drawBitmap(0, 0, numImg[availSeats[0]], 8, 8, LED_ON);
    matrix[0].writeDisplay();
    matrix[1].clear();
    matrix[1].drawBitmap(0, 0, numImg[availSeats[1]], 8, 8, LED_ON);
    matrix[1].writeDisplay();
    matrix[2].clear();
    matrix[2].drawBitmap(0, 0, numImg[availSeats[2]], 8, 8, LED_ON);
    matrix[2].writeDisplay();
  }
} 
