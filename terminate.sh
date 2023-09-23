#!/bin/bash
for PORT in 7650 7657 8381 8654 8118 8092 8888 8181 8645 8761; do
  PID=$(lsof -t -i:$PORT)
  if [ -n "$PID" ]; then
    echo "Terminating process $PID running on port $PORT..."
    kill $PID
  fi
done
