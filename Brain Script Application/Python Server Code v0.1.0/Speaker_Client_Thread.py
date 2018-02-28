#!/usr/bin/env python3
import socket 
from threading import Thread 
from socketserver import ThreadingMixIn
class Speaker_Client_Thread(Thread): 
 
    def __init__(self): 
        Thread.__init__(self)
        print('Speaker connection thread started.')
    #end__init__

    def run(self):
        #create socket
        print('Creating Speaker socket...')
        server_sock = socket.socket(family=AF_INET, type=SOCK_STREAM)
        server_sock.bind(MCU_ADDR)
        server_sock.listen(5)
        server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        print('Speaker Socket created.\n')

        #listen until speaker opens socket to server
        print('Waiting for Speaker connection...')
        speaker_client_sock, addr = server_sock.accept()
        print("Speaker client connected from {0}:{1}...".format(addr,speaker_client_sock))



        while True:
            #get phone payload data
            data = speaker_client_sock.recv(BUFFER_SIZE)
            data = data.decode('utf-8')
            data = data.rstrip()

            #interpret data and set return payload
            payload = 'I\'m the server yo'

            #send return message and close the socket
            speaker_client_sock.send(payload.encode('utf-8'))
            speaker_client_sock.close()
            # repeat forever
        #endwhile
    #endrun
#endPhone_Client_Thread
