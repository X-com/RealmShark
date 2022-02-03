A packet sniffer for Realm of the Mad God in Java. Most backend code is writen by [Cortex](https://github.com/MCRcortex), huge tanks to him for all the help.

Inspired by work done by [abrn](https://github.com/abrn/realmlib) and [thomas-crane](https://github.com/thomas-crane/realmlib-net).

Mainly made as an API to read packets without having the ability to modify, block or send any packets. An attempt to make it EULA friendlier to build useful tools for various addons to the severely lacking API features of the ROTMG game.

Current version of the API only supports Windows OS. [WinPcap](https://www.winpcap.org/default.htm) is needed to make the sniffer work so it is required.

This project uses [Jpcap](https://github.com/jpcap/jpcap) library with a MPL-1.1 license.