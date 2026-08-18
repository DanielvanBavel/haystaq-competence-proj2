FROM node:20-alpine
WORKDIR /app
COPY package.json ./
COPY src ./src
ENV ARTIFACTS_DIR=/artifacts
ENTRYPOINT ["node", "src/run.js"]
