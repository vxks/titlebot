FROM node:alpine

WORKDIR /titlebot-web
COPY . /titlebot-web
RUN npm install
RUN npm install -g serve
RUN npm run build
COPY . .

CMD serve -s build