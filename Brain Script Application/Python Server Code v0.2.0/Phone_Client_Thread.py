#!/usr/bin/env python3
# Thread for listening for mobile application commands 
import socket
from threading import Thread 
from socketserver import ThreadingMixIn
import sharedMem
import requests

# Functions #
def getExtIP():
    try:
        extIP = requests.get("https://api.ipify.org").text
    except Exception as e:
        print('Phone - ERROR: Failed to get public IP address.')
        extIP = 'na'
    #endexcept
    return extIP

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