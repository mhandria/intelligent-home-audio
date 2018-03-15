import time
starttime=time.time()
while True:
  print("tick")
  time.sleep(5.0 - ((time.time() - starttime) % 5.0))
