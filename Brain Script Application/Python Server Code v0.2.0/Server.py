#!/usr/bin/env python3
import socket
import sys
import os
import time
from threading import Thread
from socketserver import ThreadingMixIn

from Speaker_Client_Thread import Speaker_Client
from Phone_Client_Thread import Phone_Client
import sharedMem

# Multithreaded server for demo 2

#startMain

#Tinkerboard ip setup
# get info for wlan0
# ipaddrs = netifaces.ifaddresses('wlan0') # uncomment for tinkerboard
# get wlan0 interface address
# wlan0 = ipaddrs[2][0]['addr'] #uncomment for tinkerboard

#Constants
# HOST        = wlan0           #uncomment for tinkerboard (as well as above 2 statements)
# HOST        = '192.168.1.103' #uncomment for Blake's Desktop on his home network
# HOST        = '192.168.1.17'  #uncomment for Blake's Laptop on mobile hotspot
# HOST        = '192.168.1.131' #uncoment for Michael's Laptop.
HOST = '192.168.1.73' #Jonathan's ip 

PHONE_PORT  = 14123
MCU_PORT    = 14124
BUFFER_SIZE = 1024
PHONE_ADDR  = (HOST,PHONE_PORT)
MCU_ADDR    = (HOST,MCU_PORT)
#endConstants

sharedMem.init()

print(' ')
print("Hosting Server on {0}...".format(HOST))

phoneThread = Thread(target=Phone_Client, args=(PHONE_ADDR, BUFFER_SIZE))
phoneThread.start()

time.sleep(1)

speakerThread = Thread(target=Speaker_Client, args=(0, MCU_ADDR, BUFFER_SIZE))
speakerThread.start()
#endMain
