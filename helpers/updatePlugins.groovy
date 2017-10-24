#!/usr/bin/env groovy

import groovy.json.JsonSlurper

final def PLUGINS_API_BASE_URL = 'https://plugins.jenkins.io/api/plugin/'

def newPlugins = []

def parser = new JsonSlurper()

File inputList

if (args[0]) {  // Did the user provide an input plugin list?
    inputList = new File(args[0])
} else {
    println "Error! No input file provided"
}

if (!(inputList.exists() && inputList.canRead())) {
    inputList = null
    println "Specified input file `${inputList.absolutePath}` does not exist or is not readable."
}

if (inputList) {    // User passed an input file as an argument
    inputList.readLines().parallelStream().forEach { plugin ->
        def (name, _) = plugin.tokenize( ':' )
        def url = new URL("${PLUGINS_API_BASE_URL}${name}")
        def conn = url.openConnection()
        conn.addRequestProperty("Accept", "application/json")
        conn.with {
            requestMethod = 'GET'
        }
        try {
            if (conn.getResponseCode() == 200) {
                def jsonResponse = parser.parseText(conn.getInputStream().text)

                newPlugins.add("${jsonResponse.name}:${jsonResponse.version}")
            } else {
                println "Error accessing Jenkins Plugins API for `${plugin}`"
            }
        } catch (Exception e) {
            println "Error retrieving data on ${name} from Plugins API"
            e.printStackTrace()
        }
    }

    newPlugins.sort().each {
        println it
    }
} else {
    println "You MUST provide an absolute or relative path to an input file for this script to work."
    println "EX: ./updatePlugins.groovy /path/to/plugins.txt"
}