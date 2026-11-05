FROM python:3.10.14-alpine3.20

WORKDIR /opt
ENV PIP_BREAK_SYSTEM_PACKAGES 1

COPY ./devops/core-plus/bootstrap-osdu-module/*.sh ./
COPY ./deployments ./

RUN apk update && apk add jq bash py3-pip curl
RUN pip3 install --upgrade pip && \
  pip3 install -r ./scripts/requirements.txt
RUN chmod +x /opt/bootstrap_schema.sh
RUN addgroup -g 10001 -S nonroot \
  && adduser -h /opt -G nonroot -S -u 10001 nonroot
RUN chown -R 10001:10001 /opt

USER 10001:10001

CMD ["/opt/bootstrap_schema.sh"]
