# file for testing interaction with the server without need
# to program the phone
import os
from socket import*
import socket
import ipaddress as ip
import sys

host = input('input server address: ')
PORT = 14123
BUFSIZ = 256
ADDR = (host, PORT)

NUL = 0
EOT = 4
ACK = 6
NAK = 21
GS  = 29
US  = 31

while True:
    try:
        print('')
        payload = ''
        while not payload:
            payload = input("Payload: ")

        #send payload
        client_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock_addr = (host, PORT)
        client_sock.connect(sock_addr)
        client_sock.send(payload.encode('utf-8'))
        print("Socket open.")

        if(payload == 'getSongList'):
            data = ' '
            while(ord(data[0]) != EOT):
                data = client_sock.recv(BUFSIZ)
                data = data.decode('utf-8')
                print(data)
            #endwhile
        elif(payload == 'getSpeakerList'):
            data = ' '
            while(ord(data[0]) != EOT):
                data = client_sock.recv(BUFSIZ)
                data = data.decode('utf-8')
                print(data)
            #endwhile
        else:
            data = client_sock.recv(BUFSIZ)
            data = data.decode('utf-8')
            if(ord(data[0]) == ACK):
                print('Server: <ACK>')
                data = data + ' '
                print(data[1:])
            elif(ord(data[0]) == NAK):
                print('Server: <NAK>')
                data = data + ' '
                print(data[1:])
            elif(ord(data[0]) == EOT):
                print('Server: <EOT>')
                data = data + ' '
                print(data[1:])
            else:
                print("Server: {0}".format(data))
            #endelse

            logfile = open('test-phone-client.log','a')
            logfile.write(data)
            logfile.write('\n')
            logfile.close()
        #endelse
        
        client_sock.close()
        print("Socket closed.")
    except Exception as e:
        print('Error on line {}'.format(sys.exc_info()[-1].tb_lineno))
        print(e)
    #endexcept
#endwhile
