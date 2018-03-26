#!/usr/bin/env python3
# Thread that handles sending song chunks to
# the speaker embeded systems

import socket
import sharedMem
import os

# constants
SONG_CHUNK_SIZE = 1400
SPKN = 0
songFileIndex = 0
UDP_SOCKET = 0
LAST_PERCENTAGE = 0

def sendSongChunk():
    global isSendingSong
    global songToSend
    global songFileIndex
    global SPKN
    global LAST_PERCENTAGE

    while(sharedMem.isSendingSong == False and sharedMem.speakersConnected[SPKN] == 1): #hang until a song needs to be sent
        songFileIndex   = 44 # reset the index for the next song file
        LAST_PERCENTAGE = 0
    #endwhile

    # if the TCP thread detected a disconnect, procede
    # to exit this thread to free the UDP port as well
    # otherwise send a chunk
    if(sharedMem.speakersConnected[SPKN] == 1):
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
            donePercent = (songFileIndex/fileSize) * 100

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

            # get the song chunk
            songFile.seek(songFileIndex,0) # seek to the chunk to send
            songChunk = songFile.read(songChunkSize)

            # send the chunk
            UDP_SOCKET.sendto(songChunk,(ADDR, 14124))

            # update the index to point to the next chunk
            songFileIndex = songFileIndex + SONG_CHUNK_SIZE

            # print('Speaker - Client #{0} Sent Song Chunk #{1}'.format(SPKN,round((songFileIndex/SONG_CHUNK_SIZE - 1))))
            if(donePercent - LAST_PERCENTAGE > 1):
                print('Song data {0}% sent'.format(round(donePercent)))
                LAST_PERCENTAGE = donePercent
            #endif
        except Exception as e:
            print('Speaker - Client #{0} ERROR: Sending Song Chunk #{1}'.format(SPKN,round((songFileIndex/SONG_CHUNK_SIZE))))
            print(e)
        songFile.close()
        #endexcept
    #endif
#end sendSongChunk

def Speaker_UDP_Client(udp_socket, spkn, addr):
    # Initialize Memory
    global ADDR
    global SPKN
    global songFileIndex
    global UDP_SOCKET

    ADDR = addr[0]
    SPKN = spkn
    songFileIndex = 44
    UDP_SOCKET = udp_socket

    print("Speaker - UDP Socket #{0} connected for {1}...".format(SPKN,addr[0]))
    while(sharedMem.speakersConnected[SPKN] == 1):
        try:
            # if a packet is recieved from the address this thread is responsible for, and it is the special character 's'
            # then send the next chunk in the song sequence back to the speaker embeded system
            
            data, packet_address = UDP_SOCKET.recvfrom(1)

            if(packet_address[0] == addr[0] and data.decode() == 's'):
                sendSongChunk()
            elif(data.decode() == 'h'):
                # TODO: Implement a heartbeat here so that the 
                # connection can be severed if it takes too long
                placeholder = 'blah'
            #endelif

        except Exception as e:
            print("Speaker - ERROR: UDP Client #{0}, ".format(SPKN))
            print(e)
        #endexcept
    #endwhile

    UDP_SOCKET.close()
    sharedMem.speakersConnected.pop(SPKN)
    print("Speaker - UDP Client #{0} thread closed.".format(SPKN))
# end Speaker_UDP_Client