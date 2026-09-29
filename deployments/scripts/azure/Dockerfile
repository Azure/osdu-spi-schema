FROM mcr.microsoft.com/azure-cli:2.72.0

RUN tdnf install -y ca-certificates bash curl wget gettext jq bind-utils shadow-utils \
    && python3 -m ensurepip --upgrade \
    && wget -q https://storage.googleapis.com/kubernetes-release/release/v1.21.2/bin/linux/amd64/kubectl -O /usr/local/bin/kubectl \
    && chmod +x /usr/local/bin/kubectl \
    && chmod g+rwx /root \
    && mkdir /config \
    && chmod g+rwx /config \
    && ln -s /usr/bin/python3 /usr/bin/python \
    && ln -s /usr/bin/pip3 /usr/bin/pip

ENV DATA_PARTITION="opendes" \
  AZURE_BUILD_SUBDIR="provider/schema-azure" \
  AZURE_TEST_SUBDIR="testing/schema-test-core" \
  AZURE_DEPLOYMENTS_SUBDIR="deployments/scripts/azure" \
  AZURE_DEPLOYMENTS_SCRIPTS_SUBDIR="deployments/scripts" \
  AZURE_DNS_NAME=$AZURE_DNS_NAME

USER root

# Create osdu user
RUN groupadd --system osdu && useradd --system --gid osdu osdu
WORKDIR /home/osdu

# Install Requirements
ADD deployments/scripts/azure/requirements.txt ./requirements.txt
RUN pip install -r requirements.txt

# Add Scripts
COPY deployments deployments
RUN chmod +x deployments/scripts/azure/bootstrap.sh

USER osdu

ENTRYPOINT ["/bin/bash", "/home/osdu/deployments/scripts/azure/bootstrap.sh"]
