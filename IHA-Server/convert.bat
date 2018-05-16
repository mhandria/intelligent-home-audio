@echo off
"%cd%\ffmpeg\bin\ffmpeg.exe" -y -i "%cd%\library\%~1" -ac 1 -ar 25000 -acodec pcm_u8 "%cd%\temp\%~1.wav"