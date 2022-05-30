FROM alpine/git:v2.32.0 AS modules

WORKDIR /
RUN git clone https://kbss.felk.cvut.cz/gitblit/r/s-pipes-modules.git

FROM ghcr.io/kbss-cvut/s-pipes/s-pipes-engine:latest

COPY --from=modules ./s-pipes-modules /scripts/s-pipes-modules
COPY . /scripts/root
