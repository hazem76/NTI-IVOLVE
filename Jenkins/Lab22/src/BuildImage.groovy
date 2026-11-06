package com.ivolve.lab22

class BuildImage implements Serializable {
    def dockerImage = config.dockerImage ?: 'hazem7777/nodejs-docker-exercise'
    def dockerTag = config.dockerTag ?: "${env.BUILD_NUMBER}"
    def registryCredentials = config.registryCredentials ?: 'dockerhub-credentials'

    def call(Map config = [:]) {
        def imageName = "${dockerImage}:${dockerTag}"
        def fullImageName = config.dockerRegistry ? "${config.dockerRegistry}/${imageName}" : imageName

        echo "Building Docker image: ${fullImageName}"

        // Build Docker image
        sh "docker build -t ${fullImageName} -f Dockerfile ."

        // Run Trivy vulnerability scan
        echo "Running Trivy vulnerability scan on ${fullImageName}"
        sh """
        docker run --rm -v //var/run/docker.sock:/var/run/docker.sock \
            aquasec/trivy:latest image \
            --exit-code 0 \
            --severity HIGH,CRITICAL \
            ${fullImageName} || true
        """

        // Push to Docker Hub
        echo "Pushing image to Docker Hub..."
        withCredentials([usernamePassword(credentialsId: registryCredentials, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
            sh "echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin"
            sh "docker push ${fullImageName}"
            sh "docker logout"
        }

        echo "Image pushed: ${fullImageName}"

        // Clean up local image
        echo "Cleaning up local Docker image..."
        sh "docker rmi ${fullImageName} || true"

        echo "Docker image build, scan, push, and cleanup completed"
        return fullImageName
    }
}