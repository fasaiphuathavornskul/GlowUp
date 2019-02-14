#include <PololuLedStrip.h>
#define LED_COUNT 60
#define LED_DATA_PIN 12

// LEDStrip object and specify pin
PololuLedStrip<LED_DATA_PIN> ledStrip; 

// Buffer for holding colors (3 bytes per color)
rgb_color colors[LED_COUNT];

int drunk_lvl = 0;

void setup() {
  // set up serial port for communication with BT.
  Serial.begin(9600); // TODO is baud rate correct?
  Serial.println("Ready.");
}

void loop() {
  // must make sure to have no line ending, or else serial.parseint will read in 0.
  
    Serial.print("==Drunk Lvl==: ");
    Serial.println(drunk_lvl);

    if( drunk_lvl >= 0 && drunk_lvl < 30 ){
      Serial.println("Not drunk yet");
      not_drunk();
    }

    else if (drunk_lvl >= 30 && drunk_lvl < 60){
      Serial.println("Nice & Tipsy");
      tipsy();
    }

    else if (drunk_lvl >= 60 && drunk_lvl < 90){
      Serial.println("Drunk");
      drunk();
    }

    else if (drunk_lvl >= 90 && drunk_lvl <= 100){
      Serial.println("Overdrank");
      overdrank();
    }
  
}

bool check_serial(){
  if(Serial.available()) {
    // get drunk level
    drunk_lvl = Serial.read();
    return true;
  } else return false;
  
}
// Converts a color from HSV to RGB.
// h is hue, as a number between 0 and 360.
// s is the saturation, as a number between 0 and 255.
// v is the value, as a number between 0 and 255.
rgb_color hsvToRgb(uint16_t h, uint8_t s, uint8_t v)
{
    uint8_t f = (h % 60) * 255 / 60;
    uint8_t p = (255 - s) * (uint16_t)v / 255;
    uint8_t q = (255 - f * (uint16_t)s / 255) * (uint16_t)v / 255;
    uint8_t t = (255 - (255 - f) * (uint16_t)s / 255) * (uint16_t)v / 255;
    uint8_t r = 0, g = 0, b = 0;
    switch((h / 60) % 6){
        case 0: r = v; g = t; b = p; break;
        case 1: r = q; g = v; b = p; break;
        case 2: r = p; g = v; b = t; break;
        case 3: r = p; g = q; b = v; break;
        case 4: r = t; g = p; b = v; break;
        case 5: r = v; g = p; b = q; break;
    }
    return rgb_color(r, g, b);
}

void fade(unsigned char *val, unsigned char fadeTime)
{
  if (*val != 0)
  {
    unsigned char subAmt = *val >> fadeTime;  // val * 2^-fadeTime
    if (subAmt < 1)
      subAmt = 1;  // make sure we always decrease by at least 1
    *val -= subAmt;  // decrease value of byte pointed to by val
  }
}


void brightTwinkleColorAdjust(unsigned char *color)
{
  if (*color == 255)
  {
    // if reached max brightness, set to an even value to start fade
    *color = 254;
  }
  else if (*color % 2)
  {
    // if odd, approximately double the brightness
    // you should only use odd values that are of the form 2^n-1,
    // which then gets a new value of 2^(n+1)-1
    // using other odd values will break things
    *color = *color * 2 + 1;
  }
  else if (*color > 0)
  {
    fade(color, 4);
    if (*color % 2)
    {
      (*color)--;  // if faded color is odd, subtract one to keep it even
    }
  }
}

void randomWalk(unsigned char *val, unsigned char maxVal, unsigned char changeAmount, unsigned char directions)
{
  unsigned char walk = random(directions);  // direction of random walk
  if (walk == 0)
  {
    // decrease val by changeAmount down to a min of 0
    if (*val >= changeAmount)
    {
      *val -= changeAmount;
    }
    else
    {
      *val = 0;
    }
  }
  else if (walk == 1)
  {
    // increase val by changeAmount up to a max of maxVal
    if (*val <= maxVal - changeAmount)
    {
      *val += changeAmount;
    }
    else
    {
      *val = maxVal;
    }
  }
}

void not_drunk(){
  while(!check_serial()){

    rgb_color color;
    color.red = 0;
    color.green = 255;
    color.blue = 0;

    for(uint16_t i=0; i < LED_COUNT; i++){
      colors[i] = color;
    }
    ledStrip.write(colors, LED_COUNT);
  
    delay(10);
  }
}

void tipsy(){
  while(!check_serial()){
    // Update the colors.
    uint16_t time = millis() >> 2;
    for(uint16_t i = 0; i < LED_COUNT; i++)
    {
      byte x = (time >> 2) - (i << 3);
      colors[i] = hsvToRgb((uint32_t)x * 359 / 256, 255, 255);
    }
  
    // Write the colors to the LED strip.
    ledStrip.write(colors, LED_COUNT);
  
    delay(10);
  }
}

void drunk(){
  while(!check_serial()){
    for (int i = 0; i < LED_COUNT; i++)
      {
        brightTwinkleColorAdjust(&colors[i].red);
        brightTwinkleColorAdjust(&colors[i].green);
        brightTwinkleColorAdjust(&colors[i].blue);
      }
      // if we are generating new twinkles, randomly pick four new LEDs
      // to light up
      for (int i = 0; i < 4; i++)
      {
        int j = random(LED_COUNT);
        if (colors[j].red == 0 && colors[j].green == 0 && colors[j].blue == 0)
        {
          // if the LED we picked is not already lit, light it white
            colors[j] = rgb_color(1, 1, 1);  // white
        }
      }
//    const unsigned char maxBrightness = 120;  // cap on LED brighness
//    const unsigned char changeAmount = 2;   // size of random walk step
//    for (int i = 0; i < LED_COUNT; i += 2)
//    {
//      // randomly walk the brightness of every even LED
//      randomWalk(&colors[i].red, maxBrightness, changeAmount, 2);
//      
//      // warm white: red = x, green = 0.8x, blue = 0.125x
//      colors[i].green = colors[i].red*4/5;  // green = 80% of red
//      colors[i].blue = colors[i].red >> 3;  // blue = red/8
//      
//      // every odd LED gets set to a quarter the brighness of the preceding even LED
//      if (i + 1 < LED_COUNT)
//      {
//        colors[i+1] = rgb_color(colors[i].red >> 2, colors[i].green >> 2, colors[i].blue >> 2);
//      }
//    }
    ledStrip.write(colors, LED_COUNT);

    delay(10);
  }
}

void overdrank(){
  while(!check_serial()){
    rgb_color color;
    color.red = 255;
    color.green = 0;
    color.blue = 0;

    for(uint16_t i=0; i < LED_COUNT; i++){
      colors[i] = color;
    }
    ledStrip.write(colors, LED_COUNT);

    delay(1000);

    color.red = 0 ;
    color.green = 0;
    color.blue = 0;

    for(uint16_t i=0; i < LED_COUNT; i++){
      colors[i] = color;
    }
    ledStrip.write(colors, LED_COUNT);

    delay(500);
    
  }
  
}
