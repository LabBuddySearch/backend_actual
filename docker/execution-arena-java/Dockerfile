FROM eclipse-temurin:21-jdk-alpine

RUN adduser -D -H sandbox
USER sandbox
WORKDIR /workspace

# Minimal runtime utilities:
# - sh is provided by busybox
# - timeout is provided by coreutils
USER root
RUN apk add --no-cache coreutils
USER sandbox

