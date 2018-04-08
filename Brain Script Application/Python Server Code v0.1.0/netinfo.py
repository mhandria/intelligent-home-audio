import socket
import os
import netifaces
import ipaddress as ip

CLASS_C_ADDR = '192.168.0.0'

print('\n\n\n\n\n\n\n\n\n\n\n\n\n\n')

print('~Local Machine Information~')

hostname = socket.gethostname()
localIP = socket.gethostbyname(hostname)

print('\tHostname: {0}'.format(hostname))
print('\tLocal Machine IP: {0}'.format(localIP))

print(' ')

print('~Network Interfaces~')

# get interfaces list
ifaces = netifaces.interfaces()

for iface in ifaces:
    ipaddrs = netifaces.ifaddresses(iface)
    if netifaces.AF_INET in ipaddrs:
        ipaddr_desc = ipaddrs[netifaces.AF_INET]
        ipaddr_desc = ipaddr_desc[0]
        print("\tNetwork interface: {0}".format(iface))
        print("\t\tIP address: {0}".format(ipaddr_desc['addr']))
        print("\t\tNetmask: {0}".format(ipaddr_desc['netmask']))
        print('')
    #endif
#endfor

print('\tlo is the loopback interface.\n')
print('\twlan0 is the onboard WiFi module.\n')

print('~Gateway (router)~')
print("\tDefault Gateway IP address: {0}\n".format(netifaces.gateways()[2][0][0]))

print('~ramdom tests~')

