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
from Speaker_UDP_Client_Thread import Speaker_UDP_Client
from Phone_Client_Thread import Phone_Client
from SongSync_Thread import SongSync
import sharedMem

# Multithreaded server for demo 2

#startMain

# global variables #
global aliveSpeakers
global speakerAddresses
global speakerWDTs
global speakerEnumeration

#Tinkerboard ip setup
# get info for wlan0
# ipaddrs = netifaces.ifaddresses('wlan0') # uncomment for tinkerboard
# get wlan0 interface address
# wlan0 = ipaddrs[2][0]['addr'] #uncomment for tinkerboard

#Constants
# HOST        = wlan0           #uncomment for tinkerboard (as well as above 2 statements)
HOST        = '192.168.1.7' #uncomment for Blake's Desktop on his home network
# HOST        = '192.168.1.103'  #uncomment for Blake's Laptop on mobile hotspot
# HOST        = '192.168.1.131' #uncoment for Michael's Laptop.

PHONE_PORT  = 14123
MCU_PORT    = 14124
PHONE_ADDR  = (HOST,PHONE_PORT)
MCU_ADDR    = (HOST,MCU_PORT)
#endConstants

sharedMem.init()

print(' ')
print("Hosting Server on {0}...".format(HOST))

phoneThread = Thread(target=Phone_Client, args=(PHONE_ADDR, ))
phoneThread.start()

time.sleep(1)

tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)  # tcp socket
tcp_socket.bind(MCU_ADDR)        # Bind to the port
tcp_socket.listen(5)             # Now wait for client connection

UDP_listen_sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
UDP_listen_sock.bind(MCU_ADDR)
UDP_listen_sock.setblocking(0)

udp_thread = Thread(target=Speaker_UDP_Client, args=(UDP_listen_sock,))
udp_thread.start()

songSync_thread = Thread(target=SongSync)
songSync_thread.start()

print('Speaker - Waiting for speaker clients...')

speakerNumber = 0 # current lifetime connection number
speakerEnum   = 0 # current lifetime unique IPv4 connections
while True:
    tcp_client, addr = tcp_socket.accept() # Establish connection with next client.

    # add speaker to global lists
    sharedMem.aliveSpeakers.update(   {speakerNumber : True})
    try:
        oldSpeakerNumber = sharedMem.speakerAddresses[addr[0]]
    except Exception as e:
        a = 0
    #endexcept
    sharedMem.speakerAddresses.update({addr[0] : speakerNumber})
    sharedMem.speakerWDTs.update(     {speakerNumber : time.time() + 5})
    sharedMem.songFileIndexes.update( {speakerNumber : 44})

    # check if this is the first time this address has connected
    if(not (addr[0] in sharedMem.speakerEnumeration.values())):
        # the speaker hasn't connected before so assign it an enumeration
        sharedMem.speakerEnumeration.update({speakerEnum:addr[0]})
        sharedMem.speakerEnables.update(    {speakerNumber : True}) # default is true but can be changed while disconnected
        speakerEnum = speakerEnum + 1
    else: #otherwise move the old enable value to the new speaker number
        sharedMem.speakerEnables.update({speakerNumber : sharedMem.speakerEnables.pop(oldSpeakerNumber)})
    #endelse

    # Start the TCP handler thread
    tcp_thread = Thread(target=Speaker_Client, args=(tcp_client, speakerNumber, addr))
    tcp_thread.start()

    # Increment the speakerNumber
    speakerNumber = speakerNumber + 1
#endwhile
