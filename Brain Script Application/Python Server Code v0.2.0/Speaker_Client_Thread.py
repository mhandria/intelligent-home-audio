#!/usr/bin/env python3
# Thread that listens for commands from the speaker embeded system
# Currently only supports 1 speaker

import socket
from threading import Thread 
from socketserver import ThreadingMixIn
import sharedMem

def Speaker_Client(client, speaker_number, addr, BUFFER_SIZE):
    global LED0
    global LED1

    print('')
    print("Speaker - Client #{0} connected from {1}...".format(speaker_number,addr))

    try:
        while True:
            #get phone payload data
            data = client.recv(BUFFER_SIZE)
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
            client.send(payload.encode('utf-8'))
            # repeat forever
        #endwhile
    except:
        print('Speaker - Client #{0} disconnected'.format(speaker_number))
    #endexcept

    # While loop breaks out only if there is a connection error
    client.close()

#endSpeaker_Client