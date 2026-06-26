#!/data/data/com.termux/files/usr/bin/bash
set -e

if ! command -v gradle >/dev/null 2>&1; then
  echo "Gradle não foi encontrado. Instale com: pkg install gradle openjdk-17"
  exit 1
fi

if [ ! -f app/google-services.json ]; then
  echo "AVISO: app/google-services.json não foi encontrado. O APK pode até compilar, mas não conectará ao Firebase."
fi

gradle :app:assembleDebug --stacktrace
mkdir -p APK_GERADO
cp app/build/outputs/apk/debug/app-debug.apk APK_GERADO/GADM-Mobile-v3.0.0-debug.apk
echo "APK gerado em: $(pwd)/APK_GERADO/GADM-Mobile-v3.0.0-debug.apk"
