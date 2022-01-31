# Logback groovy2xml
Logback has decided to remove the support of groovy config files in version 1.2.9 (https://logback.qos.ch/news.html)  
It already has a [xml to groovy converter](https://logback.qos.ch/translator/asGroovy.html), but does not have a groovy to xml one. 
This script can help migrate large amounts of groovy configuration files to xml ones.

**Please validate the translation worked corretly before using this in production / test environments** 

## Currently not supported 
Since this script's purpose was initially to just be used only one time it is not made to translate every file correctly. 
The following features of groovy configuration files are currently not supported, but feel free to implement it and make a [pull request](https://github.com/aykborstelmann/logback-groovy2xml/compare)
* Conditional logic - All logic e.g. reading environmentvariables and change the configuration by that is not supported. 
The support inside XML files for that is limited anyways and the current approach of the script cannot offer conditional logic
* Custom logging configuration classes - If you have written custom logging classes e.g. a `LogMessagePostProcessor` and use them inside your configuration they currently cannot be translated.

## Getting Started
This is a gradle project, in order to build the project just clone it and run
```bash
./gradlew build
```

The main class of this project is `src/main/groovy/org/example/logback/groovy2xml/LogbackGroovy2xmlCommand.groovy`. 
Just run this inside the desired directory to migrate your files. 

## Feedback, Issues and new features
If you find issues and bugs, feel free to [post a issue](https://github.com/aykborstelmann/logback-groovy2xml/issues/new/choose).  
If you want to implement new features on your own, feel free to [post a pull request](https://github.com/aykborstelmann/logback-groovy2xml/issues/new/choose).  


### Feature Ideas
If you want to contribute to this project here are some ideas what can be implemented:
* Implement a "currently not supported" feature, descripted [here](https://github.com/aykborstelmann/logback-groovy2xml/#currently-not-supported)
* Make this project better accessible via e.g. gradle execution (run plugin) 
* Make this project better accessible via a simple cli
