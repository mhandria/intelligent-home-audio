#!/usr/bin/env python3
# Thread that handles sending song chunks to
# the speaker embeded systems

import socket
import sharedMem
import os
import time

# constants
SONG_CHUNK_SIZE = 1400
UDP_lastPercentage = 0

def sendSongChunk(client_addr):
    global SONG_CHUNK_SIZE
    global UDP_lastPercentage
    global isSendingSong
    global songFileIndex

    # get a copy of the current song index
    UDP_songFileIndex = sharedMem.songFileIndex
    
    if(sharedMem.isSendingSong == True):
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
            s = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
            s.sendto(songChunk,(client_addr,14124))

            # update the index to point to the next chunk
            UDP_songFileIndex = UDP_songFileIndex + SONG_CHUNK_SIZE
            sharedMem.songFileIndex = UDP_songFileIndex

        except Exception as e:
            print('Speaker - ERROR: Sending Song Chunk #{1}'.format(round((UDP_songFileIndex/SONG_CHUNK_SIZE))))
            print(e)
        #endexcept

        songFile.close()
    #endif
#end sendSongChunk

def Speaker_UDP_Client(UDP_listen_sock):
    # Initialize Memory
    global SONG_CHUNK_SIZE
    global UDP_lastPercentage

    # global variables #
    global aliveSpeakers
    global speakerAddresses
    global speakerWDTs
    
    SONG_CHUNK_SIZE = 1400

    # Send song local static variables
    UDP_lastPercentage = 0

    # address that the UDP packet was recieved from
    client_addr = -1

    # enter handle loop
    print('Speaker UDP Thread Started')
    while(True):
        try:
            # Check for any buffered UDP input #
            try:
                data, client_addr = UDP_listen_sock.recvfrom(1)
                client_addr = client_addr[0]

                # get the speaker number from address #
                client_spkn = sharedMem.speakerAddresses[client_addr]
            except BlockingIOError:
                # catch the exception thrown when there is no data in the buffer
                data = ' '.encode('UTF-8')
            #endexcept

            # Parse the input and execute the appropriate action #

            if(data.decode() == 's'):
                sendSongChunk(client_addr)
                sharedMem.speakerWDTs[client_addr] = time.time() #reset the WDT
            elif(data.decode() == 'h'):
                sharedMem.speakerWDTs[client_spkn] = time.time() #reset the WDT
            #endelse

            # Check if the any of the clients timed out #
            if(bool(sharedMem.speakerWDTs)): # if the dict isn't empty
                for k in list(sharedMem.aliveSpeakers):
                    if(time.time() - sharedMem.speakerWDTs[k] > 5):
                        sharedMem.aliveSpeakers.update({k:False})
                        print('Speaker - UDP Client #{0} timed out.'.format(k))
                    #endif
                #endfor
            #endif

            data = ' ' # clear the data so it is not parsed multiple times

        except Exception as e:
            print('Speaker - ERROR: UDP Thread')
            print(e)
        #endexcept
    #endwhile

    udp_socket.close()
# end Speaker_UDP_Client