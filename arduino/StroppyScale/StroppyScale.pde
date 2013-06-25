
#include <MeetAndroid.h>
#include <Average.h>

#define SCALE_PIN   0
#define TX_PIN      10

//weight change edge detector arrays
#define SAMPLE_SIZE 50
#define WINDOW_SIZE 0

// When to get data from sensor.
#define DELAY 10

// Print average on serial every x DELAY.
#define PRINT_TIMEOUT 20

#define STEP 3

#define BIG_STEP 10

#define STEADY 0
#define INCREASING 1
#define DECREASING 2

MeetAndroid meetAndroid;


// Rolling average array.
float sampleArray[SAMPLE_SIZE + WINDOW_SIZE + SAMPLE_SIZE];

// Global used to count when to print average.
int counter;

int lastAvg;
int beforeLastAvg;

int previousState;
int beginSteady;

// Simple function to round float.
int roundNumb(float f) {
	int roundedF = (int)f;
	if(f-0.5 < roundedF) {
		return roundedF;
	} else {
		return roundedF+1;
	}
}

void setup()
{
    lastAvg = 0;
    beforeLastAvg = 0;

    previousState = STEADY;
    beginSteady = 0;

    counter = 0;
  	pinMode(SCALE_PIN, INPUT);
    pinMode(TX_PIN, OUTPUT);

  	Serial.begin(115200);

    meetAndroid.registerFunction(powerEvent, 'p');

    meetAndroid.registerFunction(weightInfo, 'i');
}

void loop()
{

    meetAndroid.receive();
   
  	int weight = analogRead(SCALE_PIN);
  	float avg = rollingAverage(sampleArray, SAMPLE_SIZE + WINDOW_SIZE + SAMPLE_SIZE, weight);

    if(counter++ > PRINT_TIMEOUT) {
        counter = 0;
        if(beforeLastAvg > lastAvg + STEP && lastAvg > avg + STEP) {
            if(previousState != DECREASING) {
                //Serial.println("Decrease");
                //meetAndroid.send("Decrease");
                previousState = DECREASING;
            }
        } else if(beforeLastAvg < lastAvg - STEP && lastAvg < avg - STEP) {
            if(previousState != INCREASING) {
                //Serial.println("Increase");
                //meetAndroid.send("Increase");
                previousState = INCREASING;
            }
        } else {
            if(previousState != STEADY 
                || avg > beginSteady + BIG_STEP 
                || avg < beginSteady - BIG_STEP) {
                beginSteady = roundNumb(avg);
                //Serial.print("Steady, Weight = ");
                //Serial.println(beginSteady);
                //meetAndroid.send("Steady");
                meetAndroid.send(beginSteady);
                previousState = STEADY;
            }
        }
        beforeLastAvg = lastAvg; 
        lastAvg = avg;
    }
  	delay(DELAY);
}

void powerEvent(byte flag, byte numOfValues)
{
    int state = meetAndroid.getInt();
    transmit(state==1);
}

void weightInfo(byte flag, byte numOfValues)
{
    meetAndroid.send(beginSteady);
}

void sendBit(boolean b) {
    if (b) {
        digitalWrite(TX_PIN, HIGH);
        delayMicroseconds(1125);
        digitalWrite(TX_PIN, LOW);
        delayMicroseconds(375);
    }
    else {
        digitalWrite(TX_PIN, HIGH);
        delayMicroseconds(375);
        digitalWrite(TX_PIN, LOW);
        delayMicroseconds(1125);
    }
}

void sendPair(boolean b) {
    sendBit(false);
    sendBit(b);
}

void switchcode(boolean b) {
    // house code 1 = B
    sendPair(true);
    sendPair(false);
    sendPair(false);
    sendPair(false);

    // unit code 2
    sendPair(true);
    sendPair(false);
    sendPair(false);
    sendPair(false);

    // on = 14
    sendPair(false);
    sendPair(true);
    sendPair(true);
    sendPair(b);

    sendBit(false);  
}

void transmit(boolean b) {
    switchcode(b);
    delayMicroseconds(10000);
    switchcode(b);
    delayMicroseconds(10000);
    switchcode(b);
}