// Variables
def terminalNotifierPath = '/Applications/terminal-notifier.app/Contents/MacOS/terminal-notifier'
def testReportURI = "file://${grailsSettings.testReportsDir}/html/index.html"
def baseGroupId = 'org.grails'
def urlPattern = /^Server running. Browse to (http:.*)$/
def failures = []

// Helper Methods
def terminalNotifier = { Map options ->
    if (options.containsKey('title')) {
        options.title = "Grails: ${options.title}"
    }
    def args = []
    options.each { key, value ->
        args << "-${key}"
        args << value
    }
    def cmd = [terminalNotifierPath] + args
    cmd.execute()
}
def info = { terminalNotifier it + [group: "${baseGroupId}.info"] }
def warn = { terminalNotifier it + [group: "${baseGroupId}.warn"] }
def error = { terminalNotifier it + [group: "${baseGroupId}.error"] }

// Event Handlers
eventStatusError = { message ->
    error title: 'Error', message: message
}

eventStatusFinal = { message ->
    // Skip if test phase
    if (grailsEnv == "test") {
        return
    }

    // If you click too earlier, the server which hasn't be ready yet might return 503.
    def urls = (message =~ urlPattern).collect { matched, url -> url }
    if (urls) {
        info title: 'Server running', message: "${message}. Click here to open the url.", open: urls.first()
    } else {
        info title: 'Status', message: message
    }
}

eventCreatedArtifact = { type, name ->
    info title: 'Created artifact', message: "artifactType: ${type} / artifactName: ${name}"
}

eventCreatedFile = { name ->
    info title: 'Created file', message: "${name} is created."
}

eventExiting = { code ->
    error title: 'Exit', message: "Return code: ${code}"
}

eventTestFailure = { name, failure, isError ->
    warn title: 'Test failure', message: "${name}: ${failure}"
    failures << failure
}

eventTestPhasesEnd = {
    def message = 'Click here to open the report.'

    if (failures) {
        error title: "${failures.size()} tests failed", message: message, open: testReportURI
    } else {
        info title: 'All tests passed', message: message, open: testReportURI
    }
}
