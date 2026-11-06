// DeployOnK8s.groovy - Shared Library Global Variable
// Updates deployment manifest image tag and applies to cluster or commits for GitOps

def call(Map config = [:]) {
    def lib = new com.ivolve.lab22.DeployOnK8s()
    lib.call(config)
}