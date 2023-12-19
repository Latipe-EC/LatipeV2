#!/bin/bash

for PORT in 8761 7657 8381 8654 8118 8092 8888 8181 8645 7650; do
  PID=$(lsof -t -i:$PORT)
  if [ -n "$PID" ]; then
    echo "Terminating process $PID running on port $PORT..."
    kill $PID
  fi
done


