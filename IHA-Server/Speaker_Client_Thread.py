#!/usr/bin/env python3
# Thread that listens for commands from the speaker embeded system

import socket
import sharedMem
import os
import time

# global variables

def returnMessage(payload, spkn, client):
    print('Speaker - TCP Client #{0} Response: {1}'.format(spkn,payload))
    client.send(payload.encode('utf-8'))
#end returnMessage

def Speaker_Client(client, spkn, addr, self):
    # initialize variables for this file

    sharedMem.speakersConnected.update({spkn:1})

    print('')
    print("Speaker - TCP Client #{0} connected from {1}...".format(spkn,addr))

    client.setblocking(0)

    #enter loop for handling client
    try:
        while(sharedMem.speakersConnected[spkn] == 1):
            # get speaker payload data

            try:
                data = client.recv(1)
                data = data.decode('utf-8')
                data = data.rstrip()
            except Exception as e:
                data = ' '
            #endexcept
            
            #interpret data and set return payload
            if(data == '?'):
                print('Speaker - TCP Client #{0} Payload:  {1}'.format(spkn,data))
                returnMessage('y', spkn, client)
            #endif
            
            # repeat forever
        #endwhile
    except Exception as e:
        print('Speaker - TCP Client #{0} ERROR:'.format(spkn))
        print(e)
        time.sleep(5)
    #endexcept

    # While loop breaks out only if there is a connection error
    client.close()
    # this lets the UDP thread know that it can free the socket up again at this address
    sharedMem.speakersConnected.update({spkn:0})

    # send a udp packet to yourself to stop blocking 
    # the UDP thread so that it can exit
    # s = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
    # s.sendto('!'.encode(),(self, 14124))
    print('Speaker - TCP Client #{0} thread closed'.format(spkn))
#endSpeaker_Client