# Sec2
Sec2 is a project funded by Federal Ministry of Education and Research: http://www.sec2.org

Details about the project concept can be found in our research paper: https://www.nds.rub.de/research/publications/sec2-closer/


To test the developed application, you can proceed as follows:

Please Note: you have to compile the application with Apache maven 3.2.3. Otherwise, the following exception will appear: 
NoClassDefFoundError:org/eclipse/aether/spi/connector/Transfer$State
More information on this problem can be found here: http://stackoverflow.com/questions/28717739/exception-in-thread-pool-1-thread-1-java-lang-noclassdeffounderror-org-eclips

First, copy the crypto-data to your lib:
```bash
$ cp sec2-card/crypto-data /var/lib/sec2/
```

Compile commons-codec:
```bash
$ cd commons-codec-1.5-src
$ mvn clean install
```

Generate new test keys using the following commands
```bash
$ cd sec2-static-testdata
$ mvn clean install
```

Compile application for Android (please note that you have to have Android SDK installed. We tested our application for android-16):
```bash
$ export ANDROID_HOME=[path]
$ cd sec2-basis
$ mvn install --activate-profiles Android -DskipTests=true
```

To install the application on your phone, use the following command:
```bash
$ adb install -r sec2-android/target/sec2-android-1.0-SNAPSHOT.apk
```

There are two more applications you can compile and test. You can find them under *sec2-demo-apps*.


*Please, be careful. This is just a proof-of-concept of our research project!*