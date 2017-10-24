#!/usr/bin/env groovy

import groovy.json.JsonSlurper

import java.util.stream.Collectors

def jenkinsVersion = [
    major: 2,
    minor: 46,
    micro: 3
]

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
             .map({line -> line.tokenize( ':' )})
             .map({name -> [
                    url: "${PLUGINS_API_BASE_URL}${name[0]}".toString(),
                    name: name[0],
                    version: name[1]
                ]})
             .forEach { input ->  // Iterate over input file lines
        def conn = new URL(input.url).openConnection()
        conn.addRequestProperty("Accept", "application/json")
        conn.with {
            requestMethod = 'GET'
        }
        try {
            if (conn.getResponseCode() == 200) {
                def jsonResponse = parser.parseText((String) conn.getInputStream().text)

                def minVersion = [
                    major: jsonResponse.requiredCore.tokenize('.')[0] as Integer,
                    minor: jsonResponse.requiredCore.tokenize('.')[1] as Integer,
                    micro: (jsonResponse.requiredCore.tokenize('.')[2] ?: 0) as Integer
                ]
                if (minVersion.major >= jenkinsVersion.major && minVersion.minor >= jenkinsVersion.minor && minVersion.micro >= jenkinsVersion.micro) {
                    newPlugins.add("${jsonResponse.name}:${jsonResponse.version}")
                    println "Updated ${jsonResponse.name} to ${jsonResponse.version}"
                } else {
                    newPlugins.add("${input.name}:${input.version}")
                    println "Keeping ${input.name} at ${input.version}"
                }
            } else {
                println "Error accessing Jenkins Plugins API: ${input.url.toString()}"
            }
        } catch (Exception e) {
            println "Error retrieving data from Plugins API: ${input.url.toString()}"
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
    if (output) {
        output.write(pluginsText)
    } else {
        println pluginsText
    }
} else {
    println "You MUST provide an absolute or relative path to an input file for this script to work."
    println "EX: ./updatePlugins.groovy /path/to/plugins.txt"
}