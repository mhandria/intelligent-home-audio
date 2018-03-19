import os
from socket import*
import socket
import ipaddress as ip

host = input('input server address: ')
PORT = 14123
BUFSIZ = 256
ADDR = (host, PORT)

while True:
    print('')
    payload = input("Payload: ")

    print('creating socket...')
    client_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock_addr = (host, PORT)
    client_sock.connect(sock_addr)
    print("Socket created on host {0} and port {1}".format(host, PORT))
    
    client_sock.send(payload.encode('utf-8'))
    print('Sent payload, awaiting reponse')
    data = client_sock.recv(BUFSIZ)
    
    print("Server response: {0}".format(data.decode('utf-8')))
    
    client_sock.close()
    print("Socket closed.")
#endwhile
