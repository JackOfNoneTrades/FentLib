VERSION=testerino ./gradlew build
mkdir -p ../AuthTests/run/libs
cp build/libs/fentlib-testerino-dev.jar ../AuthTests/run/libs
