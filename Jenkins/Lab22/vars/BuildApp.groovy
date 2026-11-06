// BuildApp.groovy - Shared Library Global Variable
// Executes unit tests and builds the application

def call(Map config = [:]) {
    def lib = new com.ivolve.lab22.BuildApp()
    lib.call(config)
}