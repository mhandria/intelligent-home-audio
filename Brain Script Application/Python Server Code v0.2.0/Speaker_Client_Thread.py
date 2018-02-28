#!/usr/bin/env python3
import socket
from threading import Thread 
from socketserver import ThreadingMixIn
import sharedMem

def Speaker_Client(speaker_number, ADDR, BUFFER_SIZE):
        print('')
        print('Speaker connection thread #{0} started'.format(speaker_number))

        while True: #tempoary tempoary loop for demo 2
            try:
                #create socket
                server_sock = socket.socket(family=socket.AF_INET, type=socket.SOCK_STREAM)
                server_sock.bind(ADDR)
                server_sock.listen(5)
                server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            except:
                print('ERROR: While creating speaker socket #{0}'.format(speaker_number))

            #listen until speaker opens socket to server
            print('Waiting for Speaker connection...')
            speaker_client_sock, addr = server_sock.accept()
            print("Speaker client connected from {0}...".format(addr))

            #when a speaker connects, open a new thread to listen for the next speaker
            try:
                newThreadNumber = speaker_number + 1
                # t = Thread(target=Speaker_Client, args=(newThreadNumber, ADDR, BUFFER_SIZE))
                # t.start() #this will never work because it's using the same port, fix sometime after demo 2
            except:
                print('ERROR: Something went wrong creating a new speakerThread')
                while True:
                    a = 0

            try:
                while True:
                    #get phone payload data
                    data = speaker_client_sock.recv(BUFFER_SIZE)
                    data = data.decode('utf-8')
                    data = data.rstrip()
                    print('Speaker client #{0} Payload: {1}'.format(speaker_number,data))

                    #interpret data and set return payload
                    payload = 'I\'m the server yo'

                    #send return message and close the socket
                    speaker_client_sock.send(payload.encode('utf-8'))
                    # repeat forever
                #endwhile
            except:
                print('Speaker client #{0} disconnected'.format(speaker_number))
        speaker_client_sock.close()
        #endwhile
#endSpeaker_Client
