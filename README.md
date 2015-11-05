nlp100
======

nlp100 in Scala

How to run codes
----------------

```
cd nlp100

mkdir resources

### put data into the resources dir ###

# for programs depending on external libraries
sbt -mem 2048 'runMain  nlp100.P1'

# for programs not depending on external libraries
sbt compile
scala -cp .:resources:target/scala-2.11/classes nlp100.P1  

# for test
sbt test
```
