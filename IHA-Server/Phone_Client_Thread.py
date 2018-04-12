#!/usr/bin/env python3
# Thread for listening for mobile application commands
import socket
from threading import Thread
from socketserver import ThreadingMixIn
import sharedMem
import requests
import os
import sys
import platform
from mutagen.easyid3 import EasyID3 as ID3
import numpy as np
import time

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
    global songSize
    global PHONE_isSongPaused
    global currentSongName

    # TODO: if the number of files or filesize of /temp/ passes a
    #       threshold, then start deleting files as you make more
    try:
        PHONE_isSongPaused = False

        if(os.name == 'nt'): #if testing the server on windows
            libPath  = os.getcwd() + '/library/'
            tempPath = os.getcwd() + '/temp/'
            convPath = os.getcwd() + '/convert.bat '
        else: # otherwise it's the linux server
            libPath  = '/home/linaro/Desktop/library/'
            tempPath = '/home/linaro/Desktop/temp/'
            convPath = '/home/linaro/Desktop/Source/convert.sh '
        #endelse

        if(os.path.isfile(libPath + fileName)):
            if(os.path.isfile(tempPath + fileName + '.wav')):
                print('Song already converted.')
            else:
                #convert the file to mono 16-bit 44.1KHz .wav into /temp/
                print('Converting ' + fileName)
                os.system(convPath + '"' + fileName + '"')
                print('Converted')
            #endelse

            # set memory to initiate song sending
            sharedMem.isSendingSong = True
            for k in list(sharedMem.songFileIndexes):
                sharedMem.songFileIndexes[k] = 44
            #endfor
            
            # get the song data
            songFile = open(tempPath + fileName + '.wav', 'rb')
            songFile.seek(0, 2)
            sharedMem.songSize = songFile.tell()

            songFile.seek(0,0) # seek to the beggining of the file
            # read the whole file
            
            sharedMem.songToSend = np.fromfile(songFile, dtype=np.uint8)
            
            # return sucessful message and continue listening for phone commands
            returnPayload = ACK+'Playing: ' + fileName

            for k in list(sharedMem.songFileIndexes):
                sharedMem.songFileIndexes[k] = 44
            #endfor

            # set the songName for getCurrentSong#
            sharedMem.currentSongName = fileName
            pauseSong()
            time.sleep(1.5)
            resumeSong()
        else: #if the file name doesn't exist
            returnPayload = NAK+'File "' + fileName + '" does not exist'
        #endelse
    except Exception as e:
        print('Phone - ERROR: Failed to play "' + fileName + '"')
        print('Error on line {}'.format(sys.exc_info()[-1].tb_lineno))
        print(e)
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

def getCurrentSong():
    try:
        if(sharedMem.isSendingSong or PHONE_isSongPaused):
            # if sending song, or song is paused, return that song's filename
            returnPayload = ACK + sharedMem.currentSongName
        else:
            # otherwise there isn't a song playing currently
            returnPayload = NAK + 'No song is playing'
        #endelse
    except Exception as e:
        print('Phone - ERROR: getCurrentSong')
        print(e)
        returnPayload = NAK
    #endexcept

    return returnPayload
#end getCurrentSong

def getSpeakerList(client):
    try:
        if(not sharedMem.speakerEnumeration): # if empty
            returnPayload = NAK  + 'No speakers yet'
        else:
            payload = ACK
            client.send(payload.encode('utf-8'))

            for n in list(sharedMem.speakerEnumeration.keys()):
                spkr_addr   = sharedMem.speakerEnumeration[n]
                spkr_number = sharedMem.speakerAddresses[spkr_addr]

                if spkr_number in sharedMem.aliveSpeakers.keys():
                    isConnected = 'y'
                else:
                    isConnected = 'n'
                #endelse

                if(sharedMem.speakerEnables[spkr_number]):
                    isEnabled = 'y'
                else:
                    isEnabled = 'n'
                #endelse
                
                payload = str(n) + ':' + isConnected + ':' + isEnabled +  ';'
                print(payload)
                client.send(payload.encode('utf-8'))
            #endfor

            returnPayload = EOT
        #endelse
    except Exception as e:
        print('Phone - ERROR: getSpeakerList')
        print(e)
        returnPayload = NAK
    #endexcept

    return returnPayload
#end getSpeakerList

def enableSpeaker(spkr_enum):
    global speakerEnables
    global speakerEnumeration
    global speakerAddresses

    try:
        if spkr_enum in list(sharedMem.speakerEnumeration.keys()):
            spkr_addr = sharedMem.speakerEnumeration[spkr_enum]
            spkr_num = sharedMem.speakerAddresses[spkr_addr]
            sharedMem.speakerEnables.update({spkr_num : True})

            # when a speakers is connected, clear the buffers so that they are synced #
            pauseSong()
            time.sleep(2)
            resumeSong()

            returnPayload = ACK
        else:
            returnPayload = NAK + 'Invalid speaker enumerator: "' + str(spkr_enum) + '"'
        #endelse

    except Exception as e:
        print('Phone - ERROR: enableSpeaker')
        print('Error on line {}'.format(sys.exc_info()[-1].tb_lineno))
        print(e)
        returnPayload = NAK
    #endexcept

    return returnPayload
#end enableSpeaker

def disableSpeaker(spkr_enum):
    global speakerEnables
    global speakerEnumeration
    global speakerAddresses
    
    try:
        if spkr_enum in list(sharedMem.speakerEnumeration.keys()):
            spkr_addr = sharedMem.speakerEnumeration[spkr_enum]
            spkr_num = sharedMem.speakerAddresses[spkr_addr]
            sharedMem.speakerEnables.update({spkr_num : False})
            returnPayload = ACK
        else:
            returnPayload = NAK + 'Invalid speaker enumerator: "' + str(spkr_enum) + '"'
        #endelse

    except Exception as e:
        print('Phone - ERROR: disableSpeaker')
        print('Error on line {}'.format(sys.exc_info()[-1].tb_lineno))
        print(e)
        returnPayload = NAK
    #endexcept

    return returnPayload
#end enableSpeaker

def getSongList(client):
    print('Phone - Sending song list...')

    try:
        if(os.name == 'nt'): # if windows
            filepath = os.getcwd() + '/library/'
        elif(platform.system() == 'Darwin'):
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
                # print(payload.decode())
                client.send(payload.encode('utf-8'))
            #endfor

            returnPayload = EOT
        #endelse
    except Exception as e:
            print('Phone - Error getSongList')
            print('Error on line {}'.format(sys.exc_info()[-1].tb_lineno))
            print(type(e).__name__)
            print(e)
            returnPayload = NAK
    #endexcept
    return returnPayload
#end getSongList

def getSpeakerVolume():
    try:
        returnPayload = ACK + str(round(sharedMem.speakerVolume * 100))
    except Exception as e:
        print('Phone - ERROR: getSpeakerVolume')
        print(e)
        returnPayload = NAK
    #endexcept

    return returnPayload
#end getSpeakerVolume

def setSpeakerVolume(volume):
    global speakerVolume

    try:
        volume = int(volume)
        if(volume >= 0 and volume <= 100):
            sharedMem.speakerVolume = volume/100.0
            returnPayload = ACK
        else:
            returnPayload = NAK + 'Invalid volume'
        #endelse
    except ValueError:
        returnPayload = NAK + 'Invalid volume'
    except Exception as e:
        print('Phone - ERROR: setSpeakerVolume')
        print(e)
        returnPayload = NAK
    #endexcept

    return returnPayload
#end getSpeakerVolume

def incVolume():
    global speakerVolume
    try:
        newVolume = sharedMem.speakerVolume + 0.05
        if(newVolume >= 0 and newVolume <= 1):
            sharedMem.speakerVolume = newVolume
        #endif

        print('New volume = {0}%'.format(round(sharedMem.speakerVolume*100)))

        returnPayload = ACK
    except Exception as e:
        print('Phone - ERROR: incVolume')
        print(e)
        returnPayload = NAK
    #endexcept
    return returnPayload
#end incVolume

def decVolume():
    global speakerVolume
    try:
        newVolume = sharedMem.speakerVolume - 0.05
        if(newVolume >= 0 and newVolume <= 1):
            sharedMem.speakerVolume = newVolume
        #endif

        print('New volume = {0}%'.format(round(sharedMem.speakerVolume*100)))

        returnPayload = ACK
    except Exception as e:
        print('Phone - ERROR: deccVolume')
        print(e)
        returnPayload = NAK
    #endexcept
    return returnPayload
#end incVolume

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
                songName = data.split(' ',1)[1] #parse <fileName>
                payload  = playSong(songName)
            elif(data == 'pause'):
                payload = pauseSong()
            elif(data == 'resume'):
                payload = resumeSong()
            elif(data == 'isSongPlaying'):
                payload = isSendingSong()
            elif(data == 'getCurrentSong'):
                payload = getCurrentSong()
            elif(data == 'getSpeakerList'):
                payload = getSpeakerList(phone_client_sock)
            elif(data.startswith('enableSpeaker ')):
                speakerEnum = data.split(' ',1)[1] #parse <speakerNumber>
                speakerEnum =  int(speakerEnum)
                payload = enableSpeaker(speakerEnum)
            elif(data.startswith('disableSpeaker ')):
                speakerEnum = data.split(' ',1)[1] #parse <speakerNumber>
                speakerEnum =  int(speakerEnum)
                payload = disableSpeaker(speakerEnum)
            elif(data == 'getSongList'):
                payload = getSongList(phone_client_sock)
            elif(data == 'getSpeakerVolume'):
                payload = getSpeakerVolume()
            elif(data.startswith('setSpeakerVolume ')):
                volume = data.split(' ',1)[1] # parse <volume>
                payload = setSpeakerVolume(volume)
            elif(data == 'incVolume'):
                payload = incVolume()
            elif(data == 'decVolume'):
                payload = decVolume()
            else:
                payload = NAK+'Invalid Command: "' + data + '"'
            #endelse

            print('Phone - Response: {0}'.format(payload))

            #send return message
            phone_client_sock.send(payload.encode('utf-8'))
        except ValueError:
            print('Phone - ERROR: ValueError')
            print('Error on line {}'.format(sys.exc_info()[-1].tb_lineno))
            phone_client_sock.send(NAK.encode('utf-8'))
        except Exception as e:
            print('Phone - ERROR: Unexpectedly disconnected. Trying again...')
            print('Error on line {}'.format(sys.exc_info()[-1].tb_lineno))
            print(type(e).__name__)
            print(e)

        phone_client_sock.close() # close the socket and do it again
        # repeat forever
    #endwhile
#endPhone_Client
