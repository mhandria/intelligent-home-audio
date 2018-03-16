#!/usr/bin/env python3
# Thread for listening for mobile application commands 
import socket
from threading import Thread 
from socketserver import ThreadingMixIn
import sharedMem
import requests
import os

# Functions #
def getExtIP():
    try:
        extIP = requests.get("https://api.ipify.org").text
    except Exception as e:
        print('Phone - ERROR: Failed to get public IP address.')
        extIP = 'na'
    #endexcept
    return extIP
#end getExtIP

def playSong(fileName):
    global isSendingSong
    global songToSend

    # TODO: make this code less redundant
    try:
        if(os.name == 'nt'): #if testing the server on windows
            # check if the file exists
            if(os.path.isfile(os.getcwd() + '/library/' + fileName)):
                #convert the file to mono 16-bit 44.1KHz .wav into /temp/
                print('Converting ' + fileName)
                os.system('convert.bat ' + '"' + fileName + '"')
                print('Converted')

                # set memory to initiate song sending
                sharedMem.isSendingSong = True
                sharedMem.songToSend = fileName + '.wav'

                # return sucessful message and continue listening for phone commands
                returnPayload = 'Playing: ' + fileName
            else: #if the file name doesn't exist
                returnPayload = 'ERROR: File "' + fileName + '" does not exist'
            #endelse
        else: # otherwise it's the linux server
            if(os.path.isfile('/home/linaro/Desktop/library/' + fileName)):
                #convert the file to mono 16-bit 44.1KHz .wav into /temp/
                print('Converting ' + fileName)
                os.system('/home/linaro/Desktop/library/convert.sh ' + '"' + fileName + '"')
                print('Converted')

                # set memory to initiate song sending
                sharedMem.isSendingSong = True
                sharedMem.songToSend = fileName + '.wav'

                # return sucessful message and continue listening for phone commands
                returnPayload = 'Playing: ' + fileName
                returnPayload = 'Playing: ' + fileName
            else: #if the file name doesn't exist
                returnPayload = 'ERROR: File "' + fileName + '" does not exist'
        #endelse
    except Exception as e:
        print('Phone - ERROR: Failed to play "' + fileName + '"')
        returnPayload = "ERROR: Playing " + fileName
    #end except
    return returnPayload
#end playSong

def stopSong():
    if(sharedMem.isSendingSong):
        sharedMem.isSendingSong = False
        returnPayload = 'OK'
    else:
        returnPayload = 'No song is playing'
    return returnPayload
#end stopSong

# Main #
def Phone_Client(ADDR, BUFFER_SIZE):
    global LED0
    global LED1

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
        print('Phone - Waiting for phone connection...')
        
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
                payload = 'OK'
            elif(data == 'getExtIP'):
                payload = getExtIP()
            elif(data.startswith('play ')):
                songName = data.split(' ',1)[1] #parse fileName
                payload  = playSong(songName)
            elif(data == 'stop'):
                payload = stopSong()
            else:
                payload = 'Invalid Command'
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