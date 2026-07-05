#!/usr/bin/env sh
APP_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
exec java \
  -Dsun.awt.X11.XWMClass=ExeExtractor \
  -Djava.awt.application.name="Exe Extractor" \
  -jar "$APP_DIR/ExeExtractor.jar"
