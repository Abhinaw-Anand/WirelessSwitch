from bluedot.btcomm import BluetoothServer
from signal import pause
import RPi.GPIO as GPIO
import time
import threading
from subprocess import call

def timed1(s,t):
    print(s)
    print(t)
    GPIO.output(5,1)
    time.sleep(t*60)
    GPIO.output(5,0)
    time.sleep(s*60)
    GPIO.output(5,1)
    
def timed3(r,s,t):
    print(r)
    print(s)
    print(t)
    while( r != 0):
     r=r-1
     GPIO.output(10,1)
     GPIO.output(10,0)
     time.sleep(s*60)
     GPIO.output(10,1)
     time.sleep(t*60)
     GPIO.output(10,1)
     
    
def data_received(data):
    print(data)
    st=data
    if int(data[0]) == 1:
        if data[1]=='S':
            incom=data.find(',')
            st1=st[2:incom]
            st2=st[incom+1:]
            threading.Thread(target=timed1,args=(int(st1),int(st2),)).start()
          

       elif int(data[0])==3:
           incom=data.find(',')
           ino=data.find('|')
           st1=st[1:incom]
           st2=st[incom+1:ino]
           st3=st[ino+1:]
           threading.Thread(target=timed3,args=(int(st1),int(st2),int(st3),)).start()



GPIO.setmode(GPIO.BOARD)
GPIO.cleanup()
GPIO.setup(7,GPIO.OUT)
GPIO.setup(10,GPIO.OUT)
GPIO.setup(5,GPIO.OUT)
GPIO.setup(3,GPIO.OUT)
GPIO.output(3,1)
GPIO.output(10,1)
GPIO.output(7,1)
GPIO.output(5,1)
i=0
try:
 s = BluetoothServer(data_received)
 while i<40:
    GPIO.output(3,0)
    time.sleep(.07)
    GPIO.output(3,1)
    time.sleep(.07)
    i=i+1
    GPIO.output(3, 0)
 pause()
except:
    GPIO.cleanup()


