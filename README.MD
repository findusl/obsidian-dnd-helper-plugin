## Build and run

### Build
Run `gradle build`.

### CORS proxy

As obsidian is an electron app, requests to external websites are subject to CORS checking. Therefore, this plugin needs
a local CORS proxy to access the generator on https://kassoon.com. Run the following steps to set this up:
- Install the local cors proxy: `npm install -g local-cors-proxy`
- Run it for https://kassoon.com: `lcp --proxyUrl https://www.kassoon.com`
This should print a few lines, one of them saying `PORT: 8010`. If the port is a different one, the plugin will not work.
  Try freeing the port on your computer for the plugin to work.
  
### Running the plugin
Take the 


