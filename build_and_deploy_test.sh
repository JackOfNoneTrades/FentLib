VERSION=testerino ./gradlew build
mkdir -p ../AuthTests/run/libs
mkdir -p ../WawelAuth/run/libs
cp build/libs/fentlib-testerino-dev.jar ../AuthTests/run/libs/fentlib-testerino-dev.jar
cp build/libs/fentlib-testerino-dev.jar ../WawelAuth/run/libs/fentlib-testerino-dev.jar
