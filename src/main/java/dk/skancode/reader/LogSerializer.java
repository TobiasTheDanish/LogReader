package dk.skancode.reader;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class LogSerializer extends StdSerializer<Log> {
    public LogSerializer() {
        this(Log.class);
    }
    public LogSerializer(Class<Log> t) {
        super(t);
    }

    @Override
    public void serialize(Log log, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("dateTime", log.time().format(DateTimeFormatter.ISO_DATE_TIME));
        jsonGenerator.writeStringField("level", log.level().getName());
        jsonGenerator.writeStringField("method", log.method());
        jsonGenerator.writeStringField("message", log.message());
        if (log.thread().isPresent()) {
            jsonGenerator.writeStringField("thread", log.thread().get());
        }

        jsonGenerator.writeEndObject();
    }
}
