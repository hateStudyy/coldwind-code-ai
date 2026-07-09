#!/usr/bin/env bash
set -euo pipefail

SERVICE_NAME="coldwind-code-ai"
SERVICE_SOURCE="deploy/systemd/${SERVICE_NAME}.service"
SERVICE_TARGET="/etc/systemd/system/${SERVICE_NAME}.service"
APP_DIR="/opt/1panel/www/sites/coldwind-code-ai"
JAR_NAME="coldwind-code-ai-0.0.1-SNAPSHOT.jar"

if [[ ! -f "${SERVICE_SOURCE}" ]]; then
  echo "Service file not found: ${SERVICE_SOURCE}" >&2
  exit 1
fi

if [[ ! -f "${APP_DIR}/${JAR_NAME}" ]]; then
  echo "Jar file not found: ${APP_DIR}/${JAR_NAME}" >&2
  exit 1
fi

if ! sudo test -x /root/.sdkman/candidates/java/21.0.8-amzn/bin/java; then
  echo "Java executable not found: /root/.sdkman/candidates/java/21.0.8-amzn/bin/java" >&2
  exit 1
fi

if ! sudo test -x /root/.nvm/versions/node/v22.19.0/bin/npx; then
  echo "npx executable not found: /root/.nvm/versions/node/v22.19.0/bin/npx" >&2
  exit 1
fi

sudo install -m 0644 "${SERVICE_SOURCE}" "${SERVICE_TARGET}"
sudo systemctl daemon-reload
sudo systemctl enable "${SERVICE_NAME}"

if sudo systemctl is-active --quiet "${SERVICE_NAME}"; then
  sudo systemctl restart "${SERVICE_NAME}"
else
  manual_pids="$(sudo pgrep -f "java -jar ${JAR_NAME}" || true)"
  if [[ -n "${manual_pids}" ]]; then
    echo "Manual backend process is running and may occupy port 8999:"
    echo "${manual_pids}"
    echo "Stop it first, then run: sudo systemctl start ${SERVICE_NAME}"
    exit 0
  else
    sudo systemctl start "${SERVICE_NAME}"
  fi
fi

sudo systemctl --no-pager --full status "${SERVICE_NAME}"
