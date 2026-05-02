FROM python:3.11-alpine

# create sandbox user
RUN adduser -D -H sandbox
WORKDIR /workspace

# install timeout (coreutils)
USER root
RUN apk add --no-cache coreutils
USER sandbox