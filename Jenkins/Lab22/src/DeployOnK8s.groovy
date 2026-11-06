package com.ivolve.lab22

class DeployOnK8s implements Serializable {
    def deploymentFile = config.deploymentFile ?: 'deployment.yaml'
    def dockerImage = config.dockerImage ?: 'hazem7777/nodejs-docker-exercise'
    def dockerTag = config.dockerTag ?: "${env.BUILD_NUMBER}"
    def namespace = config.namespace ?: 'ivolve'
    def kubeconfigCredentials = config.kubeconfigCredentials ?: 'kubeconfig-credentials'
    def gitOpsMode = config.gitOpsMode ?: false
    def gitRepo = config.gitRepo ?: 'https://github.com/hazem76/NTI-IVOLVE.git'
    def gitBranch = config.gitBranch ?: 'main'
    def gitUserName = config.gitUserName ?: 'jenkins'
    def gitUserEmail = config.gitUserEmail ?: 'jenkins@nti-ivolve.local'

    def call(Map config = [:]) {
        def fullImageName = config.dockerRegistry ? "${config.dockerRegistry}/${dockerImage}:${dockerTag}" : "${dockerImage}:${dockerTag}"

        echo "Updating deployment manifest: ${deploymentFile}"
        echo "Setting image to: ${fullImageName}"
        echo "Target namespace: ${namespace}"

        // Update image tag in deployment manifest
        sh "sed -i 's|image: .*|image: ${fullImageName}|' ${deploymentFile}"

        if (gitOpsMode) {
            echo "GitOps mode: Committing updated manifest to Git repository..."
            commitAndPushManifest()
        } else {
            echo "Direct mode: Applying manifest to Kubernetes cluster..."
            applyToKubernetes()
        }

        echo "Deployment completed successfully"
    }

    def applyToKubernetes() {
        withCredentials([file(credentialsId: kubeconfigCredentials, variable: 'KUBECONFIG')]) {
            sh "export KUBECONFIG=${KUBECONFIG}"
            sh "kubectl apply -f ${deploymentFile} -n ${namespace}"
            sh "kubectl rollout status deployment/ivolve-app -n ${namespace} --timeout=300s"
        }
        echo "Applied to Kubernetes cluster"
    }

    def commitAndPushManifest() {
        sh "git config user.name '${gitUserName}'"
        sh "git config user.email '${gitUserEmail}'"
        sh "git add ${deploymentFile}"
        sh "git commit -m 'chore: update deployment image to ${fullImageName} [ci skip]'"
        sh "git push origin ${gitBranch}"
        echo "Manifest committed and pushed to ${gitRepo} (branch: ${gitBranch})"
        echo "Argo CD will detect the change and sync automatically"
    }
}