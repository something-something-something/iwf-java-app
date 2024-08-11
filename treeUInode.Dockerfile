FROM node:22
RUN corepack enable
USER node
RUN corepack enable
RUN corepack install -g pnpm@9.7.0
WORKDIR /home/node/app
COPY package.json package.json
COPY jsconfig.json jsconfig.json
COPY pnpm-lock.yaml pnpm-lock.yaml
RUN pnpm --version
RUN pnpm install --frozen-lockfile
