package com.ivolve.lab22

import groovy.json.JsonSlurper

class BuildApp implements Serializable {
    def appDir = 'src'

    def call(Map config = [:]) {
        def workspace = config.workspace ?: pwd()
        def appWorkspace = "${workspace}/${appDir}"

        echo "Building application from: ${appWorkspace}"

        dir(appWorkspace) {
            echo "Installing dependencies..."
            sh 'npm install'

            echo "Running unit tests..."
            try {
                sh 'npm test'
                echo "Unit tests passed successfully"
            } catch (Exception e) {
                echo "Unit tests failed: ${e.message}"
                throw e
            }
        }

        echo "Application build completed"
    }
}