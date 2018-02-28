#!/usr/bin/env python3
import socket
from threading import Thread 
from socketserver import ThreadingMixIn

def Phone_Client(ADDR, BUFFER_SIZE):
    print('')
    print('Phone connection thread started')

    while True:
        #create socket
        try:
            server_sock = socket.socket(family=socket.AF_INET, type=socket.SOCK_STREAM)
            server_sock.bind(ADDR)
            server_sock.listen(5)
            server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        except:
            print('ERROR: while creating phone socket')

        #listen until phone opens socket to server

        print(' ')
        print('Waiting for phone connection...')
        phone_client_sock, addr = server_sock.accept()
        print("Phone client connected from {0}...".format(addr))

        try:
            #get phone payload data
            data = phone_client_sock.recv(BUFFER_SIZE)
            data = data.decode('utf-8')
            data = data.rstrip()

            print('Phone client Payload: {0}'.format(data))

            #interpret data and set return payload
            if(data.lower() == 'play a'):
                payload = 'played a.wav'
            elif(data.lower() == 'play b'):
                payload = 'played b.wav'
            else:
                payload = 'Invalid Command'
            #end ifelse

            #send return message
            phone_client_sock.send(payload.encode('utf-8'))
        except:
            print('ERROR: Phone unexpectedly disconnected trying again...')
        
        phone_client_sock.close() # close the socket and do it again
        # repeat forever
    #endwhile