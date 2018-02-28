#!/usr/bin/env python3
import socket 
from threading import Thread 
from socketserver import ThreadingMixIn
class Phone_Client_Thread(Thread): 
 
    def __init__(self): 
        Thread.__init__(self)
        print('Phone connection thread started.')
    #end__init__

    def run(self):
        while True:
            #create socket
            print('Creating phone socket...')
            server_sock = socket.socket(family=AF_INET, type=SOCK_STREAM)
            server_sock.bind(PHONE_ADDR)
            server_sock.listen(5)
            server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            print('Phone Socket created.\n')

            #listen until phone opens socket to server
            print('Waiting for phone connection...')
            phone_client_sock, addr = server_sock.accept()
            print("Phone client connected from {0}:{1}...".format(addr,phone_client_sock))

            #get phone payload data
            data = phone_client_sock.recv(BUFFER_SIZE)
            data = data.decode('utf-8')
            data = data.rstrip()

            #interpret data and set return payload
            if(data.lower() == 'play a'):
                print('playing a.wav...')
                payload = 'played a.wav'
            elif(data.lower() == 'play b'):
                print('playing b.wav...')
                payload = 'played b.wav'
            else:
                payload = 'Invalid Command'
            #end ifelse

            #send return message and close the socket
            phone_client_sock.send(payload.encode('utf-8'))
            phone_client_sock.close()
            # repeat forever
        #endwhile
    #endrun
#endPhone_Client_Thread