#include <MeetAndroid.h>
#include <Average.h>

#define SCALE_PIN   0
#define TX_PIN      10

//weight change edge detector arrays
#define SAMPLE_SIZE 120
#define WINDOW_SIZE 5

// When to get data from sensor.
#define DELAY 10

// Print average on serial every x DELAY.
#define PRINT_TIMEOUT 100

#define NB_PREV_AVG 5

#define STEP 3

#define BIG_STEP 10

#define STEADY 0
#define INCREASING 1
#define DECREASING 2

MeetAndroid meetAndroid;

// Rolling average array.
float sampleArray[SAMPLE_SIZE + WINDOW_SIZE + SAMPLE_SIZE];
//float sampleArray[SAMPLE_SIZE];

// Global used to count when to print average.
int counter;

int lastAvg;
int beforeLastAvg;

int previousState;
int beginSteady;

float previousAvg[NB_PREV_AVG];
int indexPrevAvg;

float lastPrintedAvg;

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

        if(min >= max - STEP) {
            if(lastPrintedAvg > mean + STEP || lastPrintedAvg < mean - STEP) {
                Serial.println(mean);
                lastPrintedAvg = mean;
            }
        }

        indexPrevAvg = ++indexPrevAvg % NB_PREV_AVG;
        
    }

}
/*
void loop() {
    float avg = rollingAverage(sampleArray, SAMPLE_SIZE + WINDOW_SIZE + SAMPLE_SIZE, analogRead(SCALE_PIN)); 
  
    float prior_mean =0;
    float post_mean =0;

    float prior_stdev =0;
    float post_stdev =0;
  
    float diff;
    float var;
  
    for(int i=0; i<SAMPLE_SIZE; i++)
    {
        prior_mean += sampleArray[i];
        post_mean += sampleArray[SAMPLE_SIZE + WINDOW_SIZE + i];
    }
 
    prior_mean/=(float)SAMPLE_SIZE;
    post_mean/=(float)SAMPLE_SIZE;
 
    diff = abs(prior_mean - post_mean);
 
    for(int i=0; i<SAMPLE_SIZE; i++)
    {
        prior_stdev += sq(sampleArray[i] - prior_mean);
        post_stdev += sq(sampleArray[SAMPLE_SIZE + WINDOW_SIZE + i] - post_mean);
    }
 
    prior_stdev/=(float)SAMPLE_SIZE-1;
    post_stdev/=(float)SAMPLE_SIZE-1;
 
    prior_stdev =sqrt(prior_stdev);
    post_stdev =sqrt(post_stdev);
    var = (prior_stdev + post_stdev)/(float)SAMPLE_SIZE;

    float t = diff/var;

    //if(t > maxT) {
    //    maxT = t;
    //}
 
    //if(counter++ > PRINT_TIMEOUT) {
    //   counter = 0;
       
        if(t>5) { 
            Serial.println(post_mean);
        }
    //    maxT = 0;
    //}
    delay(DELAY);
}
*/
/*
void loop()
{
   
    //meetAndroid.receive();
   
  	int weight = analogRead(SCALE_PIN);
  	float avg = rollingAverage(sampleArray, SAMPLE_SIZE, weight);

    if(counter++ > PRINT_TIMEOUT) {
        counter = 0;
        if(beforeLastAvg > lastAvg + STEP && lastAvg > avg + STEP) {
            if(previousState != DECREASING) {
                Serial.println("Decrease");
                //meetAndroid.send("Decrease");
                previousState = DECREASING;
            }
        } else if(beforeLastAvg < lastAvg - STEP && lastAvg < avg - STEP) {
            if(previousState != INCREASING) {
                Serial.println("Increase");
                //meetAndroid.send("Increase");
                previousState = INCREASING;
            }
        } else {
            if(previousState != STEADY 
                || avg > beginSteady + BIG_STEP 
                || avg < beginSteady - BIG_STEP) {
                beginSteady = roundNumb(avg);
                //Serial.print("Steady, Weight = ");
                Serial.println(beginSteady);
                //meetAndroid.send("Steady");
                //meetAndroid.send(beginSteady);
                previousState = STEADY;
            }
        }
        beforeLastAvg = lastAvg; 
        lastAvg = avg;
    }
    
  	//delay(DELAY);
}
*/
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