#!/usr/bin/env python3
# Thread for listening for mobile application commands
import socket
from threading import Thread
from socketserver import ThreadingMixIn
import sharedMem
import requests
import os
import platform
from mutagen.easyid3 import EasyID3 as ID3

# Constants #
BUFFER_SIZE = 256
NUL = '\x00'
EOT = '\x04'
ACK = '\x06'
NAK = '\x15'
GS  = '\x1D'
US  = '\x1F'

# global variables #
PHONE_isSongPaused = False

# Functions #
def getExtIP():
    try:
        extIP = ACK + requests.get("https://api.ipify.org").text
    except Exception as e:
        print('Phone - ERROR: Failed to get public IP address.')
        extIP = NAK
    #endexcept
    return extIP
#end getExtIP

def playSong(fileName):
    global isSendingSong
    global songToSend
    global PHONE_isSongPaused

    # TODO: if the file already exists in temp, don't make another
    # TODO: if the number of files or filesize of /temp/ passes a
    #       threshold, then start deleting files as you make more
    try:
        PHONE_isSongPaused = False

        if(os.name == 'nt'): #if testing the server on windows
            libPath  = os.getcwd() + '/library/'
            convPath = os.getcwd() + '/convert.bat '
        else: # otherwise it's the linux server
            libPath  = '/home/linaro/Desktop/Source/'
            convPath = '/home/linaro/Desktop/Source/convert.sh '
        #endelse

        if(os.path.isfile(libPath + fileName)):
            #convert the file to mono 16-bit 44.1KHz .wav into /temp/
            print('Converting ' + fileName)
            os.system(convPath + '"' + fileName + '"')
            print('Converted')

            # set memory to initiate song sending
            sharedMem.isSendingSong = True
            sharedMem.songToSend = fileName + '.wav'
            sharedMem.songFileIndex = 44 # TODO: Change this when ogg vorbis is implemented

            # return sucessful message and continue listening for phone commands
            returnPayload = ACK+'Playing: ' + fileName

            for k in list(sharedMem.songFileIndexes):
                sharedMem.songFileIndexes[k] = 44
            #endfor

            # TODO: Make SongSync start here instead of server.py
            #       then the thread can die when isSendingSong == false.
            #       This function will have to clean up old SongSync threads
            #       when starting a new song

        else: #if the file name doesn't exist
            returnPayload = NAK+'File "' + fileName + '" does not exist'
        #endelse
    except Exception as e:
        print('Phone - ERROR: Failed to play "' + fileName + '"')
        returnPayload = "ERROR: Playing " + fileName
    #end except

    return returnPayload
#end playSong

def pauseSong():
    global isSendingSong
    global PHONE_isSongPaused

    try:
        if(sharedMem.isSendingSong and not PHONE_isSongPaused):
            sharedMem.isSendingSong = False
            PHONE_isSongPaused = True
            returnPayload = ACK
        else:
            returnPayload = NAK+'No song is playing'
        #endelse
    except Exception as e:
        print('Phone - ERROR: pauseSong')
        print(e)
        returnPayload = NAK
    #endexcept

    return returnPayload
#end pauseSong

def resumeSong():
    global isSendingSong
    global PHONE_isSongPaused

    try:
        if(not sharedMem.isSendingSong and PHONE_isSongPaused):
            sharedMem.isSendingSong = True
            PHONE_isSongPaused = False
            returnPayload = ACK
        else:
            returnPayload = NAK+'No song is paused'
        #endelse
    except Exception as e:
        print('Phone - ERROR: resumeSong')
        print(e)
        returnPayload = NAK
    #endexcept

    return returnPayload
#end resumeSong

def isSendingSong():
    global isSendingSong

    try:
        if(sharedMem.isSendingSong):
            returnPayload = ACK+'y'
        else:
            returnPayload = ACK+'n'
        #endelse
    except Exception as e:
        print('Phone - ERROR: isSendingSong')
        print(e)
        returnPayload = NAK
    #endexcept

    return returnPayload
#end isSendingSong

def getSpeakerList(phone_client_sock):
    try:
        returnPayload = NAK + 'Not yet implemented'
    except Exception as e:
        print('Phone - ERROR: getSpeakerList')
        returnPayload = NAK
    #endexcept

    return returnPayload
#end getSpeakerList

def getSongList(client):
    print('Phone - Sending song list...')

    if(os.name == 'nt'): # if windows
        filepath = os.getcwd() + '/library/'
    elif(platform.system() == 'Debian'):
        filepath = os.getcwd() + '/library'
    else: #otherwise debian server
        filepath = '/home/linaro/Desktop/library/'

    # get a list of all the filenames in the library
    files = [f for f in os.listdir(filepath) if os.path.isfile(os.path.join(filepath, f))]

    if not files: # if the library is empty return NAK
        payload = NAK
    else: # otherwise send all the information, one song per packet
        payload = ACK
        client.send(payload.encode('utf-8'))

        tags = ['title', 'artist', 'album']
        mf = None
        for f in files:
            try:
                # create ID3 tag reader
                mf = ID3(filepath + f)
                validID3 = True
            except:
                validID3 = False
            #endexcept

            if(validID3):
                # add filename to payload
                payload = f

                for t in tags:
                    payload = payload + US # mark new attribute by unit seperator

                    try:
                        # add tag to payload if it exists
                        payload = payload + mf[t][0]
                    except:
                        # otherwise it's <NUL>
                        payload = payload + NUL
                    #endexcept
                #endfor

            else:
                payload = f
                for t in tags: payload = payload + NUL + US
            #endelse

            # group seperator to show end of current song
            payload = payload + GS

            #send the payload, move to next song
            print(payload)
            client.send(payload.encode('utf-8'))
        #endfor

        returnPayload = EOT
    #endelse
    return returnPayload
#end getSongList

# Main #
def Phone_Client(ADDR):
    print('')
    print('Phone - Thread started')

    while True:
        #create socket
        try:
            server_sock = socket.socket(family=socket.AF_INET, type=socket.SOCK_STREAM)
            server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            server_sock.bind(ADDR)
            server_sock.listen(5)
        except:
            print('Phone - ERROR: while creating socket')

        #listen until phone opens socket to server
        print(' ')
        print('Phone - Waiting for TCP phone client...')

        try:
            phone_client_sock, addr = server_sock.accept()
        except Exception as e:
            print(e)
        #endexcept

        print("Phone - Connected from {0}...".format(addr))

        try:
            #get phone payload data
            data = phone_client_sock.recv(BUFFER_SIZE)
            data = data.decode('utf-8')
            data = data.rstrip()

            print('Phone - Command:  {0}'.format(data))

            # interpret command and set return payload
            if(data == 'stat'):
                payload = ACK
            elif(data == 'getExtIP'):
                payload = getExtIP()
            elif(data.startswith('play ')):
                songName = data.split(' ',1)[1] #parse fileName
                payload  = playSong(songName)
            elif(data == 'pause'):
                payload = pauseSong()
            elif(data == 'resume'):
                payload = resumeSong()
            elif(data == 'isSongPlaying'):
                payload = isSendingSong()
            elif(data == 'getSpeakerList'):
                payload = getSpeakerList(phone_client_sock)
            elif(data == 'getSongList'):
                payload = getSongList(phone_client_sock)
            else:
                payload = NAK+'Invalid Command'
            #endelse

            print('Phone - Response: {0}'.format(payload))

            #send return message
            phone_client_sock.send(payload.encode('utf-8'))
        except Exception as e:
            print('Phone - ERROR: Unexpectedly disconnected. Trying again...')
            print(e)

        phone_client_sock.close() # close the socket and do it again
        # repeat forever
    #endwhile
#endPhone_Client