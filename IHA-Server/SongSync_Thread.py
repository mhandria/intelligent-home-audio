import time
import sharedMem

# Every x seconds, sync all of the song file positions to the
# same position. (Furthest most in the song)

def SongSync():
    try:
        while True:
            time.sleep(0.3)

            maxPos = 0;
            
            # get the value of the furthest most song position #
            for k in list(sharedMem.songFileIndexes):
                if(sharedMem.songFileIndexes[k] > maxPos):
                    maxPos = sharedMem.songFileIndexes[k]
                #endif
            #endfor

            # update all song positions to maxPos #

            for k in list(sharedMem.songFileIndexes):
                sharedMem.songFileIndexes[k] = maxPos
            #endfor

            # repeat #
        #endwhile
    except Exception as e:
        print('ERROR: Failed to sync song positions')
        print(e)
    #endexcept
#end SongSync