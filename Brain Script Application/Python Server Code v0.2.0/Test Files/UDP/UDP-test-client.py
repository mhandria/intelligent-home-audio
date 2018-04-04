from socket import socket, AF_INET, SOCK_DGRAM

MAX_SIZE = 4096
PORT = 14124
addr = input('IP Address: ')

sock = socket(AF_INET,SOCK_DGRAM)

while(True):
    payload = input("Data: ")
    sock.sendto(payload.encode(),(addr, PORT))
#endwhile