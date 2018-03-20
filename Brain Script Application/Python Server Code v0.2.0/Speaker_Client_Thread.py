#!/usr/bin/env python3
# Thread that listens for commands from the speaker embeded system

import socket
import sharedMem
import os

# global variables
SPKN = 0
CLIENT = 0

def returnMessage(payload):
    global SPKN
    global CLIENT
    print('Speaker - Client #{0} Response: {1}'.format(SPKN,payload))
    CLIENT.send(payload.encode('utf-8'))
#end returnMessage

def Speaker_Client(client, spkn, addr, self):
    # initialize variables for this file
    global SPKN
    global CLIENT

    CLIENT = client
    SPKN = spkn

    sharedMem.speakersConnected.update({SPKN:1})

    print('')
    print("Speaker - TCP Client #{0} connected from {1}...".format(SPKN,addr))

    #enter loop for handling client
    try:
        while True:
            #get phone payload data
            data = client.recv(1)
            data = data.decode('utf-8')
            data = data.rstrip()
            print('Speaker - Client #{0} Payload:  {1}'.format(SPKN,data))
            
            #interpret data and set return payload
            if(data == '?'):
                returnMessage('y')
            else:
                returnMessage('n')
            #endelse
            
            # repeat forever
        #endwhile
    except Exception as e:
        print('Speaker - Client #{0} disconnected'.format(SPKN))
        print(e)
    #endexcept

    # While loop breaks out only if there is a connection error
    client.close()
    # this lets the UDP thread know that it can free the socket up again at this address
    sharedMem.speakersConnected.update({SPKN:0})

    # send a udp packet to yourself to stop blocking 
    # the UDP thread so that it can exit
    s = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
    s.sendto('!'.encode(),(self, 14124))
#endSpeaker_Client