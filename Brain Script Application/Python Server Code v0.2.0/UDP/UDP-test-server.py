from socket import socket, AF_INET, SOCK_DGRAM

MAX_SIZE = 4096
PORT = 14124
ADDR = '192.168.1.103'

sock = socket(AF_INET,SOCK_DGRAM)
sock.bind((ADDR,PORT))

print('Listening for UDP traffic at {0}:{1}'.format(ADDR,PORT))

while True:
    data, addr = sock.recvfrom(MAX_SIZE)
    print('Client: {0}'.format(data))
#endwhile
