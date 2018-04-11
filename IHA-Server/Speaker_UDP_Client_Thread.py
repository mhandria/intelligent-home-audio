#!/usr/bin/env python3
# Thread that handles sending song chunks to
# the speaker embeded systems

import socket
import sharedMem
import os
import sys
import platform
import time

# constants
SONG_CHUNK_SIZE = 1400
UDP_lastPercentage = 0

def sendSongChunk(client_spkn, client_addr):
    global SONG_CHUNK_SIZE
    global UDP_lastPercentage
    global isSendingSong
    global speakerEnables
    global UDP_sendSocket
    
    try:
        # get a copy of the current song index for this speaker
        UDP_songFileIndex = sharedMem.songFileIndexes[client_spkn]

        if(sharedMem.isSendingSong == True and sharedMem.speakerEnables[client_spkn]):
        # get the bytes remaining in the file
            bytesRemaining = sharedMem.songSize - UDP_songFileIndex
            if(bytesRemaining <= 0):
                # if we are at the end of the file,
                # the first speaker thread to reach end of file
                # stops the file sending
                sharedMem.isSendingSong = False
                print('song ended')
                return
            elif(bytesRemaining < SONG_CHUNK_SIZE):
                # if there is less than 1 chunk left
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

            # get the chunk and apply volume modifier #
            songChunkAdjusted = bytes()
            for i in range(UDP_songFileIndex,UDP_songFileIndex + songChunkSize):
                songChunkAdjusted += bytes([int(((float(sharedMem.songToSend[i]) - 128)*sharedMem.speakerVolume) + 128)])
            # endfor

            # send the chunk
            UDP_sendSocket.sendto(songChunkAdjusted,(client_addr,14124))

            # update the index to point to the next chunk
            UDP_songFileIndex = UDP_songFileIndex + SONG_CHUNK_SIZE
            sharedMem.songFileIndexes.update({client_spkn : UDP_songFileIndex})
        #endif
    except Exception as e:
        print('Speaker - ERROR: Sending Song Chunk #{0}'.format(round((UDP_songFileIndex/SONG_CHUNK_SIZE))))
        print('Error on line {}'.format(sys.exc_info()[-1].tb_lineno))
        print(type(e).__name__)
        print(e)
    #endexcept
#end sendSongChunk

def Speaker_UDP_Client(UDP_listen_sock):
    # Initialize Memory
    global SONG_CHUNK_SIZE
    global UDP_lastPercentage

    # global variables #
    global aliveSpeakers
    global speakerAddresses
    global speakerWDTs
    global UDP_sendSocket

    SONG_CHUNK_SIZE = 1400

    # Send song local static variables
    UDP_lastPercentage = 0

    # address that the UDP packet was recieved from
    client_addr = -1

    # used to send song data
    UDP_sendSocket = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)

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
            except KeyError:
                # catch the exception thrown when the TCP thread for the corresponding
                # speaker has not yet been initalized
                data = ' '.encode('UTF-8')
                print('Packet recieved from non-registered address')
                
                client_spkn = -1
                
                # assign address to a bad speaker number #
                sharedMem.speakerAddresses.update({client_addr : client_spkn})
                sharedMem.speakerWDTs.update(     {client_spkn : time.time() + 5})

                time.sleep(1)
            #endexcept

            # Parse the input and execute the appropriate action #

            if(data.decode() == 's' and client_spkn != -1):
                sendSongChunk(client_spkn, client_addr)
                sharedMem.speakerWDTs[client_spkn] = time.time() #reset the WDT
            elif(data.decode() == 'h'):
                sharedMem.speakerWDTs[client_spkn] = time.time() #reset the WDT
            #endelse

            # Check if the any of the clients timed out #
            try:
                if(bool(sharedMem.speakerWDTs)): # if the dict isn't empty
                    for k in list(sharedMem.aliveSpeakers):
                        if(time.time() - sharedMem.speakerWDTs[k] > 5):
                            sharedMem.aliveSpeakers.update({k:False})
                            print('Speaker - UDP Client #{0} timed out.'.format(k))
                        #endif
                    #endfor
                #endif
            except Exception as e:
                print('Key Error checking WDTs:')
                print(type(e).__name__)
                print(e)
            #endecept

            data = ' ' # clear the data so it is not parsed multiple times

        except Exception as e:
            print('Speaker - ERROR: UDP Thread')
            print('Error on line {}'.format(sys.exc_info()[-1].tb_lineno))
            print(type(e).__name__)
            print(e)
            time.sleep(5)
        #endexcept
    #endwhile

    udp_socket.close()
# end Speaker_UDP_Client
