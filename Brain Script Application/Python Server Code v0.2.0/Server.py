#!/usr/bin/env python3
import socket
import sys
import os
import time
from threading import Thread 
from socketserver import ThreadingMixIn
from Speaker_Client_Thread import Speaker_Client
from Phone_Client_Thread import Phone_Client

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

print(' ')
print("Hosting Server on {0}...".format(HOST))

threads = []

phoneThread = Thread(target=Phone_Client, args=(PHONE_ADDR, BUFFER_SIZE))
phoneThread.start()

time.sleep(1)

speakerThread = Thread(target=Speaker_Client, args=(0, MCU_ADDR, BUFFER_SIZE))
speakerThread.start()
#endMain
