from socket import socket, AF_INET, SOCK_DGRAM

MAX_SIZE = 4096
PORT = 14124
addr = input('IP Address: ')

sock = socket(AF_INET,SOCK_DGRAM)

while(True):
    payload = input("Client: ")
    
    sock.sendto(payload.encode(),(addr, PORT))
    data, addr = sock.recvfrom(MAX_SIZE)

    print("Server: {0}".format(data.decode()))
#endwhile