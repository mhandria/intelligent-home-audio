#!/usr/bin/env python3
# Thread that listens for commands from the speaker embeded system
# Currently only supports 1 speaker

import socket
from threading import Thread 
from socketserver import ThreadingMixIn
import sharedMem

def Speaker_Client(speaker_number, ADDR, BUFFER_SIZE):
    global LED0
    global LED1

    print('')
    print('Speaker - Thread #{0} started'.format(speaker_number))

    while True:
        try:
            #create socket
            server_sock = socket.socket(family=socket.AF_INET, type=socket.SOCK_STREAM)
            server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            # try SO_REUSEPORT
            server_sock.bind(ADDR)
            server_sock.listen(5)
        except:
            print('Speaker - ERROR: While creating socket #{0}'.format(speaker_number))

        #listen until speaker opens socket to server
        print('Speaker - Waiting for connection...')
        speaker_client_sock, addr = server_sock.accept()
        print("Speaker - Client connected from {0}...".format(addr))

        #when a speaker connects, open a new thread to listen for the next speaker
        try:
            newThreadNumber = speaker_number + 1
            t = Thread(target=Speaker_Client, args=(newThreadNumber, ADDR, BUFFER_SIZE))
            # t.start()
        except:
            print('Speaker - ERROR: Something went wrong creating a new speakerThread')
            while True:
                a = 0

        try:
            while True:
                #get phone payload data
                data = speaker_client_sock.recv(BUFFER_SIZE)
                data = data.decode('utf-8')
                data = data.rstrip()
                print('Speaker - Client #{0} Payload:  {1}'.format(speaker_number,data))

                #interpret data and set return payload
                #sharedMem.LED0 
                if(data == 'stat'):
                    payload = 'y'
                else:
                    payload = 'n'
                #endelse

                print('Speaker - Client #{0} Response: {1}'.format(speaker_number,payload))
                #send return message and close the socket
                speaker_client_sock.send(payload.encode('utf-8'))
                # repeat forever
            #endwhile
        except:
            print('Speaker - Client #{0} disconnected'.format(speaker_number))
    speaker_client_sock.close()
    #endwhile
#endSpeaker_Client
