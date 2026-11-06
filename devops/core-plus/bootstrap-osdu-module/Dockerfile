FROM python:3.10-alpine3.24

WORKDIR /opt
ENV PIP_BREAK_SYSTEM_PACKAGES 1

COPY ./devops/core-plus/bootstrap-osdu-module/*.sh ./
COPY ./deployments ./

RUN apk upgrade --no-cache && apk add --no-cache jq bash curl
RUN pip3 install --no-cache-dir --upgrade pip setuptools wheel && \
  pip3 install --no-cache-dir -r ./scripts/requirements.txt && \
  pip3 uninstall -y pip wheel && \
  rm -rf /usr/local/lib/python3*/ensurepip
RUN chmod +x /opt/bootstrap_schema.sh
RUN addgroup -g 10001 -S nonroot \
  && adduser -h /opt -G nonroot -S -u 10001 nonroot
RUN chown -R 10001:10001 /opt

USER 10001:10001

CMD ["/opt/bootstrap_schema.sh"]
