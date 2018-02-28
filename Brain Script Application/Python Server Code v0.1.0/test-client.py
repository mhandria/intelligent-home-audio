import os
from socket import*
import socket
import ipaddress as ip

host = input('input server address: ')
PORT = 14123
BUFSIZ = 256
ADDR = (host, PORT)

print('creating socket...')
client_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock_addr = (host, PORT)
client_sock.connect(sock_addr)
print("Socket created on host {0} and port {1}.\n".format(host, PORT))

payload = input("Client: ")


while True:
    client_sock.send(payload.encode('utf-8'))
    data = client_sock.recv(BUFSIZ)
    print("Server: {0}".format(data.decode('utf-8')))
    payload = input("Client: ")
    if payload.lower() == 'end':
        print("Closing socket...")
        break
#endwhile

client_sock.close()
print("Socket closed.")