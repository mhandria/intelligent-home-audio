#!/usr/bin/env python3
# Thread that listens for commands from the speaker embeded system
# Currently only supports 1 speaker

import socket
from threading import Thread 
from socketserver import ThreadingMixIn
import sharedMem
import os

# constants
SONG_CHUNK_SIZE = 2048
SPKN = 0
CLIENT = 0

# global variables
songFileIndex = 44 #this might be off by one

def returnMessage(payload):
    global SPKN
    global CLIENT
    print('Speaker - Client #{0} Response: {1}'.format(SPKN,payload))
    CLIENT.send(payload.encode('utf-8'))
#end returnMessage

def sendSongChunk():
    global isSendingSong
    global songToSend
    global SPKN
    global CLIENT
    global songFileIndex

    while(sharedMem.isSendingSong == False): #hang until a song needs to be sent
        songFileIndex = 44 # reset the index for the next song file
    #endwhile

    try:
        # open the file
        if(os.name == 'nt'): #if windows
            songFile = open(os.getcwd() + '/temp/' + sharedMem.songToSend, 'rb')
        else: #else it's debian
            songFile = open('/home/linaro/Desktop/temp/' + sharedMem.songToSend, 'rb')
        
        # get the bytes remaining in the file
        songFile.seek(0, 2)
        fileSize = songFile.tell()
        bytesRemaining = fileSize - songFileIndex

        if(bytesRemaining <= 0):
            # if we are at the end of the file,
            # the first speaker thread to reach end of file
            # stops the file sending
            sharedMem.isSendingSong = False
            returnMessage('f') # return 'f' which means to chill out for a little
            print('song ended')
            return
        elif(bytesRemaining < SONG_CHUNK_SIZE):
            # if there is less than 1 cunk left
            # the index is the bytes remaining

            songChunkSize = bytesRemaining

            # the first speaker thread to reach end of file
            # stops the file sending
            sharedMem.isSendingSong = False
            print('Song ended on chunk #{0}'.format(round((songFileIndex/SONG_CHUNK_SIZE - 1))))
        else: # if there are more than 1 chunk left
            # the index is the normal chunk size
            songChunkSize = SONG_CHUNK_SIZE
        #endelse

        # seek to the chunk to send
        songFile.seek(songFileIndex,0)

        # get the song chunk
        songChunk = songFile.read(songChunkSize)

        # send the chunk
        CLIENT.send(songChunk)

        # update the index to point to the next chunk
        songFileIndex = songFileIndex + SONG_CHUNK_SIZE

        print('Speaker - Client #{0} Sent Song Chunk #{1}'.format(SPKN,round((songFileIndex/SONG_CHUNK_SIZE - 1))))
    except Exception as e:
        print('Speaker - Client #{0} ERROR: Sending Song Chunk #{1}'.format(SPKN,round((songFileIndex/SONG_CHUNK_SIZE - 1))))
        print(e)
        payload = 'na'
        CLIENT.send(payload.encode('utf-8'))
    songFile.close()
    #endexcept
#end sendSongChunk

def Speaker_Client(client, speaker_number, addr, BUFFER_SIZE):
    print('')
    print("Speaker - Client #{0} connected from {1}...".format(speaker_number,addr))

    # initialize variables for this file
    global SPKN
    global CLIENT
    global songFileIndex

    CLIENT = client
    SPKN = speaker_number
    songFileIndex = 44 #this might be off by one

    #enter loop for handling client
    try:
        while True:
            #get phone payload data
            data = client.recv(BUFFER_SIZE)
            data = data.decode('utf-8')
            data = data.rstrip()
            print('Speaker - Client #{0} Payload:  {1}'.format(speaker_number,data))
            
            #interpret data and set return payload
            if(data == 'stat'):
                returnMessage('y')
            elif(data == 'songData'):
                sendSongChunk()
            else:
                returnMessage('n')
            #endelse
            
            # repeat forever
        #endwhile
    except Exception as e:
        print(e)
        print('Speaker - Client #{0} disconnected'.format(speaker_number))
    #endexcept

    # While loop breaks out only if there is a connection error
    client.close()

#endSpeaker_Client