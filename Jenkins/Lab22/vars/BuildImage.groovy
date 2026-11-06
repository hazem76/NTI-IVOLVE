// BuildImage.groovy - Shared Library Global Variable
// Builds Docker image, runs Trivy scan, pushes to Docker Hub, cleans up

def call(Map config = [:]) {
    def lib = new com.ivolve.lab22.BuildImage()
    return lib.call(config)
}