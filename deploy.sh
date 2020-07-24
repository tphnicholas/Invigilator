#!/bin/bash
docker build -t invigilator:latest -f Dockerfile --no-cache .
docker run -e BOT_TOKEN=$1 -v $(pwd):/config invigilator:latest