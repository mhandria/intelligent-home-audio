FILENAME=$1
ffmpeg -y -i "/home/linaro/Desktop/library/$FILENAME" -ac 1 -ab 128k "/home/linaro/Desktop/temp/$FILENAME"