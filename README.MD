You need to import this project with IntelliJ.

There is a .pom file (Maven) which can be used to properly compile ProtocolBuffer.
All the dependencies are included as well.

To compile the proto files, you can do it both within IntelliJ as well as with the command line.

The command you need to launch is:

mvn package

To launch a "main" from the command line:

mvn package exec:java -Dexec.mainClass=chat.ChatServer

(replace "chat.ChatServer" with the class containing a main method that you need to launch)