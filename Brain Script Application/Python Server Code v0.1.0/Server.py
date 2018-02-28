#!/usr/bin/env python3

import socket 
from threading import Thread 
from socketserver import ThreadingMixIn
import Phone_Client_Thread
import Speaker_Client_Thread

# Multithreaded server for demo 2

#startMain

#Tinkerboard ip setup
# get info for wlan0
# ipaddrs = netifaces.ifaddresses('wlan0') # uncomment for tinkerboard
# get wlan0 interface address
# wlan0 = ipaddrs[2][0]['addr'] #uncomment for tinkerboard

#Constants
#HOST        = wlan0 #uncomment for tinkerboard
HOST        = '192.168.1.103' #uncomment and set manually for windows operation
PHONE_PORT  = 14123
MCU_PORT    = 14124
BUFFER_SIZE = 1024 
PHONE_ADDR  = (HOST,PHONE_PORT)
MCU_ADDR    = (HOST,MCU_PORT)
#endConstants

print("Hosting Server on {0}...".format(HOST))

threads = [] 
 
while True:
    #start phone client thread
    newthread = Phone_Client_Thread()
    newthread.start()
    threads.append(newthread)

    newthread = Speaker_Client_Thread()
    newthread.start()
    threads.append(newthread)
    while True: # let the threads run forever
        a = 1
 #endwhile

for t in threads:
    t.join() 
#endfor
#endMain
