#!/usr/bin/env python3
# First file to run on startup
# Launches two threads, one for listening for
# incoming mobile app data and the other
# Embeded speaker system data

import socket
import sys
import os
import time
# import netifaces # uncomment for tinkerboard
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
HOST        = '192.168.1.103' #uncomment for Blake's Desktop on his home network
# HOST        = '192.168.1.17'  #uncomment for Blake's Laptop on mobile hotspot
# HOST        = '192.168.1.131' #uncoment for Michael's Laptop.

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

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)  # Create a socket object

s.bind(MCU_ADDR)        # Bind to the port
s.listen(5)             # Now wait for client connection

print(' ')
print('Server is listening for speaker clients...')

speakerNumber = 0
while True:
   client, addr = s.accept()     # Establish connection with client.
   UDP_sock = 0
   t = Thread(target=Speaker_Client, args=(client, UDP_sock, speakerNumber, addr))
   t.start()
   
   speakerNumber = speakerNumber + 1
#endwhile
s.close()
