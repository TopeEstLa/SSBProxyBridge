# SSBProxyBridge

This module adds support for multiple servers synchronization to SuperiorSkyblock2.<br>
It is aimed for large networks that use SuperiorSkyblock and their server cannot hold their playerbase.<br>

The solution nowadays is to make multiple servers inside your network of Skyblock that do not share anything together.
The issue with this approach is that you divide your community between these servers, and
players on server A cannot play with servers from server B. This module aims to solve this issue.

A more detailed documentation about the module can be found [here](https://docs.google.com/document/d/1IHG7-ID9LJHXE2fWZwJA1Aam6JmI9YTTFLh0yp1uZsA/edit). <br>
You can join our discord and help with the development [here](https://discord.gg/UcQ3Uerz9N). <br>

## Tasks
- [ ] Create a basic synchronization between servers
  - [X] Create a simple DatabaseBridge that will send data to a message broker.
  - [ ] Listen to messages from the message broker and process them.
- [ ] Handle teleports between servers
  - [X] Create a custom communication between the proxy and the module to handle teleports.
  - [ ] Create a custom PlayerTeleportAlgorithm that will handle teleports between servers.
- [ ] Handle new island creations to be forwarded to other servers.
  - This will require a 3rd-party service to handle the requests.


## Donation

In order to run all the required tests, I bought a dedicated server that hosts a few Skyblock servers and 2 proxies
to simulate a network that the module will be used on. The server costs me money, and if you wanna help with funding
it, please check out my [Patreon page](https://bg-software.com/patreon/).