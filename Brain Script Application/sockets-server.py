from socket import*
import socket
import netifaces
import sys
import os

os.system('clear')

# get info for wlan0
ipaddrs = netifaces.ifaddresses('en0')
# get wlan0 interface address
wlan0 = ipaddrs[2][0]['addr']
print(wlan0);

HOST = wlan0
PORT = 14124
BUFSIZ = 1024
ADDR = (HOST, PORT)

print("Hosting Server on {0}:{1}...".format(HOST,PORT))

#create a TCP socket (SOCK_STREAM)

# Socket Family (family=)
# AF_INET - 90% of sockets use this idk why

# Socket type (type=)
# SOCK_STREAM - TCP
# SOCK_DGRAM - UDP

# Protocol (proto=)
# Usually left as zero - specifies variation of protocol within the socket family
print('Creating socket...')

server_sock = socket.socket(family=AF_INET, type=SOCK_STREAM)
server_sock.bind(ADDR)
server_sock.listen(5)
server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

print('Socket created.\n')

while True:
    print('Server waiting for connection...')
    client_sock, addr = server_sock.accept()
    print('Client connected from: ', addr)

    while True:
        data = client_sock.recv(BUFSIZ) #get data from client

        if not data or data.decode('utf-8') == 'END':
            break #close the connection if the client sent END or invalid
        #endif

        print("Client: %s" %data.decode('utf-8'))
        #payload = input("Server: ")

        #client_sock.send(payload.encode('utf-8'))
    #endwhile1
    client_sock.close()
    print("Client disconnected")
#endwhile0
server_sock.close()
