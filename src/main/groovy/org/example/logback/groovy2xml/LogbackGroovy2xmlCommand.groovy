package org.example.logback.groovy2xml

import groovy.io.FileType

import java.nio.file.Files
import java.nio.file.Paths

class LogbackGroovy2xmlCommand {

    public static final String LOGBACK_GROOVY_FILE_MATCHER = 'logback(-\\w+)?\\.groovy$'

    boolean gitCommits = true

    void execute() throws Exception {
        def executionDir = Paths.get(".").toFile()

        List<Closure> moveOperations = new ArrayList<>()
        List<Closure> writeOperations = new ArrayList<>()

        List<File> successfullyConvertedFiles = new ArrayList<>()
        List<Tuple2<File, Exception>> unsuccessfullyConvertedFiles = new ArrayList<>()

        executionDir.eachFileRecurse(FileType.FILES) {
            if (it.name.matches(LOGBACK_GROOVY_FILE_MATCHER)) {
                try {
                    def (moveOperation, writeOperation) = buildMoveAndWriteOperation(it)
                    moveOperations.add(moveOperation)
                    writeOperations.add(writeOperation)

                    successfullyConvertedFiles.add(it)
                } catch (Exception e) {
                    unsuccessfullyConvertedFiles.add(new Tuple2<>(it, e))
                }
            }
        }

        moveOperations.forEach(move -> move.run())
        if (gitCommits) ["git", "commit", "-m", "Move logback groovy to xml"].execute().waitFor()
        writeOperations.forEach(write -> write.run())
        if (gitCommits) ["git", "commit", "-m", "Change logback groovy to xml"].execute().waitFor()

        printResult(successfullyConvertedFiles, unsuccessfullyConvertedFiles)
    }

    static void printResult(List<File> successfullyConvertedFiles, List<Tuple2<File, Exception>> unsuccessfullyConvertedFiles) {
        if (!successfullyConvertedFiles.isEmpty()) {
            println("Successfully converted the following logback files from groovy to xml:")
            successfullyConvertedFiles.forEach(file -> println(file))
        }

        println()

        if (!unsuccessfullyConvertedFiles.isEmpty()) {
            println("Couldn't convert the following logback files from groovy to xml:")
            unsuccessfullyConvertedFiles.forEach(tuple -> {
                def (file, exception) = tuple
                println(file)
                exception.printStackTrace(System.out)
            })
        }
    }

    Tuple<Closure> buildMoveAndWriteOperation(File file) {
        def newFile = getXmlFileName(file)

        def fileContent = file.getText("UTF-8")
        def xmlContent = LogbackGroovy2xmlConverter.parseScript(fileContent).generateXml()

        def moveOperation = () -> {
            Files.move(file.toPath(), newFile.toPath())

            if (gitCommits) {
                "git add ${getFilePathFromRoot(file)}".execute().waitFor()
                "git add ${getFilePathFromRoot(newFile)}".execute().waitFor()
            }
        }
        def writeOperation = () -> {
            newFile.write(xmlContent)
            if (gitCommits) "git add ${getFilePathFromRoot(newFile)}".execute().waitFor()
        }
        new Tuple<>(moveOperation, writeOperation)
    }

    static String getFilePathFromRoot(File file) {
        Paths.get(".").relativize(file.toPath()).toString()
    }

    static File getXmlFileName(File file) {
        def fileNameWithoutExtension = file.name.substring(0, file.name.lastIndexOf("."))
        file.toPath().getParent().resolve("${fileNameWithoutExtension}.xml").toFile()
    }

    static void main(String[] args) {
        new LogbackGroovy2xmlCommand().execute()
    }
}
