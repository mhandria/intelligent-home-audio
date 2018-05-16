#!/usr/bin/env python3
import time

#file storing global variable names to be used by different threads
aliveSpeakers      = {} # {speakerNumber : isAlive}
speakerAddresses   = {} # {'##.##.##.##' : speakerNumber}
speakerWDTs        = {} # {speakerNumber : LastAliveTime}
songFileIndexes    = {} # {speakerNumber : songFileIndex}
speakerEnables     = {} # {speakerNumber : isEnabled}
speakerEnumeration = {} # {speakerEnumeration : '##.##.##.##'}
speakerVolume      = 1  # 0.0 - 1.0
isSendingSong = False
songToSend = bytes()
currentSongName = ''
songSize = 0
songFileIndex = 44
timeOfLastPause = 0

# starting values of global variables
def init():
    speakerWDTs        = {}
    aliveSpeakers      = {}
    speakerAddresses   = {}
    songFileIndexes    = {}
    # songFileIndexes.update({-1:44})
    speakerEnables     = {}
    speakerEnumeration = {}
    speakerVolume      = 0.0 
    isSendingSong = False
    currentSongName = ''
    songToSend = bytes()
    songSize = 0
    songFileIndex = 44
    timeOfLastPause = time.time()
#end init

def syncIndexes():
    # Also, sync the songs while you're waiting #
    # get the value of the furthest most song position #
    maxPos = 0
    for k in list(songFileIndexes):
        if(songFileIndexes[k] > maxPos):
            maxPos = songFileIndexes[k]
        #endif
    #endfor

    # update all song positions to maxPos #
    for k in list(songFileIndexes):
        songFileIndexes[k] = maxPos
    #endfor
#end syncIndexes