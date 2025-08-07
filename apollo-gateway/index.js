const { ApolloServer } = require('@apollo/server');
const { startStandaloneServer } = require('@apollo/server/standalone');
const { ApolloGateway, IntrospectAndCompose } = require('@apollo/gateway');
require('dotenv').config();
//const jwt = require('jsonwebtoken');

//const JWT_SECRET = process.env.JWT_SECRET;
const USER_SERVICE_URL = process.env.USER_SERVICE_URL;
const CHAT_SERVICE_URL = process.env.CHAT_SERVICE_URL;
const MESSAGE_SERVICE_URL = process.env.MESSAGE_SERVICE_URL;

const gateway = new ApolloGateway({
  supergraphSdl: new IntrospectAndCompose({
    subgraphs: [
      { name: 'users', url: USER_SERVICE_URL },
      { name: 'chat', url: CHAT_SERVICE_URL },
      { name: 'message', url: MESSAGE_SERVICE_URL },
    ],
  })
});

async function start() {
  const server = new ApolloServer({
    gateway,
    introspection: true,
  });

  const { url } = await startStandaloneServer(server, {
    listen: { port: 4000 },
  });

  console.log(`🚀 Apollo Gateway ready at ${url}`);
}

start();