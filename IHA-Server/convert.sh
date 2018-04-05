FILENAME=$1
ffmpeg -y -i "/home/linaro/Desktop/library/$FILENAME" -ac 1 -ar 44100 -acodec pcm_u8 "/home/linaro/Desktop/temp/$FILENAME.wav"