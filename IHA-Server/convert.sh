FILENAME=$1
ffmpeg -y -i "/home/linaro/Desktop/library/$FILENAME" -ac 1 -ar 25000 -acodec pcm_u8 "/home/linaro/Desktop/temp/$FILENAME.wav"