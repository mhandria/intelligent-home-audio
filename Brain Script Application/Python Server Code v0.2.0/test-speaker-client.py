# file for testing interaction with the server without need
# to program MCU
import os
from socket import*
import socket
import ipaddress as ip

host = input('input server address: ')
PORT = 14124
BUFSIZ = 2048
ADDR = (host, PORT)

print('creating socket...')
client_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock_addr = (host, PORT)
client_sock.connect(sock_addr)
print("Socket created on host {0} and port {1}.\n".format(host, PORT))

f = open('songData.bin', 'wb')
f.close()

while True:
    payload = input("Client: ")

    client_sock.send(payload.encode('utf-8'))
    data = client_sock.recv(BUFSIZ)

    if(payload == 'songData'):
        # log to file because it's a lot of data
        f = open('songData.bin', 'wb+')
        while True:
            f.write(data)
            client_sock.send(payload.encode('utf-8'))
            data = client_sock.recv(BUFSIZ)
        #endwhile
        f.close()
        print('Wrote a song chunk to "songData.bin"')
    else: # otherwise print to console
        print("Server: {0}".format(data.decode('utf-8')))
    #endelse
#endwhile

client_sock.close()
print("Socket closed.")