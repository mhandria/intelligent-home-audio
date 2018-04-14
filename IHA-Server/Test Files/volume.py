import numpy as np

volume = 0.3
song = np.array([1,2,3,4], np.int8)
songAdjusted = (song*volume).astype(np.int8).tobytes()
print(song)
print(songAdjusted)