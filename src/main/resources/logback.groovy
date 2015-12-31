import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.classic.filter.ThresholdFilter

import static ch.qos.logback.classic.Level.DEBUG

appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg [%file:%line]%n"
  }
}

appender("FILE_TRACE", FileAppender) {
  file = "logs/trace.log"
  append = true
  encoder(PatternLayoutEncoder) {
    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{0}.%method - %msg[%file:%line]%n"
  }

  filter(ThresholdFilter) {
    level = TRACE
  }
}

appender("FILE_DEBUG", FileAppender) {
  file = "logs/debug.log"
  append = true
  encoder(PatternLayoutEncoder) {
    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{0}.%method - %msg[%file:%line]%n"
  }

  filter(ThresholdFilter) {
    level = DEBUG
  }
}

appender("FILE_INFO", FileAppender) {
  file = "logs/info.log"
  append = true
  encoder(PatternLayoutEncoder) {
    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{0}.%method - %msg[%file:%line]%n"
  }

  filter(ThresholdFilter) {
    level = INFO
  }
}


root(TRACE, ["STDOUT"])
logger("eu.kreativzone", TRACE, ["FILE_TRACE", "FILE_DEBUG", "FILE_INFO"])

