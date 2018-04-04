# file for testing interaction with the server without need
# to program the phone
import os
from socket import*
import socket
import ipaddress as ip

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
        payload = input("Payload: ")

        #send payload
        client_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock_addr = (host, PORT)
        client_sock.connect(sock_addr)
        client_sock.send(payload.encode('utf-8'))
        print("Socket open.")

        if(payload == 'getSongList'):
            data = ' '
            while(data != EOT):
                data = client_sock.recv(BUFSIZ)
                print(data.decode('utf-8'))
        else:
            data = client_sock.recv(BUFSIZ)
            
            if(ord(data) == ACK):
                print('Server: <ACK>')
            elif(ord(data) == NAK):
                print('Server: <NAK>')
            elif(ord(data) == EOT):
                print('Server: <EOT>')
            else:
                print("Server: {0}".format(data.decode('utf-8')))
            #endelse
        #endelse
        
        client_sock.close()
        print("Socket closed.")
    except Exception as e:
        print('ERROR:')
        print(e)
    #endexcept
#endwhile
