# thresh-pailler-wo-td
Implementation of a Threshold Paillier Cryptosystem distributed key generation protocol without trusted party described 
by Takashi Nishide and Kouichi Sakurai in their paper "**Distributed Paillier Cryptosystem without Trusted Dealer**".

A patch for the [Paillier Treshold Encryption Toolbox](http://www.cs.utdallas.edu/dspl/cgi-bin/pailliertoolbox/) by UT Dallas is also provided to allow this toolbox to work with
our generated keys.

## Warning
The implementation is still incomplete and wasn't yet properly reviewed. Therefor, it shouldn't be considered as secure or
used in some security-critical systems.

## Compilation and test
Maven is used to manage dependencies and compiling. The following command lines will run the provided local test:
```bash
git clone https://github.com/ChristianMct/thresh-pailler-wo-td.git
cd thresh-pailler-wo-td
mvn package
cd target
java -cp classes/:test-classes/:libs/* ProtocolTest
```





