# Buf integration

## Build the generators code
Gradle by default outputs the binary in some strange format that hasn't been really investigated. Instead, a workaround
is implemented.
```
./gradlew wire-protoc-integration:kotlinGeneratorBinary
```

## References
https://docs.buf.build/bsr/remote-generation/template-example
