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
  - [ ] Create a simple DatabaseBridge that will send data to a message broker.
  - [ ] Listen to messages from the message broker and process them.
- [ ] Handle teleports between servers
  - [ ] Create a custom communication between the proxy and the module to handle teleports.
    - Important to remember to make it secured, so players cannot interface with it.
  - [ ] Create a custom PlayerTeleportAlgorithm that will handle teleports between servers.
- [ ] Handle new island creations to be forwarded to other servers.
  - This will require a 3rd-party service to handle the requests.