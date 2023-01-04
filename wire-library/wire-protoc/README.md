# Buf integration

## Build the generators code
Gradle by default outputs the binary in some strange format that hasn't been really investigated. Instead, a workaround
is implemented.
```
./gradlew wire-protoc:kotlinGeneratorBinary
```

## Build and push docker image
Docker build and push to my plugins.
```
docker build --platform=linux/amd64 -f wire-protoc/Dockerfile.swift -t plugins.buf.build/achiu/pg-wire-swift:v0.0.1-1 .
docker push plugins.buf.build/achiu/pg-wire-swift:v0.0.1-1
```

## References
https://docs.buf.build/bsr/remote-generation/template-example