# Threshold Paillier Without Trusted Dealer

Implementation of a Threshold Paillier Cryptosystem distributed key generation protocol without a trusted party, as described 
by Takashi Nishide and Kouichi Sakurai in their paper "**Distributed Paillier Cryptosystem without Trusted Dealer**".

A patch for the [Paillier Threshold Encryption Toolbox](http://www.cs.utdallas.edu/dspl/cgi-bin/pailliertoolbox/) from 
UT Dallas is also provided, to allow this toolbox to work with the generated keys.

## Warning

This implementation is a proof of concept and is not maintained. It should not be considered secure or be 
used in some security-critical systems.

## Compilation and test

Maven is used to manage dependencies and compile. The following command lines will run the provided local test:
```bash
git clone https://github.com/ChristianMct/thresh-pailler-wo-td.git
cd thresh-pailler-wo-td
mvn package
cd target
java -cp classes/:test-classes/:libs/* ProtocolTest
```
