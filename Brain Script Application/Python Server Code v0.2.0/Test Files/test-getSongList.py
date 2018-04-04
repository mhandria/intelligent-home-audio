import os
from mutagen.easyid3 import EasyID3 as ID3

if(os.name == 'nt'): # if windows
    filepath = os.getcwd() + '/library/'
else: #otherwise debian server
    filepath = '/home/linaro/Desktop/library/'

files = [f for f in os.listdir(filepath) if os.path.isfile(os.path.join(filepath, f))]

if not files: # if the library is empty return NAK
    payload = '<NAK>'
else: # otherwise send all the information at one song per packet
    payload = ('<ACK>')
    print(payload)
    # phone_client_sock.send(payload.encode('utf-8'))

    tags = ['title', 'artist', 'album'] 

    for f in files:
        # create ID3 tag reader
        mf = ID3(filepath + f)
    
        # add filename to payload
        payload = f

        for t in tags:
            payload = payload + '<US>'

            try:
                # add tag to payload if it exists
                payload = payload + mf[t][0]
            except:
                # otherwise it's <NUL>
                payload = payload + '<NUL>'
            #endexcept
        #endfor

        # group seperator to show end of current song
        payload = payload + '<GS>'

        #send the payload, move to next song
        print(payload)
        # phone_client_sock.send(payload.encode('utf-8'))
    #endfor

    payload = '<EOT>'
#endelse
print(payload)
# phone_client_sock.send(payload.encode('utf-8'))