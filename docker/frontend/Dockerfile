FROM node:lts-alpine
WORKDIR /app

RUN npm install -g http-server
COPY src/e4-collab/app/package*.json .
COPY src/e4-collab/app .
RUN npm install
RUN npm run build

EXPOSE 8080


CMD ["http-server", "dist"]