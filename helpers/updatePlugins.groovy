#!/usr/bin/env groovy

import groovy.json.JsonSlurper

import java.util.stream.Collectors

final def PLUGINS_API_BASE_URL = 'https://plugins.jenkins.io/api/plugin/'

def newPlugins = []

def parser = new JsonSlurper()

File inputList

if (args && args[0]) {  // Did the user provide an input plugin list?
    inputList = new File(args[0])
} else {
    println "Error! No input file provided"
}

if (!(inputList.exists() && inputList.canRead())) {
    inputList = null
    println "Specified input file `${inputList.absolutePath}` does not exist or is not readable."
}

if (inputList) {    // User DID pass an input file as an argument
    inputList.readLines()
             .parallelStream()
             .map({line -> line.tokenize( ':' )[0]})
             .map({name -> new URL("${PLUGINS_API_BASE_URL}${name}")})
             .forEach { url ->  // Iterate over input file lines
        def conn = url.openConnection()
        conn.addRequestProperty("Accept", "application/json")
        conn.with {
            requestMethod = 'GET'
        }
        try {
            if (conn.getResponseCode() == 200) {
                def jsonResponse = parser.parseText((String)conn.getInputStream().text)

                newPlugins.add("${jsonResponse.name}:${jsonResponse.version}")
            } else {
                println "Error accessing Jenkins Plugins API: ${url.toString()}"
            }
        } catch (Exception e) {
            println "Error retrieving data from Plugins API: ${url.toString()}"
            e.printStackTrace()
        }
    }

    File output
    if (args.length>1 && args[1]) {  // Did the user provide an output file?
        output = new File(args[1])
        if (!(output.canWrite() || output.createNewFile())) {
            output = null
        }
    }
    def pluginsText = newPlugins
            .sort({ a,b -> a.toLowerCase().compareTo(b.toLowerCase())})
            .stream()
            .collect(Collectors.joining('\n'))
    output.write(pluginsText)
} else {
    println "You MUST provide an absolute or relative path to an input file for this script to work."
    println "EX: ./updatePlugins.groovy /path/to/plugins.txt"
}