A packet sniffer for Realm of the Mad God in Java. Most backend code is writen by [Cortex](https://github.com/MCRcortex), huge tanks to him for all the help.

Inspired by work done by [abrn](https://github.com/abrn/realmlib) and [thomas-crane](https://github.com/thomas-crane/realmlib-net).

Mainly made as an API to read packets without having the ability to modify, block or send any packets. An attempt to make it EULA friendlier to build useful tools for various addons to the severely lacking API features of the ROTMG game.

Given this program reads packets directly out of the network tap it can even be used on any pc on the same wire to construct the ROTMG packets. This means it doesn't even need to run on the same pc the game is running on, the sniffer works as long as you can read the ones and zeros on the network wire coming out of the PC running the ROTMG game on. It is a totally independent program from your game and if not used to automate the game in any way then it can be considered an independent program separate from Realm of the Mad God totally. This in turn also means that it can't be used in any way possible to edit your game whatsoever, it also means it's impossible to be detected.

This project uses [ardikars](https://github.com/ardikars/pcap) library with MIT License.
[Libpcap or winpcap](https://npcap.com/) is used by ardikars library to make the packet sniffing possible.
