@echo off
"%cd%\ffmpeg\bin\ffmpeg.exe" -y -i "%cd%\library\%~1" -ac 1 -ar 44100 -acodec pcm_u8 "%cd%\temp\%~1.wav"