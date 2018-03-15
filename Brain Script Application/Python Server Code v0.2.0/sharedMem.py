#!/usr/bin/env python3

#file storing global variable names to be used by different threads
isSendingSong = False
songToSend = ' '

# starting values of global variables
def init():
    isSendingSong = False
    songToSend = ' '