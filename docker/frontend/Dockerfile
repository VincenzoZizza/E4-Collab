FROM node:lts-alpine
WORKDIR /app

RUN npm install -g http-server
COPY frontend/package*.json .
COPY frontend .
RUN npm install
RUN npm run build

ENV VITE_API_URL=http://localhost:8081

EXPOSE 4000


CMD ["http-server", "dist", "-p", "4000"]