package org.example.logback.groovy2xml

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import de.cronn.assertions.validationfile.FileExtensions
import de.cronn.assertions.validationfile.junit5.JUnit5ValidationFileAssertions
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThat
import static org.example.logback.groovy2xml.LogbackGroovy2xmlConverter.parseScript

class LogbackGroovy2XmlConverterTest implements JUnit5ValidationFileAssertions {

	@Test
	void parseLogbackConfig() {

		def script = """
		println "Picked up logback.groovy from al-mv-plugin"
		appender("console", ConsoleAppender) {
			encoder(PatternLayoutEncoder) {
				pattern = "%date{HH:mm:ss.SSS} [%t] %-5p %c{40}:%L - %m%n"
			}
		}
		root(INFO, ["console"])

		logger("de.cronn", DEBUG)
		logger("de.telekom", DEBUG)
		// logger("org.springframework.test.context.cache", DEBUG)
		// logger("org.springframework.jdbc", DEBUG)
		"""

		def context = parseScript(script)

		assertThat(context.root.level).isEqualTo("INFO")
		assertThat(context.root.appenderRefs[0].ref).isEqualTo("console")

		assertThat(context.loggers.get(0).name).isEqualTo("de.cronn")
		assertThat(context.loggers.get(0).level).isEqualTo("DEBUG")
		assertThat(context.loggers.get(1).name).isEqualTo("de.telekom")
		assertThat(context.loggers.get(1).level).isEqualTo("DEBUG")

		assertThat(context.appenders.get(0).name).isEqualTo("console")
		assertThat(context.appenders.get(0).clazz).isEqualTo(ConsoleAppender)
		assertThat(context.appenders.get(0).encoder.clazz).isEqualTo(PatternLayoutEncoder)
		assertThat(context.appenders.get(0).encoder.pattern).isEqualTo("%date{HH:mm:ss.SSS} [%t] %-5p %c{40}:%L - %m%n")
	}

	@Test
	void generateXml() {
		def script = """
		println "Picked up logback.groovy from al-mv-plugin"
		appender("console", ConsoleAppender) {
			encoder(PatternLayoutEncoder) {
				pattern = "%date{HH:mm:ss.SSS} [%t] %-5p %c{40}:%L - %m%n"
			}
		}
		root(INFO, ["console"])

		logger("de.cronn", DEBUG)
		logger("de.telekom", DEBUG)
		// logger("org.springframework.test.context.cache", DEBUG)
		// logger("org.springframework.jdbc", DEBUG)
		"""

		def context = parseScript(script)
		assertWithFile(context.generateXml(), FileExtensions.XML)
	}

	@Test
	void specialXml() {
		def script = """
		// Usage: gradle -PjenkinsLogback -DHPBX_LOG_LEVEL=<log-level>
		println "Picked up logback-jenkins.groovy"

		appender("console", ConsoleAppender) {
			encoder(PatternLayoutEncoder) {
				pattern = "%date{HH:mm:ss.SSS} [%t] %-5p %c{40}:%L - #O%X{orderId}# %m%n"
			}
		}
		root(INFO, ["console"])

		def HPBX_LOG_LEVEL = toLevel(System.getProperty("HPBX_LOG_LEVEL") ?: 'INFO')
		println "HPBX_LOG_LEVEL set to \$HPBX_LOG_LEVEL"
		logger("de.cronn", HPBX_LOG_LEVEL)
		logger("de.telekom", HPBX_LOG_LEVEL)
		logger("org.hibernate", WARN)
		logger("org.springframework", WARN)
		"""

		def context = parseScript(script)
		assertWithFile(context.generateXml(), FileExtensions.XML)
	}
}
