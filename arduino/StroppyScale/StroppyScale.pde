#include <MeetAndroid.h>
#include <Average.h>

#define SCALE_PIN   0
#define TX_PIN      10

//weight change edge detector arrays
#define SAMPLE_SIZE 120

// Print average on serial every x DELAY.
#define PRINT_TIMEOUT 100

#define NB_PREV_AVG 5

#define STEP 3

MeetAndroid meetAndroid;

// Rolling average array.
float sampleArray[SAMPLE_SIZE];

// Global used to count when to print average.
int counter;

int beginSteady;

float previousAvg[NB_PREV_AVG];
int indexPrevAvg;

float lastPrintedAvg;

void setup()
{
    beginSteady = 0;

    counter = 0;
  	pinMode(SCALE_PIN, INPUT);
    pinMode(TX_PIN, OUTPUT);

    indexPrevAvg = 0;
    lastPrintedAvg = 0;

    for (int i = 0; i<NB_PREV_AVG; i++) {
        previousAvg[i] = 0;
    }

  	Serial.begin(115200);

    meetAndroid.registerFunction(powerEvent, 'p');
    meetAndroid.registerFunction(weightInfo, 'i');
}


void loop()
{
    
    meetAndroid.receive();
   
    int weight = analogRead(SCALE_PIN);
    float avg = rollingAverage(sampleArray, SAMPLE_SIZE, weight);


    if(counter++ > PRINT_TIMEOUT) {
        counter = 0;
        
        previousAvg[indexPrevAvg] = avg;


        float min = 32767;
        float max = 0;
        float sum = 0;

        for (int i = 0; i<NB_PREV_AVG; i++) {
            sum += previousAvg[i];
            if(min > previousAvg[i]) {
                min = previousAvg[i];
            }
            if(max < previousAvg[i]) {
                max = previousAvg[i];
            }
        }

        float mean = sum/NB_PREV_AVG;

        // If it is stable.
        if(min >= max - STEP) {
            // If it doesn't change from last time.
            if(lastPrintedAvg > mean + STEP || lastPrintedAvg < mean - STEP) {
                //Serial.println(mean);
                meetAndroid.send(mean);
                lastPrintedAvg = mean;
            }
        }

        indexPrevAvg = ++indexPrevAvg % NB_PREV_AVG;
        
        //Serial.println(avg);
    }
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