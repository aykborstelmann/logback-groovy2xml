package org.example.logback.groovy2xml

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

class LogbackGroovy2xmlConverter {

    static class Encoder {
        @JacksonXmlProperty(isAttribute = true, localName = "class")
        public Class clazz
        public String pattern
    }

    static class Appender {
        @JacksonXmlProperty(isAttribute = true)
        public String name
        @JacksonXmlProperty(isAttribute = true, localName = "class")
        public Class clazz
        public Encoder encoder


        void encoder(Class clazz, Closure closure = null) {
            def encoder = new Encoder()
            encoder.clazz = clazz

            closure.delegate = encoder
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure()

            this.encoder = encoder
        }
    }

    static class AppenderRef {
        @JacksonXmlProperty(isAttribute = true)
        public String ref
    }

    static class Root {
        @JacksonXmlProperty(isAttribute = true)
        public String level

        @JacksonXmlProperty(localName = "appender-ref")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<AppenderRef> appenderRefs = new ArrayList<>()
    }

    static class Logger {
        @JacksonXmlProperty(isAttribute = true)
        public String name

        @JacksonXmlProperty(isAttribute = true)
        public String level

        @JacksonXmlProperty(isAttribute = true)
        public Boolean additivity = null

        @JacksonXmlProperty(localName = "appender-ref")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<AppenderRef> appenderRefs = new ArrayList<>()
    }

    @JacksonXmlRootElement(localName = "configuration")
    @JsonPropertyOrder(["appender", "root", "logger"])
    static class Configuration {
        @JacksonXmlProperty(localName = "appender")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Appender> appenders = new ArrayList<>();

        @JacksonXmlProperty(localName = "logger")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Logger> loggers = new ArrayList<>()

        public Root root;

        void appender(String name, Class clazz, Closure closure = null) {
            def appender = new Appender()
            appender.name = name
            appender.clazz = clazz

            closure.delegate = appender
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure()

            this.appenders.add(appender)
        }

        void root(Level level, List<String> appenderNames = []) {
            def root = new Root()
            root.level = level.toString()
            appenderNames.forEach(ref -> {
                def appenderRef = new AppenderRef()
                appenderRef.ref = ref
                root.appenderRefs.add(appenderRef)
            })
            this.root = root
        }

        void logger(String name, Level level, List<String> appenderNames = [], Boolean additivity = null) {
            def logger = new Logger()
            logger.name = name
            logger.level = level.toString()

            appenderNames.forEach(ref -> {
                def appenderRef = new AppenderRef()
                appenderRef.ref = ref
                logger.appenderRefs.add(appenderRef)
            })

            logger.additivity = additivity
            this.loggers.add(logger)
        }

        String generateXml() {
            XmlMapper xmlMapper = new XmlMapper()
            return xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
        }
    }

    public static Configuration parseScript(String script) {
        def configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers(importCustomizer())

        def binding = new Binding()

        def dslScript = new GroovyShell(binding, configuration).parse(script)
        dslScript.metaClass.mixin(Configuration)
        dslScript.metaClass.getDeclaredOrigin = { dslScript }
        dslScript.run()

        return dslScript as Configuration
    }

    static def importCustomizer() {
        def customizer = new ImportCustomizer()

        def core = 'ch.qos.logback.core'
        customizer.addStarImports(core, "${core}.encoder", "${core}.read", "${core}.rolling", "${core}.status",
                "ch.qos.logback.classic.net")

        customizer.addImports(PatternLayoutEncoder.class.name)

        customizer.addStaticStars(Level.class.name)

        customizer.addStaticImport('off', Level.class.name, 'OFF')
        customizer.addStaticImport('error', Level.class.name, 'ERROR')
        customizer.addStaticImport('warn', Level.class.name, 'WARN')
        customizer.addStaticImport('info', Level.class.name, 'INFO')
        customizer.addStaticImport('debug', Level.class.name, 'DEBUG')
        customizer.addStaticImport('trace', Level.class.name, 'TRACE')
        customizer.addStaticImport('all', Level.class.name, 'ALL')

        customizer
    }

}
