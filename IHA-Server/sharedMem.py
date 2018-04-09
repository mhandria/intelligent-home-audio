#!/usr/bin/env python3

#file storing global variable names to be used by different threads
aliveSpeakers      = {} # {speakerNumber : isAlive}
speakerAddresses   = {} # {'##.##.##.##' : speakerNumber}
speakerWDTs        = {} # {speakerNumber : LastAliveTime}
songFileIndexes    = {} # {speakerNumber : songFileIndex}
speakerEnables     = {} # {speakerNumber : isEnabled}
speakerEnumeration = {} # {'##.##.##.##' : speakerEnumeration}
speakerVolume      = 1  # 0.0 - 1.0
isSendingSong = False
songToSend = ' '
songFileIndex = 44

# starting values of global variables
def init():
    speakerWDTs        = {}
    aliveSpeakers      = {}
    speakerAddresses   = {}
    songFileIndexes    = {}
    speakerEnables     = {}
    speakerEnumeration = {}
    speakerVolume      = 0.0 
    isSendingSong = False
    songToSend = ' '
    songFileIndex = 44