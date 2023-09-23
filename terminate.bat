echo off

:: Terminate processes running on specified ports
for /f "tokens=5" %%p in ('netstat -a -n -o ^| findstr :7650 :7657 :8381 :8654 :8118 :8092 :8888 :8181 :8645 :8761') do taskkill /f /pid %%p

:: Wait for all processes to finish
timeout /t 5