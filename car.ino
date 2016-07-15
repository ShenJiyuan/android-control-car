const int PAEN = P2_4;
const int PBEN = P2_5;
const int LED = P1_0;
int incoming=0;
int l=0;
int t=0;

const int PA1 = P1_5;//left forward
const int PA2 = P1_0;//left	back
const int PB1 = P1_4;//right forward
const int PB2 = P1_3;//right back
const int ServoDelay = 144;
const int STOP = 0, FORWARD = 1, BACK = 2, LEFT = 4, RIGHT = 3;
const int SC = 9;

int sp = 4;    		//speed档位，分为0,1,2,3,4档

const int R_FORWARD_PIN = PA1;
const int R_BACK_PIN = PA2;
const int L_FORWARD_PIN = PB1;
const int L_BACK_PIN = PB2;
int moveFlag = 0;
int dirFlag = 0;

const int Loop_Cyc = 5;  //ms
//Servo servo; 
int i = 0;

void leftWheelForward(int speedFlag){			//左轮前转
  switch(speedFlag){
    case 0:{   digitalWrite(PA1, LOW); break;   }
    case 1:{   
        digitalWrite(PA1, LOW);
        delay(50);
        analogWrite(PA1, 255);
        delay(1);
    }
    case 2:{   
        digitalWrite(PA1, LOW);
        delay(25);
        analogWrite(PA1, 255);
        delay(1);
    }
    case 3:{   
        digitalWrite(PA1, LOW);
        delay(1);
        analogWrite(PA1, 255);
        delay(1);
    }
    case 4:{   analogWrite(PA1, 255); break;   }
  }
}

void rightWheelForward(int speedFlag){			//右轮前转
  switch(speedFlag){
    case 0:{   digitalWrite(PB1, LOW); break;   }
    case 1:{   
        digitalWrite(PB1, HIGH);
        delay(1);
        digitalWrite(PB1, LOW);
        delay(50);
    }
    case 2:{   
        digitalWrite(PB1, HIGH);
        delay(1);
        digitalWrite(PB1, LOW);
        delay(25);
    }
    case 3:{   
        digitalWrite(PB1, LOW);
        delay(1);
        digitalWrite(PB1, HIGH);
        delay(1);
    }
    case 4:{   digitalWrite(PB1, HIGH); break;   }
  }
}

void leftWheelBack(int speedFlag){				//左轮后转
  switch(speedFlag){
    case 0:{   digitalWrite(PA2, LOW); break;   }
    case 1:{   
        digitalWrite(PA2, LOW);
        delay(50);
        analogWrite(PA2, 255);
        delay(1);
    }
    case 2:{   
        digitalWrite(PA2, LOW);
        delay(25);
        analogWrite(PA2, 255);
        delay(1);
    }
    case 3:{   
        digitalWrite(PA2, LOW);
        delay(1);
        analogWrite(PA2, 255);
        delay(1);
    }
    case 4:{   analogWrite(PA2, 255); break;   }
  }
}

void rightWheelBack(int speedFlag){				//右轮后转
  switch(speedFlag){
    case 0:{   digitalWrite(PB2, LOW); break;   }
    case 1:{   
        digitalWrite(PB2, HIGH);
        delay(1);
        digitalWrite(PB2, LOW);
        delay(50);
    }
    case 2:{   
        digitalWrite(PB2, HIGH);
        delay(1);
        digitalWrite(PB2, LOW);
        delay(25);        
    }
    case 3:{   
        digitalWrite(PB2, LOW);
        delay(1);
        digitalWrite(PB2, HIGH);
        delay(1);
    }
    case 4:{   digitalWrite(PB2, HIGH); break;   }
  }
}

void stop(){				//停止
  rightWheelBack(0);
  rightWheelForward(0);
  leftWheelBack(0);
  leftWheelForward(0);
  dirFlag = 0;
}

void forward(){				//前进
  rightWheelForward(sp);
  leftWheelForward(sp);
  rightWheelBack(0);
  leftWheelBack(0);
  dirFlag = 0;
}

void back(){				//后退
  rightWheelBack(sp);
  leftWheelBack(sp);
  leftWheelForward(0);
  rightWheelForward(0);
  dirFlag = 1;
}

void turnLeft(){			//左转
  if ( dirFlag == 0 ){
    rightWheelForward(sp);
    leftWheelBack(0);
    leftWheelForward(0);
    rightWheelBack(0);
  }
  else if ( dirFlag == 1 ){
    rightWheelBack(sp);
    rightWheelForward(0);
    leftWheelBack(0);
    leftWheelForward(0);    
  }
}

void turnRight(){			//右转
  if ( dirFlag == 0 ){
    rightWheelForward(0);
    leftWheelBack(0);
    leftWheelForward(sp);
    rightWheelBack(0);
  }
  else if ( dirFlag == 1 ){
    rightWheelBack(0);
    rightWheelForward(0);
    leftWheelBack(sp);
    leftWheelForward(0);    
  }
}

void setup()
{
	Serial.begin(9600); 
	pinMode(PA1, OUTPUT);
	pinMode(PA2, OUTPUT);
	pinMode(PB2, OUTPUT);
	pinMode(PB1, OUTPUT);
	pinMode(PAEN, OUTPUT);
	pinMode(PBEN, OUTPUT);
    pinMode(LED, OUTPUT);
	stop();
	int flag = 1;
    digitalWrite(LED, l);
    Serial.println("start");
	i = 0;
}

void loop() 				//监听事件
{
  digitalWrite(PAEN, HIGH);
  digitalWrite(PBEN, HIGH);

    if(Serial.available() > 0){
        l=~l;
  		digitalWrite(LED, l);
		incoming = Serial.read();
		incoming = incoming - 48;
    }

    switch(incoming){
		case STOP:		{	moveFlag = 0; stop(); break;	    }
		case FORWARD:	{	moveFlag = 1; forward(); break;	    }
		case BACK:		{	moveFlag = 2; back(); break;	    }
		case LEFT:		{	moveFlag = 3; turnLeft(); break;	}
		case RIGHT:		{	moveFlag = 4; turnRight(); break;	}
		case 5:		    {	sp = 1; incoming = moveFlag;  Serial.println("1s"); Serial.println(moveFlag); break;	}
        case 6:		    {	sp = 2; incoming = moveFlag;  Serial.println("2s"); Serial.println(moveFlag); break;	}
        case 7:		    {	sp = 3; incoming = moveFlag;  Serial.println("3s"); Serial.println(moveFlag); break;	}
        case 8:		    {	sp = 4; incoming = moveFlag;  Serial.println("4s"); Serial.println(moveFlag); break;	}
        case 9:		    {	incoming = moveFlag;  Serial.println("1"); Serial.println(moveFlag); break;	            }
	}
	
}
