# RealmShark  
### A packet sniffer for Realm of the Mad God in Java.

RealmShark is a Java library/API created to read network packets at the kernel level without the the ability to modify, block or send packets. The library is EULA/copyright compliant because it does not use any game code or assets.  

Given RealmShark reads packets directly from the network adapter it can even be used to listen on a PC that is not running the game. It is an independent program from the game and is completely extendable/customizable.  

Multiple instances of Realm of the Mad Gods are not supported using this sniffer. 
As of now the sniffer can't filter packets from multiple instances of the game running at the same time.   
The sniffer crashes if it can't distinguish the packets from different instances at the network layer.  
In the future, OS specific functionality will be added to support multiple instances of clients.

#### Credits:

- Most backed code was written by [Cortex](https://github.com/MCRcortex). Huge thanks to him.
- Inspired by work done by [abrn](https://github.com/abrn/realmlib) and [thomas-crane](https://github.com/thomas-crane/realmlib-net).
- [ardikars](https://github.com/ardikars/pcap) library for packet processing.
- [libpcap or winpcap](https://npcap.com/) is used by the ardikars library to make the packet sniffing possible.

## Running a release

Java is required for running the program. It can be installed [here](https://www.java.com/en/download/).  

Download the latest `Tomato-v*.*.jar` file from [Releases](https://github.com/X-com/RealmShark/releases).

Once downloaded, open a **cmd.exe** or terminal window and run the command:  
`java -jar Tomato-v*.*.jar`  

The RealmShark GUI should open and the packet sniffer can be started.  
If there are errors running the above command, please [open an issue here,]() so it can be resolved.

## Building from source

This is written for use with IntelliJ IDEA only. Other IDEs can also  be used in a similar manner.  

You will need a OpenJDK >16.0 SDK from [here.](https://jdk.java.net/18/) Once downloaded, add the SDK to IntelliJ.  

Download the .zip or clone the repo via CLI:  
`git clone https://github.com/X-com/RealmShark && cd RealmShark`

Open IntelliJ and click **File > New > Project from Existing Sources...**  
Navigate to the source folder and click Open, then import using **gradle** when prompted.  

The application can now be built, ran and debugged from IntelliJ.


### More coming soon...  
  
