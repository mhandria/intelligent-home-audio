import time
import sharedMem

# Every x seconds, sync all of the song file positions to the
# same position. (Furthest most in the song)

def SongSync():
    global songFileIndexes

    try:
        while True:
            time.sleep(5)
            
            # sync only if the buffer is good and full
            if(time.time() - sharedMem.timeOfLastPause > 10):
                sharedMem.syncIndexes()
                print('<sync>')
            else:
                print('<too early to sync>')
            #endelse

            # repeat #
        #endwhile
    except Exception as e:
        print('ERROR: Failed to sync song positions')
        print(e)
    #endexcept
#end SongSync