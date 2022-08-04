ARG VARIANT=
FROM openjdk:${VARIANT}

LABEL vendor="GeneXus Labs"
USER root

# Set timezone
ENV TZ=America/Montevideo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Install dependencies
RUN apt-get update \
    && apt-get install -y \
        curl \
        git \
        git-lfs \
        maven \
        sudo \
        tar \
        zip \
        gnupg2 \
        -o APT::Immediate-Configure=0 \
    && apt-get clean \
    && rm -fr /var/lib/apt/lists/* /tmp/* /var/tmp/*

#Set Github user
RUN useradd -m github \
    && usermod -aG sudo github \
    && echo "%sudo ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

USER github
WORKDIR /home/github

# Enable Git LFS
RUN git lfs install

# Install updated cacerts
# Build a new Java cacerts using CAs trusted by Mozilla
RUN cd /tmp \
    && curl -OL https://hg.mozilla.org/mozilla-central/raw-file/tip/security/nss/lib/ckfw/builtins/certdata.txt \
    && curl -OL https://raw.githubusercontent.com/curl/curl/master/scripts/mk-ca-bundle.pl \
    && curl -OL https://github.com/use-sparingly/keyutil/releases/download/0.4.0/keyutil-0.4.0.jar \
    && perl mk-ca-bundle.pl -n > ca-bundle.crt \
    && java -jar keyutil-0.4.0.jar --import --new-keystore cacerts-mozilla --password changeit --force-new-overwrite --import-pem-file ca-bundle.crt \
    && sudo cp cacerts-mozilla ${JAVA_HOME}/lib/security/cacerts \
    && rm -r /tmp/*

# Copy Maven settings
COPY --chown=github:github settings.xml /tmp/settings.xml
RUN mkdir -p /home/github/.m2/  \
    && mv /tmp/settings.xml /home/github/.m2/