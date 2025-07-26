mvn spring-boot:build-image -Dspring-boot.build-image.imageName="AiDetectorBE"

docker run --rm -d --name "AiDetectsBE" -p 8080:8080 AiDetectorBE


