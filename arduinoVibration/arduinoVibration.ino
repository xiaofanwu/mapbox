
#include <SoftwareSerial.h>
SoftwareSerial mySerial = SoftwareSerial(10,11); //rx, tx
int outputValue = 255;        // value output to the PWM (analog out)
String input = "";

void setup() {
  mySerial.begin(9600);
  pinMode(2, OUTPUT);
}

void loop() {
  boolean keep = false;
  if (!keep){
    input = ""; //testing controlling the loop
  }
  if (mySerial.available()){
    input = mySerial.readString();
    }
    
  if (input == "left"){
    mySerial.print("in left");
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400);  
    
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400);  
    }
    
  if (input == "right"){
    mySerial.print("in right");
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400);  
    
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400);  
    }
    
  else if (input == "slight left"){
    mySerial.print("in slight left");
    
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400);    
    }

  else if (input == "slight right"){
    mySerial.print("in slight right");
    
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400);    
    }
    
  else if (input == "sharp left"){
    mySerial.print("in sharp left");
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400);  
    
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400);  

    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400); 

    }
  else if (input == "sharp right"){
    mySerial.print("in sharp right");
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400);  
    
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400);  

    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400); 
    
  else if (input == "arrived"){
    mySerial.print("in arrived");
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400); 
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400); 
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400); 
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400); 
    digitalWrite(2, HIGH);
    delay(400);
    digitalWrite(2, LOW);
    delay(400); 
    }
    
  else if (input == "wrong"){
    mySerial.print("in wrong");
    digitalWrite(2, HIGH);
    delay(200);
    digitalWrite(2, LOW);
    delay(200); 
    digitalWrite(2, HIGH);
    delay(200);
    digitalWrite(2, LOW);
    delay(200); 
    digitalWrite(2, HIGH);
    delay(200);
    digitalWrite(2, LOW);
    delay(200); 
    digitalWrite(2, HIGH);
    delay(200);
    digitalWrite(2, LOW);
    delay(200); 
    digitalWrite(2, HIGH);
    delay(200);
    digitalWrite(2, LOW);
    delay(200); 
  }
  
  else if (input == ""){
    mySerial.print("in blank");
    digitalWrite(2, LOW);
    delay(20);
    }
    
  else if (input == "approaching"){
    mySerial.print("approaching"); 
    outputValue = 255;
    analogWrite(2, 255);
    delay(20); 
    }
    
  else {
    mySerial.print("in else");
    digitalWrite(2, LOW);
    delay(20);
    }
    
  mySerial.print("\t input = ");
  mySerial.println(input);
  delay(2);
}
