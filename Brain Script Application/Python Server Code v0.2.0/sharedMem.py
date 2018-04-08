#!/usr/bin/env python3

#file storing global variable names to be used by different threads
isSendingSong = False
songToSend = ' '
speakersConnected = { 'a': 0 }

# starting values of global variables
def init():
    speakersConnected = { 'a': 0 }
    isSendingSong = False
    songToSend = ' '