package org.example.logback.groovy2xml;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;


class LogbackGroovy2XmlCommandTest {

	@Test
	void matchesRegex() {
		SoftAssertions.assertSoftly(softly -> {
			softly.assertThat("logback.groovy".matches(LogbackGroovy2xmlCommand.LOGBACK_GROOVY_FILE_MATCHER)).isTrue();
			softly.assertThat("logback-wsdl2.groovy".matches(LogbackGroovy2xmlCommand.LOGBACK_GROOVY_FILE_MATCHER)).isTrue();
			softly.assertThat("logback2xml.groovy".matches(LogbackGroovy2xmlCommand.LOGBACK_GROOVY_FILE_MATCHER)).isFalse();
		});
	}
}
