FROM mcr.microsoft.com/devcontainers/java:1-17-bookworm

# The upstream Java image includes Yarn 1.x plus its apt source. This project
# uses npm only, and the bundled Yarn key can lag behind repository key
# rotation. Remove the unused third-party apt source before Features run
# apt-get update. Keep Yarn itself untouched; only future apt access is removed.
RUN rm -f /etc/apt/sources.list.d/yarn.list \
    /usr/share/keyrings/yarn-archive-keyring.gpg \
    && apt-get update \
    && apt-get install -y maven \
    && rm -rf /var/lib/apt/lists/*
