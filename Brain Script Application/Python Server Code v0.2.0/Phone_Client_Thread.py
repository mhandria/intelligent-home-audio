#!/usr/bin/env python3
import socket
from threading import Thread 
from socketserver import ThreadingMixIn
import sharedMem

def Phone_Client(ADDR, BUFFER_SIZE):
    global LED0
    global LED1

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
        
        try:
            phone_client_sock, addr = server_sock.accept()
        except Exception as e:
            print(e)
        #endexcept

        print("Phone client connected from {0}...".format(addr))
        
        try:
            #get phone payload data
            data = phone_client_sock.recv(BUFFER_SIZE)
            data = data.decode('utf-8')
            data = data.rstrip()

            print('Phone client Payload: {0}'.format(data))

            #interpret data and set return payload
            if(data.lower() == 'play a'):
                payload = 'Played a...'
                if(sharedMem.LED0): # toggle LED0
                    sharedMem.LED0 = 0
                    print('LED0 turned off')
                else:
                    sharedMem.LED0 = 1
                    print('LED0 turned on')
                #endelse
            elif(data.lower() == 'play b'):
                payload = 'Playing b...'
                if(sharedMem.LED1): # toggle LED1
                    sharedMem.LED1 = 0
                    print('LED1 turned off')
                else:
                    sharedMem.LED1 = 1
                    print('LED1 turned on')
                #endelse
            else:
                payload = 'Invalid Command'
            #endelse

            #send return message
            phone_client_sock.send(payload.encode('utf-8'))
        except Exception as e:
            print('ERROR: Phone unexpectedly disconnected. Trying again...')
            print(e)
        
        phone_client_sock.close() # close the socket and do it again
        # repeat forever
    #endwhile