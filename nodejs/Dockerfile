FROM node:16.15.1

ARG NODE_ENV=production

ENV NODE_ENV=${NODE_ENV}

ENV EXPRESS_PORT=8080

RUN mkdir -p /home/node/app/node_modules && chown -R node:node /home/node/app

WORKDIR /home/node/app

COPY package*.json ./

USER node

RUN npm install

COPY --chown=node:node . .

RUN echo '----------------------------------------------'
RUN echo 'EXPRESS PORT:'$EXPRESS_PORT    'ENV:'$NODE_ENV
RUN echo '----------------------------------------------'

EXPOSE 8080

CMD [ "node", "ws-server.js" ]
