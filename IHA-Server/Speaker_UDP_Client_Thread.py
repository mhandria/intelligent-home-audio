#!/usr/bin/env python3
# Thread that handles sending song chunks to
# the speaker embeded systems

import socket
import sharedMem
import os

# constants
SONG_CHUNK_SIZE = 1400
UDP_songFileIndex = 0
UDP_SOCKET = 0
UDP_lastPercentage = 0
UDP_SPKN = -1
UDP_ADDR = 0

def sendSongChunk():
    global isSendingSong
    global songToSend

    global SONG_CHUNK_SIZE
    global UDP_songFileIndex
    global UDP_SOCKET
    global UDP_lastPercentage
    global UDP_SPKN
    global UDP_ADDR

    while(sharedMem.isSendingSong == False and sharedMem.speakersConnected[UDP_SPKN] == 1): #hang until a song needs to be sent
        UDP_songFileIndex   = 44 # reset the index for the next song file
        UDP_lastPercentage = 0
    #endwhile

    # if the TCP thread detected a disconnect, procede
    # to exit this thread to free the UDP port as well
    # otherwise send a chunk
    if(sharedMem.speakersConnected[UDP_SPKN] == 1):
        try:
            # open the file
            if(os.name == 'nt'): #if windows
                songFile = open(os.getcwd() + '/temp/' + sharedMem.songToSend, 'rb')
            else: #else it's debian
                songFile = open('/home/linaro/Desktop/temp/' + sharedMem.songToSend, 'rb')
            
            # get the bytes remaining in the file
            songFile.seek(0, 2)
            fileSize = songFile.tell()
            bytesRemaining = fileSize - UDP_songFileIndex
            donePercent = (UDP_songFileIndex/fileSize) * 100

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
                print('Song ended on chunk #{0}'.format(round((UDP_songFileIndex/SONG_CHUNK_SIZE - 1))))
            else: # if there are more than 1 chunk left
                # the index is the normal chunk size
                songChunkSize = SONG_CHUNK_SIZE
            #endelse

            # get the song chunk
            songFile.seek(UDP_songFileIndex,0) # seek to the chunk to send
            songChunk = songFile.read(songChunkSize)

            # send the chunk
            UDP_SOCKET.sendto(songChunk,(UDP_ADDR, 14124))

            # update the index to point to the next chunk
            UDP_songFileIndex = UDP_songFileIndex + SONG_CHUNK_SIZE

            # print('Speaker - Client #{0} Sent Song Chunk #{1}'.format(spkn,round((songFileIndex/SONG_CHUNK_SIZE - 1))))
            if(donePercent - UDP_lastPercentage > 1):
                print('Song data {0}% sent'.format(round(donePercent)))
                UDP_lastPercentage = donePercent
            #endif
        except Exception as e:
            print('Speaker - Client #{0} ERROR: Sending Song Chunk #{1}'.format(UDP_SPKN,round((UDP_songFileIndex/SONG_CHUNK_SIZE))))
            print(e)
        songFile.close()
        #endexcept
    #endif
#end sendSongChunk

def Speaker_UDP_Client(udp_socket, spkn, addr):
    # Initialize Memory
    global SONG_CHUNK_SIZE
    global UDP_songFileIndex
    global UDP_SOCKET
    global UDP_lastPercentage
    global UDP_SPKN
    global UDP_ADDR

    SONG_CHUNK_SIZE = 1400
    UDP_songFileIndex = 44
    UDP_SOCKET = udp_socket
    UDP_lastPercentage = 0
    UDP_SPKN = spkn
    UDP_ADDR = addr[0]

    print("Speaker - UDP Socket #{0} connected for {1}...".format(spkn,addr))
    while(sharedMem.speakersConnected[spkn] == 1):
        try:
            # if a packet is recieved from the address this thread is responsible for, and it is the special character 's'
            # then send the next chunk in the song sequence back to the speaker embeded system
            
            data, packet_address = udp_socket.recvfrom(1)

            if(packet_address[0] == UDP_ADDR and data.decode() == 's'):
                sendSongChunk()
            elif(data.decode() == 'h'):
                # TODO: Implement a heartbeat here so that the 
                # connection can be severed if it takes too long
                placeholder = 'blah'
            #endelif

        except Exception as e:
            print("Speaker - ERROR: UDP Client #{0}, ".format(spkn))
            print(e)
        #endexcept
    #endwhile

    udp_socket.close()
    sharedMem.speakersConnected.pop(spkn)
    print("Speaker - UDP Client #{0} thread closed.".format(spkn))
# end Speaker_UDP_Client